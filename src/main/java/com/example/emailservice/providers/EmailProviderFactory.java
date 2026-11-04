package com.example.emailservice.providers;

import com.example.emailservice.config.AppProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmailProviderFactory {

    private final Map<String, EmailProvider> providers;
    private final AppProperties appProperties;


    public EmailProviderFactory(List<EmailProvider> all, AppProperties appProperties) {
        this.providers = all.stream().collect(Collectors.toMap(EmailProvider::name, p -> p));
        this.appProperties = appProperties;
    }

    public EmailProvider get(String name) {
        String key = (name == null || name.isBlank())
                ? appProperties.getEmail().getProvider()
                : name;
        EmailProvider p = providers.get(key.toLowerCase());
        if (p == null) {
            throw new IllegalStateException(
                    "Unknown email provider '" + key + "'. Available: " + providers.keySet());
        }
        return p;
    }
}
