package org.example.week2;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProductService {

    private final RedissonClient redisson;
    private final FakeProductRepository db;
    private final long ttlSeconds = 3;
    private final BlockingQueue<UpdateProductCommand> updateQueue;


    public ProductService(RedissonClient redisson,
                          FakeProductRepository db,
                          BlockingQueue<UpdateProductCommand> updateQueue) {
        this.redisson = redisson;
        this.db = db;
        this.updateQueue = updateQueue;
        startAsyncDbSyncWorker(); // 워커 시작
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
                bucket.set(product, ttlSeconds, TimeUnit.SECONDS); // SETEX 효과
            }
            return product;
        }
    }

    // write-back 전략 예시
    public void updateProduct(String key, Product updated) {

        // 1. 캐시에 먼저 저장
        redisson.getBucket(key)
                .set(updated, 10, TimeUnit.MINUTES);
        System.out.println("캐시 업데이트 완료");

        // 2. 비동기 큐에 저장, 동기화 명령 추가
        updateQueue.offer(new UpdateProductCommand(key, updated));
        System.out.println("DB 업데이트 요청을 큐에 저장 (비동기)");
    }

    private void startAsyncDbSyncWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    UpdateProductCommand cmd = updateQueue.take();// 블로킹 대기
                    db.update(cmd.keyId(), cmd.updated());
                    System.out.println("DB 동기화 완료 : " + cmd.keyId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }


    // write-through 전략 예시
    public void updateAndSyncToDB(String id, Product updated) {
        redisson.getBucket("product:" + id).set(updated, ttlSeconds, TimeUnit.SECONDS);
        db.update(id, updated);
        System.out.println("캐시 + DB 모두 업데이트 (write-through)");
    }

}
