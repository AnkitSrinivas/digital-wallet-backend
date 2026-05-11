package com.walletapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "User name is mandatory")
    private String userName;
    @NotBlank(message = "password is mandatory")
    private String password;
}
