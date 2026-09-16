package com.notif.decision.match;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.policy.DecisionPolicy;

class InterestMatcherTest {

    private final InterestMatcher matcher = new InterestMatcher(new DecisionPolicy());

    @Test
    void exactMagnitudeSixFiresOnGte() {
        NormalizedEvent event = event(EventFamily.disaster, Map.of("kind", "earthquake", "magnitude", 6));
        Map<String, Object> kit = kit(Map.of("type", "disaster", "kind", "earthquake", "minMagnitude", 6));
        assertThat(matcher.match(event, kit)).isPresent();
    }

    @Test
    void nearMissFiveNineDoesNotFire() {
        NormalizedEvent event = event(EventFamily.disaster, Map.of("kind", "earthquake", "magnitude", 5.9));
        Map<String, Object> kit = kit(Map.of("type", "disaster", "kind", "earthquake", "minMagnitude", 6));
        assertThat(matcher.match(event, kit)).isEmpty();
    }

    @Test
    void worldTopicIntersectsBbcScience() {
        NormalizedEvent event = event(EventFamily.breaking, Map.of("topics", List.of("world", "science")));
        Map<String, Object> kit = kit(Map.of("type", "breaking", "topics", List.of("world")));
        assertThat(matcher.match(event, kit)).isPresent().get().extracting(InterestHit::matchedTopics)
                .isEqualTo(List.of("world"));
    }

    @Test
    void ethereumDoesNotMatchBitcoinInterest() {
        NormalizedEvent event = event(EventFamily.market, Map.of("instrument", "ethereum", "movePercent", 7.2));
        Map<String, Object> kit = kit(Map.of("type", "market", "instrument", "bitcoin", "movePercent", 5));
        assertThat(matcher.match(event, kit)).isEmpty();
    }

    private static NormalizedEvent event(EventFamily family, Map<String, Object> payload) {
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.randomUUID());
        event.setFamily(family);
        event.setSourceId(family == EventFamily.disaster ? SourceId.usgs : family == EventFamily.market ? SourceId.coingecko : SourceId.bbc);
        event.setExternalId("x");
        event.setOccurredAt(Instant.parse("2026-09-16T12:00:00Z"));
        event.setIngestedAt(event.getOccurredAt());
        event.setLocale("en");
        event.setHeadline("h");
        event.setPayload(payload);
        return event;
    }

    private static Map<String, Object> kit(Map<String, Object> interest) {
        return Map.of("interests", List.of(interest));
    }
}
