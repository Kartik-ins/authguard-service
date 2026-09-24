package com.authguard.authguard_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Sliding-window rate limiter using Redis ZSET.
     *
     * @param key           Unique rate-limit key (client IP or username)
     * @param maxRequests   Maximum allowed requests in the time window
     * @param windowSeconds Window duration in seconds
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long now = System.currentTimeMillis();
        long windowStart = now - ((long) windowSeconds * 1000);

        try {
            // 1. Remove expired request timestamps outside the sliding window
            stringRedisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, (double) windowStart);

            // 2. Count requests currently in the sliding window
            Long currentRequests = stringRedisTemplate.opsForZSet().zCard(redisKey);
            long count = (currentRequests != null) ? currentRequests : 0L;

            if (count < maxRequests) {
                // 3. Add unique member for current timestamp with score = current timestamp
                String member = now + ":" + UUID.randomUUID().toString().substring(0, 8);
                stringRedisTemplate.opsForZSet().add(redisKey, member, (double) now);

                // 4. Refresh TTL to auto-expire idle keys and prevent memory leaks
                stringRedisTemplate.expire(redisKey, Duration.ofSeconds((long) windowSeconds + 1));
                return true;
            } else {
                // Rate limit reached; refresh TTL to protect window
                stringRedisTemplate.expire(redisKey, Duration.ofSeconds((long) windowSeconds + 1));
                log.warn("Rate limit exceeded for key '{}' ({} / {} requests in {}s)", key, count, maxRequests, windowSeconds);
                return false;
            }
        } catch (Exception ex) {
            log.error("Redis rate limiter error for key {}: {}. Allowing request as fail-open.", key, ex.getMessage());
            // Fail-open strategy to prevent Redis outages from taking down API
            return true;
        }
    }
}
