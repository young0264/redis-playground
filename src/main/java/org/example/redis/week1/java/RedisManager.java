package org.example.redis.week1.java;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.List;
import java.util.Map;

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

    // List 추가 (LPUSH)
    public void pushToList(String listName, String value) {
        commands.lpush(listName, value);
    }

    // List 조회
    public List<String> getList(String listName) {
        return commands.lrange(listName, 0, -1);
    }

    // Set 추가
    public void addToSet(String setName, String value) {
        commands.sadd(setName, value);
    }

    // Set 조회
    public List<String> getSet(String setName) {
        return commands.smembers(setName).stream().toList();
    }

    // Sorted Set 추가
    public void addToSortedSet(String sortedSetName, double score, String value) {
        commands.zadd(sortedSetName, score, value);
    }

    // Sorted Set 조회
    public List<String> getSortedSet(String sortedSetName) {
        return commands.zrange(sortedSetName, 0, -1);
    }

    // Hash 추가
    public void setHash(String hashName, String key, String value) {
        commands.hset(hashName, key, value);
    }

    // Hash 조회
    public Map<String, String> getHash(String hashName) {
        return commands.hgetall(hashName);
    }

    // EXPIRE 설정
    public void setExpiration(String key, int seconds) {
        commands.expire(key, seconds);
    }

    // TTL 조회
    public long getTTL(String key) {
        return commands.ttl(key);
    }

    // 데이터 삭제
    public void deleteKey(String key) {
        commands.del(key);
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }

}