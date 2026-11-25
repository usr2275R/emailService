package com.example.emailservice.service;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.providers.EmailProvider;
import com.example.emailservice.providers.EmailProviderFactory;
import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailDispatcher {

    private final EmailProviderFactory emailProviderFactory;
    private final EmailRecordRepositary emailRecordRepositary;
    private final AppProperties appProperties;

    // Id-based entry: which does the usual or previous flow
    public void dispatch(Long emailId) {
        EmailRecord r = emailRecordRepositary.findById(emailId).orElse(null);
        if (r != null) dispatch(r);
    }

    // Record-based entry: does the real work here is the status checker means prevents sending same email twicw
    public void dispatch(EmailRecord emailRecord) {
        if (emailRecord.getEmailStatus() == EmailStatus.SENT) return;

        emailRecord.setAttempts(emailRecord.getAttempts() + 1);
        emailRecord.setLastAttemptAt(Instant.now());

        try {
            EmailProvider provider = emailProviderFactory.get(emailRecord.getProvider());
            log.info("Email {} using provider {}", emailRecord.getId(), provider.name());
            provider.send(emailRecord);
        } catch (Exception e) {
            int maxAttempts = appProperties.getEmail().getMaxAttempts();
            boolean exhausted = emailRecord.getAttempts() >= maxAttempts;

            emailRecord.setEmailStatus(exhausted ? EmailStatus.FAILED : EmailStatus.PENDING);
            emailRecord.setLastError(truncate(e.getMessage()));
            emailRecordRepositary.save(emailRecord);

            log.warn("Email {} failed (attempt {}/{}): {}",
                    emailRecord.getId(), emailRecord.getAttempts(), maxAttempts, e.getMessage());
            throw e instanceof RuntimeException re ? re : new RuntimeException(e);
        }

        // The email is already out. if email is send it comes straight here
        try {
            emailRecord.setEmailStatus(EmailStatus.SENT);
            emailRecord.setSentAt(Instant.now());
            emailRecord.setLastError(null);
            emailRecordRepositary.save(emailRecord);
            log.info("Email {} sent to {}", emailRecord.getId(), emailRecord.getToAddress());
        } catch (Exception e) {
            log.error("Email {} was SENT but marking it SENT failed", emailRecord.getId(), e);
        }
    }

    private static String truncate(String s) {
        if (s == null) return "unknown error";
        return s.length() > 900 ? s.substring(0, 900) : s;
    }
}