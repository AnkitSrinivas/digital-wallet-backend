package com.walletapp.service;

import com.walletapp.dto.UserRequestDto;
import com.walletapp.dto.UserResponseDto;

public interface UserService {

    UserResponseDto saveUser(UserRequestDto userRequestDto);
}
