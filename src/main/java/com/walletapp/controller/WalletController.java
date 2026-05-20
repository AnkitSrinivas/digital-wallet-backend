package com.walletapp.controller;


import com.walletapp.dto.*;
import com.walletapp.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/wallets")
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
        WalletResponseDto walletResponseDto = walletService.deposit(userName, requestDto.getAmount(), requestDto.getIdempotencyKey());
        ApiResponse<WalletResponseDto> response = new ApiResponse<>("Amount Deposited", walletResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletResponseDto>> withdraw(@Valid @RequestBody WithdrawRequestDto withdrawRequestDto) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        WalletResponseDto walletResponseDto = walletService.withdraw(userName, withdrawRequestDto.getAmount(), withdrawRequestDto.getIdempotencyKey());
        ApiResponse<WalletResponseDto> response = new ApiResponse<>("Withdraw Successful", walletResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<WalletResponseDto>> transfer(@Valid @RequestBody TransferRequestDto transferRequestDto) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        WalletResponseDto walletResponseDto = walletService.transfer(userName, transferRequestDto.getToUserName(), transferRequestDto.getAmount(), transferRequestDto.getIdempotencyKey());
        ApiResponse<WalletResponseDto> response = new ApiResponse<>("Transaction Successful", walletResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponseDto>>> getAllTransactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<TransactionResponseDto> transactionResponseDto = walletService.getTransactions(userName, pageable);
        ApiResponse<Page<TransactionResponseDto>> response = new ApiResponse<>("Fetch all transaction", transactionResponseDto, HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
