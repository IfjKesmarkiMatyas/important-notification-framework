package com.notif.scrape.parse;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class TopicsTest {

    @Test
    void slugsHungarianCategoriesAndFallsBackToBelfold() {
        assertThat(Topics.slug("Belföld")).isEqualTo("belfold");
        assertThat(Topics.fromTelex("https://telex.hu/kulfold/2026/09/16/x", List.of()))
                .containsExactly("kulfold");
        assertThat(Topics.fromTelex("https://example.com/x", List.of())).containsExactly("belfold");
    }
}
