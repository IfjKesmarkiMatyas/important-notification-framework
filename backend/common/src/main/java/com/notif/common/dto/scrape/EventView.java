package com.notif.common.dto.scrape;

import com.notif.common.entity.scrape.NormalizedEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventView(
        UUID id,
        String family,
        String sourceId,
        String externalId,
        Instant occurredAt,
        Instant ingestedAt,
        String locale,
        String headline,
        String summary,
        String canonicalUrl,
        Map<String, Object> payload
) {
    public static EventView from(NormalizedEvent event) {
        return new EventView(
                event.getId(),
                event.getFamily().name(),
                event.getSourceId().name(),
                event.getExternalId(),
                event.getOccurredAt(),
                event.getIngestedAt(),
                event.getLocale(),
                event.getHeadline(),
                event.getSummary(),
                event.getCanonicalUrl(),
                event.getPayload()
        );
    }
}
