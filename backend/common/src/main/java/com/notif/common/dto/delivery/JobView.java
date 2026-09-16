package com.notif.common.dto.delivery;

import com.notif.common.entity.delivery.DeliveryJob;

import java.time.Instant;
import java.util.UUID;

public record JobView(
        UUID id,
        String purpose,
        String channel,
        String recipient,
        String status,
        String errorMessage,
        int attemptCount,
        Instant createdAt,
        Instant sentAt
) {
    public static JobView from(DeliveryJob job) {
        return new JobView(
                job.getId(),
                job.getPurpose().name(),
                job.getChannel().name(),
                job.getRecipient(),
                job.getStatus().name(),
                job.getErrorMessage(),
                job.getAttemptCount(),
                job.getCreatedAt(),
                job.getSentAt()
        );
    }
}
