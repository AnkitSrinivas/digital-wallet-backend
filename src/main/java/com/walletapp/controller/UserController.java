package com.walletapp.controller;

import com.walletapp.dto.*;
import com.walletapp.entity.AuditAction;
import com.walletapp.service.AuditLogService;
import com.walletapp.service.UserService;
import com.walletapp.utility.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Registering User Details {}", userRequestDto);
        UserResponseDto result = userService.saveUser(userRequestDto);
        log.info("Registered User Details {}", result);
        ApiResponse<UserResponseDto> response = new ApiResponse<>("User Created Successfully", result, HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> userLogin(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletRequest httpServletRequest) {
        log.info("Request for user login - {}", loginRequestDto.getUserName());
        String ipAddress = IpUtils.getClientIp(httpServletRequest);
        log.info("Ip Aderess for the request is {}", ipAddress);
        LoginResponseDto loginResponseDto = userService.generateLoginToken(loginRequestDto);
        ApiResponse<LoginResponseDto> response = new ApiResponse<>("Login Successfully", loginResponseDto, HttpStatus.OK.value());
        auditLogService.log(loginRequestDto.getUserName(), AuditAction.LOGIN, "USER LOGIN", ipAddress);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RequestTokenResponseDto>> getTokenFromRefreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        RequestTokenResponseDto requestTokenResponseDto = userService.refreshAccessToken(refreshTokenRequestDto);
        ApiResponse<RequestTokenResponseDto> response = new ApiResponse<>("token for refresh token", requestTokenResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody LogoutRequestDto logoutRequestDto, HttpServletRequest request) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String ipAddress = IpUtils.getClientIp(request);
        log.info("Logout Api request from ip {}", ipAddress);
        userService.logout(logoutRequestDto.getRefreshToken(), request);
        auditLogService.log(userName, AuditAction.LOGOUT, "LOG OUT", ipAddress);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("logging out", "LOGOUT SUCCESSFUL", HttpStatus.OK.value()));
    }
}

