package com.notif.common.dto.identity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.entity.identity.AppUser;

class UserViewTest {

    @Test
    void viewCopiesIdentityFieldsAndOptionalInviteStatus() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setDisplayName("Ada");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        UserView plain = UserView.from(user);
        UserView withInvite = UserView.from(user, "failed", "SMTP down", Instant.parse("2026-01-02T00:00:00Z"));

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

        KitView kit = KitView.from(user);

        assertThat(kit.userId()).isEqualTo(user.getId());
        assertThat(kit.kit()).containsEntry("version", 1);
        assertThat(kit.rulesHu()).containsEntry("locale", "hu");
    }
}
