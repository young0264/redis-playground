package org.example.redis.week4.bankAccountSystem;

import org.example.redis.RedisManager;
import org.junit.jupiter.api.*;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;

class AccountTransferSystemTest {
    private RedissonClient redissonClient;
    private AccountTransferSystem transferSystem;

    private final String fromAccount = "SLIPP:A";
    private final String toAccount = "SLIPP:B";

    @BeforeEach
    void setUpAccounts() {
        transferSystem = new AccountTransferSystem();
        redissonClient = RedisManager.createClient();

        redissonClient.getAtomicLong(fromAccount).set(1000L);
        redissonClient.getAtomicLong(toAccount).set(500L);
    }

    @AfterEach
    void cleanUp() {
        redissonClient.getAtomicLong(fromAccount).delete();
        redissonClient.getAtomicLong(toAccount).delete();
    }


    @Test
    @DisplayName("트랜잭션으로 송금 시도")
    void testTransferWithTransaction() {
        int transferAmount = 200;

        transferSystem.transferWithTransaction(fromAccount, toAccount, transferAmount);

        long fromBalance = redissonClient.getAtomicLong(fromAccount).get();
        long toBalance = redissonClient.getAtomicLong(toAccount).get();

        assertEquals(800L, fromBalance);
        assertEquals(700L, toBalance);
    }


}