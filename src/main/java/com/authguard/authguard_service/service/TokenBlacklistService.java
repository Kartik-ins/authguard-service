package com.authguard.authguard_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Blacklists a revoked JWT token with its exact remaining time-to-live.
     *
     * @param token          JWT token string
     * @param remainingTtlMs Remaining time-to-live in milliseconds
     */
    public void blacklistToken(String token, long remainingTtlMs) {
        if (token == null || token.isBlank()) {
            return;
        }

        if (remainingTtlMs <= 0) {
            log.debug("Token is already expired; skipping Redis blacklisting.");
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, "revoked", remainingTtlMs, TimeUnit.MILLISECONDS);
        log.info("Token blacklisted in Redis with key {} for {} ms", key, remainingTtlMs);
    }

    /**
     * Checks if a JWT token is present in the blacklist.
     *
     * @param token JWT token string
     * @return true if blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String key = BLACKLIST_PREFIX + token;
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
