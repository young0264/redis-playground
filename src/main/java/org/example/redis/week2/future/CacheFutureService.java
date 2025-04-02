package org.example.redis.week2.future;

import java.util.concurrent.*;

public class CacheFutureService {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<String>> inProgress = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public String get(String key) {
        // 1. 캐시 확인
        String cached = cache.get(key);
        if (cached != null) return cached;

        // 2. 진행 중인 요청 있는지 확인
        CompletableFuture<String> future = inProgress.computeIfAbsent(key, k -> {
            CompletableFuture<String> f = new CompletableFuture<>();
            executor.submit(() -> {
                try {
                    String value = mockDbQuery(k);       // DB 조회
                    cache.put(k, value);                 // 캐시 저장
                    f.complete(value);                   // Future 완료
                } catch (Exception e) {
                    f.completeExceptionally(e);
                } finally {
                    inProgress.remove(k);                // 맵 정리
                }
            });
            return f;
        });

        return future.join(); // 예외 발생 시 RuntimeException 발생
    }

    private String mockDbQuery(String key) {
        // DB 조회 시뮬레이션 (지연 포함)
        try {
            Thread.sleep(100); // DB 처리 시간
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "value-for-" + key;
    }

    public void clearCache() {
        cache.clear();
    }
}
