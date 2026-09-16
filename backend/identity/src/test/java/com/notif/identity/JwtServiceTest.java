package com.notif.identity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void issueAndParseRoundTrip() {
        JwtService jwt = new JwtService("unit-test-secret-that-is-long-enough", 3600);
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@notif.local");
        user.setRole(UserRole.ADMIN);

        String token = jwt.issue(user);

        assertThat(jwt.userId(token)).isEqualTo(user.getId());
        assertThat(jwt.parse(token).get("email", String.class)).isEqualTo("admin@notif.local");
        assertThat(jwt.parse(token).get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void padsShortSecretRatherThanFailing() {
        JwtService jwt = new JwtService("short", 60);
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setRole(UserRole.USER);

        assertThat(jwt.issue(user)).isNotBlank();
    }

    @Test
    void rejectsTamperedToken() {
        JwtService jwt = new JwtService("unit-test-secret-that-is-long-enough", 3600);
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setRole(UserRole.USER);
        String token = jwt.issue(user);

        assertThatThrownBy(() -> jwt.parse(token + "x"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
