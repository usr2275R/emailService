package com.example.emailservice.exception;

public class InvalidEmailStateException extends RuntimeException {

    public InvalidEmailStateException(String message) {
        super(message);
    }
}
