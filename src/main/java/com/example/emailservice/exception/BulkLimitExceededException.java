package com.example.emailservice.exception;

public class BulkLimitExceededException extends EmailServiceException {
    public BulkLimitExceededException(int max, int actual) {

        super("Too many recipients: " + actual + " (max " + max + ")");
    }
}