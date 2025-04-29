package org.example.redis.week4.bankAccountSystem;

import io.lettuce.core.RedisClient;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;
/** 아무튼, fromAccount에 한해서 watch 실행중
 *  - 다른 개입이 있을 시, transaction 깨짐
 **/
public class AccountTransferSystemWithWatch {
    private final RedisClient redisClient; //lettuce 사용

    public AccountTransferSystemWithWatch(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public boolean transfer(String fromAccount, String toAccount, int amount) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> commands = connection.sync();

            // 1. 충돌 감지를 위한 감시 시작
            commands.watch(fromAccount);

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
//            List<Object> result = (List<Object>) commands.exec(); // 3. 트랜잭션 실행

            return result != null; // null이면 충돌로 실패
        }
    }
}
