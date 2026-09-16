package com.notif.common.exception;

import org.springframework.http.HttpStatus;

public class DecisionException extends RuntimeException {

    private final HttpStatus status;

    public DecisionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
