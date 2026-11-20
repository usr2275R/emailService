package com.example.emailservice.service;

import com.example.emailservice.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(Long emailId) {
        String id = String.valueOf(emailId);
        kafkaTemplate.send(KafkaConfig.EMAIL_TOPIC, id, id)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish email {} to Kafka", id, ex);
                    }
                });
    }
}