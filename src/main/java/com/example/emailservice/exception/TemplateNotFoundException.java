package com.example.emailservice.exception;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String templateName) {
        super("Email template not found: " + templateName);
    }
}
