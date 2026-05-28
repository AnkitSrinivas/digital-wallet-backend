package com.walletapp.service.impl;

import com.walletapp.config.JwtService;
import com.walletapp.dto.*;
import com.walletapp.entity.RefreshToken;
import com.walletapp.entity.Role;
import com.walletapp.entity.User;
import com.walletapp.entity.Wallet;
import com.walletapp.exception.InvalidCredentialException;
import com.walletapp.exception.UserAlreadyExistsException;
import com.walletapp.exception.UsernameNotFoundException;
import com.walletapp.repository.UserRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.RefreshTokenService;
import com.walletapp.service.TokenBlacklistService;
import com.walletapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletRepository walletRepository, JwtService jwtService, RefreshTokenService refreshTokenService, TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;

    }

    @Override
    @Transactional
    public UserResponseDto saveUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            log.info("User already exists for email {}", userRequestDto.getEmail());
            throw new UserAlreadyExistsException("User already Exist for this email");
        }

        if (userRepository.existsByUserName(userRequestDto.getUsername())) {
            log.info("User already exists for username {}", userRequestDto.getUsername());
            throw new UserAlreadyExistsException("User already Exist for this username");
        }

        User user = new User();
        user.setUserName(userRequestDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setEmail(userRequestDto.getEmail());
        user.setRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(0L);
        wallet.setCurrency("INR");
        walletRepository.save(wallet);

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setUserName(savedUser.getUserName());
        userResponseDto.setEmail(savedUser.getEmail());
        userResponseDto.setId(savedUser.getId());
        userResponseDto.setCreatedAt(savedUser.getCreatedAt());

        return userResponseDto;

    }

    @Override
    public LoginResponseDto generateLoginToken(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUserName(loginRequestDto.getUserName()).orElseThrow(() -> new UsernameNotFoundException("User doesn't exists"));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid UserName and Password");
        }
        return new LoginResponseDto(jwtService.generateToken(user), user.getUserName(), refreshTokenService.generateRefreshToken(user));
    }

    @Override
    @Transactional
    public RequestTokenResponseDto refreshAccessToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenRequestDto.getRefreshToken());
        String token = jwtService.generateToken(refreshToken.getUser());
        return new RequestTokenResponseDto(token);
    }

    @Override
    @Transactional
    public void logout(String token, HttpServletRequest request) {
        String accessToken = jwtService.resolveToken(request);
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(token);
        refreshTokenService.revokeAllUserTokens(refreshToken.getUser());
        tokenBlacklistService.blacklistToken(accessToken, jwtService.getRemainingExpiration(accessToken));
    }
}
