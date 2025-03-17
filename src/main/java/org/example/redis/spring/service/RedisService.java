package org.example.redis.spring.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** 레디스 기본 연산 서비스 */
@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // String 저장
    public void saveString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // String 조회
    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 만료 시간 설정 (EXPIRE)
    public void setExpiration(String key, long seconds) {
        redisTemplate.expire(key, Duration.ofSeconds(seconds));
    }

    // 데이터 삭제
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}
