package org.example.week2.readThrough;

import org.example.week2.Product;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

/**
 * [조회] Read-Through -> 캐시에서만 데이터를 읽어오는 전략
 *  <Cache Hit>
 *   1. 캐시에서 읽은 결과 반환
 *  <Cache Miss>
 *   1. 캐시가 DB를 조회
 *   2. 그 DB 결과를 반환받아 캐시에 저장
 *   3. 결과값 반환
 *  -> 애플리케이션은 캐시에만 접근하고 DB에는 접근을 안함
 *
 * [특징]
 * - Look Aside와 비슷해보여도 데이터 동기화를 라이브러리 혹은 캐시에 위임 한다는 점에서 차이가 있음.
 **/
public class ReadThroughCacheExam {

    private final RedissonClient redisson;
    private final RedisCacheLoader redisCacheLoader;

    public ReadThroughCacheExam(RedissonClient redisson, RedisCacheLoader redisCacheLoader) {
        this.redisson = redisson;
        this.redisCacheLoader = redisCacheLoader;
    }

    public Product getProduct(String key) {
        // 1. Cache Store에 검색하는 데이터가 있는지 확인
        RBucket<Product> bucket = redisson.getBucket(key);
        Product cachedProduct = bucket.get();

        if (cachedProduct != null) {
            System.out.println("[Cache Hit] key : " + key);
            return cachedProduct;
        }

        System.out.println("[Cache Miss] key : " + key);
        Product loaded = redisCacheLoader.load(key);
        return loaded;
    }

}
