package org.example.week2.PER;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public abstract class RedisPERCache<T> {

    protected final RedissonClient redisson;
    protected final Random random = new Random();

    public RedisPERCache(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public T perFetch(double beta) {
        RedisCacheInfo cacheInfo = getCacheInfo();
        String key = cacheInfo.getKey();

        long ttl = redisson.getBucket(key).remainTimeToLive();

        // TTL 만료인 경우는 바로 save
        if (ttl <= 0) {
            return saveToRedis();
        }

        RedisPERData<T> cachedData = findRedisCache(); //캐시에서 데이터 조회
        double gapScore = cachedData.getExpiryGapMs() * beta * -Math.log(random.nextDouble()); // 조기 재계산 점수 계산

        // TTL보다 '조기 재계산 점수'가 높으면 save.
        if (gapScore >= ttl) {
            return saveToRedis();
        }
        return cachedData.getCachedData();
    }

    public T perFetch() {
        return perFetch(1.0);
    }

    private RedisPERData<T> findRedisCache() {
        RedisCacheInfo cacheInfo = getCacheInfo();
        RBucket<RedisPERData<T>> bucket = redisson.getBucket(cacheInfo.getKey());
        return bucket.get();
    }

    private T saveToRedis() {
        long start = System.currentTimeMillis();
        T data = getResourceData(); // fake db 데이터 가져옴
        long gap = System.currentTimeMillis() - start;

        RedisCacheInfo cacheInfo = getCacheInfo();
        RedisPERData<T> perData = new RedisPERData<>(data, (int) gap);

        RBucket<RedisPERData<T>> bucket = redisson.getBucket(cacheInfo.getKey()); // cache 가져와서 저장.
        bucket.set(perData, cacheInfo.getCacheKeepSecond(), TimeUnit.SECONDS);

        return data;
    }

    protected abstract RedisCacheInfo getCacheInfo();

    protected abstract T getResourceData();

    protected abstract String getCacheKey();

}
