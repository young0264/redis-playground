package org.example.week2;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProductService {

    private final RedissonClient redisson;
    private final FakeProductRepository db;
    private final long TTL_SECONDS = 3;
    private final BlockingQueue<UpdateProductCommand> updateQueue;


    public ProductService(RedissonClient redisson,
                          FakeProductRepository db,
                          BlockingQueue<UpdateProductCommand> updateQueue) {
        this.redisson = redisson;
        this.db = db;
        this.updateQueue = updateQueue;
    }

    public Product getProduct(String id) {
        String key = id;
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
                bucket.set(product, TTL_SECONDS, TimeUnit.SECONDS); // SETEX 효과
            }
            return product;
        }
    }

    // write-through 전략 예시
    public void updateAndSyncToDB(String key, Product updated) {
        redisson.getBucket(key)
                .set(updated, TTL_SECONDS, TimeUnit.SECONDS);
        db.update(key, updated);
        System.out.println("캐시 + DB 모두 업데이트 (write-through)");
    }

}
