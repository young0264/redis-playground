package org.example.week2;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class ProductService {

    private final RedissonClient redisson;
    private final FakeProductRepository db;
    private final long ttlSeconds = 3;

    public ProductService(RedissonClient redisson, FakeProductRepository db) {
        this.redisson = redisson;
        this.db = db;
    }

    public Product getProduct(String id) {
        String key = "product:" + id;
        RBucket<Product> bucket = redisson.getBucket(key);

        if (bucket.isExists()) {
            System.out.println("Cache Hit: " + key);
            return bucket.get();
        }

        synchronized (this) { // cache stampede 대응
            if (bucket.isExists()) return bucket.get();
            System.out.println(" Cache Miss: " + key);
            Product product = db.findById(id);
            if (product != null) {
                bucket.set(product, ttlSeconds, TimeUnit.SECONDS); // SETEX 효과
            }
            return product;
        }
    }

    // write-back 전략 예시
    public void updateProduct(String id, Product updated) {
        redisson.getBucket("product:" + id).set(updated, ttlSeconds, TimeUnit.SECONDS);
        System.out.println("캐시만 업데이트 (write-back)");
    }

    // write-through 전략 예시
    public void updateAndSyncToDB(String id, Product updated) {
        redisson.getBucket("product:" + id).set(updated, ttlSeconds, TimeUnit.SECONDS);
        db.update(id, updated);
        System.out.println("캐시 + DB 모두 업데이트 (write-through)");
    }
}
