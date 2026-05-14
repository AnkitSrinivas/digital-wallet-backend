package com.walletapp.service;

import com.walletapp.dto.TransactionResponseDto;
import com.walletapp.dto.WalletResponseDto;

import java.util.List;

public interface WalletService {
    WalletResponseDto getBalance(String userName);

    WalletResponseDto deposit(String userName, Long amount, String idempotencyKey);

    WalletResponseDto withdraw(String userName, Long amount,String idempotencyKey);

    WalletResponseDto transfer(String username, String toUsername, Long amount, String idempotencyKey);

    List<TransactionResponseDto> getTransactions(String userName);
}
