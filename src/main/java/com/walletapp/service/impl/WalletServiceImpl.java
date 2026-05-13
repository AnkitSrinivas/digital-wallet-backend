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

import java.util.Optional;

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
        Optional<Wallet> wallet = walletRepository.findWallet(userName);

        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found for the user");
        }

        WalletResponseDto walletResponseDto = new WalletResponseDto();
        walletResponseDto.setBalance(wallet.get().getBalance());
        walletResponseDto.setCurrency(wallet.get().getCurrency());
        walletResponseDto.setUserName(wallet.get().getUser().getUserName());

        return walletResponseDto;
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
        Optional<Wallet> wallet = walletRepository.findWallet(userName);
        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("wallet not found for the user");
        }
        if (wallet.get().getBalance() == 0 || wallet.get().getBalance() < amount) {
            throw new InsufficientBalanceException("in-sufficent amount for withdraw");
        }
        wallet.get().setBalance(wallet.get().getBalance() - amount);

        return new WalletResponseDto(wallet.get().getBalance(), wallet.get().getCurrency(), wallet.get().getUser().getUserName());
    }

    @Override
    @Transactional
    public WalletResponseDto transfer(String username, String toUsername, Long amount) {
        Optional<Wallet> wallet = walletRepository.findWallet(username);
        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found for user");
        } else {
            Optional<Wallet> toUserWallet = walletRepository.findWallet(toUsername);
            if (toUserWallet.isEmpty()) {
                throw new WalletNotFoundException("Wallet not found for to-User");
            }
            if (wallet.get().getBalance() < amount) {
                throw new InsufficientBalanceException("Insufficient Balance to transfer");
            }

            wallet.get().setBalance(wallet.get().getBalance() - amount);
            toUserWallet.get().setBalance(toUserWallet.get().getBalance() + amount);
            return new WalletResponseDto(wallet.get().getBalance(), wallet.get().getCurrency(), wallet.get().getUser().getUserName());
        }

    }
}
