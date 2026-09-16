package com.notif.scrape;

public record FetchResult(Integer httpStatus, String contentType, String body, String errorMessage) {

    public static FetchResult ok(int status, String contentType, String body) {
        return new FetchResult(status, contentType, body, null);
    }

    public static FetchResult fail(Integer status, String body, String error) {
        return new FetchResult(status, null, body, error == null ? "fetch failed" : error);
    }

    public boolean ok() {
        return errorMessage == null && httpStatus != null && httpStatus >= 200 && httpStatus < 300;
    }
}
