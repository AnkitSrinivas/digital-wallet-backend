package com.walletapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {

    private Integer id;
    private String userName;
    private String email;
    private LocalDateTime createdAt;
}
