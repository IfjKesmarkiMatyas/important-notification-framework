package com.notif.api.error;

import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.notif.common.exception.DecisionException;
import com.notif.common.exception.IdentityException;
import com.notif.common.exception.ScrapeException;

class ApiExceptionHandlerTest {

    @Test
    void mapsIdentityExceptionToJsonErrorBody() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.identity(new IdentityException(HttpStatus.CONFLICT, "User already active"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "User already active");
    }

    @Test
    void mapsScrapeExceptionToJsonErrorBody() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.scrape(new ScrapeException(HttpStatus.NOT_FOUND, "Event not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Event not found");
    }

    @Test
    void mapsDecisionExceptionToJsonErrorBody() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.decision(new DecisionException(HttpStatus.BAD_REQUEST, "Unknown decision engine: foo"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Unknown decision engine: foo");
    }
}
