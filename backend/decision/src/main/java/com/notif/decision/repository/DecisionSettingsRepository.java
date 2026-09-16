package com.notif.decision.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.entity.decision.DecisionSettings;

public interface DecisionSettingsRepository extends JpaRepository<DecisionSettings, Short> {
    default DecisionSettings require() {
        Optional<DecisionSettings> row = findById((short) 1);
        return row.orElseThrow(() -> new IllegalStateException("decision_settings missing"));
    }
}
