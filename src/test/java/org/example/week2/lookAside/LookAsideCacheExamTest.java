package org.example.week2.lookAside;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.Product;
import org.example.redis.week2.lookAside.LookAsideCacheExam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import static org.junit.jupiter.api.Assertions.*;

class LookAsideCacheExamTest {

    RedissonClient redisson;
    FakeProductRepository db;
    LookAsideCacheExam lookAsideCacheService;

    @BeforeEach
    void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        redisson = Redisson.create(config);
        db = new FakeProductRepository();
        lookAsideCacheService = new LookAsideCacheExam(redisson, db);
    }

    @Test
    @DisplayName("")
    void testCacheMiss_thenHit() {
        db.resetFindCallCount();
        // 캐시 초기 상태: 비어 있음
        Product fromFirstCall = lookAsideCacheService.getProduct("1");
        assertNotNull(fromFirstCall);
        assertEquals("샴푸", fromFirstCall.name());
        assertEquals(1, db.getFindCallCount());


        // 두 번째 호출: 캐시 히트
        Product fromSecondCall = lookAsideCacheService.getProduct("1");
        assertNotNull(fromSecondCall);
        assertEquals("샴푸", fromSecondCall.name());
        assertEquals(1, db.getFindCallCount());
    }

    @Test
    void testCacheExpire() throws InterruptedException {
        // 첫 조회: 캐시 저장됨
        Product product = lookAsideCacheService.getProduct("1");
        assertNotNull(product);

        // TTL 3초 기다리기
        Thread.sleep(4000);

        // 캐시 만료 후 → 다시 DB 조회 → 캐시에 다시 저장됨
        Product afterExpire = lookAsideCacheService.getProduct("1");
        assertNotNull(afterExpire);
        assertEquals("샴푸", afterExpire.name());
    }

    @Test
    void testCacheMiss_noDbData() {
        // DB에도 없는 데이터
        Product result = lookAsideCacheService.getProduct("unknown-key");
        assertNull(result);
    }
}