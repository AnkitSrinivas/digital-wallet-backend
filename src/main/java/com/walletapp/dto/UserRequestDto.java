package com.walletapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDto {

 private String username;

 @Email(message = "Enter a valid email")
 private String email;

 private String password;
}
