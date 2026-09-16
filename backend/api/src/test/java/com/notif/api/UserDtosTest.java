package com.notif.api;

import com.notif.identity.AppUser;
import com.notif.identity.UserRole;
import com.notif.identity.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtosTest {

    @Test
    void viewCopiesIdentityFieldsAndOptionalInviteStatus() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setDisplayName("Ada");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        UserDtos.UserView plain = UserDtos.view(user);
        UserDtos.UserView withInvite = UserDtos.view(user, "failed", "SMTP down", Instant.parse("2026-01-02T00:00:00Z"));

        assertThat(plain.email()).isEqualTo("ada@notif.local");
        assertThat(plain.lastInviteStatus()).isNull();
        assertThat(withInvite.lastInviteStatus()).isEqualTo("failed");
        assertThat(withInvite.lastInviteError()).isEqualTo("SMTP down");
    }

    @Test
    void kitWrapsUserDocuments() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setKit(Map.of("version", 1));
        user.setRulesHu(Map.of("locale", "hu"));
        user.setRulesEn(Map.of("locale", "en"));

        UserDtos.KitView kit = UserDtos.kit(user);

        assertThat(kit.userId()).isEqualTo(user.getId());
        assertThat(kit.kit()).containsEntry("version", 1);
        assertThat(kit.rulesHu()).containsEntry("locale", "hu");
    }
}
