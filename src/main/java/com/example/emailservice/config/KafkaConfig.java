package com.example.emailservice.config;



import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    public static final String EMAIL_SEND_TOPIC = "email.send";
    public static final String EMAIL_TOPIC = "email-events";
    public static final String EMAIL_DLT_TOPIC = EMAIL_TOPIC + ".DLT";
    public static final String EMAIL_REQUEST_TOPIC = "email-request-topic";

    private final AppProperties appProperties;

    @Bean
    public NewTopic emailSendTopic() {
        return TopicBuilder.name(EMAIL_SEND_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic emailDltTopic() {
        return TopicBuilder.name(EMAIL_DLT_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> template) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template, (record, ex) -> new TopicPartition(EMAIL_DLT_TOPIC, -1));
        long retries = Math.max(0, appProperties.getEmail().getMaxAttempts() - 1);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, retries));
    }
}