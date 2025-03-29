package org.example.week2.writeBack;

import org.example.week2.FakeProductRepository;
import org.example.week2.Product;
import org.example.week2.UpdateProductCommand;
import org.redisson.api.RedissonClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * [쓰기] Write-Back 캐시 전략 예제 클래스
 * 1. 우선 캐시 저장 -> 사용자 요청이 빠르게 반영됨 (빠른 응답)
 * 2. 비동기 큐에 저장 -> 나중에 백그라운드 쓰레드나 워커가 DB에 실제 반영
 * 3. 워커가 DB에 저장 -> 데이터의 영속성 보장 (Redis 날아가도 안전)
 **/
public class WriteBackExam {

    private final long ttlSeconds = 3;
    private final RedissonClient redisson;
    private final FakeProductRepository db;
    private final BlockingQueue<UpdateProductCommand> updateQueue;

    /**
     * 생성자 -> 캐시, DB, 큐를 주입받고 워커 스레드를 시작한다.
     **/
    public WriteBackExam(RedissonClient redisson,
                         FakeProductRepository db,
                         BlockingQueue<UpdateProductCommand> updateQueue) {
        this.redisson = redisson;
        this.db = db;
        this.updateQueue = updateQueue;
        startAsyncDbSyncWorker(); // 워커 시작
    }

    /**
     * Write-Back 전략으로 상품 정보를 업데이트.
     *  1. 캐시에 즉시 저장하여 빠른 응답 보장
     *  2. 비동기 큐에 업데이트 명령을 먼저 저장(추후 DB에 반영)
     **/
    public void updateProduct(String key, Product updated) {

        // 1. 캐시에 먼저 저장
        redisson.getBucket(key)
                .set(updated, 10, TimeUnit.MINUTES);
        System.out.println("캐시 업데이트 완료");

        // 2. 비동기 큐에 저장, 동기화 명령 추가
        updateQueue.offer(new UpdateProductCommand(key, updated));
        System.out.println("DB 업데이트 요청을 큐에 저장 (비동기)");
    }

    /**
     * 비동기 워커 스레드를 실행.
     * 큐에서 동기화 명령을 꺼내어 DB에 반영,블로킹 대기 상태로 계속 실행.
     */
    private void startAsyncDbSyncWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    UpdateProductCommand cmd = updateQueue.take();// 블로킹 대기
                    Thread.sleep(3000); // 일정 주기(예제용)
                    db.update(cmd.keyId(), cmd.updatedProduct());
                    System.out.println("DB 동기화 완료 key : " + cmd.keyId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

}
