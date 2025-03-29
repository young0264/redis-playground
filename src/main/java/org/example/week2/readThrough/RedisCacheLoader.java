package org.example.week2.readThrough;

import org.example.week2.FakeProductRepository;
import org.example.week2.Product;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

//Map loader used for read-through operations or during RMap.loadAll execution.
/**
 * 캐시 미스 시 DB에서 데이터를 가져오는 로더
 **/
public class RedisCacheLoader implements MapLoader<String, Product> {

    private final FakeProductRepository db;
    private final RedissonClient redisson;
    private final long ttlSeconds = 3;

    public RedisCacheLoader(FakeProductRepository db, RedissonClient redisson) {
        this.db = db;
        this.redisson = redisson;
    }

    // [Cache Miss]
    // 2. 캐시미스시 DB에서 데이터를 조회하여 자체 업데이트.
    @Override
    public Product load(String key) {
        System.out.println(">> 캐시 미스 → MapLoader가 DB에서 직접 조회 시도함");
        Product productByDb = db.findById(key); // DB에서 데이터를 조회

        redisson.getBucket(key)
                .set(productByDb, ttlSeconds, TimeUnit.MINUTES); // 자체 업데이트
        return productByDb;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        System.out.println("전체 캐시 프리로드는 사용하지 않습니다.");
        return Collections.emptyList(); // 전체 캐시 프리로드는 사용 안함
    }
}