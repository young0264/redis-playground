package org.example.week2.readThrough;

import org.example.week2.FakeProductRepository;
import org.example.week2.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import static org.junit.jupiter.api.Assertions.*;

class ReadThroughCacheExamTest {
    private RedissonClient redisson;
    private FakeProductRepository db;
    private RedisCacheLoader loader;
    private ReadThroughCacheExam readThroughCacheExam;

    @BeforeEach
    void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);
        redisson.getKeys().flushall();
        db = new FakeProductRepository();
        loader = new RedisCacheLoader(db, redisson);
        readThroughCacheExam = new ReadThroughCacheExam(redisson, loader);
    }

    @Test
    @DisplayName("캐시 미스 시 DB에서 조회하고 Redis에 저장")
    void testCacheMiss_thenSaveToRedis() {
        Product product = readThroughCacheExam.getProduct("1"); // 캐시 미스
        assertNotNull(product);
        assertEquals("샴푸", product.name());
        assertEquals(1, db.getFindCallCount());

        // 두 번째 호출 → 캐시 히트
        Product cached = readThroughCacheExam.getProduct("1");
        assertNotNull(cached);
        assertEquals("샴푸", cached.name());
        assertEquals(1, db.getFindCallCount()); // 캐시를 읽어 count 증가X
    }

    @Test
    @DisplayName("DB에도 없는 경우 null 반환")
    void testNotFoundInCacheAndDb() {
        Product result = loader.load("999");
        Product product = readThroughCacheExam.getProduct("999"); // 존재하지 않음

        assertNull(result);
        assertNull(product);
        assertNull(redisson.getBucket("999").get()); // 캐시도 null
        assertEquals(0, db.getFindCallCount());  // db 조회 null 인경우 count 증가X
    }
}