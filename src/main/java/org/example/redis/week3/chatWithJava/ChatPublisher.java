package org.example.redis.week3.chatWithJava;

import org.redisson.Redisson;

public class ChatPublisher {

    Redisson redisson;
    ChatSubscriber chatSubscriber;

    // 메시지 발행
    public void publish(String channel, String message) {
        chatSubscriber.subscribe(channel)
                .publish(message);
    }
}
