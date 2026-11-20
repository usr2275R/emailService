package com.example.emailservice.exception;

public class EmailServiceException extends RuntimeException {


    public EmailServiceException(String message) {super(message);}
    protected EmailServiceException(String message, Throwable cause) { super(message, cause);}
}
