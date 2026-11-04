package com.example.emailservice.providers;

import com.example.emailservice.config.AppProperties;
import com.example.emailservice.model.EmailRecord;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SMTPEmailProvider implements EmailProvider {
    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    @Override
    public String name() {
        return "smtp";
    }


    @Override
    public void send(EmailRecord emailRecord) throws Exception {
        MimeMessage mime = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

        helper.setFrom(appProperties.getEmail().getFrom(), appProperties.getEmail().getFromName());
        helper.setTo(Address.split(emailRecord.getToAddress()));

        if (emailRecord.getCcAddress() != null && !emailRecord.getCcAddress().isBlank()) {
            helper.setCc(Address.split(emailRecord.getCcAddress()));
        }
        if (emailRecord.getBccAddress() != null && !emailRecord.getBccAddress().isBlank()) {
            helper.setBcc(Address.split(emailRecord.getBccAddress()));
        }


        helper.setSubject(emailRecord.getSubject());
        helper.setText(emailRecord.getBody(), emailRecord.isHtml());

        javaMailSender.send(mime);

    }
}