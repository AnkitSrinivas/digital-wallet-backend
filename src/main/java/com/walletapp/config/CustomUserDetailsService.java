package com.walletapp.config;


import com.walletapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.walletapp.entity.User user = userRepository.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("user not found: " + username));
        return User.withUsername(user.getUserName())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .build();

    }
}
