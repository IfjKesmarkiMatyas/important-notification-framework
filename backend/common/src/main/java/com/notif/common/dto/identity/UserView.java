package com.notif.common.dto.identity;

import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.entity.identity.AppUser;

import java.time.Instant;
import java.util.UUID;

public record UserView(
        UUID id,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        String lastInviteStatus,
        String lastInviteError,
        Instant lastInviteAt
) {
    public static UserView from(AppUser user) {
        return from(user, null, null, null);
    }

    public static UserView from(AppUser user, String lastInviteStatus, String lastInviteError, Instant lastInviteAt) {
        return new UserView(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                lastInviteStatus,
                lastInviteError,
                lastInviteAt
        );
    }
}
