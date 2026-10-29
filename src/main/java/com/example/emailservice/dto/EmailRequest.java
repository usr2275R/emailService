package com.example.emailservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class EmailRequest {

    @NotEmpty(message = "at least one recipient must be required")
    private List<@Email(message = "invalid 'to' address") String> to;
    private List<@Email(message = "invalid 'cc' address") String> cc;
    private List<@Email(message =  "invalid 'bcc' address") String> bcc;

    @Size(max=1000)
    private String subject;
    private String body;
    private boolean html=true;
    private String templateName;
    private Map<String,Object> variables;

    @AssertTrue(message = "provide either 'body'or 'templateName'")
    public boolean isBodyOrTemplatePresent(){
        return (body !=null && !body.isBlank()) || (templateName !=null && !templateName.isEmpty());
    }

    @AssertTrue(message = "'subject' is required when ")
    public boolean isSubjectPresent(){
        return (subject != null && !subject.isBlank()) || (templateName !=null && !templateName.isEmpty());
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
