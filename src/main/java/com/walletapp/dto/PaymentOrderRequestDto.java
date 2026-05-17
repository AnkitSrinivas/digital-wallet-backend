package com.walletapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentOrderRequestDto {

    @NotNull(message = "Not null field needed")
    @Positive
    private Long amount;
}
