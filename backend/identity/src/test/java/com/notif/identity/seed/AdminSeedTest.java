package com.notif.identity.seed;

import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.identity.repository.AppUserRepository;
import com.notif.identity.service.DefaultKitService;

@ExtendWith(MockitoExtension.class)
class AdminSeedTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private DefaultKitService defaultKits;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void seedsAdminWhenNoneExists() {
        when(users.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("adminadmin")).thenReturn("hash");
        when(defaultKits.copyKitFor("admin@notif.local")).thenReturn(Map.of("v", 1));
        when(defaultKits.copyRulesHu()).thenReturn(Map.of("locale", "hu"));
        when(defaultKits.copyRulesEn()).thenReturn(Map.of("locale", "en"));

        new AdminSeed(users, defaultKits, passwordEncoder, "Admin@Notif.local", "adminadmin")
                .run(new DefaultApplicationArguments());

        verify(defaultKits).ensureSeeded();
        verify(users).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getEmail().equals("admin@notif.local")
                        && user.getRole() == UserRole.ADMIN
                        && user.getStatus() == UserStatus.ACTIVE
                        && "hash".equals(user.getPasswordHash())
        ));
    }

    @Test
    void skipsWhenAdminAlreadyExists() {
        when(users.existsByRole(UserRole.ADMIN)).thenReturn(true);

        new AdminSeed(users, defaultKits, passwordEncoder, "admin@notif.local", "adminadmin")
                .run(new DefaultApplicationArguments());

        verify(defaultKits).ensureSeeded();
        verify(users, never()).save(any());
        assertThat(users.existsByRole(UserRole.ADMIN)).isTrue();
    }
}
