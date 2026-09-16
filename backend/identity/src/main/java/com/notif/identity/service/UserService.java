package com.notif.identity.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.exception.IdentityException;
import com.notif.identity.repository.AppUserRepository;

@Service
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> list() {
        return users.findAll();
    }

    public List<AppUser> listActive() {
        return users.findByStatus(UserStatus.ACTIVE);
    }

    public AppUser require(UUID id) {
        return users.findById(id).orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public AppUser requireByEmail(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IdentityException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    public AppUser authenticate(String email, String password) {
        AppUser user = users.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new IdentityException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.getStatus() != UserStatus.ACTIVE || user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IdentityException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return user;
    }

    @Transactional
    public AppUser updateKit(UUID userId, Map<String, Object> kit, Map<String, Object> rulesHu, Map<String, Object> rulesEn) {
        AppUser user = require(userId);
        if (kit != null) {
            user.setKit(kit);
        }
        if (rulesHu != null) {
            user.setRulesHu(rulesHu);
        }
        if (rulesEn != null) {
            user.setRulesEn(rulesEn);
        }
        user.setUpdatedAt(Instant.now());
        return users.save(user);
    }

    @Transactional
    public AppUser deactivate(UUID userId) {
        AppUser user = require(userId);
        if (user.getRole() == UserRole.ADMIN) {
            throw new IdentityException(HttpStatus.CONFLICT, "Cannot deactivate an admin");
        }
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(Instant.now());
        return users.save(user);
    }
}
