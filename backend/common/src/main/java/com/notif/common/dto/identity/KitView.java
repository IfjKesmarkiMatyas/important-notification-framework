package com.notif.common.dto.identity;

import com.notif.common.entity.identity.AppUser;

import java.util.Map;
import java.util.UUID;

public record KitView(
        UUID userId,
        Map<String, Object> kit,
        Map<String, Object> rulesHu,
        Map<String, Object> rulesEn
) {
    public static KitView from(AppUser user) {
        return new KitView(user.getId(), user.getKit(), user.getRulesHu(), user.getRulesEn());
    }
}
