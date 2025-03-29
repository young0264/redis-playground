package org.example.week2;

import org.junit.jupiter.api.*;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RedisCacheTest {
    static RedissonClient redisson;

    @BeforeAll
    static void setup() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);
    }

    @AfterAll
    static void teardown() {
        if (redisson != null) {
            redisson.shutdown();
        }
    }

    @Test
    @DisplayName("bucket TTL 설정이 정상적으로 되는지")
    void testCacheSetAndGet_withTTL() throws InterruptedException {
        // given
        String key = "test:product:1";
        String value = "샴푸";

        RBucket<String> bucket = redisson.getBucket(key);

        // when
        bucket.set(value, 3, TimeUnit.SECONDS); // TTL 3초
        String cached = bucket.get();

        // then
        assertEquals(value, cached);

        // TTL 만료후 null
        Thread.sleep(3500);
        assertNull(bucket.get(), "캐시 만료 후 null 이어야 함");
    }

    @Test
    @DisplayName("redisson 같은 key에 value 덮어쓰기가 정상적으로 되는지")
    void testCacheOverwrite() {
        String key = "AB:overwrite";
        RBucket<String> AB_bucket = redisson.getBucket(key);

        AB_bucket.set("hello");
        assertEquals("hello", AB_bucket.get());

        AB_bucket.set("world");
        assertEquals("world", AB_bucket.get());
    }

}