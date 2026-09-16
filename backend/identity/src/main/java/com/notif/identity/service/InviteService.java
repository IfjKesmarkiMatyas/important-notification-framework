package com.notif.identity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.dto.delivery.EnqueueDeliveryCommand;
import com.notif.common.dto.identity.InvitePeek;
import com.notif.common.dto.identity.InviteResult;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.identity.InviteToken;
import com.notif.common.exception.IdentityException;
import com.notif.common.port.DeliveryDispatcher;
import com.notif.identity.repository.AppUserRepository;
import com.notif.identity.repository.InviteTokenRepository;

@Service
public class InviteService {

    private final AppUserRepository users;
    private final InviteTokenRepository tokens;
    private final DefaultKitService defaultKits;
    private final DeliveryDispatcher delivery;
    private final PasswordEncoder passwordEncoder;
    private final String publicBaseUrl;
    private final SecureRandom random = new SecureRandom();

    public InviteService(
            AppUserRepository users,
            InviteTokenRepository tokens,
            DefaultKitService defaultKits,
            DeliveryDispatcher delivery,
            PasswordEncoder passwordEncoder,
            @Value("${notif.public-base-url:http://localhost:4200}") String publicBaseUrl
    ) {
        this.users = users;
        this.tokens = tokens;
        this.defaultKits = defaultKits;
        this.delivery = delivery;
        this.passwordEncoder = passwordEncoder;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional
    public InviteResult invite(String email, String locale, String inviterName) {
        String normalized = email.trim().toLowerCase();
        AppUser user = users.findByEmailIgnoreCase(normalized).orElse(null);
        if (user != null && user.getStatus() == UserStatus.ACTIVE) {
            throw new IdentityException(HttpStatus.CONFLICT, "User already active");
        }
        if (user != null && user.getStatus() == UserStatus.INACTIVE) {
            throw new IdentityException(HttpStatus.CONFLICT, "User is inactive");
        }
        Instant now = Instant.now();
        if (user == null) {
            user = new AppUser();
            user.setId(UUID.randomUUID());
            user.setEmail(normalized);
            user.setRole(UserRole.USER);
            user.setStatus(UserStatus.INVITED);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            users.save(user);
        } else {
            user.setStatus(UserStatus.INVITED);
            user.setUpdatedAt(now);
            users.save(user);
        }
        String raw = issueToken(user);
        UUID jobId = enqueueInvite(user, raw, locale, inviterName);
        return new InviteResult(user.getId(), jobId);
    }

    @Transactional
    public InviteResult resend(UUID userId, String locale, String inviterName) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getStatus() != UserStatus.INVITED) {
            throw new IdentityException(HttpStatus.CONFLICT, "User is not awaiting invite");
        }
        String raw = issueToken(user);
        UUID jobId = enqueueInvite(user, raw, locale, inviterName);
        return new InviteResult(user.getId(), jobId);
    }

    public InvitePeek peek(String rawToken) {
        InviteToken token = requireOpenToken(rawToken);
        return new InvitePeek(token.getUser().getEmail());
    }

    @Transactional
    public AppUser accept(String rawToken, String displayName, String password) {
        InviteToken token = requireOpenToken(rawToken);
        AppUser user = token.getUser();
        if (user.getStatus() != UserStatus.INVITED) {
            throw new IdentityException(HttpStatus.BAD_REQUEST, "Invite is no longer valid");
        }
        Instant now = Instant.now();
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setStatus(UserStatus.ACTIVE);
        user.setKit(defaultKits.copyKitFor(user.getEmail()));
        user.setRulesHu(defaultKits.copyRulesHu());
        user.setRulesEn(defaultKits.copyRulesEn());
        user.setUpdatedAt(now);
        token.setUsedAt(now);
        tokens.save(token);
        tokens.findByUserAndUsedAtIsNull(user).forEach(open -> {
            open.setUsedAt(now);
            tokens.save(open);
        });
        return users.save(user);
    }

    @Transactional
    public void revoke(UUID userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getStatus() != UserStatus.INVITED) {
            throw new IdentityException(HttpStatus.CONFLICT, "Only pending invites can be revoked");
        }
        Instant now = Instant.now();
        user.setStatus(UserStatus.REVOKED);
        user.setUpdatedAt(now);
        users.save(user);
        tokens.findByUserAndUsedAtIsNull(user).forEach(open -> {
            open.setUsedAt(now);
            tokens.save(open);
        });
    }

    private InviteToken requireOpenToken(String rawToken) {
        String hash = sha256(rawToken);
        InviteToken token = tokens.findByTokenHash(hash)
                .orElseThrow(() -> new IdentityException(HttpStatus.BAD_REQUEST, "Invalid invite"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new IdentityException(HttpStatus.BAD_REQUEST, "Invite expired");
        }
        return token;
    }

    private String issueToken(AppUser user) {
        Instant now = Instant.now();
        tokens.findByUserAndUsedAtIsNull(user).forEach(open -> {
            open.setUsedAt(now);
            tokens.save(open);
        });
        byte[] buf = new byte[32];
        random.nextBytes(buf);
        String raw = HexFormat.of().formatHex(buf);
        InviteToken token = new InviteToken();
        token.setId(UUID.randomUUID());
        token.setUser(user);
        token.setTokenHash(sha256(raw));
        token.setExpiresAt(now.plus(7, ChronoUnit.DAYS));
        token.setCreatedAt(now);
        tokens.save(token);
        return raw;
    }

    private UUID enqueueInvite(AppUser user, String rawToken, String locale, String inviterName) {
        String loc = locale == null || locale.isBlank() ? "hu" : locale;
        String url = publicBaseUrl.replaceAll("/$", "") + "/invite/" + rawToken;
        return delivery.enqueue(new EnqueueDeliveryCommand(
                DeliveryPurpose.INVITE,
                DeliveryChannelType.email,
                user.getEmail(),
                loc,
                null,
                null,
                null,
                Map.of(
                        "inviteUrl", url,
                        "inviterName", inviterName == null || inviterName.isBlank() ? "Notif" : inviterName
                )
        ));
    }

    private static String sha256(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
