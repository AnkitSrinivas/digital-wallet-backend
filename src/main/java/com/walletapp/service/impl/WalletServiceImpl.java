package com.walletapp.service.impl;

import com.walletapp.dto.WalletResponseDto;
import com.walletapp.entity.Wallet;
import com.walletapp.exception.InsufficientBalanceException;
import com.walletapp.exception.WalletNotFoundException;
import com.walletapp.repository.UserRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }


    @Override
    public WalletResponseDto getBalance(String userName) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found for user"));
        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto deposit(String userName, Long amount) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        wallet.setBalance(wallet.getBalance() + amount);
        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto withdraw(String userName, Long amount) {
        Wallet wallet = walletRepository.findWallet(userName).orElseThrow(() -> new WalletNotFoundException("Wallet not found for user"));
        if ( wallet.getBalance() < amount) {
            throw new InsufficientBalanceException("in-sufficient amount for withdraw");
        }
        wallet.setBalance(wallet.getBalance() - amount);
        return new WalletResponseDto(wallet.getBalance(), wallet.getCurrency(), wallet.getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto transfer(String username, String toUsername, Long amount) {
        Wallet senderWallet = walletRepository.findWallet(username).orElseThrow(() -> new WalletNotFoundException("Wallet not found for sender"));

        Wallet receiverWallet = walletRepository.findWallet(toUsername).orElseThrow(() -> new WalletNotFoundException("Wallet not found for receiver"));

        if (senderWallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient Balance to transfer");
        }

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        return new WalletResponseDto(senderWallet.getBalance(), senderWallet.getCurrency(), senderWallet.getUser().getUserName());
    }

}

