package org.example.redis.spring.controller;

import org.example.redis.spring.service.RedisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
public class RedisController {
    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/string")
    public void setString(@RequestParam String key, @RequestParam String value) {
        redisService.saveString(key, value);
    }

    @GetMapping("/string")
    public String getString(@RequestParam String key) {
        return redisService.getString(key);
    }

    @DeleteMapping("/key")
    public void deleteKey(@RequestParam String key) {
        redisService.deleteKey(key);
    }
}
