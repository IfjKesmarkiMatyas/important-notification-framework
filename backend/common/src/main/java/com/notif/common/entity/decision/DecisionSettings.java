package com.notif.common.entity.decision;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.notif.common.domain.decision.DecisionEngineMode;
import com.notif.common.domain.decision.DecisionEngineModeConverter;

@Entity
@Table(name = "decision_settings")
public class DecisionSettings {

    @Id
    private Short id = 1;

    @Convert(converter = DecisionEngineModeConverter.class)
    @Column(nullable = false, length = 20)
    private DecisionEngineMode mode = DecisionEngineMode.native_;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public DecisionEngineMode getMode() {
        return mode;
    }

    public void setMode(DecisionEngineMode mode) {
        this.mode = mode;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
