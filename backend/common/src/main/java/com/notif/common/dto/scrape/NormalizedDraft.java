package com.notif.common.dto.scrape;

import java.time.Instant;
import java.util.Map;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;

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
