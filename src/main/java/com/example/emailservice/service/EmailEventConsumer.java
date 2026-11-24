package com.example.emailservice.service;

import com.example.emailservice.config.KafkaConfig;
import com.example.emailservice.dto.EmailRequest;
import com.example.emailservice.kafka.EmailEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventConsumer {

    private final EmailDispatcher emailDispatcher;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.EMAIL_TOPIC, groupId = "email-service")
    public void onMessage(String emailId) {

        log.info("Received email id {} from Kafka", emailId);
        emailDispatcher.dispatch(Long.valueOf(emailId));
    }

    @KafkaListener(topics = KafkaConfig.EMAIL_REQUEST_TOPIC, groupId = "email-service-requests")
    public void onEmailEvent(String payload) throws JsonProcessingException {
        EmailEvent event = objectMapper.readValue(payload, EmailEvent.class);
        log.info("Received email event for {} template={}", event.to(), event.templateName());

        EmailRequest request = new EmailRequest(
                event.to(),
                List.of(),              // cc
                List.of(),              // bcc
                event.subject(),
                null,             // body (template is used instead)
                true,                   // html
                event.templateName(),
                event.variables(),
                null                    // provider (default)
        );

        emailService.submit(request,"event management");     // use your actual method name
    }
}