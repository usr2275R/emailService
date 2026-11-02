package com.example.emailservice.service;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.dto.EmailRequest;
import com.example.emailservice.dto.EmailResponse;
import com.example.emailservice.exception.NotFoundException;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor

public class EmailService {

   private final EmailRecordRepositary emailRecordRepositary;
   private final EmailDispatcher emailDispatcher;
   private final TemplateEngine templateEngine;
   private final AppProperties appProperties;

public EmailResponse submit(EmailRequest emailRequest, String clientId){
    String body = emailRequest.body();
    String subject = emailRequest.subject();
    boolean html = emailRequest.html();

    if(emailRequest.templateName() !=null && !emailRequest.templateName().isBlank()){
        Context context =new Context();
        if(emailRequest.variables() != null)
            context.setVariables(emailRequest.variables());
        try{
            body = templateEngine.process(emailRequest.templateName(),context);
        }
        catch (TemplateInputException e){
            throw new IllegalArgumentException("Unknown template:" +emailRequest.templateName());
        }

        html=true;

        if(subject ==null || subject.isBlank()){
            subject= defaultSubject(emailRequest.templateName(),emailRequest.variables());
        }
    }

    EmailRecord emailRecord = new EmailRecord();
    emailRecord.setClientId(clientId);
    emailRecord.setToAddress(join(emailRequest.to()));
    emailRecord.setCcAddress(join(emailRequest.cc()));
    emailRecord.setBccAddress(join(emailRequest.bcc()));
    emailRecord.setSubject(subject);
    emailRecord.setBody(body);
    emailRecord.setHtml(html);
    emailRecordRepositary.save(emailRecord);

    emailDispatcher.dispatch(emailRecord.getId());
    log.info("Email{} accepted from client {}",emailRecord.getId(),clientId);
    return EmailResponse.from(emailRecord);
}

public EmailResponse getStatus(Long id, String clientId){
    return emailRecordRepositary.findByIdAndClientId(id,clientId)
            .map(EmailResponse::from)
            .orElseThrow(()-> new NotFoundException("Email not found"+id));
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
    private static String defaultSubject(String template, Map<String, Object> vars) {
        if (vars != null && vars.get("subject") != null) return String.valueOf(vars.get("subject"));
        return "Notification: " + template;
    }

}