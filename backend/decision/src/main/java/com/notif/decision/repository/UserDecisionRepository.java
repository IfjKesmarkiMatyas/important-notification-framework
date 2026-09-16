package com.notif.decision.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.entity.decision.UserDecision;

public interface UserDecisionRepository extends JpaRepository<UserDecision, UUID> {
    Optional<UserDecision> findByUserIdAndSourceIdAndExternalId(UUID userId, String sourceId, String externalId);

    List<UserDecision> findByEventIdOrderByCreatedAtAsc(UUID eventId);

    List<UserDecision> findTop100ByOrderByCreatedAtDesc();
}
