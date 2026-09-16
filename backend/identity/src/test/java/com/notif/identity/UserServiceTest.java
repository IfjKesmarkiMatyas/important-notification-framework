package com.notif.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(users, passwordEncoder);
    }

    @Test
    void authenticateAcceptsActiveUserWithMatchingPassword() {
        AppUser user = user(UserRole.USER, UserStatus.ACTIVE);
        user.setPasswordHash("hash");
        when(users.findByEmailIgnoreCase("ada@notif.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        assertThat(service.authenticate("  Ada@Notif.local ", "secret")).isSameAs(user);
    }

    @Test
    void authenticateRejectsUnknownEmail() {
        when(users.findByEmailIgnoreCase("ghost@notif.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("ghost@notif.local", "x"))
                .isInstanceOf(IdentityException.class)
                .extracting(ex -> ((IdentityException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticateRejectsInvitedUser() {
        AppUser user = user(UserRole.USER, UserStatus.INVITED);
        user.setPasswordHash("hash");
        when(users.findByEmailIgnoreCase("ada@notif.local")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.authenticate("ada@notif.local", "secret"))
                .isInstanceOf(IdentityException.class)
                .hasMessage("Invalid credentials");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deactivateRejectsAdmin() {
        AppUser admin = user(UserRole.ADMIN, UserStatus.ACTIVE);
        when(users.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.deactivate(admin.getId()))
                .isInstanceOf(IdentityException.class)
                .hasMessage("Cannot deactivate an admin");
    }

    @Test
    void deactivateMarksUserInactive() {
        AppUser user = user(UserRole.USER, UserStatus.ACTIVE);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = service.deactivate(user.getId());

        assertThat(saved.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void updateKitReplacesProvidedDocuments() {
        AppUser user = user(UserRole.USER, UserStatus.ACTIVE);
        user.setKit(Map.of("version", 1));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = service.updateKit(user.getId(), Map.of("version", 2), null, Map.of("locale", "en"));

        assertThat(saved.getKit()).containsEntry("version", 2);
        assertThat(saved.getRulesEn()).containsEntry("locale", "en");
    }

    private static AppUser user(UserRole role, UserStatus status) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
