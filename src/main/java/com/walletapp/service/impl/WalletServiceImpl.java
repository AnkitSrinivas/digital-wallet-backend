package com.walletapp.service.impl;

import com.walletapp.dto.ApiResponse;
import com.walletapp.dto.WalletResponseDto;
import com.walletapp.entity.User;
import com.walletapp.entity.Wallet;
import com.walletapp.exception.InsufficientBalanceException;
import com.walletapp.exception.UsernameNotFoundException;
import com.walletapp.exception.WalletNotFoundException;
import com.walletapp.repository.UserRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
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
        Optional<Wallet> wallet = walletRepository.findWallet(userName);
        if (wallet.isEmpty()) {
            Optional<User> user = userRepository.findByUserName(userName);
            if (user.isPresent()) {
                Wallet wallet1 = new Wallet();
                wallet1.setCurrency("INR");
                wallet1.setUser(user.orElse(new User()));
                wallet1.setBalance(amount);
                walletRepository.save(wallet1);
                return new WalletResponseDto(wallet1.getBalance(), wallet1.getCurrency(), wallet1.getUser().getUserName());
            } else {
                throw new UsernameNotFoundException("User Not Exists");
            }
        } else {
            wallet.get().setBalance(wallet.get().getBalance() + amount);
            return new WalletResponseDto(wallet.get().getBalance(), wallet.get().getCurrency(), wallet.get().getUser().getUserName());
        }

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
    public ApiResponse<Map<String, Object>> transfer(String username, String toUsername, Long amount) {
        Optional<Wallet> wallet = walletRepository.findWallet(username);
        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found for user");
        } else {
            Optional<Wallet> toUserWallet = walletRepository.findWallet(toUsername);
            if (toUserWallet.isEmpty()) {
                throw new WalletNotFoundException("Wallet not found for to-User");
            }
            if (wallet.get().getBalance() <= amount) {
                throw new InsufficientBalanceException("Insufficient Balance to transfer");
            }

            wallet.get().setBalance(wallet.get().getBalance() - amount);
            toUserWallet.get().setBalance(toUserWallet.get().getBalance() + amount);

            Map<String, Object> result = new HashMap<>();
            result.put("Transaction amount ", amount);
            result.put("Sender Wallet ", wallet.get().getUser().getUserName());
            result.put("Receiver Wallet ", toUserWallet.get().getUser().getUserName());

            return new ApiResponse<>("Transactional Successful", result, HttpStatus.OK.value());
        }

    }
}
