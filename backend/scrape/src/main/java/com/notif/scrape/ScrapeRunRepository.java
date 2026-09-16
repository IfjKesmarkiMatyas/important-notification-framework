package com.notif.scrape;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScrapeRunRepository extends JpaRepository<ScrapeRun, UUID> {

    Optional<ScrapeRun> findFirstBySourceIdOrderByStartedAtDesc(SourceId sourceId);

    Optional<ScrapeRun> findFirstBySourceIdAndStatusInOrderByStartedAtDesc(
            SourceId sourceId, List<ScrapeRunStatus> statuses);

    List<ScrapeRun> findTop50ByOrderByStartedAtDesc();

    List<ScrapeRun> findByStartedAtAfterOrderByStartedAtDesc(Instant after);
}
