package com.walletapp.exception;

public class WebhookSignatureMismatchException extends RuntimeException {
    public WebhookSignatureMismatchException(String message) {
        super(message);
    }
}
