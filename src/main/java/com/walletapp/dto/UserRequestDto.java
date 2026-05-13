package com.walletapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotBlank(message = "User Name should not be blank")
    private String username;

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email field cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
