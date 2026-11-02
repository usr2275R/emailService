package com.example.emailservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    public final Email email = new Email();
    public final Security security = new Security();

    @Setter
    @Getter
    public static class Email{
        private String from;
        private String fromName;
        private int maxAttempts=3;
        private long retryAttemptsMs=6000;

    }

    @Setter
    @Getter
    public static class Security{
        private Map<String,String> apiKeys = new HashMap<>();

    }
}
