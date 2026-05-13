package com.walletapp.service.impl;

import com.walletapp.config.JwtService;
import com.walletapp.dto.LoginRequestDto;
import com.walletapp.dto.LoginResponseDto;
import com.walletapp.dto.UserRequestDto;
import com.walletapp.dto.UserResponseDto;
import com.walletapp.entity.User;
import com.walletapp.entity.Wallet;
import com.walletapp.exception.InvalidCredentialException;
import com.walletapp.exception.UserAlreadyExistsException;
import com.walletapp.exception.UsernameNotFoundException;
import com.walletapp.repository.UserRepository;
import com.walletapp.repository.WalletRepository;
import com.walletapp.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletRepository walletRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponseDto saveUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            log.info("User already exists for email {}", userRequestDto.getEmail());
            throw new UserAlreadyExistsException("User already Exist for this email");
        }

        if (userRepository.existsByUserName(userRequestDto.getUsername())) {
            log.info("User already exists for username {}", userRequestDto.getUsername());
            throw new UserAlreadyExistsException("User already Exist for this username");
        }

        User user = new User();
        user.setUserName(userRequestDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setEmail(userRequestDto.getEmail());
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(0L);
        wallet.setCurrency("INR");
        walletRepository.save(wallet);

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setUserName(savedUser.getUserName());
        userResponseDto.setEmail(savedUser.getEmail());
        userResponseDto.setId(savedUser.getId());
        userResponseDto.setCreatedAt(savedUser.getCreatedAt());

        return userResponseDto;

    }

    @Override
    public LoginResponseDto generateLoginToken(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUserName(loginRequestDto.getUserName()).orElseThrow(() -> new UsernameNotFoundException("User doesn't exists"));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid UserName and Password");
        }
        return new LoginResponseDto(jwtService.generateToken(user), user.getUserName());
    }
}
