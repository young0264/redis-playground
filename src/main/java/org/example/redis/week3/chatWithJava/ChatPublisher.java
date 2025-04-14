package org.example.redis.week3.chatWithJava;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

public class ChatPublisher {

    RedissonClient redissonClient;

    public ChatPublisher(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // 메시지 발행
    public void publish(String channel, String message) {
        redissonClient.getTopic(channel).publish(message);
    }
}
