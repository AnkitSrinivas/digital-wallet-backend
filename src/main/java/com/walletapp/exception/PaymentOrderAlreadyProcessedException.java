package com.walletapp.exception;

public class PaymentOrderAlreadyProcessedException extends RuntimeException{
    public PaymentOrderAlreadyProcessedException(String message){
        super(message);
    }
}
