package com.notif.scrape;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RssFeedParserTest {

    @Test
    void parsesRfc1123DatesAndCategories() {
        List<RssFeedParser.Item> items = RssFeedParser.parse(Feeds.load("telex.rss.xml"));

        assertThat(items).hasSize(1);
        RssFeedParser.Item item = items.getFirst();
        assertThat(item.title()).isEqualTo("Földrengés Japán partjainál");
        assertThat(item.guid()).isEqualTo("https://telex.hu/belfold/2026/09/16/foldrenges");
        assertThat(item.link()).isEqualTo("https://telex.hu/belfold/2026/09/16/foldrenges");
        assertThat(item.published()).isEqualTo(Instant.parse("2026-09-16T12:00:00Z"));
        assertThat(item.categories()).containsExactly("Belföld");
        assertThat(item.description()).contains("Rövid kivonat");
    }

    @Test
    void parsesGmtPubDate() {
        List<RssFeedParser.Item> items = RssFeedParser.parse(Feeds.load("bbc.rss.xml"));
        assertThat(items.getFirst().published()).isEqualTo(Instant.parse("2026-09-16T12:05:00Z"));
    }

    @Test
    void rejectsMalformedXml() {
        assertThatThrownBy(() -> RssFeedParser.parse("<not-rss"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot parse RSS");
    }
}
