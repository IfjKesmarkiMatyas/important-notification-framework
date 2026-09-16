package com.notif.common.dto.identity;

import com.notif.common.entity.identity.DefaultKit;

import java.time.Instant;
import java.util.Map;

public record DefaultsView(
        Map<String, Object> kit,
        Map<String, Object> rulesHu,
        Map<String, Object> rulesEn,
        Instant updatedAt
) {
    public static DefaultsView from(DefaultKit row) {
        return new DefaultsView(row.getKit(), row.getRulesHu(), row.getRulesEn(), row.getUpdatedAt());
    }
}
