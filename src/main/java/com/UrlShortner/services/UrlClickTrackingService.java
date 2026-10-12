package com.UrlShortner.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UrlClickTrackingService {

    final StringRedisTemplate redisTemplate;

    public UrlClickTrackingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void trackCount(String shortUrl) {
        String key = "clicks:"+shortUrl;
        redisTemplate.opsForValue().increment(key);
    }
}
