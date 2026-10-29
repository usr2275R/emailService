package com.example.emailservice.dto;

import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;

import java.time.Instant;

public record EmailResponse(String id, EmailStatus emailStatus, int attempts,
                            String lastError, Instant createdAt, Instant sentAt) {
    public static EmailResponse from(EmailRecord emailRecord ) {
        return new EmailResponse(emailRecord.getId(),
                emailRecord.getEmailStatus(),
                emailRecord.getAttempts(),
                emailRecord.getLastError(),
                emailRecord.getCreatedAt(),
                emailRecord.getSentAt());
    }
}
