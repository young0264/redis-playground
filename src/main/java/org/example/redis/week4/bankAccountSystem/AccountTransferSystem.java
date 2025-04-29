package org.example.redis.week4.bankAccountSystem;

import org.example.redis.RedisManager;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AccountTransferSystem {

    private final RedissonClient redissonClient;

    public AccountTransferSystem() {
        this.redissonClient = RedisManager.createClient();
    }

    // MULTI/EXEC 기반 트랜잭션 처리
    public void transferWithTransaction(String fromAccount, String toAccount, int amount) {
        RBatch batch = redissonClient.createBatch();

        batch.getAtomicLong(fromAccount).addAndGetAsync(-amount); // 송금쪽 계좌(출금)
        batch.getAtomicLong(toAccount).addAndGetAsync(amount);    // 송신쪽 계좌(입금)

        batch.execute();
        System.out.printf("Transferred %d from %s to %s via MULTI/EXEC%n", amount, fromAccount, toAccount);
    }

    public boolean transferWithTransaction2(String fromAccount, String toAccount, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("마이너스 통장은 안돼요.");
        }

        RAtomicLong from = redissonClient.getAtomicLong(fromAccount);
        RAtomicLong to = redissonClient.getAtomicLong(toAccount);

        if (from == null || to == null) {
            throw new IllegalArgumentException("존재하지 않는 계좌입니다.");
        }

        long currentBalance = from.get();

        if (currentBalance < amount) {
            System.out.println("잔액이 부족합니다.");
            return false;
        }

        RBatch batch = redissonClient.createBatch();
        batch.getAtomicLong(fromAccount).addAndGetAsync(-amount);
        batch.getAtomicLong(toAccount).addAndGetAsync(amount);
        batch.execute();

        System.out.printf("Transferred %d from %s to %s via MULTI/EXEC%n", amount, fromAccount, toAccount);
        return true;

    }

}
