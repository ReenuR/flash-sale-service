package com.flashsale.service;

import com.flashsale.config.RedisKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, String>  redisTemplate;
    private final int maxRequests;
    private final int windowSeconds;

    public RateLimiterService(RedisTemplate<String, String>  redisTemplate,
                              @Value("${app.rate-limit.max-requests}") int maxRequests,
                              @Value("${app.rate-limit.window-seconds}") int windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

   public boolean isAllowed(String userId){
       System.out.println("=== isAllowed called for: " + userId + " maxRequests: " + maxRequests + " windowSeconds: " + windowSeconds);
       long now = System.currentTimeMillis();
       long windowStart = now - (windowSeconds * 1000L);
       String key = RedisKeys.rateLimitKey(userId);
       redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
       Long count = redisTemplate.opsForZSet().size(key);

       if (count != null && count >= maxRequests) {
           return false;
       }
       // needs: key, value, score
       redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now);

       redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
       return  true;
   }






}
