package com.example.emailservice.providers;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.model.EmailRecord;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SendGridEmailProvider implements EmailProvider{

    private final RestClient client;
    private final AppProperties appProperties;

    public SendGridEmailProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.client = RestClient.builder()
                .baseUrl("https://api.sendgrid.com/v3")
                .defaultHeader("Authorization", "Bearer " + appProperties.getEmail().getSendgridApiKey())
                .build();
    }
    @Override
    public String name() {
        return "sendgrid";
    }

    @Override
    public void send(EmailRecord emailRecord) {
        Map<String, Object> personalization = new HashMap<>();
        personalization.put("to", toList(emailRecord.getToAddress()));
        if (emailRecord.getCcAddress() != null && !emailRecord.getCcAddress().isBlank())
            personalization.put("cc", toList(emailRecord.getCcAddress()));
        if (emailRecord.getBccAddress() != null && !emailRecord.getBccAddress().isBlank())
            personalization.put("bcc", toList(emailRecord.getBccAddress()));

        Map<String, Object> body = Map.of(
                "personalizations", List.of(personalization),
                "from", Map.of("email", appProperties.getEmail().getFrom()),
                "subject", emailRecord.getSubject(),
                "content", List.of(Map.of(
                        "type", emailRecord.isHtml() ? "text/html" : "text/plain",
                        "value", emailRecord.getBody()))
        );

        client.post()
                .uri("/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
    private static List<Map<String, String>> toList(String csv) {
        return Arrays.stream(Address.split(csv))
                .map(e -> Map.of("email", e))
                .toList();
    }
}
