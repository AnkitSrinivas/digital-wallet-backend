package com.walletapp.service.impl;

import com.walletapp.dto.TransactionResponseDto;
import com.walletapp.dto.WalletResponseDto;
import com.walletapp.entity.*;
import com.walletapp.exception.InsufficientBalanceException;
import com.walletapp.exception.UsernameNotFoundException;
import com.walletapp.exception.WalletNotFoundException;
import com.walletapp.repository.TransactionRepository;
import com.walletapp.repository.UserRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }


    @Override
    public WalletResponseDto getBalance(String userName) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found for user"));
        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto deposit(String userName, Long amount, String idempotencyKey) {

        Optional<Transaction> transactionIdempotent = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (transactionIdempotent.isPresent()) {
            Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
        }

        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setAmount(amount);
        transaction.setTransactionId("TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        transaction.setReceiverWallet(wallet);
        transaction.setDescription("Deposit of amount " + amount + " paise");
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setType(TransactionType.DEPOSIT);
        transactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance() + amount);

        transaction.setStatus(TransactionStatus.COMPLETED);

        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto withdraw(String userName, Long amount, String idempotencyKey) {
        Optional<Transaction> idempotentTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (idempotentTransaction.isPresent()) {
            Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
        }
        Wallet wallet = walletRepository.findWalletForUpdate(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found for user"));
        if (wallet.getBalance() < amount) {
            throw new InsufficientBalanceException("in-sufficient amount for withdraw");
        }
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setAmount(amount);
        transaction.setSenderWallet(wallet);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setTransactionId("TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        transaction.setDescription("Withdraw trasaction of amount " + amount + " paise");
        transactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance() - amount);

        transaction.setStatus(TransactionStatus.COMPLETED);

        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto transfer(String username, String toUsername, Long amount, String idempotencyKey) {
        Optional<Transaction> idempotentTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (idempotentTransaction.isPresent()) {
            Wallet senderWallet = walletRepository.findWallet(username).orElseThrow(() -> new WalletNotFoundException("Wallet Not Found"));
            return new WalletResponseDto(senderWallet.getBalance(), senderWallet.getCurrency(), senderWallet.getUser().getUserName());
        }

        User senderUser = userRepository.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User Not Found " + username));
        User receiverUser = userRepository.findByUserName(toUsername).orElseThrow(() -> new UsernameNotFoundException("User Not Found " + toUsername));

        Wallet senderWallet, receiverWallet;
        if (senderUser.getId() < receiverUser.getId()) {
            senderWallet = walletRepository.findWalletForUpdate(username).orElseThrow(() -> new WalletNotFoundException("Wallet not found for sender " + username));
            receiverWallet = walletRepository.findWalletForUpdate(toUsername).orElseThrow(() -> new WalletNotFoundException("Wallet not found for receiver " + username));
        } else {
            receiverWallet = walletRepository.findWalletForUpdate(toUsername).orElseThrow(() -> new WalletNotFoundException("Wallet not found for receiver " + username));
            senderWallet = walletRepository.findWalletForUpdate(username).orElseThrow(() -> new WalletNotFoundException("Wallet not found for sender " + username));
        }


        if (senderWallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient Balance to transfer");
        }

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setReceiverWallet(receiverWallet);
        transaction.setSenderWallet(senderWallet);
        transaction.setDescription("Transfer of amount " + amount + " in paise from wallet " + senderWallet.getUser().getUserName() + " to " + receiverWallet.getUser().getUserName());
        transaction.setTransactionId("TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        transactionRepository.save(transaction);

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        transaction.setStatus(TransactionStatus.COMPLETED);

        return new WalletResponseDto(senderWallet.getBalance(), senderWallet.getCurrency(), senderWallet.getUser().getUserName());
    }


    public Page<TransactionResponseDto> getTransactions(String userName, Pageable pageable) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("User doesn't exists"));
        Page<Transaction> transactions = transactionRepository.findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(wallet.getId(), wallet.getId(), pageable);
        return transactions.map(transaction -> {
            TransactionResponseDto transactionResponseDto = new TransactionResponseDto();
            transactionResponseDto.setTransactionId(transaction.getTransactionId());
            transactionResponseDto.setStatus(transaction.getStatus());
            transactionResponseDto.setType(transaction.getType());
            transactionResponseDto.setAmount(transaction.getAmount());
            transactionResponseDto.setDescription(transaction.getDescription());
            transactionResponseDto.setSender(transaction.getSenderWallet() != null ? transaction.getSenderWallet().getUser().getUserName() : null);
            transactionResponseDto.setReceiver(transaction.getReceiverWallet() != null ? transaction.getReceiverWallet().getUser().getUserName() : null);
            transactionResponseDto.setCreatedAt(transaction.getCreatedAt());
            return transactionResponseDto;
        });
    }

}

