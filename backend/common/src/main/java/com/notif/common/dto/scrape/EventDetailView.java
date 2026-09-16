package com.notif.common.dto.scrape;

import com.notif.common.entity.scrape.NormalizedEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventDetailView(
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
        Map<String, Object> payload,
        UUID rawIntakeId,
        String rawExcerpt
) {
    public static EventDetailView from(NormalizedEvent event, String rawExcerpt) {
        return new EventDetailView(
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
                event.getPayload(),
                event.getRawIntakeId(),
                rawExcerpt
        );
    }
}
