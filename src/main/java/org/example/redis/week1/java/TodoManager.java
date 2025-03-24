package org.example.redis.java;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class TodoManager {
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;
    private static final String TODO_KEY = "todo_list";

    public TodoManager(String redisUri) {
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.commands = connection.sync();
    }

    // 해야 할 일 추가 (LPUSH)
    public void addTodo(String task) {
        commands.lpush(TODO_KEY, task);
    }

    // 할 일 목록 조회
    public void printTodos() {
        System.out.println("할 일 목록: " + commands.lrange(TODO_KEY, 0, -1));
    }

    // 완료된 일 제거 (LPOP)
    public void completeTodo() {
        String completed = commands.lpop(TODO_KEY);
        System.out.println("완료된 일(제거 대상): " + completed);
    }

    // 특정 시간 후 자동 삭제 (EXPIRE)
    public void setExpiration(int seconds) {
        commands.expire(TODO_KEY, seconds);
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
