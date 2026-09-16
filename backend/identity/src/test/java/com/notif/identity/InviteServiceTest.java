package com.notif.identity;

import com.notif.delivery.DeliveryChannelType;
import com.notif.delivery.DeliveryDispatcher;
import com.notif.delivery.DeliveryPurpose;
import com.notif.delivery.EnqueueDeliveryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private InviteTokenRepository tokens;

    @Mock
    private DefaultKitService defaultKits;

    @Mock
    private DeliveryDispatcher delivery;

    @Mock
    private PasswordEncoder passwordEncoder;

    private InviteService service;
    private final List<InviteToken> storedTokens = new ArrayList<>();

    @BeforeEach
    void setUp() {
        storedTokens.clear();
        service = new InviteService(
                users, tokens, defaultKits, delivery, passwordEncoder, "http://localhost:4200/"
        );
        lenient().when(tokens.save(any())).thenAnswer(invocation -> {
            InviteToken token = invocation.getArgument(0);
            storedTokens.removeIf(existing -> existing.getId().equals(token.getId()));
            storedTokens.add(token);
            return token;
        });
        lenient().when(tokens.findByUserAndUsedAtIsNull(any())).thenAnswer(invocation ->
                storedTokens.stream()
                        .filter(token -> token.getUsedAt() == null)
                        .toList()
        );
        lenient().when(tokens.findByTokenHash(any())).thenAnswer(invocation -> {
            String hash = invocation.getArgument(0);
            return storedTokens.stream()
                    .filter(token -> hash.equals(token.getTokenHash()))
                    .findFirst();
        });
        lenient().when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void inviteCreatesUserAndEnqueuesEmailJob() {
        when(users.findByEmailIgnoreCase("ada@notif.local")).thenReturn(Optional.empty());
        UUID jobId = UUID.randomUUID();
        when(delivery.enqueue(any())).thenReturn(jobId);

        InviteService.InviteResult result = service.invite("  Ada@Notif.local ", "hu", "Admin");

        assertThat(result.deliveryJobId()).isEqualTo(jobId);
        ArgumentCaptor<EnqueueDeliveryCommand> captor = ArgumentCaptor.forClass(EnqueueDeliveryCommand.class);
        org.mockito.Mockito.verify(delivery).enqueue(captor.capture());
        EnqueueDeliveryCommand command = captor.getValue();
        assertThat(command.purpose()).isEqualTo(DeliveryPurpose.INVITE);
        assertThat(command.channel()).isEqualTo(DeliveryChannelType.email);
        assertThat(command.recipient()).isEqualTo("ada@notif.local");
        assertThat(command.payload().get("inviteUrl").toString()).startsWith("http://localhost:4200/invite/");
        assertThat(command.payload().get("inviterName")).isEqualTo("Admin");
        assertThat(storedTokens).hasSize(1);
    }

    @Test
    void inviteRejectsActiveUser() {
        AppUser active = user(UserStatus.ACTIVE);
        when(users.findByEmailIgnoreCase("ada@notif.local")).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.invite("ada@notif.local", "hu", "Admin"))
                .isInstanceOf(IdentityException.class)
                .hasMessage("User already active")
                .extracting(ex -> ((IdentityException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void inviteRejectsInactiveUser() {
        AppUser inactive = user(UserStatus.INACTIVE);
        when(users.findByEmailIgnoreCase("ada@notif.local")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.invite("ada@notif.local", "hu", "Admin"))
                .isInstanceOf(IdentityException.class)
                .hasMessage("User is inactive");
    }

    @Test
    void acceptCopiesDefaultKitAndActivatesUser() {
        when(users.findByEmailIgnoreCase("ada@notif.local")).thenReturn(Optional.empty());
        ArgumentCaptor<EnqueueDeliveryCommand> captor = ArgumentCaptor.forClass(EnqueueDeliveryCommand.class);
        when(delivery.enqueue(any())).thenReturn(UUID.randomUUID());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(defaultKits.copyKitFor("ada@notif.local")).thenReturn(Map.of("email", "ada@notif.local"));
        when(defaultKits.copyRulesHu()).thenReturn(Map.of("locale", "hu"));
        when(defaultKits.copyRulesEn()).thenReturn(Map.of("locale", "en"));

        service.invite("ada@notif.local", "hu", "Admin");
        org.mockito.Mockito.verify(delivery).enqueue(captor.capture());
        String raw = captor.getValue().payload().get("inviteUrl").toString().substring("http://localhost:4200/invite/".length());

        AppUser accepted = service.accept(raw, "Ada", "password123");

        assertThat(accepted.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(accepted.getDisplayName()).isEqualTo("Ada");
        assertThat(accepted.getPasswordHash()).isEqualTo("hashed");
        assertThat(accepted.getKit()).containsEntry("email", "ada@notif.local");
        assertThat(storedTokens).allMatch(token -> token.getUsedAt() != null);
    }

    @Test
    void peekAndAcceptRejectExpiredOrUnknownTokens() {
        InviteToken expired = new InviteToken();
        expired.setTokenHash(sha256("dead"));
        expired.setExpiresAt(Instant.now().minusSeconds(60));
        expired.setUser(user(UserStatus.INVITED));
        storedTokens.add(expired);

        assertThatThrownBy(() -> service.peek("dead"))
                .isInstanceOf(IdentityException.class)
                .hasMessage("Invite expired");
        assertThatThrownBy(() -> service.peek("missing"))
                .isInstanceOf(IdentityException.class)
                .hasMessage("Invalid invite");
    }

    @Test
    void resendRequiresInvitedUser() {
        AppUser active = user(UserStatus.ACTIVE);
        when(users.findById(active.getId())).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.resend(active.getId(), "hu", "Admin"))
                .isInstanceOf(IdentityException.class)
                .hasMessage("User is not awaiting invite");
    }

    @Test
    void revokeMarksPendingInviteRevoked() {
        AppUser invited = user(UserStatus.INVITED);
        when(users.findById(invited.getId())).thenReturn(Optional.of(invited));

        service.revoke(invited.getId());

        assertThat(invited.getStatus()).isEqualTo(UserStatus.REVOKED);
    }

    @Test
    void revokeRejectsActiveUser() {
        AppUser active = user(UserStatus.ACTIVE);
        when(users.findById(active.getId())).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.revoke(active.getId()))
                .isInstanceOf(IdentityException.class)
                .hasMessage("Only pending invites can be revoked");
    }

    private static AppUser user(UserStatus status) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setRole(UserRole.USER);
        user.setStatus(status);
        return user;
    }

    private static String sha256(String raw) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
