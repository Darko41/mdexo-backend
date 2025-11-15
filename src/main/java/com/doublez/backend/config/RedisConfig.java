package com.doublez.backend.config;

import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@EnableCaching
@Profile("prod")
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl:false}")
    private boolean useSsl;
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        if (!redisPassword.isEmpty()) {
            config.setPassword(RedisPassword.of(redisPassword));
        }

        LettuceClientConfigurationBuilder clientConfigBuilder = LettuceClientConfiguration.builder();
        
        if (useSsl) {
            clientConfigBuilder.useSsl().disablePeerVerification();
        }
        
        clientConfigBuilder
            .commandTimeout(Duration.ofSeconds(10))
            .shutdownTimeout(Duration.ofSeconds(10));

        return new LettuceConnectionFactory(config, clientConfigBuilder.build());
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // Default TTL 10 minutes
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("propertySearch", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("propertyDetails", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("uniqueFeatures", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
            .build();
    }
    
    @Bean
    @Order(1)
    CommandLineRunner testRedisConnection(RedisTemplate<String, Object> redisTemplate) {
        return args -> {
            logger.info("🔍 Testing Redis connection to: {}:{}", redisHost, redisPort);
            
            try {
                // Test basic Redis operations
                String testKey = "startup_test_" + System.currentTimeMillis();
                String testValue = "success_" + UUID.randomUUID();
                
                redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(30));
                String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
                
                if (testValue.equals(retrievedValue)) {
                    logger.info("✅ Redis connection test: SUCCESS - Connection established and basic operations work");
                } else {
                    logger.error("❌ Redis connection test: FAILED - Values don't match. Expected: {}, Got: {}", testValue, retrievedValue);
                }
                
            } catch (Exception e) {
                logger.error("❌ Redis connection test: ERROR - {}", e.getMessage());
                logger.debug("Stack trace:", e);
            }
        };
    }
}