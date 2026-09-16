package com.notif.common.entity.identity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> kit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_hu", columnDefinition = "jsonb")
    private Map<String, Object> rulesHu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_en", columnDefinition = "jsonb")
    private Map<String, Object> rulesEn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Map<String, Object> getKit() {
        return kit;
    }

    public void setKit(Map<String, Object> kit) {
        this.kit = kit;
    }

    public Map<String, Object> getRulesHu() {
        return rulesHu;
    }

    public void setRulesHu(Map<String, Object> rulesHu) {
        this.rulesHu = rulesHu;
    }

    public Map<String, Object> getRulesEn() {
        return rulesEn;
    }

    public void setRulesEn(Map<String, Object> rulesEn) {
        this.rulesEn = rulesEn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
