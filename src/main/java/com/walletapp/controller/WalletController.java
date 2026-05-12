package com.walletapp.controller;


import com.walletapp.dto.*;
import com.walletapp.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("api/v1/wallets")
@RestController
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponseDto>> getBalance() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        WalletResponseDto walletResponseDto = walletService.getBalance(userName);
        ApiResponse<WalletResponseDto> response = new ApiResponse<>("Wallet fetched successfully", walletResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletResponseDto>> deposit(@Valid @RequestBody DepositRequestDto requestDto) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        WalletResponseDto walletResponseDto = walletService.deposit(userName, requestDto.getAmount());
        ApiResponse<WalletResponseDto> response = new ApiResponse<>("Amount Deposited", walletResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletResponseDto>> withdraw(@Valid @RequestBody WithdrawRequestDto withdrawRequestDto) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        WalletResponseDto walletResponseDto = walletService.withdraw(userName, withdrawRequestDto.getAmount());
        ApiResponse<WalletResponseDto> response = new ApiResponse<>("Withdraw Successful", walletResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> transfer(@Valid @RequestBody TransferRequestDto transferRequestDto) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        ApiResponse<Map<String, Object>> response = walletService.transfer(userName, transferRequestDto.getToUserName(), transferRequestDto.getAmount());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
