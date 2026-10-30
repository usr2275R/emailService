package com.example.emailservice.service;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.stream.Stream;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;

import static org.aspectj.util.LangUtil.split;

@Service
@Data
@Slf4j
public class EmailDispatcher {

    private final JavaMailSender javaMailSender;
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
            MimeMessage mimeMessage=javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper= new MimeMessageHelper(mimeMessage,true,"UTF-8");
            mimeMessageHelper.setFrom(appProperties.getEmail().getFrom(),appProperties.getEmail().getFromName());
            mimeMessageHelper.setTo(split(emailRecord.getToAddress()));

            if (emailRecord.getCcAddress() != null && !emailRecord.getCcAddress().isBlank()) {
                mimeMessageHelper.setCc(split(emailRecord.getCcAddress()));
            }

            if (emailRecord.getBccAddress() != null && !emailRecord.getBccAddress().isBlank()) {
                mimeMessageHelper.setBcc(split(emailRecord.getBccAddress()));
            }
            mimeMessageHelper.setSubject(emailRecord.getSubject());
            mimeMessageHelper.setText(emailRecord.getBody(), emailRecord.isHtml());

            javaMailSender.send(mimeMessage);
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
    private static String[] split(String spt){
        return Arrays.stream(spt.split(","))
                .map(String::trim)
                .filter(s->!s.isEmpty())
                .toArray(String[]::new);
    }
    private static String truncate(String s){
        if(s==null)
            return "unknown error";
        return s.length()>900 ? s.substring(0,900) : s;
    }

}
