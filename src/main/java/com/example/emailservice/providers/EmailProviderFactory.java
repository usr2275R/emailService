package com.example.emailservice.providers;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.exception.ProviderConfigurationException;
import com.example.emailservice.exception.UnsupportedProviderException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
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
        boolean useDefault = (name == null || name.isBlank());
        String key = useDefault ? appProperties.getEmail().getProvider() : name;

        if (key == null || key.isBlank()) {
            throw new ProviderConfigurationException(
                    "No email provider specified and no default provider configured");
        }

        EmailProvider p = providers.get(key.toLowerCase(Locale.ROOT));
        if (p == null) {
            String msg = "Unknown email provider '" + key + "'. Available: " + providers.keySet();
            throw useDefault
                    ? new ProviderConfigurationException(msg)
                    : new UnsupportedProviderException(msg);
        }
        return p;
    }
}
