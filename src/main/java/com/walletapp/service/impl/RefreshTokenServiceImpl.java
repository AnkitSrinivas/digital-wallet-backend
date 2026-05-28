package com.walletapp.service.impl;

import com.walletapp.entity.RefreshToken;
import com.walletapp.entity.User;
import com.walletapp.exception.RefreshTokenAlreadyExpired;
import com.walletapp.exception.RefreshTokenAlreadyRevokedException;
import com.walletapp.exception.RefreshTokenNotFoundException;
import com.walletapp.repository.RefreshTokenRepository;
import com.walletapp.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.token.expiration}")
    private Long expiryTime;

    @Override
    @Transactional
    public String generateRefreshToken(User user) {

        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);

        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.setRevoked(true);
        }
        refreshTokenRepository.saveAll(refreshTokens);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken(UUID.randomUUID().toString().replace("-", ""));
        newToken.setUser(user);
        newToken.setExpiryDate(LocalDateTime.now().plusSeconds(expiryTime / 1000));
        newToken.setRevoked(false);
        refreshTokenRepository.save(newToken);

        return newToken.getToken();
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not Found"));
        if (refreshToken.isRevoked()) {
            throw new RefreshTokenAlreadyRevokedException("Token already revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenAlreadyExpired("Token Expired");
        }

        return refreshToken;
    }

    @Override
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.setRevoked(true);
        }
        refreshTokenRepository.saveAll(refreshTokens);

    }
}
