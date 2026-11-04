package com.example.emailservice.providers;

import com.example.emailservice.model.EmailRecord;

public interface EmailProvider {
    String name();
    void send(EmailRecord emailRecord) throws Exception;
}
