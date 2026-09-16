package com.notif.scrape.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.domain.scrape.ScrapeRunStatus;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.entity.scrape.ScrapeRun;

public interface ScrapeRunRepository extends JpaRepository<ScrapeRun, UUID> {

    Optional<ScrapeRun> findFirstBySourceIdOrderByStartedAtDesc(SourceId sourceId);

    Optional<ScrapeRun> findFirstBySourceIdAndStatusInOrderByStartedAtDesc(
            SourceId sourceId, List<ScrapeRunStatus> statuses);

    List<ScrapeRun> findTop50ByOrderByStartedAtDesc();

    List<ScrapeRun> findByStartedAtAfterOrderByStartedAtDesc(Instant after);
}
