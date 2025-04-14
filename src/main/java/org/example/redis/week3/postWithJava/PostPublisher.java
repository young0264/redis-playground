package org.example.redis.week3.postWithJava;

import org.redisson.api.RedissonClient;

public class PostPublisher {
    RedissonClient redissonClient;

    public PostPublisher(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void publish(String channel, String message) {
        redissonClient.getTopic(channel).publish(message);
    }

}
