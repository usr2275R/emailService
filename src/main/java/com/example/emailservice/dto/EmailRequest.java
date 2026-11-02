package com.example.emailservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Setter
@Getter
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

}
