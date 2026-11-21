package com.example.emailservice.errorResponse;

import com.example.emailservice.config.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ApiErrorBuilder {

    private ApiErrorBuilder() {}


    public static ApiError of(HttpStatus status, String message, HttpServletRequest request) {
            return new ApiError(
                    Instant.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    request.getRequestURI(),
                    (String) request.getAttribute(TraceIdFilter.TRACE_ID));
        }
    }

