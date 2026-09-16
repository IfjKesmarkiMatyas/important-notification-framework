package com.notif.common.dto.decision;

import java.util.List;
import java.util.UUID;
import com.notif.common.domain.decision.AlertLevel;
import com.notif.common.domain.decision.DecisionEngineMode;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.entity.decision.UserDecision;

public record DecisionResult(
        UUID id,
        UUID userId,
        String email,
        UUID eventId,
        String sourceId,
        String externalId,
        DecisionOutcome outcome,
        AlertLevel level,
        String reason,
        String engine,
        List<String> channels,
        List<String> deliveryJobIds,
        String errorMessage
) {
    public static DecisionResult from(UserDecision row, String email) {
        return new DecisionResult(
                row.getId(),
                row.getUserId(),
                email,
                row.getEventId(),
                row.getSourceId(),
                row.getExternalId(),
                row.getOutcome(),
                row.getLevel(),
                row.getReason(),
                row.getEngine() == null ? "native" : row.getEngine().toWire(),
                row.getChannels() == null ? List.of() : row.getChannels(),
                row.getDeliveryJobIds() == null ? List.of() : row.getDeliveryJobIds(),
                row.getErrorMessage()
        );
    }
}
