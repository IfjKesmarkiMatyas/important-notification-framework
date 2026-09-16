package com.notif.decision.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.entity.decision.DecisionRun;

public interface DecisionRunRepository extends JpaRepository<DecisionRun, UUID> {
}
