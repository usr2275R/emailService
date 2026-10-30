package com.example.emailservice.exception;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

//    private ResponseEntity<Map<String ,Object>> buildErrorResponse(
//            HttpStatus status,
//            String message
//    ) {
//        String traceId = MDC.get("traceId");
//        Map<String, Object> body = new LinkedHashMap<>();
//
//        Map<String, Object> error = LinkedHashMap.newLinkedHashMap(5);
//        error.put("traceId", traceId != null ? traceId : "");
//        error.put("status", status.name());
//        error.put("errorCode", status.value());
//        error.put("timestamp", LocalDateTime.now().toString());
//        error.put("message", message);
//        body.put("error", error);
//        body.put("data", null);
//        return ResponseEntity
//                .status(status)
//                .body(body);
//    }
}
