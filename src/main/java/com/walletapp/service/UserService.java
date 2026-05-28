package com.walletapp.service;

import com.walletapp.dto.*;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    UserResponseDto saveUser(UserRequestDto userRequestDto);

    LoginResponseDto generateLoginToken(LoginRequestDto loginRequestDto);

    RequestTokenResponseDto refreshAccessToken(RefreshTokenRequestDto refreshTokenRequestDto);

    void logout(String token, HttpServletRequest request);
}
