package com.notif.scrape.connector;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.scrape.NormalizedDraft;
import com.notif.scrape.support.Feeds;
import tools.jackson.databind.json.JsonMapper;

class CoinGeckoConnectorTest {

    private final CoinGeckoConnector connector = new CoinGeckoConnector(
            "https://api.coingecko.com/api/v3/simple/price",
            new JsonMapper()
    );

    @Test
    void normalizesMarketPricesWithoutMovePercent() {
        List<NormalizedDraft> events = connector.normalize(Feeds.load("coingecko.json"));

        assertThat(connector.family()).isEqualTo(EventFamily.market);
        assertThat(events).hasSize(2);
        NormalizedDraft bitcoin = events.stream()
                .filter(event -> "bitcoin".equals(event.payload().get("instrument")))
                .findFirst()
                .orElseThrow();
        assertThat(bitcoin.sourceId()).isEqualTo(SourceId.coingecko);
        assertThat(bitcoin.externalId()).startsWith("bitcoin@");
        assertThat(bitcoin.payload())
                .containsEntry("instrument", "bitcoin")
                .containsEntry("priceUsd", 108000.0)
                .containsEntry("priceHuf", 39600000.0)
                .doesNotContainKey("movePercent");
    }
}
