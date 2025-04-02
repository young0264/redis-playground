package org.example.week2.writeThrough;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.Product;
import org.example.redis.week2.writeThrough.RedisCacheLoader;
import org.example.redis.week2.writeThrough.WriteThroughExam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import static org.junit.jupiter.api.Assertions.*;

public class WriteThroughExamTest {
    private RedissonClient redisson;
    private FakeProductRepository db;
    private RedisCacheLoader loader;
    private WriteThroughExam writeThroughExam;

    @BeforeEach
    void setUp() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);

        db = new FakeProductRepository();
        loader = new RedisCacheLoader(redisson, db);
        writeThroughExam = new WriteThroughExam(redisson, loader);

        redisson.getKeys().flushall(); // 테스트 환경 정리
    }

    @Test
    @DisplayName("write-through: 저장 시 캐시와 DB에 모두 저장되고, 이후 캐시에서 읽는다")
    void testSaveAndLoadFromCache() {
        Product product = new Product("item:1", "샴푸", 3000);
        writeThroughExam.saveProduct(product);

        Product cachedProduct = writeThroughExam.getProduct("item:1");
        Product dbProduct = db.findById("item:1");

        assertNotNull(cachedProduct);
        assertEquals("샴푸", cachedProduct.name());
        assertEquals(cachedProduct.name(), dbProduct.name());
        assertEquals(3000, cachedProduct.price());
        assertEquals(1, db.getFindCallCount());
    }

    @Test
    @DisplayName("write-through: 캐시가 없을 때 DB에서 읽고 캐시에 적재된다")
    void testLoadFromDbWhenCacheMissAndWriteBackToCache() throws InterruptedException {
        Product product = new Product("item:2", "린스", 4000);
        db.update(product.id(), product); // DB에만 저장

        Product dbProduct = writeThroughExam.getProduct("item:2");
        RBucket<Product> cached = redisson.getBucket("item:2");
        Product cachedProduct = cached.get();

        assertNotNull(dbProduct);
        assertEquals("린스", dbProduct.name());

        // Redis에 캐시된 값이 있는지 확인
        assertNotNull(cachedProduct);
        assertEquals("린스", cachedProduct.name());
    }

    @Test
    @DisplayName("TTL: 캐시 만료 후 재조회 시 DB에서 읽어 다시 캐시에 저장")
    void testCacheExpiresAfterTTL() throws InterruptedException {
        Product product = new Product("item:3", "비누", 2000);
        writeThroughExam.saveProduct(product);

        // 3초 TTL 조금 이후, saveProduct 내 TTL 3초
        Thread.sleep(3100);

        // 캐시 만료
        RBucket<Product> cached = redisson.getBucket("item:3");
        Product cachedProduct = cached.get();
        assertNull(cachedProduct);

        // write-through, load() 시 DB에서 다시 가져오고 Redis에 적재됨
        Product dbProduct = writeThroughExam.getProduct("item:3");
        assertNotNull(dbProduct);
        assertEquals("비누", dbProduct.name());
        assertEquals(1, db.getFindCallCount());
    }
}
