package com.notif.api;

import com.notif.identity.IdentityException;
import com.notif.scrape.ScrapeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<Map<String, String>> identity(IdentityException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ScrapeException.class)
    public ResponseEntity<Map<String, String>> scrape(ScrapeException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }
}
