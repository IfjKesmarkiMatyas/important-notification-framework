package com.notif.scrape;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NormalizedEventRepository extends JpaRepository<NormalizedEvent, UUID> {

    boolean existsBySourceIdAndExternalId(SourceId sourceId, String externalId);

    List<NormalizedEvent> findTop100ByOrderByIngestedAtDesc();

    List<NormalizedEvent> findTop100ByFamilyOrderByIngestedAtDesc(EventFamily family);

    List<NormalizedEvent> findTop100BySourceIdOrderByIngestedAtDesc(SourceId sourceId);

    List<NormalizedEvent> findTop100ByFamilyAndSourceIdOrderByIngestedAtDesc(EventFamily family, SourceId sourceId);

    Optional<NormalizedEvent> findById(UUID id);
}
