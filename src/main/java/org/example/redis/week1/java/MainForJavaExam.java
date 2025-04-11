package org.example.redis.week1.java;

public class MainForJavaExam {
    public static void main(String[] args) throws InterruptedException {
        String redisUri = "redis://localhost:6379";
        try {
            RedisTester redisTester = new RedisTester(redisUri);
            redisTester.runTests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}