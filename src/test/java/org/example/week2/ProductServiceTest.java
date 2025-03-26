package org.example.week2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductServiceTest {
    private RedissonClient redisson;
    private FakeProductRepository db;
    private ProductService service;

    @BeforeAll
    void setup() {
        Config config = new Config(); //todo: RedisConfig
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);
        db = new FakeProductRepository();
        service = new ProductService(redisson, db);
    }

    @AfterAll
    void teardown() {
        redisson.shutdown();
    }

    @Test
    void testCacheMiss_thenHit() {
        String key = "1";

        // 최초 요청 → 캐시 미스 → DB 조회
        Product p1 = service.getProduct(key);
        assertNotNull(p1);

        // 두 번째 요청 → 캐시 히트
        Product p2 = service.getProduct(key);
        assertEquals(p1.name(), p2.name());
    }

    @Test
    void testCacheExpiration() throws InterruptedException {
        String key = "2";

        Product p1 = service.getProduct(key);
        assertNotNull(p1);

        // TTL 3초라면, 4초 기다리기
        Thread.sleep(4_000);

        Product p2 = service.getProduct(key); // 캐시 만료 후 재조회
        assertNotNull(p2);
        assertEquals(p1.id(), p2.id());
    }

    @Test
    void testWriteBack() {
        Product updated = new Product("3", "캐시전용상품", 9999);
        service.updateProduct("3", updated);

        Product cached = service.getProduct("3");
        assertEquals("캐시전용상품", cached.name());

        // DB에는 아직 반영되지 않았을 수 있음
        Product fromDb = db.findById("3");
        assertNotEquals("캐시전용상품", fromDb.name());
    }

    @Test
    void testWriteThrough() {
        Product updated = new Product("4", "동기화상품", 7777);
        service.updateAndSyncToDB("4", updated);

        Product cached = service.getProduct("4");
        Product fromDb = db.findById("4");

        assertEquals("동기화상품", cached.name());
        assertEquals("동기화상품", fromDb.name());
    }
}
