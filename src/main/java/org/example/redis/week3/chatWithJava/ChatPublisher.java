package org.example.redis.week3.chatWithJava;

import org.redisson.Redisson;

public class ChatPublisher {

    Redisson redisson;

    public ChatPublisher(Redisson redisson) {
        this.redisson = redisson;
    }

    // 메시지 발행
    public void publish(String channel, String message) {
        redisson.getTopic(channel).publish(message);
    }
}
