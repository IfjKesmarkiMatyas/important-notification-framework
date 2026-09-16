package com.notif.common.dto.scrape;

import java.time.Instant;

public record SourceHealthView(
        String sourceId,
        String family,
        String locale,
        String url,
        String health,
        String lastStatus,
        Instant lastOkAt,
        String lastError,
        int lastNormalized
) {}
