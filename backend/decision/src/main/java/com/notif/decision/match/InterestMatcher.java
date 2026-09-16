package com.notif.decision.match;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.parse.Maps;
import com.notif.decision.policy.DecisionPolicy;

@Component
public class InterestMatcher {

    private final DecisionPolicy policy;

    public InterestMatcher(DecisionPolicy policy) {
        this.policy = policy;
    }

    public Optional<InterestHit> match(NormalizedEvent event, Map<String, Object> kit) {
        if (event == null || kit == null) {
            return Optional.empty();
        }
        Map<String, Object> payload = event.getPayload() == null ? Map.of() : event.getPayload();
        for (Object raw : Maps.list(kit.get("interests"))) {
            Map<String, Object> interest = Maps.map(raw);
            Optional<InterestHit> hit = matchOne(event.getFamily(), payload, interest);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    private Optional<InterestHit> matchOne(EventFamily family, Map<String, Object> payload, Map<String, Object> interest) {
        String type = Maps.str(interest.get("type")).toLowerCase(Locale.ROOT);
        if (family == EventFamily.disaster && "disaster".equals(type)) {
            return matchDisaster(payload, interest);
        }
        if (family == EventFamily.market && "market".equals(type)) {
            return matchMarket(payload, interest);
        }
        if (family == EventFamily.breaking && "breaking".equals(type)) {
            return matchBreaking(payload, interest);
        }
        return Optional.empty();
    }

    private Optional<InterestHit> matchDisaster(Map<String, Object> payload, Map<String, Object> interest) {
        String eventKind = Maps.str(payload.get("kind")).toLowerCase(Locale.ROOT);
        String kitKind = Maps.str(interest.get("kind")).toLowerCase(Locale.ROOT);
        if (eventKind.isBlank() || !eventKind.equals(kitKind)) {
            return Optional.empty();
        }
        Double magnitude = Maps.num(payload.get("magnitude"));
        Double min = Maps.num(interest.get("minMagnitude"));
        if (magnitude == null || min == null) {
            return Optional.empty();
        }
        if (!policy.passes(magnitude, min, policy.getMagnitudeCompare())) {
            return Optional.empty();
        }
        return Optional.of(new InterestHit(
                "disaster",
                "disaster." + kitKind + ".minMagnitude",
                kitKind,
                null,
                magnitude,
                min,
                List.of()
        ));
    }

    private Optional<InterestHit> matchMarket(Map<String, Object> payload, Map<String, Object> interest) {
        String eventInstrument = Maps.str(payload.get("instrument")).toLowerCase(Locale.ROOT);
        String kitInstrument = Maps.str(interest.get("instrument")).toLowerCase(Locale.ROOT);
        if (eventInstrument.isBlank() || !eventInstrument.equals(kitInstrument)) {
            return Optional.empty();
        }
        Double move = Maps.num(payload.get("movePercent"));
        Double min = Maps.num(interest.get("movePercent"));
        if (move == null || min == null) {
            return Optional.empty();
        }
        if (!policy.passes(move, min, policy.getMoveCompare())) {
            return Optional.empty();
        }
        return Optional.of(new InterestHit(
                "market",
                "market.movePercent",
                null,
                kitInstrument,
                move,
                min,
                List.of()
        ));
    }

    private Optional<InterestHit> matchBreaking(Map<String, Object> payload, Map<String, Object> interest) {
        Set<String> eventTopics = normalizeTopics(Maps.list(payload.get("topics")));
        Set<String> kitTopics = normalizeTopics(Maps.list(interest.get("topics")));
        if (eventTopics.isEmpty() || kitTopics.isEmpty()) {
            return Optional.empty();
        }
        List<String> matched = new ArrayList<>();
        for (String topic : kitTopics) {
            if (eventTopics.contains(topic)) {
                matched.add(topic);
            }
        }
        if (matched.isEmpty()) {
            return Optional.empty();
        }
        if ("all".equalsIgnoreCase(policy.getTopicMode()) && matched.size() != kitTopics.size()) {
            return Optional.empty();
        }
        return Optional.of(new InterestHit(
                "breaking",
                "breaking",
                null,
                null,
                null,
                null,
                List.copyOf(matched)
        ));
    }

    private static Set<String> normalizeTopics(List<?> raw) {
        Set<String> topics = new LinkedHashSet<>();
        for (Object item : raw) {
            String topic = Maps.str(item).toLowerCase(Locale.ROOT);
            if (!topic.isBlank()) {
                topics.add(topic);
            }
        }
        return topics;
    }
}
