package com.notif.api;

import com.notif.identity.IdentityException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
                handler.scrape(new com.notif.scrape.ScrapeException(HttpStatus.NOT_FOUND, "Event not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Event not found");
    }
}
