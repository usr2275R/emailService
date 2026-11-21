package com.example.emailservice.exception;


import com.example.emailservice.errorResponse.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex,
                                                     HttpServletRequest req) {

        log.warn("Unreadable request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {

        String message = "Parameter '" + ex.getName() + "' has an invalid value";
        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {

        return build(HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName(), req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException ex,
                                                        HttpServletRequest req) {

        return build(HttpStatus.BAD_REQUEST,
                "Missing required header: " + ex.getHeaderName(), req);
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ApiError> handleTemplateNotFound(TemplateNotFoundException ex,
                                                           HttpServletRequest req) {
        log.warn("Template not found: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(TemplateRenderException.class)
    public ResponseEntity<ApiError> handleTemplateRender(TemplateRenderException ex,
                                                         HttpServletRequest req) {
        log.warn("Template render failed: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), req);
    }

    @ExceptionHandler(UnsupportedProviderException.class)
    public ResponseEntity<ApiError> handleUnsupportedProvider(UnsupportedProviderException ex,
                                                              HttpServletRequest req) {
        log.warn("Unsupported provider: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidEmailStateException.class)
    public ResponseEntity<ApiError> handleInvalidState(InvalidEmailStateException ex,
                                                       HttpServletRequest req) {
        log.warn("Invalid email state: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(BulkLimitExceededException.class)
    public ResponseEntity<ApiError> handleBulkLimit(BulkLimitExceededException ex,
                                                    HttpServletRequest req) {
        log.warn("Bulk limit exceeded: {}", ex.getMessage());
        return build(HttpStatus.CONTENT_TOO_LARGE, ex.getMessage(), req);
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiError> handleSendFailure(EmailSendException ex,
                                                      HttpServletRequest req) {
        log.error("Email provider failure", ex);

        return build(HttpStatus.BAD_GATEWAY, "Email provider failed to process the request", req);
    }

    @ExceptionHandler(ProviderConfigurationException.class)
    public ResponseEntity<ApiError> handleProviderConfig(ProviderConfigurationException ex,
                                                         HttpServletRequest req) {
        log.error("Provider misconfigured: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Email provider is not configured", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest req) {
        log.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status,
                                           String message,
                                           HttpServletRequest req) {

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI(),
                MDC.get("traceId"));
        return ResponseEntity.status(status).body(body);
    }
}