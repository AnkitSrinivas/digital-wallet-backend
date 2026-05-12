package com.walletapp.service;

import com.walletapp.dto.ApiResponse;
import com.walletapp.dto.WalletResponseDto;

import java.util.Map;

public interface WalletService {
    WalletResponseDto getBalance(String userName);
    WalletResponseDto deposit(String userName,Long amount);
    WalletResponseDto withdraw(String userName,Long amount);
    ApiResponse<Map<String,Object>> transfer(String username, String toUsername, Long amount);
}
