package org.example.week2.PER;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.PER.ProductListCache;
import org.example.redis.week2.PER.ProductService;
import org.example.redis.week2.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RedisPERCacheTest {

    private RedissonClient redisson;
    private FakeProductRepository db;
    private ProductService productService;
    private ProductListCache cache;


    @BeforeEach
    void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);

        db = new FakeProductRepository();
        productService = new ProductService();
        cache = new ProductListCache(redisson);

        // 초기 데이터 저장
//        db.update("1", new Product("1", "apple", 2000));
//        db.update("2", new Product("2", "banana", 3000));

        // 캐시 초기화 (clear)
        redisson.getKeys().flushall();
    }

    @Test
    @DisplayName("TTL이 없으면 DB에서 조회하고 캐시에 저장해야 한다")
    void testInitialFetchShouldLoadFromDBAndSetToCache() {
        List<Product> products = cache.perFetch();
        assertEquals(3, products.size());

        // 캐시에 들어갔는지 확인
        boolean exists = redisson.getBucket(cache.getCacheKey()).isExists();
        assertTrue(exists);
    }

    @Test
    @DisplayName("캐시에 값이 있으면 그대로 반환해야 한다")
    void testSecondFetchShouldUseCache() {
        // 첫 호출: DB 조회 + 캐시 저장
        cache.perFetch();

        // 두 번째 호출: 캐시 조회 (PER 조건 만족해야)
        List<Product> products = cache.perFetch();
        assertEquals(3, products.size());
    }

    @Test
    @DisplayName("gapScore가 TTL을 초과하면 조기 재계산이 발생해야 한다")
    void testFetchTriggersEarlyRecomputeIfGapScoreExceedsTTL() throws InterruptedException {
        // 첫 호출: 캐싱
        cache.perFetch();

        // TTL 을 낮게 설정한 상태에서 수동으로 expire time 줄이기
        redisson.getBucket(cache.getCacheKey()).expire(10, TimeUnit.MILLISECONDS);
        Thread.sleep(50); // TTL 만료 유도

        // 호출 시 TTL 만료되었으므로 재조회됨
        List<Product> products = cache.perFetch();
        assertEquals(3, products.size());
    }

}