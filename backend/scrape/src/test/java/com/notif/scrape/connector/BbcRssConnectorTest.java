package com.notif.scrape.connector;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.scrape.NormalizedDraft;
import com.notif.scrape.support.Feeds;

class BbcRssConnectorTest {

    private final BbcRssConnector connector = new BbcRssConnector("https://feeds.bbci.co.uk/news/world/rss.xml");

    @Test
    void normalizesBreakingEnWithWorldTopic() {
        List<NormalizedDraft> events = connector.normalize(Feeds.load("bbc.rss.xml"));

        assertThat(connector.family()).isEqualTo(EventFamily.breaking);
        assertThat(connector.locale()).isEqualTo("en");
        assertThat(events).hasSize(1);
        NormalizedDraft event = events.getFirst();
        assertThat(event.sourceId()).isEqualTo(SourceId.bbc);
        assertThat(event.locale()).isEqualTo("en");
        assertThat(event.headline()).isEqualTo("Major quake hits Tokyo region");
        assertThat(event.payload().get("topics")).isEqualTo(List.of("world", "asia"));
    }
}
