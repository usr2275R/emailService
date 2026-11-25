package com.example.emailservice.service;

import com.example.emailservice.kafka.EmailEvent;
import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class EmailPayloadService {

    private final EmailRecordRepositary emailRecordRepositary;
    private final PlatformTransactionManager txManager;

    public EmailRecord saveOrGet(EmailEvent event, String payload) {
        if (event.requestId() == null || event.requestId().isBlank()) {
            throw new IllegalArgumentException("requestId is required");
        }

        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            return tx.execute(status ->
                    emailRecordRepositary.findByRequestId(event.requestId()).orElseGet(() -> {
                        EmailRecord r = new EmailRecord();
                        r.setRequestId(event.requestId());
                        r.setRequestPayload(payload);
                        r.setEmailStatus(EmailStatus.PENDING);
                        r.setAttempts(0);
                        r.setClientId("event management");
                        r.setToAddress(String.join(",", event.to()));
                        r.setTraceId(MDC.get("traceId"));
                        return emailRecordRepositary.saveAndFlush(r);
                    }));
        } catch (DataIntegrityViolationException e) {

            return emailRecordRepositary.findByRequestId(event.requestId())
                    .orElseThrow(() -> e);
        }
    }
}