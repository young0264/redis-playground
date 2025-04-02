package org.example.redis.week2.lookAside;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.Product;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * [조회] Look-Aside 캐시 전략 예제 클래스
 * 1. 캐시에 찾는 데이터가 있는지 확인 (있으면 Cache Hit)
 * 2. 없으면 DB 조회 (Cache Miss)
 * 3. DB에서 조회해온 데이터를 Cache Store(Redis)에 update
 **/
public class LookAsideCacheExam {
    private final RedissonClient redisson;
    private final FakeProductRepository db;
    private final long TTL_SECONDS = 3;

    public LookAsideCacheExam(RedissonClient redisson, FakeProductRepository db) {
        this.redisson = redisson;
        this.db = db;
    }

    /**
     * 캐시에서 상품을 조회하고 없으면 DB에서 조회 후 캐시에 저장하는 look-aside 패턴
     */
    public Product getProduct(String key) {
        RBucket<Product> bucket = redisson.getBucket(key);
        Product product = bucket.get();

        // CACHE HIT
        Product cacheHitProduct = returnIfCacheHit(product);
        if (cacheHitProduct != null) return cacheHitProduct; // hit -> null이 아니면 return

        // CACHE MISS
        System.out.println("캐시 미스 → DB 조회 시도 시작");

        // DB 조회
        product = db.findById(key);

        // cache save
        saveProductToCache(bucket, product);
        return product;
    }

    private void saveProductToCache(RBucket<Product> bucket, Product product) {
        if (product != null) {
            bucket.set(product, TTL_SECONDS, TimeUnit.SECONDS);
            System.out.println("DB에서 조회 후 캐시에 저장");
        } else {
            System.out.println("DB에 해당하는 데이터가 없습니다.\n");
        }
    }

    private static Product returnIfCacheHit(Product product) {
        if (product != null) {
            System.out.println("캐시 조회 성공");
            return product;
        }
        return null;
    }

}
