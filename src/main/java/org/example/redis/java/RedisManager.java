package org.example.redis.java;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisManager {
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;

    public RedisManager(String redisUri) {
        // Redis 클라이언트 객체 생성
        this.redisClient = RedisClient.create(redisUri);

        // Redis 서버와 실제 연결, StatefulRedisConnection<String, String> 객체를 통해 Redis와 데이터를 주고 받음
        this.connection = redisClient.connect();

        // 동기 명령어 실행 객체 생성, RedisCommands<K, V> 객체는 Redis 명령어를 직접 실행할 수 있는 인터페이스.
        this.commands = connection.sync();
    }

    // String 저장
    public void setString(String key, String value) {
        commands.set(key, value);
    }

    // String 조회
    public String getString(String key) {
        return commands.get(key);
    }

    // 만료 시간 설정 (EXPIRE)
    public void setExpiration(String key, int seconds) {
        commands.expire(key, seconds);
    }

    // 키 삭제
    public void deleteKey(String key) {
        commands.del(key);
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }

}