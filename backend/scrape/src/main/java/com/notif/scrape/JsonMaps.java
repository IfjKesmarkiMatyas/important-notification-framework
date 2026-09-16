package com.notif.scrape;

import java.util.List;
import java.util.Map;

final class JsonMaps {

    private JsonMaps() {}

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Map.of();
    }

    static List<?> list(Object value) {
        return value instanceof List<?> raw ? raw : List.of();
    }

    static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    static Double num(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
