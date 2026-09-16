package com.notif.common.domain.decision;

public enum DecisionEngineMode {
    native_,
    ai;

    public static DecisionEngineMode fromWire(String raw) {
        if (raw == null || raw.isBlank()) {
            return native_;
        }
        String value = raw.trim().toLowerCase();
        if ("ai".equals(value)) {
            return ai;
        }
        if ("native".equals(value) || "native_".equals(value)) {
            return native_;
        }
        throw new IllegalArgumentException("Unknown decision engine: " + raw);
    }

    public String toWire() {
        return this == ai ? "ai" : "native";
    }
}
