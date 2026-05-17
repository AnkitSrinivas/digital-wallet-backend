package com.walletapp.exception;

public class PaymentOrderNotFoundException extends RuntimeException{
    public PaymentOrderNotFoundException(String message){
        super(message);
    }
}
