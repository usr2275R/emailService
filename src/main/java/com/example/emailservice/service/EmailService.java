package com.example.emailservice.service;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.dto.EmailRequest;
import com.example.emailservice.dto.EmailResponse;
import com.example.emailservice.exception.NotFoundException;
import com.example.emailservice.model.EmailProviderType;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final EmailRecordRepositary emailRecordRepositary;
    private final EmailEventProducer emailEventProducer;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final EmailDispatcher emailDispatcher;

    @Value("${spring.mail.username:noreply@mycompany.com}")
    private String fromEmail;

    @Value("${app.product-brand:My Company}")
    private String productBrand;

    @Value("${app.platform-brand:}")
    private String platformBrand;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${app.support-url:}")
    private String supportUrl;

    @Value("${app.privacy-url:}")
    private String privacyUrl;

    private static final DateTimeFormatter REPORTED_AT_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm z");

    public EmailResponse submit(EmailRequest emailRequest, String clientId) {
        String body = emailRequest.body();
        String subject = emailRequest.subject();
        boolean html = emailRequest.html();

        EmailProviderType chosen = emailRequest.provider() != null
                ? emailRequest.provider()
                : EmailProviderType.valueOf(appProperties.getEmail().getProvider().toUpperCase());

        if (emailRequest.templateName() != null && !emailRequest.templateName().isBlank()) {
            Context context = new Context();


            if (emailRequest.variables() != null) {
                context.setVariables(emailRequest.variables());
            }

            if (subject == null || subject.isBlank()) {
                subject = defaultSubject(emailRequest.templateName(), emailRequest.variables());
            }


            context.setVariable("subject", subject);
            if (!context.containsVariable("title")) {
                context.setVariable("title", subject);
            }
            context.setVariable("templateName", emailRequest.templateName());
            context.setVariable("recipientEmail", join(emailRequest.to()));
            context.setVariable("fromEmail", fromEmail);
            context.setVariable("productBrand", productBrand);
            context.setVariable("platformBrand", platformBrand);
            context.setVariable("appBaseUrl", appBaseUrl);
            context.setVariable("supportUrl", blankToNull(supportUrl));
            context.setVariable("privacyUrl", blankToNull(privacyUrl));
            context.setVariable("copyrightYear", String.valueOf(Year.now().getValue()));
            context.setVariable("reportedAt", ZonedDateTime.now().format(REPORTED_AT_FMT));


            try {
                body = templateEngine.process(emailRequest.templateName(), context);
            } catch (TemplateInputException e) {
                throw new IllegalArgumentException("Unknown template:" + emailRequest.templateName());
            }

            html = true;
        }

        EmailRecord emailRecord = new EmailRecord();

        emailRecord.setClientId(clientId);
        emailRecord.setToAddress(join(emailRequest.to()));
        emailRecord.setCcAddress(join(emailRequest.cc()));
        emailRecord.setBccAddress(join(emailRequest.bcc()));
        emailRecord.setSubject(subject);
        emailRecord.setBody(body);
        emailRecord.setHtml(html);
        emailRecord.setProvider(chosen.name().toLowerCase());
        emailRecord.setTraceId(MDC.get("traceId"));

        emailRecordRepositary.save(emailRecord);
        emailEventProducer.publish(emailRecord.getId());

        log.info("Email{} accepted from client {}", emailRecord.getId(), clientId);
        return EmailResponse.from(emailRecord);
    }

    public EmailResponse getStatus(Long id, String clientId) {
        return emailRecordRepositary.findByIdAndClientId(id, clientId)
                .map(EmailResponse::from)
                .orElseThrow(() -> new NotFoundException("Email not found" + id));
    }

    @Scheduled(fixedDelayString = "${app.email.retry-interval-ms:60000}")
    public void retryFailed() {
        List<EmailRecord> failed = emailRecordRepositary.findPendingEmails(
                EmailStatus.FAILED, appProperties.getEmail().getMaxAttempts());
        if (!failed.isEmpty()) {
            log.info("Retrying {} failed email(s)", failed.size());
            failed.forEach(r -> emailDispatcher.dispatch(r.getId()));
        }
    }

    private static String join(List<String> list) {
        return (list == null || list.isEmpty()) ? null : String.join(",", list);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static String defaultSubject(String template, Map<String, Object> vars) {
        if (vars != null && vars.get("subject") != null) return String.valueOf(vars.get("subject"));
        return "Notification: " + template;
    }

}