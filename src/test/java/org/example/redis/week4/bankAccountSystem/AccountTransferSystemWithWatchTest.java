package org.example.redis.week4.bankAccountSystem;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class AccountTransferSystemWithWatchTest {
    private static RedisClient redisClient;
    private static RedisCommands<String, String> commands;
    private AccountTransferSystemWithWatch transferSystem;

    private final String fromAccount = "slipp:A";
    private final String toAccount = "slipp:B";

    @BeforeAll
    static void initRedis() {
        redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        commands = connection.sync();
    }

    @BeforeEach
    void setUp() {
        commands.set(fromAccount, "1000");
        commands.set(toAccount, "500");
        transferSystem = new AccountTransferSystemWithWatch(redisClient);
    }

    @AfterEach
    void tearDown() {
        commands.del(fromAccount);
        commands.del(toAccount);
    }

    @Test
    @DisplayName("잔액 충분하면 송금 성공")
    void testSuccessfulTransfer() {
        boolean result = transferSystem.transfer(fromAccount, toAccount, 300);

        assertTrue(result);
        assertEquals("700", commands.get(fromAccount));
        assertEquals("800", commands.get(toAccount));
    }

    @Test
    @DisplayName("송금 중 다른 사용자가 fromAccount를 변경하면 트랜잭션 롤백된다")
    void testRollbackWhenFromAccountIsModified() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch latch = new CountDownLatch(2);

        // 메인 송금 스레드
        executor.submit(() -> {
            try {
                boolean result = transferSystem.transfer(fromAccount, toAccount, 300);
                System.out.println("[메인 송금] 성공 여부: " + result);
            } finally {
                latch.countDown();
            }
        });

        // 개입하는 스레드 (송금 도중 fromAccount를 바꿔버림)
        executor.submit(() -> {
            try {
                Thread.sleep(50); // 타이밍을 송금 중간에 맞추기 위해 약간 딜레이
                commands.incrby(fromAccount, 100); // fromAccount를 외부에서 수정
                System.out.println("[개입] fromAccount 강제 변경");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();

        assertEquals(fromAccount, 1000);
        assertEquals(toAccount, 500);

    }

    @Test
    @DisplayName("잔액 부족하면 송금 실패")
    void testInsufficientBalance() {
        boolean result = transferSystem.transfer(fromAccount, toAccount, 2000);

        assertFalse(result);
        assertEquals("1000", commands.get(fromAccount));
        assertEquals("500", commands.get(toAccount));
    }

    @Test
    @DisplayName("여러 송금 요청 중 일부만 성공해야 함 (동시성 테스트)")
    void testConcurrentTransfers() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);
        int transferAmount = 300;

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean result = transferSystem.transfer(fromAccount, toAccount, transferAmount);
                    System.out.println("Transfer result: " + result);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int finalFrom = Integer.parseInt(commands.get(fromAccount));
        int finalTo = Integer.parseInt(commands.get(toAccount));
        int expectedSuccessfulTransfers = (1000 / transferAmount);

        assertEquals(1000 - (expectedSuccessfulTransfers * transferAmount), finalFrom);
        assertEquals(500 + (expectedSuccessfulTransfers * transferAmount), finalTo);
    }

}