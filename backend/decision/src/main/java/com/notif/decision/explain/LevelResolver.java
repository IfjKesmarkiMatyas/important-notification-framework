package com.notif.decision.explain;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.notif.common.domain.decision.AlertLevel;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.match.InterestHit;
import com.notif.decision.parse.Maps;

@Component
public class LevelResolver {

    public AlertLevel resolve(AppUser user, NormalizedEvent event, InterestHit hit) {
        Map<String, Object> rules = rulesFor(user, event);
        for (Object raw : Maps.list(rules.get("rules"))) {
            Map<String, Object> rule = Maps.map(raw);
            String when = Maps.str(rule.get("when"));
            if (when.isBlank()) {
                continue;
            }
            if (when.equals(hit.whenKey()) || when.equals(hit.type())) {
                return parseLevel(Maps.str(rule.get("level")));
            }
        }
        return AlertLevel.medium;
    }

    static Map<String, Object> rulesFor(AppUser user, NormalizedEvent event) {
        boolean english = event != null && "en".equalsIgnoreCase(event.getLocale());
        Map<String, Object> preferred = english ? user.getRulesEn() : user.getRulesHu();
        if (preferred != null && !preferred.isEmpty()) {
            return preferred;
        }
        if (user.getRulesHu() != null) {
            return user.getRulesHu();
        }
        return user.getRulesEn() == null ? Map.of() : user.getRulesEn();
    }

    private static AlertLevel parseLevel(String raw) {
        if (raw == null || raw.isBlank()) {
            return AlertLevel.medium;
        }
        try {
            return AlertLevel.valueOf(raw.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            return AlertLevel.medium;
        }
    }

    public static List<String> enabledChannels(Map<String, Object> kit) {
        Map<String, Object> channels = Maps.map(Maps.map(kit == null ? null : kit.get("preferences")).get("channels"));
        List<String> enabled = new java.util.ArrayList<>();
        if (Maps.bool(channels.get("email"))) {
            enabled.add("email");
        }
        if (Maps.bool(channels.get("slack"))) {
            enabled.add("slack");
        }
        if (Maps.bool(channels.get("pushover"))) {
            enabled.add("pushover");
        }
        return List.copyOf(enabled);
    }
}
