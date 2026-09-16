package com.notif.common.entity.decision;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.notif.common.domain.decision.DecisionEngineMode;
import com.notif.common.domain.decision.DecisionEngineModeConverter;

@Entity
@Table(name = "decision_runs")
public class DecisionRun {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Convert(converter = DecisionEngineModeConverter.class)
    @Column(nullable = false, length = 20)
    private DecisionEngineMode engine;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(nullable = false)
    private int evaluated;

    @Column(nullable = false)
    private int fired;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public DecisionEngineMode getEngine() {
        return engine;
    }

    public void setEngine(DecisionEngineMode engine) {
        this.engine = engine;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public int getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(int evaluated) {
        this.evaluated = evaluated;
    }

    public int getFired() {
        return fired;
    }

    public void setFired(int fired) {
        this.fired = fired;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}
