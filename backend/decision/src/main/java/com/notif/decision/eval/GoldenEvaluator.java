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
import com.notif.common.domain.decision.AlertLevel;
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

    private static final Map<String, AlertLevel> FIRE_LEVEL = Map.of(
            "D2_clear_hit_quake_67|ada", AlertLevel.critical,
            "D4_exact_threshold_60|ada", AlertLevel.critical,
            "M2_btc_14d_span_hit|ada", AlertLevel.high,
            "M2_btc_14d_span_hit|bela", AlertLevel.high,
            "B1_telex_belfold_hit|ada", AlertLevel.medium,
            "B3_bbc_world_miss_on_hu_kit|elena", AlertLevel.medium,
            "B4_bbc_science_en_kit|elena", AlertLevel.medium
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
        int fireLevelChecked = 0;
        int fireLevelOk = 0;
        List<GoldenScore.Mismatch> mismatches = new ArrayList<>();
        List<GoldenScore.FireCase> fires = new ArrayList<>();
        Map<String, Acc> familyAcc = new LinkedHashMap<>();
        Map<String, Acc> userAcc = new LinkedHashMap<>();
        for (Map.Entry<String, NormalizedEvent> entry : events.entrySet()) {
            String caseId = entry.getKey();
            String family = entry.getValue().getFamily().name();
            for (AppUser user : users) {
                String userName = user.getDisplayName();
                String key = caseId + "|" + userName;
                NativeDecisionEngine.Verdict verdict = engine.evaluate(entry.getValue(), user);
                boolean goldenFire = FIRE.contains(key);
                boolean engineFire = verdict.outcome() == DecisionOutcome.fire;
                List<String> expectedChannels = goldenFire ? FIRE_CHANNELS.getOrDefault(userName, List.of()) : List.of();
                boolean channelsOk = !engineFire || expectedChannels.equals(verdict.channels());
                Acc familyRow = familyAcc.computeIfAbsent(family, ignored -> new Acc());
                Acc persona = userAcc.computeIfAbsent(userName, ignored -> new Acc());
                if (goldenFire && engineFire && channelsOk) {
                    tp++;
                    familyRow.tp++;
                    persona.tp++;
                    AlertLevel expectedLevel = FIRE_LEVEL.get(key);
                    fireLevelChecked++;
                    if (expectedLevel != null && expectedLevel == verdict.level()) {
                        fireLevelOk++;
                    } else {
                        mismatches.add(new GoldenScore.Mismatch(
                                caseId,
                                userName,
                                "LEVEL " + expectedLevel,
                                "LEVEL " + verdict.level(),
                                "LEVEL"
                        ));
                    }
                    fires.add(new GoldenScore.FireCase(
                            caseId,
                            userName,
                            family,
                            verdict.level() == null ? null : verdict.level().name(),
                            verdict.channels(),
                            verdict.reason()
                    ));
                } else if (!goldenFire && !engineFire) {
                    tn++;
                    familyRow.tn++;
                    persona.tn++;
                } else if (goldenFire && (!engineFire || !channelsOk)) {
                    fn++;
                    familyRow.fn++;
                    persona.fn++;
                    if (engineFire && !channelsOk) {
                        fp++;
                        familyRow.fp++;
                        persona.fp++;
                    }
                    mismatches.add(new GoldenScore.Mismatch(
                            caseId,
                            userName,
                            "FIRE " + String.join(",", expectedChannels),
                            engineFire ? "FIRE " + String.join(",", verdict.channels()) : "NO",
                            engineFire ? "FP+FN" : "FN"
                    ));
                } else {
                    fp++;
                    familyRow.fp++;
                    persona.fp++;
                    mismatches.add(new GoldenScore.Mismatch(
                            caseId,
                            userName,
                            "NO",
                            "FIRE " + String.join(",", verdict.channels()),
                            "FP"
                    ));
                }
            }
        }
        int n = events.size() * users.size();
        return new GoldenScore(
                "native",
                n,
                tp,
                fp,
                fn,
                tn,
                precision(tp, fp),
                recall(tp, fn),
                f1(tp, fp, fn),
                fireLevelChecked,
                fireLevelOk,
                toSlices(familyAcc),
                toSlices(userAcc),
                fires,
                mismatches
        );
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

    private static List<GoldenScore.Slice> toSlices(Map<String, Acc> accs) {
        List<GoldenScore.Slice> slices = new ArrayList<>();
        for (Map.Entry<String, Acc> entry : accs.entrySet()) {
            Acc acc = entry.getValue();
            int n = acc.tp + acc.fp + acc.fn + acc.tn;
            slices.add(new GoldenScore.Slice(
                    entry.getKey(),
                    n,
                    acc.tp,
                    acc.fp,
                    acc.fn,
                    acc.tn,
                    f1(acc.tp, acc.fp, acc.fn)
            ));
        }
        return slices;
    }

    private static Double precision(int tp, int fp) {
        return tp + fp == 0 ? null : tp / (double) (tp + fp);
    }

    private static Double recall(int tp, int fn) {
        return tp + fn == 0 ? null : tp / (double) (tp + fn);
    }

    private static Double f1(int tp, int fp, int fn) {
        Double p = precision(tp, fp);
        Double r = recall(tp, fn);
        if (p == null || r == null || p + r == 0) {
            return null;
        }
        return 2 * p * r / (p + r);
    }

    private static final class Acc {
        int tp;
        int fp;
        int fn;
        int tn;
    }
}
