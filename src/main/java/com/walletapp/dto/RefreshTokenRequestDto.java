package com.walletapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    @NotNull(message = "Refresh token is required cannot be null")
    private String refreshToken;
}
