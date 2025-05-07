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
        boolean result = transferSystem.transfer(fromAccount, toAccount, 300, new CountDownLatch(1));

        assertTrue(result);
        assertEquals("700", commands.get(fromAccount));
        assertEquals("800", commands.get(toAccount));
    }

    @Test
    @DisplayName("송금은 실패, 외부 변경은 반영.")
    void testRollbackWhenFromAccountIsModified() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);

        // 메인 송금 스레드
        executor.submit(() -> {
            System.out.println("== main start ==");
            try {
                transferSystem.transfer(fromAccount, toAccount, 300, ready);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                finish.countDown();
            }
        });

        // 개입하는 스레드 (송금 도중 fromAccount를 바꿔버림)
        executor.submit(() -> {
            System.out.println("== interrupt start ==");
            try {
                ready.await();
                commands.incrby(fromAccount, 100); // fromAccount를 외부에서 수정
                System.out.println("[개입] fromAccount 강제 변경");
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            } finally {
                finish.countDown();
            }
        });

        finish.await();
        executor.shutdown();

        assertEquals(commands.get(fromAccount), "1100");
        assertEquals(commands.get(toAccount), "500");
    }

    @Test
    @DisplayName("잔액 부족하면 송금 실패")
    void testInsufficientBalance() {
        boolean result = transferSystem.transfer(fromAccount, toAccount, 2000, new CountDownLatch(1));

        assertFalse(result);
        assertEquals("1000", commands.get(fromAccount));
        assertEquals("500", commands.get(toAccount));
    }

    @Test
    @DisplayName("잔액 이하의 금액만 송금이 가능 (동기 테스트)") void testConcurrentTransfers() {
        int transferAmount = 300;
        int attemptCnt = 4;
        int successCnt = 0;

        for(int i = 0; i < attemptCnt; i++) {
            boolean result = transferSystem.transfer(fromAccount, toAccount, transferAmount, new CountDownLatch(1));
            if(result) successCnt++;
        }

        assertEquals(successCnt, 3);
        assertEquals("100", commands.get(fromAccount));
        assertEquals("1400", commands.get(toAccount));
    }

}