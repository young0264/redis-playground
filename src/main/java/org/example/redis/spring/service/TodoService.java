package org.example.redis.spring.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/** 할 일 관리 서비스 */
@Service
public class TodoService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TODO_KEY = "todo_list";

    public TodoService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 해야 할 일 추가 (LPUSH)
    public void addTodo(String task) {
        redisTemplate.opsForList().leftPush(TODO_KEY, task);
    }

    // 할 일 목록 조회
    public List<String> getTodos() {
        return redisTemplate.opsForList().range(TODO_KEY, 0, -1);
    }

    // 완료된 일 제거 (LPOP)
    public String removeTodo() {
        return redisTemplate.opsForList().leftPop(TODO_KEY);
    }

    // 특정 시간 후 자동 삭제 (EXPIRE)
    public void setExpiration(long seconds) {
        redisTemplate.expire(TODO_KEY, Duration.ofSeconds(seconds));
    }
}
