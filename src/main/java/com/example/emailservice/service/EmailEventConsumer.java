package com.example.emailservice.service;

import com.example.emailservice.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventConsumer {

    private final EmailDispatcher emailDispatcher;

    @KafkaListener(topics = KafkaConfig.EMAIL_TOPIC, groupId = "email-service")
    public void onMessage(String emailId) {
        log.info("Received email id {} from Kafka", emailId);
        emailDispatcher.dispatch(Long.valueOf(emailId));
    }
}