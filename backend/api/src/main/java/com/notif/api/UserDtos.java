package com.notif.api;

import com.notif.identity.AppUser;
import com.notif.identity.UserRole;
import com.notif.identity.UserStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {}

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
    ) {}

    public record KitView(
            UUID userId,
            Map<String, Object> kit,
            Map<String, Object> rulesHu,
            Map<String, Object> rulesEn
    ) {}

    public record KitUpdate(
            Map<String, Object> kit,
            Map<String, Object> rulesHu,
            Map<String, Object> rulesEn
    ) {}

    public static UserView view(AppUser user) {
        return view(user, null, null, null);
    }

    public static UserView view(AppUser user, String lastInviteStatus, String lastInviteError, Instant lastInviteAt) {
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

    public static KitView kit(AppUser user) {
        return new KitView(user.getId(), user.getKit(), user.getRulesHu(), user.getRulesEn());
    }
}
