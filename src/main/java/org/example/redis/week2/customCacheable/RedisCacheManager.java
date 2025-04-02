package org.example.redis.week2.customCacheable;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class RedisCacheManager {

    // Redis를 바라보고
    private final RedissonClient redisson;

    public RedisCacheManager(RedissonClient redisson) {
        this.redisson = redisson;
    }

    // 조회
    public Object get(String key) {
        RBucket<Object> bucket = redisson.getBucket(key);
        return bucket.get();
    }

    // 저장
    public void put(String key, Object value, long ttlSeconds) {
        RBucket<Object> bucket = redisson.getBucket(key);
        bucket.set(value, ttlSeconds, TimeUnit.SECONDS);
    }

}
