package com.example.emailservice.dto;

import com.example.emailservice.model.EmailProviderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;


public record EmailRequest (

    @NotEmpty(message = "at least one recipient must be required")
     List<@Email(message = "invalid 'to' address") String> to,
     List<@Email(message = "invalid 'cc' address") String> cc,
     List<@Email(message =  "invalid 'bcc' address") String> bcc,

    @Size(max=1000)
     String subject,
     String body,
     Boolean html,
     String templateName,
     Map<String,Object> variables,
    EmailProviderType provider
){

    public EmailRequest {
        if (html == null) html = true;
        if (cc == null) cc = List.of();
        if (bcc == null) bcc = List.of();
    }

    @JsonIgnore
    @AssertTrue(message = "provide either 'body' or 'templateName'")
    public boolean isBodyOrTemplatePresent() {
        return (body != null && !body.isBlank())
                || (templateName != null && !templateName.isBlank());
    }

    @JsonIgnore
    @AssertTrue(message = "'subject' is required when 'templateName' is not provided")
    public boolean isSubjectPresent() {
        return (subject != null && !subject.isBlank())
                || (templateName != null && !templateName.isBlank());
    }
}