package com.example.emailservice.service;

// keep your existing imports and add:
import com.example.emailservice.config.KafkaConfig;
import com.example.emailservice.dto.EmailRequest;
import com.example.emailservice.kafka.EmailEvent;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventConsumer {

    private final EmailDispatcher emailDispatcher;
    private final EmailService emailService;
    private final EmailPayloadService emailPayloadService;     // NEW
    private final ObjectMapper objectMapper;

    // REST path: id published by EmailService.submit() the first one
    @KafkaListener(topics = KafkaConfig.EMAIL_TOPIC, groupId = "email-service")
    public void onMessage(String emailId) {
        log.info("Received email id {} from Kafka", emailId);
        emailDispatcher.dispatch(Long.valueOf(emailId));
    }

    // Event path: other services request an email the payload one
    @KafkaListener(topics = KafkaConfig.EMAIL_REQUEST_TOPIC, groupId = "email-service-requests")
    public void onEmailEvent(String payload) throws JsonProcessingException {
        EmailEvent event = objectMapper.readValue(payload, EmailEvent.class);
        log.info("Received email event for {} template={}", event.to(), event.templateName());

        EmailRequest request = new EmailRequest(
                event.to(), List.of(), List.of(), event.subject(), null, true,
                event.templateName(), event.variables(), null);

        Map<String, String> mdc = MDC.getCopyOfContextMap();

        CompletableFuture<EmailRecord> saveF = CompletableFuture.supplyAsync(
                withMdc(mdc, () -> emailPayloadService.saveOrGet(event, payload)));
        CompletableFuture<EmailService.PreparedEmail> prepF = CompletableFuture.supplyAsync(
                withMdc(mdc, () -> emailService.prepare(request)));

        EmailRecord record;
        EmailService.PreparedEmail prepared;
        try {
            record = saveF.join();
            prepared = prepF.join();
        } catch (CompletionException e) {
            throw unwrap(e);
        }

        if (record.getEmailStatus() == EmailStatus.SENT) {
            log.info("Event {} already SENT, skipping duplicate", event.requestId());
            return;
        }

        emailService.fill(record, request, prepared);
        emailDispatcher.dispatch(record);
    }

    private static <T> Supplier<T> withMdc(Map<String, String> mdc, Supplier<T> task) {
        return () -> {
            if (mdc != null) MDC.setContextMap(mdc);
            try { return task.get(); } finally { MDC.clear(); }
        };
    }

    private static RuntimeException unwrap(CompletionException e) {
        Throwable c = e.getCause() != null ? e.getCause() : e;
        return c instanceof RuntimeException re ? re : new RuntimeException(c);
    }
}