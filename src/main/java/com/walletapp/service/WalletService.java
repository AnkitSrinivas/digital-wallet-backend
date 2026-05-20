package com.walletapp.service;

import com.walletapp.dto.TransactionResponseDto;
import com.walletapp.dto.WalletResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletService {
    WalletResponseDto getBalance(String userName);

    WalletResponseDto deposit(String userName, Long amount, String idempotencyKey);

    WalletResponseDto withdraw(String userName, Long amount, String idempotencyKey);

    WalletResponseDto transfer(String username, String toUsername, Long amount, String idempotencyKey);

    Page<TransactionResponseDto> getTransactions(String userName, Pageable pageable);
}
