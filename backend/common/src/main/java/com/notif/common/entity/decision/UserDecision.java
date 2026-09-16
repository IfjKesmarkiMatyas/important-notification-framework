package com.notif.common.entity.decision;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.notif.common.domain.decision.AlertLevel;
import com.notif.common.domain.decision.DecisionEngineMode;
import com.notif.common.domain.decision.DecisionEngineModeConverter;
import com.notif.common.domain.decision.DecisionOutcome;

@Entity
@Table(name = "user_decisions")
public class UserDecision {

    @Id
    private UUID id;

    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "source_id", nullable = false, length = 40)
    private String sourceId;

    @Column(name = "external_id", nullable = false, length = 500)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisionOutcome outcome;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AlertLevel level;

    @Column
    private String reason;

    @Convert(converter = DecisionEngineModeConverter.class)
    @Column(nullable = false, length = 20)
    private DecisionEngineMode engine;

    @Column(name = "error_message")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kit_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> kitSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> channels = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "delivery_job_ids", columnDefinition = "jsonb")
    private List<String> deliveryJobIds = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public DecisionOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(DecisionOutcome outcome) {
        this.outcome = outcome;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public DecisionEngineMode getEngine() {
        return engine;
    }

    public void setEngine(DecisionEngineMode engine) {
        this.engine = engine;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getKitSnapshot() {
        return kitSnapshot;
    }

    public void setKitSnapshot(Map<String, Object> kitSnapshot) {
        this.kitSnapshot = kitSnapshot;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public List<String> getDeliveryJobIds() {
        return deliveryJobIds;
    }

    public void setDeliveryJobIds(List<String> deliveryJobIds) {
        this.deliveryJobIds = deliveryJobIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
