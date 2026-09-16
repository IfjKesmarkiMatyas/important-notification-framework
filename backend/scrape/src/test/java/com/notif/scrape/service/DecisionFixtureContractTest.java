package com.notif.scrape.service;

import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.scrape.support.Feeds;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class DecisionFixtureContractTest {

    @Test
    void exportedFixtureHasTheDecisionContractShape() {
        List<Map<String, Object>> rows = new JsonMapper().readValue(
                Feeds.load("/fixtures/decision-s3.json"),
                new TypeReference<>() {}
        );

        assertThat(rows).hasSize(4);
        assertThat(rows).allSatisfy(row -> assertThat(row).containsKeys(
                "id", "family", "sourceId", "externalId", "occurredAt", "ingestedAt",
                "locale", "headline", "summary", "canonicalUrl", "payload"
        ));

        Map<String, Object> disaster = row(rows, "usgs");
        assertThat(disaster).containsEntry("family", "disaster");
        assertThat(map(disaster.get("payload")))
                .containsEntry("kind", "earthquake")
                .containsEntry("magnitude", 6.4);

        Map<String, Object> weak = rows.stream()
                .filter(row -> "us7000test41".equals(row.get("externalId")))
                .findFirst()
                .orElseThrow();
        assertThat(map(weak.get("payload"))).containsEntry("magnitude", 4.1);

        Map<String, Object> market = row(rows, "coingecko");
        assertThat(market).containsEntry("family", "market");
        assertThat(map(market.get("payload")))
                .containsEntry("instrument", "bitcoin")
                .containsEntry("movePercent", 8.0);

        Map<String, Object> breaking = row(rows, "telex");
        assertThat(breaking).containsEntry("family", "breaking").containsEntry("locale", "hu");
        assertThat(map(breaking.get("payload")).get("topics")).isEqualTo(List.of("belfold"));
    }

    private static Map<String, Object> row(List<Map<String, Object>> rows, String sourceId) {
        return rows.stream().filter(row -> sourceId.equals(row.get("sourceId"))).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
