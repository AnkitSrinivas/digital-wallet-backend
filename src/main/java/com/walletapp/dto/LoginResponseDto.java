package com.walletapp.dto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private String userName;
}
