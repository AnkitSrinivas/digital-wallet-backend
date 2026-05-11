package com.walletapp.service;

import com.walletapp.dto.LoginRequestDto;
import com.walletapp.dto.LoginResponseDto;
import com.walletapp.dto.UserRequestDto;
import com.walletapp.dto.UserResponseDto;
import com.walletapp.entity.User;

public interface UserService {

    UserResponseDto saveUser(UserRequestDto userRequestDto);

    User getUserDetails(String userName);

    LoginResponseDto generateLoginToken(LoginRequestDto loginRequestDto,User user);
}
