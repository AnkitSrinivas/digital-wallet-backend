package com.walletapp.controller;

import com.walletapp.dto.ApiResponse;
import com.walletapp.dto.PaymentOrderRequestDto;
import com.walletapp.dto.PaymentOrderResponseDto;
import com.walletapp.dto.WebhookRequestDto;
import com.walletapp.entity.AuditAction;
import com.walletapp.service.AuditLogService;
import com.walletapp.service.PaymentOrderService;
import com.walletapp.utility.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentOrderService paymentOrderService;
    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentOrderResponseDto>> createPaymentOrders(@Valid @RequestBody PaymentOrderRequestDto paymentOrderDto, HttpServletRequest request) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String ipAddress = IpUtils.getClientIp(request);
        PaymentOrderResponseDto paymentOrderResponseDto = paymentOrderService.createOrder(paymentOrderDto, userName);
        ApiResponse<PaymentOrderResponseDto> response = new ApiResponse<>("PAYMENTS ORDER PROCESSED", paymentOrderResponseDto, HttpStatus.CREATED.value());
        auditLogService.log(userName, AuditAction.CREATE_ORDER,"Order Created",ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/simulate/{orderId}")
    public ResponseEntity<ApiResponse<PaymentOrderResponseDto>> simulatePayments(@PathVariable String orderId,HttpServletRequest request) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String ipAddress = IpUtils.getClientIp(request);
        PaymentOrderResponseDto paymentOrderResponseDto = paymentOrderService.simulatePayment(orderId);
        ApiResponse<PaymentOrderResponseDto> response = new ApiResponse<>("PAYMENTS ORDER PROCESSED", paymentOrderResponseDto, HttpStatus.OK.value());
        auditLogService.log(userName,AuditAction.PAYMENT_SUCCESS,"payment order simulate",ipAddress);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<PaymentOrderResponseDto>> webhookPayments(@Valid @RequestBody WebhookRequestDto webhookRequestDto) {
        PaymentOrderResponseDto paymentOrderResponseDto = paymentOrderService.handleWebhook(webhookRequestDto);
        ApiResponse<PaymentOrderResponseDto> response = new ApiResponse<>("Webhook Response", paymentOrderResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
