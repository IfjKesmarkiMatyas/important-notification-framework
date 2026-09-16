package com.notif.scrape;

import org.springframework.http.HttpStatus;

public class ScrapeException extends RuntimeException {

    private final HttpStatus status;

    public ScrapeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
