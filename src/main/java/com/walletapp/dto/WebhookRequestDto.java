package com.walletapp.dto;

import com.walletapp.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookRequestDto {

    private String orderId;
    private PaymentStatus status;
    private String signature;
}
