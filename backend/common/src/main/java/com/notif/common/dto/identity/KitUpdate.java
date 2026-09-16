package com.notif.common.dto.identity;

import java.util.Map;

public record KitUpdate(
        Map<String, Object> kit,
        Map<String, Object> rulesHu,
        Map<String, Object> rulesEn
) {}
