package com.example.emailservice.templates;

import com.example.emailservice.exception.InvalidTemplateException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public enum EmailTemplate {
    WELCOME("welcome", v -> "Welcome to FinFlow"),
    PASSWORD_RESET("ticket/password-reset", v -> "Reset your password"),
    ORDER_CONFIRMATION("order-confirmation", v -> "Order confirmation"),
    NOTIFICATION("notification", v -> "Notification"),
    TICKET_CREATED("ticket/ticket-created", v -> "Ticket created: " + v.getOrDefault("ticketId", "")),
    TICKET_ASSIGNED("ticket/ticket-assigned", v -> "Ticket assigned: " + v.getOrDefault("ticketId", "")),
    TICKET_APPROVAL("ticket/ticket-approval", v -> "Approval needed: " + v.getOrDefault("ticketId", "")),
    TICKET_REJECTED("ticket/ticket-rejected", v -> "Ticket rejected: " + v.getOrDefault("ticketId", "")),
    TICKET_RESOLVED("ticket/ticket-resolved", v -> "Ticket resolved: " + v.getOrDefault("ticketId", "")),
    TICKET_COMMENT("ticket/ticket-comment", v -> "New comment on " + v.getOrDefault("ticketId", "")),
    TICKET_SLA_WARNING("ticket/ticket-sla-warning", v -> "SLA warning: " + v.getOrDefault("ticketId", "")),
    TICKET_STATUS_CHANGED("ticket/ticket-status-changed", v -> "Status changed: " + v.getOrDefault("ticketId", "")),
    TICKET_UPDATED("ticket/ticket-updated", v -> "Ticket updated"),
    NOTIFICATION_EMAIL_FULL("notification-email-full",
                            v -> String.valueOf(v.getOrDefault("subject", "Notification")));

    private final String key;
    private final Function<Map<String, Object>, String> subject;

    EmailTemplate(String key, Function<Map<String, Object>, String> subject) {
        this.key = key;
        this.subject = subject;
    }

    public String key() { return key; }

    public String defaultSubject(Map<String, Object> variables) {
        return subject.apply(variables == null ? Map.of() : variables);
    }

    public static EmailTemplate fromKey(String key) {
        return Arrays.stream(values())
                .filter(t -> t.key.equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new InvalidTemplateException("Unknown template: " + key));
    }
}