package com.notif.scrape;

import java.time.Instant;
import java.util.Map;

public record NormalizedDraft(
        EventFamily family,
        SourceId sourceId,
        String externalId,
        Instant occurredAt,
        String locale,
        String headline,
        String summary,
        String canonicalUrl,
        Map<String, Object> payload
) {}
