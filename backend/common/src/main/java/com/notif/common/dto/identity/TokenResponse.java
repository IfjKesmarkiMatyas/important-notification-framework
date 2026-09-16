package com.notif.common.dto.identity;

import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;

import java.util.UUID;

public record TokenResponse(
        String token,
        UUID id,
        String email,
        String displayName,
        UserRole role,
        UserStatus status
) {}
