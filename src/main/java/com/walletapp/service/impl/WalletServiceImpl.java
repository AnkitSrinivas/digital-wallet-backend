package com.walletapp.service.impl;

import com.walletapp.dto.TransactionResponseDto;
import com.walletapp.dto.WalletResponseDto;
import com.walletapp.entity.Transaction;
import com.walletapp.entity.TransactionStatus;
import com.walletapp.entity.TransactionType;
import com.walletapp.entity.Wallet;
import com.walletapp.exception.DuplicateTransactionException;
import com.walletapp.exception.InsufficientBalanceException;
import com.walletapp.exception.WalletNotFoundException;
import com.walletapp.repository.TransactionRepository;
import com.walletapp.repository.UserRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

        if(transactionIdempotent.isPresent()){
            Wallet wallet = walletRepository.findWallet(userName).orElseThrow(()->new WalletNotFoundException("Wallet not found"));
            return new WalletResponseDto(wallet.getBalance(),wallet.getCurrency(),wallet.getUser().getUserName());
        }

        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setAmount(amount);
        transaction.setTransactionId("TXN_" + System.currentTimeMillis());
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
        if(idempotentTransaction.isPresent()){
            Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            return new WalletResponseDto(wallet.getBalance(),wallet.getCurrency(),wallet.getUser().getUserName());
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
        transaction.setTransactionId("TXN_" + System.currentTimeMillis());
        transaction.setDescription("Withdraw trasaction of amount " + amount + " paise");
        transactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance() - amount);

        transaction.setStatus(TransactionStatus.COMPLETED);

        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto transfer(String username, String toUsername, Long amount,String idempotencyKey) {
        Optional<Transaction> idempotentTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if(idempotentTransaction.isPresent()){
            Wallet senderWallet = walletRepository.findWallet(username).orElseThrow(()-> new WalletNotFoundException("Wallet Not Found"));
            return new WalletResponseDto(senderWallet.getBalance(),senderWallet.getCurrency(),senderWallet.getUser().getUserName());
        }
        Wallet senderWallet = walletRepository.findWalletForUpdate(username).orElseThrow(() -> new WalletNotFoundException("Wallet not found for sender"));

        Wallet receiverWallet = walletRepository.findWalletForUpdate(toUsername).orElseThrow(() -> new WalletNotFoundException("Wallet not found for receiver"));

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
        transaction.setTransactionId("TXN_" + System.currentTimeMillis());
        transactionRepository.save(transaction);

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        transaction.setStatus(TransactionStatus.COMPLETED);

        return new WalletResponseDto(senderWallet.getBalance(), senderWallet.getCurrency(), senderWallet.getUser().getUserName());
    }


    public List<TransactionResponseDto> getTransactions(String userName) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("User doesn't exists"));
        List<Transaction> transactions = transactionRepository.findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(wallet.getId(), wallet.getId());
        return transactions.stream().map(transaction -> {
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
        }).toList();
    }

}

