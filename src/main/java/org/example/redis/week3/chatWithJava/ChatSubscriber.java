package org.example.redis.week3.chatWithJava;

import org.redisson.Redisson;
import org.redisson.api.RTopic;

public class ChatSubscriber {

    Redisson redisson;

    // 채널 생성 및 구독
    public RTopic subscribe(String channel) {
        return redisson.getTopic(channel);
    }
}
