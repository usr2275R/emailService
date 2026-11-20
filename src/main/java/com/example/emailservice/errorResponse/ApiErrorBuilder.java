package com.example.emailservice.errorResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public class ApiErrorBuilder {

    private ApiErrorBuilder() {}

    public static ApiError of(HttpStatus status, String message, HttpServletRequest request) {
            return of(status, message, request, List.of());
    }

    public static ApiError of(HttpStatus status, String message, HttpServletRequest request,
                                  List<ApiError.FieldViolation> violations) {
            return new ApiError(
                    Instant.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    request.getRequestURI(),
                    violations);
        }
    }

