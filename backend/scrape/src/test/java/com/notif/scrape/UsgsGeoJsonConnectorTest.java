package com.notif.scrape;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsgsGeoJsonConnectorTest {

    private final UsgsGeoJsonConnector connector = new UsgsGeoJsonConnector(
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson",
            new JsonMapper()
    );

    @Test
    void normalizesDisasterEventsWithoutMagnitudeFilter() {
        List<NormalizedDraft> events = connector.normalize(Feeds.load("usgs.geojson"));

        assertThat(connector.family()).isEqualTo(EventFamily.disaster);
        assertThat(events).hasSize(2);

        NormalizedDraft strong = events.getFirst();
        assertThat(strong.sourceId()).isEqualTo(SourceId.usgs);
        assertThat(strong.externalId()).isEqualTo("us7000test64");
        assertThat(strong.occurredAt()).isEqualTo(Instant.ofEpochMilli(1758024000000L));
        assertThat(strong.payload())
                .containsEntry("kind", "earthquake")
                .containsEntry("magnitude", 6.4)
                .containsEntry("place", "near Tokyo, Japan")
                .containsEntry("lat", 35.68)
                .containsEntry("lon", 139.69);
        assertThat(strong.headline()).contains("6.4");

        NormalizedDraft weak = events.get(1);
        assertThat(weak.externalId()).isEqualTo("us7000test41");
        assertThat(weak.payload()).containsEntry("magnitude", 4.1);
    }
}
