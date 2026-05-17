package com.walletapp.dto;


import com.walletapp.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponseDto {

    private String orderId;
    private PaymentStatus status;
    private boolean webhookVerified;
    private String userName;
    private Long amount;
    private LocalDateTime createdAt;
}
