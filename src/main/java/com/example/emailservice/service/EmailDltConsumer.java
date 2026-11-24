package com.example.emailservice.service;

import com.example.emailservice.config.KafkaConfig;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.EMAIL_DLT_TOPIC, groupId = "email-service-dlt")
    public void consumeDlt(String payload,
                           @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String error) {

        log.error("Dead letter received | reason: {} | payload: {}", error, payload);

        String ref = "unknown";
        EmailRecord r = null;
        String recipient = "unknown";

        try {
            String body = payload == null ? "" : payload.trim();

            if (body.matches("\\d+")) {
                ref = "id " + body;
                r = emailRecordRepositary.findById(Long.valueOf(body)).orElse(null);
                if (r != null && r.getEmailStatus() != EmailStatus.SENT) {
                    r.setEmailStatus(EmailStatus.FAILED);
                    r.setLastError(error);
                    emailRecordRepositary.save(r);
                }
            } else {
                JsonNode req = objectMapper.readTree(body);
                ref = req.path("subject").asText("(no subject)");
                recipient = req.path("to").toString();
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(adminEmail);
            msg.setSubject("Email delivery failed: " + ref);
            msg.setText("""
                    An email could not be delivered after all retries.
                    
                    Email ID:  %s
                    Recipient: %s
                    Attempts:  %s
                    Error:     %s
                    """.formatted(
                    ref,
                    r != null ? r.getToAddress() : recipient,
                    r != null ? r.getAttempts() : "unknown",
                    error));

            mailSender.send(msg);
            log.info("Admin notified about failed email {}", ref);

        } catch (Exception e) {
            log.error("Could not process dead letter for email {}", ref, e);
        }
    }
}