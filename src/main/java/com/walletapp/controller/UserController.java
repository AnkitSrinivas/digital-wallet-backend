package com.walletapp.controller;

import com.walletapp.dto.*;
import com.walletapp.entity.User;
import com.walletapp.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<ApiResponse<LoginResponseDto>> userLogin(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        User user = userService.getUserDetails(loginRequestDto.getUserName());
        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            ApiResponse<LoginResponseDto> response = new ApiResponse<>("User not exists", null, HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            LoginResponseDto loginResponseDto = userService.generateLoginToken(loginRequestDto);
            ApiResponse<LoginResponseDto> response = new ApiResponse<>("Login Successfully", loginResponseDto, HttpStatus.OK.value());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
}
