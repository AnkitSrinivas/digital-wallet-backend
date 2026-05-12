package com.walletapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WithdrawRequestDto {
    @NotNull(message = "Amount should not be null")
    @Positive(message = "Amount should be non-negative")
    private Long amount;
}
