package com.doublez.backend.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/redis-test")
@PreAuthorize("hasRole('ADMIN')")
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisTestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/set")
    public Map<String, Object> setValue(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10));
        
        return Map.of(
            "status", "SUCCESS",
            "action", "SET",
            "key", key,
            "value", value
        );
    }

    @GetMapping("/get/{key}")
    public Map<String, Object> getValue(@PathVariable String key) {
        Object value = redisTemplate.opsForValue().get(key);
        
        return Map.of(
            "status", value != null ? "FOUND" : "NOT_FOUND",
            "key", key,
            "value", value
        );
    }

    @DeleteMapping("/delete/{key}")
    public Map<String, Object> deleteValue(@PathVariable String key) {
        Boolean deleted = redisTemplate.delete(key);
        
        return Map.of(
            "status", deleted ? "DELETED" : "NOT_FOUND",
            "key", key
        );
    }

    @GetMapping("/info")
    public Map<String, Object> getRedisInfo() {
        try {
            // Simple connection test instead of deprecated info() method
            redisTemplate.opsForValue().set("connection_test", "success", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("connection_test");
            
            return Map.of(
                "status", "SUCCESS",
                "connection_test", "OK",
                "message", "Redis is connected and working"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR", 
                "error", e.getMessage()
            );
        }
    }
}
