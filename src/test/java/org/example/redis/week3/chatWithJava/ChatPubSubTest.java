package org.example.redis.week3.chatWithJava;

import org.example.redis.RedisManager;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ChatPubSubTest {

    @Test
    void testPubSub_withRedisson() throws Exception {
        // 1. Redisson 클라이언트 설정
        RedissonClient redissonClient = RedisManager.createClient();

        String channel = "TEST_CHANNEL";
        String testMessage = "TEST_HELLO_GUYS";

        CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder received = new StringBuilder();

        // 2. 구독자 생성 및 리스너 등록
        redissonClient.getTopic(channel).addListener(String.class, (c, msg) -> {
            System.out.println("수신 메시지: " + msg);
            received.append(msg);
            latch.countDown();
        });

        // 3. 발행자 생성 및 메시지 발행
        ChatPublisher publisher = new ChatPublisher(redissonClient);
        publisher.publish(channel, testMessage);

        // 4. 메시지가 수신될 때까지 최대 2초 대기
        boolean completed = latch.await(2, TimeUnit.SECONDS);

        // 5. 메시지 수신 확인
        assertTrue(completed);
        assertEquals(testMessage, received.toString());

        redissonClient.shutdown();
    }

}