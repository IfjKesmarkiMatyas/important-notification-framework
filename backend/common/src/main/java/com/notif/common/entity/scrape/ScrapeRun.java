package com.notif.common.entity.scrape;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.notif.common.domain.scrape.ScrapeRunStatus;
import com.notif.common.domain.scrape.SourceId;

@Entity
@Table(name = "scrape_runs")
public class ScrapeRun {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_id", nullable = false, length = 40)
    private SourceId sourceId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScrapeRunStatus status;

    @Column(nullable = false)
    private int fetched;

    @Column(nullable = false)
    private int normalized;

    @Column(name = "error_message")
    private String errorMessage;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SourceId getSourceId() {
        return sourceId;
    }

    public void setSourceId(SourceId sourceId) {
        this.sourceId = sourceId;
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

    public ScrapeRunStatus getStatus() {
        return status;
    }

    public void setStatus(ScrapeRunStatus status) {
        this.status = status;
    }

    public int getFetched() {
        return fetched;
    }

    public void setFetched(int fetched) {
        this.fetched = fetched;
    }

    public int getNormalized() {
        return normalized;
    }

    public void setNormalized(int normalized) {
        this.normalized = normalized;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
