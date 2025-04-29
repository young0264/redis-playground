package org.example.redis.week4.bankAccountSystem;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.Arrays;

public class AccountTransferSystemWithLua {
    private final RedissonClient redissonClient;

    public AccountTransferSystemWithLua(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void transferWithLua(String fromAccount, String toAccount, int amount) {
        String luaScript = LuaScriptLoader.loadScript("src/main/resources/transfer.lua");

        Object result = redissonClient.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.VALUE,
                Arrays.asList(fromAccount, toAccount),
                String.valueOf(amount)
        );

        if ("1".equals(result.toString())) {
            System.out.printf("Transferred %d from %s to %s via Lua%n", amount, fromAccount, toAccount);
        } else {
            System.out.printf("Transfer failed from %s to %s due to insufficient balance%n", fromAccount, toAccount);
        }
    }

}
