package org.example.week2;

import org.junit.jupiter.api.*;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProductServiceTest {
    private RedissonClient redisson;
    private FakeProductRepository db;
    private ProductService productService;
    private BlockingQueue<UpdateProductCommand> updateQueue;

    @BeforeEach
    void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);
        db = new FakeProductRepository();
        updateQueue = new LinkedBlockingQueue<>();
        productService = new ProductService(redisson, db, updateQueue);
    }

    @Test
    @DisplayName("cache-miss exam test")
    void testCacheMiss_thenHit() {
        String key = "1";

        // 최초 요청 → 캐시 미스 → DB 조회
        Product p1 = productService.getProduct(key);
        Assertions.assertNotNull(p1);

        // 두 번째 요청 → 캐시 히트
        Product p2 = productService.getProduct(key);
        Assertions.assertEquals(p1.name(), p2.name());
    }

    @Test
    @DisplayName("cache-expiration exam test")
    void testCacheExpiration() throws InterruptedException {
        String key = "2";

        Product p1 = productService.getProduct(key);
        Assertions.assertNotNull(p1);

        // TTL 3초라면, 4초 기다리기

        Product p2 = productService.getProduct(key); // 캐시 만료 후 재조회
        Assertions.assertNotNull(p2);
        Assertions.assertEquals(p1.id(), p2.id());
    }

    @Test
    @DisplayName("Write-Through exam test")
    void testWriteThrough() {
        Product updated = new Product("4", "동기화상품", 7777);
        productService.updateAndSyncToDB("4", updated);

        Product cached = productService.getProduct("4");
        Product fromDb = db.findById("4");

        Assertions.assertEquals("동기화상품", cached.name());
        Assertions.assertEquals("동기화상품", fromDb.name());
    }
}
