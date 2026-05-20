package com.walletapp.service;

import com.walletapp.dto.PaymentOrderRequestDto;
import com.walletapp.dto.PaymentOrderResponseDto;
import com.walletapp.dto.WebhookRequestDto;

public interface PaymentOrderService {
    PaymentOrderResponseDto createOrder(PaymentOrderRequestDto paymentOrderRequestDto, String userName);

    PaymentOrderResponseDto simulatePayment(String orderId);

    PaymentOrderResponseDto handleWebhook(WebhookRequestDto webhookRequestDto);
}
