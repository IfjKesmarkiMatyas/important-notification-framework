package com.notif.scrape.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.entity.scrape.NormalizedEvent;

public interface NormalizedEventRepository extends JpaRepository<NormalizedEvent, UUID> {

    boolean existsBySourceIdAndExternalId(SourceId sourceId, String externalId);

    Optional<NormalizedEvent> findBySourceIdAndExternalId(SourceId sourceId, String externalId);

    List<NormalizedEvent> findTop100ByOrderByIngestedAtDesc();

    List<NormalizedEvent> findTop100ByFamilyOrderByIngestedAtDesc(EventFamily family);

    List<NormalizedEvent> findTop100BySourceIdOrderByIngestedAtDesc(SourceId sourceId);

    List<NormalizedEvent> findTop100ByFamilyAndSourceIdOrderByIngestedAtDesc(EventFamily family, SourceId sourceId);

    Optional<NormalizedEvent> findById(UUID id);
}
