package com.notif.common.entity.identity;

import java.time.Instant;
import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "default_kits")
public class DefaultKit {

    @Id
    private Short id = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> kit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_hu", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> rulesHu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_en", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> rulesEn;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
