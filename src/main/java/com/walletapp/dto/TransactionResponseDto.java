package com.walletapp.dto;

import com.walletapp.entity.TransactionStatus;
import com.walletapp.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    private String transactionId;
    private TransactionStatus status;
    private TransactionType type;
    private String description;
    private Long amount;
    private String receiver;
    private String sender;
    private LocalDateTime createdAt;
}
