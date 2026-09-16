package com.notif.scrape;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelexRssConnectorTest {

    private final TelexRssConnector connector = new TelexRssConnector("https://telex.hu/rss");

    @Test
    void normalizesBreakingHuWithTelexTopics() {
        List<NormalizedDraft> events = connector.normalize(Feeds.load("telex.rss.xml"));

        assertThat(connector.id()).isEqualTo(SourceId.telex);
        assertThat(connector.family()).isEqualTo(EventFamily.breaking);
        assertThat(connector.locale()).isEqualTo("hu");
        assertThat(events).hasSize(1);
        NormalizedDraft event = events.getFirst();
        assertThat(event.family()).isEqualTo(EventFamily.breaking);
        assertThat(event.sourceId()).isEqualTo(SourceId.telex);
        assertThat(event.locale()).isEqualTo("hu");
        assertThat(event.externalId()).isEqualTo("https://telex.hu/belfold/2026/09/16/foldrenges");
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-09-16T12:00:00Z"));
        assertThat(event.headline()).isEqualTo("Földrengés Japán partjainál");
        assertThat(event.summary()).isEqualTo("Rövid kivonat a hírről.");
        assertThat(event.canonicalUrl()).startsWith("https://telex.hu/");
        assertThat(event.payload()).containsEntry("topics", List.of("belfold"));
    }
}
