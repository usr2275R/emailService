package com.example.emailservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name="email_record", indexes = {
        @Index(name="idx_email_status", columnList = "email_status"),
        @Index(name ="idx_email_client", columnList = "client_id")
})
@RequiredArgsConstructor
@Getter
@Setter
public class EmailRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false, length = 300)
    private String toAddress;

    @Column(columnDefinition = "TEXT")
    private String ccAddress;

    @Column(columnDefinition = "TEXT")
    private String bccAddress;

    @Column(nullable = false,length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    private boolean html;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus emailStatus = EmailStatus.PENDING;

    private int attempts;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant sentAt;
    private Instant lastAttemptAt;
}
