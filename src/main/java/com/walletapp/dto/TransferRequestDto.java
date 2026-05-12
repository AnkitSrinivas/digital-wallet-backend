package com.walletapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequestDto {
    @NotBlank(message = "Receiver name should be valid")
    private String toUserName;
    @NotNull(message = "Amount should not be null")
    @Positive(message = "Amount should be positive")
    private Long amount;
}
