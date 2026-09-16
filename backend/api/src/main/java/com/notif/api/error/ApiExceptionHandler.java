package com.notif.api.error;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.notif.common.exception.DecisionException;
import com.notif.common.exception.IdentityException;
import com.notif.common.exception.ScrapeException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<Map<String, String>> identity(IdentityException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DecisionException.class)
    public ResponseEntity<Map<String, String>> decision(DecisionException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ScrapeException.class)
    public ResponseEntity<Map<String, String>> scrape(ScrapeException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }
}
