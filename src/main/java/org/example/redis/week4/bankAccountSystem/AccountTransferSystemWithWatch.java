package org.example.redis.week4.bankAccountSystem;

import io.lettuce.core.RedisClient;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.concurrent.CountDownLatch;

/** 아무튼, fromAccount에 한해서 watch 실행중
 *  - 다른 개입이 있을 시, transaction 깨짐
 **/
public class AccountTransferSystemWithWatch {

    private final RedisClient redisClient; //lettuce 사용

    public AccountTransferSystemWithWatch(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public boolean transfer(String fromAccount, String toAccount, int amount, CountDownLatch readySignal) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> commands = connection.sync();

        // 1. 충돌 감지를 위한 감시 시작. 감시 후에 카운트다운
        commands.watch(fromAccount); //1-1
        readySignal.countDown(); //1-2

        String fromBalanceStr = commands.get(fromAccount);
        if (fromBalanceStr == null) {
            commands.unwatch();
            return false;
        }

        int fromBalance = Integer.parseInt(fromBalanceStr);
        if (fromBalance < amount) {
            commands.unwatch();
            return false;
        }

        // 2. 송금 트랜잭션 시작
        commands.multi();
        commands.decrby(fromAccount, amount); // from 계좌 출금
        commands.incrby(toAccount, amount); // to 계좌 입금

        TransactionResult result = commands.exec();

        return result != null; // null이면 충돌로 실패
    }

}
