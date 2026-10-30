package com.example.emailservice.controller;

import com.example.emailservice.config.ApiKeyFilter;
import com.example.emailservice.dto.EmailRequest;
import com.example.emailservice.dto.EmailResponse;
import com.example.emailservice.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emails")
@Data
public class EmailController {
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<EmailResponse> send(@Valid @RequestBody EmailRequest emailRequest,
                                              HttpServletRequest httpServletRequest){
        String clientId = (String) httpServletRequest.getAttribute(ApiKeyFilter.CLIENT);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(emailService.submit(emailRequest,clientId));
    }

    @GetMapping("/{id}")
    public EmailResponse status(@PathVariable Long id, HttpServletRequest httpServletRequest){
        String clientId = (String) httpServletRequest.getAttribute(ApiKeyFilter.CLIENT);
        return emailService.getStatus(id,clientId);
    }
}
