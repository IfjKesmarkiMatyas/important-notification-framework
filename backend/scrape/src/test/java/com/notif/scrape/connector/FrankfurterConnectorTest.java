package com.notif.scrape.connector;

import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.scrape.NormalizedDraft;
import com.notif.scrape.support.Feeds;
import tools.jackson.databind.json.JsonMapper;

class FrankfurterConnectorTest {

    private final FrankfurterConnector connector = new FrankfurterConnector(
            "https://api.frankfurter.app/latest?from=EUR&to=HUF",
            new JsonMapper()
    );

    @Test
    void normalizesEurHufRate() {
        List<NormalizedDraft> events = connector.normalize(Feeds.load("frankfurter.json"));

        assertThat(events).hasSize(1);
        NormalizedDraft event = events.getFirst();
        assertThat(event.sourceId()).isEqualTo(SourceId.frankfurter);
        assertThat(event.family()).isEqualTo(EventFamily.market);
        assertThat(event.externalId()).isEqualTo("eurhuf@2026-09-16");
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-09-16T00:00:00Z"));
        assertThat(event.payload())
                .containsEntry("instrument", "eurhuf")
                .containsEntry("priceHuf", 390.12);
    }
}
