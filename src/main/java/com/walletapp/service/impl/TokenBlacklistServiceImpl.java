package com.walletapp.service.impl;

import com.walletapp.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "blacklist:";


    @Override
    public void blacklistToken(String token, long expirationMs) {

        if (expirationMs <= 0) {
            return;
        }

        redisTemplate.opsForValue().set(
                PREFIX + token,
                "revoked",
                expirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(PREFIX + token);
    }
}
