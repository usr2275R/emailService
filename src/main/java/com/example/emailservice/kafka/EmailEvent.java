package com.example.emailservice.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailEvent(
        String requestId,
        List<String> to,
        String subject,
        String templateName,
        Map<String, Object> variables
) {}