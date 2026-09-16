package com.notif.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AdminSeed implements ApplicationRunner {

    private final AppUserRepository users;
    private final DefaultKitService defaultKits;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminSeed(
            AppUserRepository users,
            DefaultKitService defaultKits,
            PasswordEncoder passwordEncoder,
            @Value("${notif.admin.email:admin@notif.local}") String email,
            @Value("${notif.admin.password:adminadmin}") String password
    ) {
        this.users = users;
        this.defaultKits = defaultKits;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        defaultKits.ensureSeeded();
        if (users.existsByRole(UserRole.ADMIN)) {
            return;
        }
        Instant now = Instant.now();
        String normalized = email.trim().toLowerCase();
        AppUser admin = new AppUser();
        admin.setId(UUID.randomUUID());
        admin.setEmail(normalized);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setDisplayName("Admin");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setKit(defaultKits.copyKitFor(normalized));
        admin.setRulesHu(defaultKits.copyRulesHu());
        admin.setRulesEn(defaultKits.copyRulesEn());
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        users.save(admin);
    }
}
