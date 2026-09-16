package com.notif.decision.eval;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.decision.GoldenScore;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.service.NativeDecisionEngine;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GoldenEvaluator {

    static final String SKIP = "D5_hard_miss_for_min7";

    private static final Set<String> FIRE = Set.of(
            "D2_clear_hit_quake_67|ada",
            "D4_exact_threshold_60|ada",
            "M2_btc_14d_span_hit|ada",
            "B1_telex_belfold_hit|ada",
            "M2_btc_14d_span_hit|bela",
            "B3_bbc_world_miss_on_hu_kit|elena",
            "B4_bbc_science_en_kit|elena"
    );

    private static final Map<String, List<String>> FIRE_CHANNELS = Map.of(
            "ada", List.of("email"),
            "bela", List.of("slack"),
            "elena", List.of("email")
    );

    private final NativeDecisionEngine engine;
    private final JsonMapper jsonMapper;

    public GoldenEvaluator(NativeDecisionEngine engine, JsonMapper jsonMapper) {
        this.engine = engine;
        this.jsonMapper = jsonMapper;
    }

    public GoldenScore score() {
        return score(loadEvents(), personas());
    }

    public GoldenScore score(Map<String, NormalizedEvent> events, List<AppUser> users) {
        int tp = 0;
        int fp = 0;
        int fn = 0;
        int tn = 0;
        List<GoldenScore.Mismatch> mismatches = new ArrayList<>();
        for (Map.Entry<String, NormalizedEvent> entry : events.entrySet()) {
            String caseId = entry.getKey();
            for (AppUser user : users) {
                String key = caseId + "|" + user.getDisplayName();
                NativeDecisionEngine.Verdict verdict = engine.evaluate(entry.getValue(), user);
                boolean goldenFire = FIRE.contains(key);
                boolean engineFire = verdict.outcome() == DecisionOutcome.fire;
                List<String> expectedChannels = goldenFire ? FIRE_CHANNELS.getOrDefault(user.getDisplayName(), List.of()) : List.of();
                boolean channelsOk = !engineFire || expectedChannels.equals(verdict.channels());
                if (goldenFire && engineFire && channelsOk) {
                    tp++;
                } else if (!goldenFire && !engineFire) {
                    tn++;
                } else if (goldenFire && (!engineFire || !channelsOk)) {
                    fn++;
                    if (engineFire && !channelsOk) {
                        fp++;
                    }
                    mismatches.add(new GoldenScore.Mismatch(
                            caseId,
                            user.getDisplayName(),
                            "FIRE " + String.join(",", expectedChannels),
                            engineFire ? "FIRE " + String.join(",", verdict.channels()) : "NO",
                            engineFire ? "FP+FN" : "FN"
                    ));
                } else {
                    fp++;
                    mismatches.add(new GoldenScore.Mismatch(
                            caseId,
                            user.getDisplayName(),
                            "NO",
                            "FIRE " + String.join(",", verdict.channels()),
                            "FP"
                    ));
                }
            }
        }
        int n = events.size() * users.size();
        Double precision = tp + fp == 0 ? null : tp / (double) (tp + fp);
        Double recall = tp + fn == 0 ? null : tp / (double) (tp + fn);
        Double f1 = precision == null || recall == null || precision + recall == 0
                ? null
                : 2 * precision * recall / (precision + recall);
        return new GoldenScore(n, tp, fp, fn, tn, precision, recall, f1, mismatches);
    }

    public Map<String, NormalizedEvent> loadEvents() {
        try {
            ClassPathResource resource = new ClassPathResource("golden/selected.json");
            Map<String, Map<String, Object>> raw = jsonMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            Map<String, NormalizedEvent> events = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : raw.entrySet()) {
                if (SKIP.equals(entry.getKey())) {
                    continue;
                }
                events.put(entry.getKey(), toEvent(entry.getValue()));
            }
            return events;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load golden fixtures", ex);
        }
    }

    public List<AppUser> personas() {
        return List.of(ada(), bela(), cora(), denes(), elena());
    }

    static NormalizedEvent toEvent(Map<String, Object> raw) {
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.fromString(String.valueOf(raw.get("id"))));
        event.setFamily(EventFamily.valueOf(String.valueOf(raw.get("family"))));
        event.setSourceId(SourceId.valueOf(String.valueOf(raw.get("sourceId"))));
        event.setExternalId(String.valueOf(raw.get("externalId")));
        event.setOccurredAt(OffsetDateTime.parse(String.valueOf(raw.get("occurredAt"))).toInstant());
        event.setIngestedAt(event.getOccurredAt());
        event.setLocale(String.valueOf(raw.get("locale")));
        event.setHeadline(String.valueOf(raw.get("headline")));
        event.setSummary(raw.get("summary") == null ? null : String.valueOf(raw.get("summary")));
        event.setCanonicalUrl(raw.get("canonicalUrl") == null ? null : String.valueOf(raw.get("canonicalUrl")));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = raw.get("payload") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        event.setPayload(payload);
        return event;
    }

    static AppUser ada() {
        return persona("ada", "ada@notif.local", UserStatus.ACTIVE, defaultKit(true, false), defaultRules("hu"));
    }

    static AppUser bela() {
        Map<String, Object> kit = kit(
                false,
                true,
                List.of(Map.of("type", "market", "instrument", "bitcoin", "movePercent", 5))
        );
        return persona("bela", "bela@notif.local", UserStatus.ACTIVE, kit, defaultRules("hu"));
    }

    static AppUser cora() {
        Map<String, Object> kit = kit(
                true,
                false,
                List.of(Map.of("type", "disaster", "kind", "earthquake", "minMagnitude", 7))
        );
        return persona("cora", "cora@notif.local", UserStatus.ACTIVE, kit, defaultRules("hu"));
    }

    static AppUser denes() {
        return persona("denes", "denes@notif.local", UserStatus.INACTIVE, defaultKit(true, false), defaultRules("hu"));
    }

    static AppUser elena() {
        Map<String, Object> kit = kit(
                true,
                false,
                List.of(Map.of("type", "breaking", "topics", List.of("world")))
        );
        return persona("elena", "elena@notif.local", UserStatus.ACTIVE, kit, defaultRules("en"));
    }

    private static AppUser persona(String name, String email, UserStatus status, Map<String, Object> kit, Map<String, Object> rules) {
        AppUser user = new AppUser();
        user.setId(UUID.nameUUIDFromBytes(email.getBytes()));
        user.setEmail(email);
        user.setDisplayName(name);
        user.setRole(UserRole.USER);
        user.setStatus(status);
        user.setKit(kit);
        if ("en".equals(rules.get("locale"))) {
            user.setRulesEn(rules);
            user.setRulesHu(defaultRules("hu"));
        } else {
            user.setRulesHu(rules);
            user.setRulesEn(defaultRules("en"));
        }
        return user;
    }

    private static Map<String, Object> defaultKit(boolean email, boolean slack) {
        return kit(email, slack, List.of(
                Map.of("type", "disaster", "kind", "earthquake", "minMagnitude", 6),
                Map.of("type", "market", "instrument", "bitcoin", "movePercent", 5),
                Map.of("type", "breaking", "topics", List.of("belfold"))
        ));
    }

    private static Map<String, Object> kit(boolean email, boolean slack, List<Map<String, Object>> interests) {
        Map<String, Object> channels = new LinkedHashMap<>();
        channels.put("email", email);
        channels.put("slack", slack);
        channels.put("pushover", false);
        Map<String, Object> slackCfg = new LinkedHashMap<>();
        slackCfg.put("mode", "chatbot");
        slackCfg.put("userId", slack ? "U_BELA" : "");
        Map<String, Object> prefs = new LinkedHashMap<>();
        prefs.put("channels", channels);
        prefs.put("email", "");
        prefs.put("slack", slackCfg);
        prefs.put("pushoverUserKey", "");
        Map<String, Object> kit = new LinkedHashMap<>();
        kit.put("version", 1);
        kit.put("preferences", prefs);
        kit.put("interests", interests);
        return kit;
    }

    private static Map<String, Object> defaultRules(String locale) {
        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("locale", locale);
        rules.put("rules", List.of(
                Map.of("when", "disaster.earthquake.minMagnitude", "level", "critical"),
                Map.of("when", "market.movePercent", "level", "high"),
                Map.of("when", "breaking", "level", "medium")
        ));
        return rules;
    }

    public Set<String> fireKeys() {
        return new LinkedHashSet<>(FIRE);
    }
}
