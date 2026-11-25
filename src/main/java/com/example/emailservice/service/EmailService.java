package com.example.emailservice.service;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.dto.EmailRequest;
import com.example.emailservice.dto.EmailResponse;
import com.example.emailservice.exception.NotFoundException;
import com.example.emailservice.exception.TemplateNotFoundException;
import com.example.emailservice.kafka.EmailEvent;
import com.example.emailservice.model.EmailProviderType;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import com.example.emailservice.templates.EmailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

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
    private final EmailPayloadService emailPayloadService;

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

    public record PreparedEmail(String subject, String body, boolean html,
                                EmailProviderType provider) {}          // NEW

    // NEW: template + provider logic moved out of submit()
    public PreparedEmail prepare(EmailRequest emailRequest) {
        String body = emailRequest.body();
        String subject = emailRequest.subject();
        boolean html = emailRequest.html();
        EmailTemplate template = EmailTemplate.fromKey(emailRequest.templateName());

        EmailProviderType chosen = emailRequest.provider() != null
                ? emailRequest.provider()
                : EmailProviderType.valueOf(appProperties.getEmail().getProvider().toUpperCase());

        if (emailRequest.templateName() != null && !emailRequest.templateName().isBlank()) {
            Context context = new Context();

            if (emailRequest.variables() != null) {
                context.setVariables(emailRequest.variables());
            }

            if (subject == null || subject.isBlank()) {
                subject = template.defaultSubject(emailRequest.variables());
            }

            context.setVariable("subject", subject);
            if (!context.containsVariable("title")) {
                context.setVariable("title", subject);
            }
            context.setVariable("templateName", template.key());

            try {
                body = templateEngine.process(template.key(), context);
            } catch (TemplateInputException e) {
                throw new TemplateNotFoundException("Unknown template:" + emailRequest.templateName());
            }

            html = true;
        }
        return new PreparedEmail(subject, body, html, chosen);
    }

    // just after prepare
    public void fill(EmailRecord r, EmailRequest req, PreparedEmail p) {
        r.setToAddress(join(req.to()));
        r.setCcAddress(join(req.cc()));
        r.setBccAddress(join(req.bcc()));
        r.setSubject(p.subject());
        r.setBody(p.body());
        r.setHtml(p.html());
        r.setProvider(p.provider().name().toLowerCase());
    }

    // two submit Eventsubmit 1st
    public EmailResponse submit(EmailRequest emailRequest, String clientId) {
        PreparedEmail p = prepare(emailRequest);
        EmailRecord emailRecord = new EmailRecord();
        emailRecord.setClientId(clientId);
        fill(emailRecord, emailRequest, p);
        emailRecord.setTraceId(MDC.get("traceId"));

        emailRecordRepositary.save(emailRecord);
        emailEventProducer.publish(emailRecord.getId());

        log.info("Email {} accepted from client {}", emailRecord.getId(), clientId);
        return EmailResponse.from(emailRecord);
    }

    // second here to combine  prepare then fill
    public void submitEvent(EmailEvent event, String payload, EmailRequest req) {
        EmailRecord r = emailPayloadService.saveOrGet(event, payload);

        if (r.getEmailStatus() == EmailStatus.SENT) {
            return;
        }

        PreparedEmail p = prepare(req);
        fill(r, req, p);
        emailRecordRepositary.save(r);
        emailEventProducer.publish(r.getId());

        log.info("Email {} accepted from event {}", r.getId(), event.requestId());
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