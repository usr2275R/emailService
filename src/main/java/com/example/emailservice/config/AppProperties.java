package config;

import jakarta.validation.constraints.Email;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app")

public class AppProperties {

    public final Email email = new Email();
    public final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

    public Email getEmail() {
        return email;
    }
    public static class Email{
        private String from;
        private String fromName;
        private int maxAttempts=3;
        private long retryAttemptsMs=6000;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getRetryAttemptsMs() {
            return retryAttemptsMs;
        }

        public void setRetryAttemptsMs(long retryAttemptsMs) {
            this.retryAttemptsMs = retryAttemptsMs;
        }
    }
    public static class Security{
        private Map<String,String> apiKeys = new HashMap<>();

        public Map<String, String> getApiKeys() {
            return apiKeys;
        }

        public void setApiKeys(Map<String, String> apiKeys) {
            this.apiKeys = apiKeys;
        }
    }
}
