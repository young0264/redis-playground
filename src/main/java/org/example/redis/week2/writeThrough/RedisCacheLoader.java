package org.example.redis.week2.writeThrough;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.Product;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 Write-Through 캐싱 전략 구현 클래스.

 [쓰기]: 캐시(Redis)와 DB에 동시에 저장
 [조회]: 캐시 조회 후 없으면 DB에서 조회하고 캐시에 다시 저장 (Lazy Loading)
 캐시가 만료되거나 evicted(LRU 정책 등)될 수 있으므로 항상 DB fallback을 고려함.
 */

public class RedisCacheLoader {

    private final long TTL_SECONDS = 3;
    private final RedissonClient redisson;
    private final FakeProductRepository db;


    public RedisCacheLoader(RedissonClient redisson, FakeProductRepository db) {
        this.redisson = redisson;
        this.db = db;
    }

    /**
     * [조회]
     * 1. 캐쉬 조회
     * 2. cache에 없으면 db에서 가져옴
     * 3. cache에 다시 저장
     **/
    /**
     * [db와 cache에 반영됨에도 캐시가 없는 경우]
     * - TTL에 의해 만료된 경우
     * - LRU eviction을 수행한경우
     **/
    public Product load(String key) {
        RBucket<Product> cachedProduct = redisson.getBucket(key);
        Product product = cachedProduct.get();

        if (product != null) {
            return product;
        }

        Product dbProduct = db.findById(key);
        if (dbProduct != null) {
            redisson.getBucket(key).set(dbProduct, TTL_SECONDS, TimeUnit.SECONDS); // 캐시에 다시 적재
        }
        return dbProduct;
    }

    /**
     * - DB에 업데이트할 때마다 매번 캐시에도 데이터를 함께 업데이트시키는 방식.
     * 1. redis에 먼저 저장
     * 2. db에 저장
     **/
    public void save(Product product) {
        String key = product.id();
        redisson.getBucket(key).set(product, TTL_SECONDS, TimeUnit.SECONDS);
        db.update(key, product);
    }
}
