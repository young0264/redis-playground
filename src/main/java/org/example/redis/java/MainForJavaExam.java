package org.example.redis.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainForJavaExam {
    public static void main(String[] args) throws InterruptedException {

        String redisUri = "redis://localhost:6379";

        // Redis 기본 연산 테스트
        RedisManager redisManager = new RedisManager(redisUri);
        redisManager.setString("myKey", "Hello Redis!");
        System.out.println("myKey 값: " + redisManager.getString("myKey"));
        redisManager.deleteKey("myKey");
        System.out.println("myKey 삭제 후 값: " + redisManager.getString("myKey"));

        redisManager.setString("myKey", "Hello Redis2!");
        System.out.println("myKey 만료 전: " + redisManager.getString("myKey"));
        redisManager.setExpiration("myKey", 5); // 머냐 이건
        Thread.sleep(5000);
        System.out.println("myKey 만료 후: " + redisManager.getString("myKey"));

        redisManager.close();

    }
}