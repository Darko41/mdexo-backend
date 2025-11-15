package com.doublez.backend.controller;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicRedisTestController {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public PublicRedisTestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis-test")
    public Map<String, Object> testRedis() {
        try {
            String testKey = "public_test_" + System.currentTimeMillis();
            String testValue = "success_" + UUID.randomUUID();
            
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(30));
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            boolean testPassed = testValue.equals(retrievedValue);
            
            return Map.of(
                "status", testPassed ? "SUCCESS" : "FAILED",
                "redis_working", testPassed,
                "message", testPassed ? "Redis is connected and working!" : "Redis test failed"
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "redis_working", false,
                "error", e.getMessage()
            );
        }
    }
}
