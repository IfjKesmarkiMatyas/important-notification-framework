package com.notif.common.dto.identity;

import java.util.UUID;

public record InviteResponse(UUID userId, UUID deliveryJobId) {}
