package com.notif.common.dto.scrape;

import com.notif.common.entity.scrape.ScrapeRun;

import java.time.Instant;
import java.util.UUID;

public record RunView(
        UUID id,
        String sourceId,
        String status,
        int fetched,
        int normalized,
        String errorMessage,
        Instant startedAt,
        Instant finishedAt
) {
    public static RunView from(ScrapeRun run) {
        return new RunView(
                run.getId(),
                run.getSourceId().name(),
                run.getStatus().name(),
                run.getFetched(),
                run.getNormalized(),
                run.getErrorMessage(),
                run.getStartedAt(),
                run.getFinishedAt()
        );
    }
}
