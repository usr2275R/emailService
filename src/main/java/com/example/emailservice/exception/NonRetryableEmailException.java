package com.example.emailservice.exception;

public class NonRetryableEmailException extends RuntimeException {

    public NonRetryableEmailException(String message,Throwable cause) {
        super(message,cause);
    }
}
