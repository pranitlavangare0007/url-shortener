package com.UrlShortner.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUEST = 10;
    private static final Duration WINDOW =
            Duration.ofSeconds(60);
    public RateLimiterService(StringRedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    public boolean isAllowed(String clintIp) {
        String key = "rate:" + clintIp;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return false;
        }

        if (count == 1) {
            redisTemplate.expire(key, WINDOW);
        }

        return count <= MAX_REQUEST;
    }
}
