package com.walletapp.service;

import com.walletapp.entity.RefreshToken;
import com.walletapp.entity.User;

public interface RefreshTokenService {

    String generateRefreshToken(User user);

    RefreshToken validateRefreshToken(String token);

    void revokeAllUserTokens(User user);
}
