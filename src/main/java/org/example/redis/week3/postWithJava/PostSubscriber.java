package org.example.redis.week3.postWithJava;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

public class PostSubscriber {

    RedissonClient redissonClient;

    public PostSubscriber(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void subscribe(String keyWord) {
        RTopic topic = redissonClient.getTopic(keyWord);
        topic.addListener(String.class, (channel, msg) -> {
            String.format("메시지 수신: %s", msg);
        });
    }

}
