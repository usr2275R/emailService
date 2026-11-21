package com.example.emailservice.exception;

public class RetryableEmailException extends RuntimeException {
    public RetryableEmailException(String message,Throwable cause) {
        super(message,cause);
    }
}
