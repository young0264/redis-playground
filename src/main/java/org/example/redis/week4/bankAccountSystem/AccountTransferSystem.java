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

    public void transfer(String fromAccount, String toAccount, int amount){
        TransactionOptions options = TransactionOptions.defaults().timeout(3_000, MILLISECONDS.MILLISECONDS);
        RTransaction transaction = redissonClient.createTransaction(options);
        try{
            transaction.getBucket(fromAccount).set(
                    Integer.parseInt(String.valueOf(redissonClient.getBucket(fromAccount))) - amount
            );

            transaction.getBucket(toAccount).set(
                    String.valueOf(
                            Integer.parseInt(String.valueOf(redissonClient.getBucket(toAccount))) + amount
                    )
            );

            transaction.commit();
            System.out.println("transfer success");
            System.out.println("== from account == : " + fromAccount);
            System.out.println("==  to account  == : " + toAccount);


        }


    }


//    // MULTI/EXEC 기반 트랜잭션 처리
//    public void transferWithTransaction(String fromAccount, String toAccount, int amount) {
//
//        RBatch batch = redissonClient.createBatch();
//        batch.getAtomicLong(fromAccount).addAndGetAsync(-amount);
//        batch.getAtomicLong(toAccount).addAndGetAsync(amount);
//
//        batch.execute();
//    }
//
//    // Lua 스크립트 기반 트랜잭션 처리
//    public void transferWithLua(String fromAccount, String toAccount, int amount) {
//        String luaScript = LuaScriptLoader.loadScript("transfer.lua");
//
//        redissonClient.getScript(StringCodec.INSTANCE).eval(
//                RScript.Mode.READ_WRITE,
//                luaScript,
//                RScript.ReturnType.VALUE,
//                Arrays.asList(fromAccount, toAccount),
//                String.valueOf(amount)
//        );
//    }

}
