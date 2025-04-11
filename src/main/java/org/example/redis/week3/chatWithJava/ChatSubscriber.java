package org.example.redis.week3.chatWithJava;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

public class ChatSubscriber {

    Redisson redisson;

    public ChatSubscriber(Redisson redisson) {
        this.redisson = redisson;
    }

    // 채널 생성 및 구독
    public void subscribe(String channel) {
        RTopic topic = redisson.getTopic(channel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence channel, String message) {
                String.format("메시지 수신: %s", message);
            }
        });
    }

}
