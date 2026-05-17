package com.walletapp.service.impl;

import com.walletapp.dto.PaymentOrderRequestDto;
import com.walletapp.dto.PaymentOrderResponseDto;
import com.walletapp.dto.WebhookRequestDto;
import com.walletapp.entity.*;
import com.walletapp.exception.PaymentOrderAlreadyProcessedException;
import com.walletapp.exception.PaymentOrderNotFoundException;
import com.walletapp.exception.WalletNotFoundException;
import com.walletapp.exception.WebhookSignatureMismatchException;
import com.walletapp.repository.PaymentOrdersRepository;
import com.walletapp.repository.TransactionRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.PaymentOrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private final WalletRepository walletRepository;
    private final PaymentOrdersRepository paymentOrdersRepository;
    private final String webhookSecret;
    private final TransactionRepository transactionRepository;

    @Autowired
    public PaymentOrderServiceImpl(WalletRepository walletRepository, PaymentOrdersRepository paymentOrdersRepository, @Value("${payment.webhook.secret}") String webhookSecret, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.paymentOrdersRepository = paymentOrdersRepository;
        this.webhookSecret = webhookSecret;
        this.transactionRepository = transactionRepository;
    }


    @Override
    public PaymentOrderResponseDto createOrder(PaymentOrderRequestDto paymentOrderRequestDto, String userName) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet Not Found"));
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(paymentOrderRequestDto.getAmount());
        String id = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
        paymentOrder.setOrderId("ORD_" + id);
        paymentOrder.setStatus(PaymentStatus.PENDING);
        paymentOrder.setWallet(wallet);
        paymentOrder.setWebhookVerified(false);
        PaymentOrder savedPaymentOrder = paymentOrdersRepository.save(paymentOrder);
        return new PaymentOrderResponseDto(savedPaymentOrder.getOrderId(), savedPaymentOrder.getStatus(), savedPaymentOrder.isWebhookVerified(), userName, savedPaymentOrder.getAmount(), savedPaymentOrder.getCreatedAt());
    }

    @Override
    public PaymentOrderResponseDto simulatePayment(String orderId) {
        PaymentOrder paymentOrder = paymentOrdersRepository.findByOrderId(orderId).orElseThrow(() -> new PaymentOrderNotFoundException("PAYMENT ORDER NOT FOUND FOR ORDER-ID :" + orderId));
        if (!paymentOrder.getStatus().equals(PaymentStatus.PENDING)) {
            throw new PaymentOrderAlreadyProcessedException("PAYMENT ORDER IS ALREADY PROCESSED FOR ORDER ID :" + orderId);
        }
        return handleWebhook(new WebhookRequestDto(orderId, PaymentStatus.SUCCESS, generateSignature(orderId, paymentOrder.getAmount())));
    }

    @Override
    @Transactional
    public PaymentOrderResponseDto handleWebhook(WebhookRequestDto webhookRequestDto) {
        PaymentOrder paymentOrder = paymentOrdersRepository.findByOrderId(webhookRequestDto.getOrderId()).orElseThrow(() -> new PaymentOrderNotFoundException(" PAYMENT ORDER NOT FOUND FOR ORDER ID: " + webhookRequestDto.getOrderId()));
        String hashedSignature = generateSignature(paymentOrder.getOrderId(), paymentOrder.getAmount());
        if (!webhookRequestDto.getSignature().equals(hashedSignature)) {
            throw new WebhookSignatureMismatchException("SIGNATURE MISMATCH: " + webhookRequestDto.getOrderId());
        }
        paymentOrder.setStatus(PaymentStatus.SUCCESS);
        paymentOrder.setWebhookVerified(true);
        paymentOrder.getWallet().setBalance(paymentOrder.getWallet().getBalance() + paymentOrder.getAmount());
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setIdempotencyKey(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        transaction.setTransactionId("TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription("DEPOSIT");
        transaction.setPaymentOrder(paymentOrder);
        transaction.setReceiverWallet(paymentOrder.getWallet());
        transaction.setAmount(paymentOrder.getAmount());
        transactionRepository.save(transaction);
        return new PaymentOrderResponseDto(webhookRequestDto.getOrderId(), PaymentStatus.SUCCESS, true, paymentOrder.getWallet().getUser().getUserName(), paymentOrder.getAmount(), paymentOrder.getCreatedAt());
    }


    private String generateSignature(String orderId, Long amount) {
        try {
            String data = orderId + amount;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secret);
            byte[] hash = mac.doFinal(data.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Signature", e);
        }
    }

}
