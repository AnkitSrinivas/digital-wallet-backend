package com.walletapp.exception;

public class RefreshTokenAlreadyExpired extends RuntimeException{
    public RefreshTokenAlreadyExpired(String message){
        super(message);
    }
}
