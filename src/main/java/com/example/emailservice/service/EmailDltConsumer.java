package com.example.emailservice.service;

import com.example.emailservice.config.KafkaConfig;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailDltConsumer {

    private final EmailRecordRepositary emailRecordRepositary;
    private final JavaMailSender mailSender;

    @Value("${app.email.admin-alert-to}")
    private String adminEmail;

    @Value("${app.email.alert-from}")
    private String fromAddress;

    @KafkaListener(topics = KafkaConfig.EMAIL_DLT_TOPIC, groupId = "email-service-dlt")
    public void consumeDlt(String emailId,
                           @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String error) {
        log.error("Dead letter received for email {} | reason: {}", emailId, error);
        try {
            EmailRecord r = emailRecordRepositary.findById(Long.valueOf(emailId)).orElse(null);
            if (r != null && r.getEmailStatus() != EmailStatus.SENT) {
                r.setEmailStatus(EmailStatus.FAILED);
                r.setLastError(error);
                emailRecordRepositary.save(r);
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(adminEmail);
            msg.setSubject("Email delivery failed: id " + emailId);
            msg.setText("""
                An email could not be delivered after all retries.

                Email ID:  %s
                Recipient: %s
                Attempts:  %s
                Error:     %s
                """.formatted(emailId,
                    r != null ? r.getToAddress() : "unknown",
                    r != null ? r.getAttempts() : "unknown",
                    error));
            mailSender.send(msg);
            log.info("Admin notified about failed email {}", emailId);
        } catch (Exception e) {
            log.error("Could not process dead letter for email {}", emailId, e);
        }
    }
}