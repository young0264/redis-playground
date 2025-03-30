package org.example.week2.writeThrough;

import org.example.week2.Product;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

/**
 * [쓰기] Write-Through 캐시 전략 예제 클래스
 * DB와 Cache 동시에 데이터를 저장하는 전략
 * - DB에 업데이트할 때마다 매번 캐시에도 데이터를 함께 업데이트시키는 방식.
 * 1. Redis(Cache Store)에 저장
 * 2. Cache Store에서 DB에 저장
 **/
public class WriteThroughExam {

    private final long ttlSeconds = 3;
    private final RedissonClient redisson;
    private final RedisCacheLoader redisCacheLoader;

    public WriteThroughExam(RedissonClient redisson, RedisCacheLoader redisCacheLoader) {
        this.redisson = redisson;
        this.redisCacheLoader = redisCacheLoader;
    }

    public void saveProduct(Product product) {
        redisCacheLoader.save(product);
    }

    public Product getProduct(String key) {
        return redisCacheLoader.load(key);
    }
}
