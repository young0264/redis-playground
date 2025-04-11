package org.example.redis.week2.future;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.Product;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.*;

public class CacheFutureService {
    private final FakeProductRepository db;
    private final RedissonClient redissonCache;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, CompletableFuture<Product>> futureConcurrentHashMap = new ConcurrentHashMap<>();
    private final long TTL_SECONDS = 3;

    public CacheFutureService(RedissonClient redissonCache, FakeProductRepository db) {
        this.redissonCache = redissonCache;
        this.db = db;
    }

    public Product getProductByFuture(String key) {
        // 1. 캐시 확인
        RBucket<Product> valueBucket = redissonCache.getBucket(key);
        Product cached = valueBucket.get();
        if (cached != null) return cached;

        // 2.진행 중인 요청 있는지 확인/중복 요청 제어
//        CompletableFuture<Product> future = getProductUsingManualExecutor(key);
        CompletableFuture<Product> future = getProductUsingSupplyAsync(key);

        // 같은 Future의 완료를 기다리는 곳.
        // LockSupport.unpark(), 순서 제어 X
        return future.join();
    }

    /**
     * [CompletableFuture API 사용 + 자동완료]
     *   supplyAsync 라는 CompletableFuture 내에 만들어진 기능 사용
     *   -> Executor, Future, Thread가 숨겨져있고 메서드 형태대로만 사용하면 됨.
     **/
    private CompletableFuture<Product> getProductUsingSupplyAsync(String key) {
        // 1. ConcurrentHashMap을 이용, 현재 진행중인 요청이 있는지 확인
        return futureConcurrentHashMap.computeIfAbsent(key, k ->
                // 2. 없다면 supplyAsync로 새 비동기 작업 생성, futureConcurrentHashMap에 등록
                // 2-1. 있다면 CompletableFuture<Product>를 그대로 반환함 -> '동시 중복 요청 방지' 핵심 로직
            CompletableFuture.supplyAsync(() -> {
                try {
                    Product product = db.findById(k);
                    redissonCache.getBucket(k).set(product, TTL_SECONDS, TimeUnit.SECONDS);
                    return product;
                } finally {
                    futureConcurrentHashMap.remove(k);  // 맵 정리
                }
            }, executor)
        );
    }

    /**
     * [수동 생성 & 수동 완료]
     *   직접 future 객체를 만든 후, 스레드로 값을 채워주는 로직.
     **/
    private CompletableFuture<Product> getProductUsingManualExecutor(String key) {
        CompletableFuture<Product> future = futureConcurrentHashMap.computeIfAbsent(key, k -> {

            CompletableFuture<Product> f = new CompletableFuture<>();
            executor.submit(() -> {
                try {
                    Product product = db.findById(k);       // DB 조회
                    redissonCache.getBucket(k).set(product, TTL_SECONDS, TimeUnit.SECONDS); // 캐시 저장
                    f.complete(product);                   // Future 완료
                } catch (Exception e) {
                    f.completeExceptionally(e);
                } finally {
                    futureConcurrentHashMap.remove(k);  // 맵 정리
                }
            });
            return f;
        });

        return future;
    }

}
