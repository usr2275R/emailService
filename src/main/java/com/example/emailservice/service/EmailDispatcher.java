package com.example.emailservice.service;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.providers.EmailProvider;
import com.example.emailservice.providers.EmailProviderFactory;
import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Data
@Slf4j
public class EmailDispatcher {

    private final EmailProviderFactory emailProviderFactory;
    private final EmailRecordRepositary emailRecordRepositary;
    private final AppProperties appProperties;

    @Async("emailExecutor")
    public void dispatch(Long emailId){
        EmailRecord emailRecord= emailRecordRepositary.findById(emailId).orElse(null);
        if(emailRecord==null || emailRecord.getEmailStatus()== EmailStatus.SENT)
            return;

        emailRecord.setAttempts(emailRecord.getAttempts() + 1);
        emailRecord.setLastAttemptAt(Instant.now());

        try{
            EmailProvider provider = emailProviderFactory.get(emailRecord.getProvider());
            log.info("Email {} using provider {}", emailRecord.getId(), provider.name());
            provider.send(emailRecord);
            emailRecord.setEmailStatus(EmailStatus.SENT);

            emailRecord.setSentAt(Instant.now());
            emailRecord.setLastError(null);
            log.info("Email {} sent to {}", emailRecord.getId(), emailRecord.getToAddress());
        }
        catch (Exception e){
            emailRecord.setEmailStatus(EmailStatus.FAILED);
            emailRecord.setLastError(truncate(e.getMessage()));
            log.warn("Email {} failed (attempt {}/{}) : {}",
                    emailRecord.getId(),
                    emailRecord.getAttempts(),
                    appProperties.getEmail().getMaxAttempts(),
                    e.getMessage());
        }
        emailRecordRepositary.save(emailRecord);
    }

    private static String truncate(String s){
        if(s==null)
            return "unknown error";
        return s.length()>900 ? s.substring(0,900) : s;
    }

}
