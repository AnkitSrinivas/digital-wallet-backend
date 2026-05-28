package com.walletapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequestDto {
    @NotBlank(message = "refresh token cannot be blank")
    private String refreshToken;
}
