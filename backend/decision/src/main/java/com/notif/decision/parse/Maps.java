package com.notif.decision.parse;

import java.util.List;
import java.util.Map;

public final class Maps {

    private Maps() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Map.of();
    }

    public static List<?> list(Object value) {
        return value instanceof List<?> raw ? raw : List.of();
    }

    public static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public static Double num(Object value) {
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

    public static boolean bool(Object value) {
        if (value instanceof Boolean flag) {
            return flag;
        }
        return "true".equalsIgnoreCase(str(value));
    }
}
