package com.notif.decision.service;

import java.util.List;
import com.notif.common.domain.decision.AlertLevel;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.explain.LevelResolver;
import com.notif.decision.explain.NativeExplainer;
import com.notif.decision.match.InterestHit;
import com.notif.decision.match.InterestMatcher;
import org.springframework.stereotype.Component;

@Component
public class NativeDecisionEngine {

    private final InterestMatcher matcher;
    private final LevelResolver levels;
    private final NativeExplainer explainer;

    public NativeDecisionEngine(InterestMatcher matcher, LevelResolver levels, NativeExplainer explainer) {
        this.matcher = matcher;
        this.levels = levels;
        this.explainer = explainer;
    }

    public Verdict evaluate(NormalizedEvent event, AppUser user) {
        List<String> channels = LevelResolver.enabledChannels(user.getKit());
        if (user.getStatus() != UserStatus.ACTIVE) {
            return new Verdict(DecisionOutcome.no, null, explainer.inactive(event), channels, null);
        }
        var hit = matcher.match(event, user.getKit());
        if (hit.isEmpty()) {
            return new Verdict(DecisionOutcome.no, null, explainer.noMatch(event), channels, null);
        }
        InterestHit matched = hit.get();
        AlertLevel level = levels.resolve(user, event, matched);
        return new Verdict(DecisionOutcome.fire, level, explainer.explain(event, matched), channels, matched);
    }

    public record Verdict(
            DecisionOutcome outcome,
            AlertLevel level,
            String reason,
            List<String> channels,
            InterestHit hit
    ) {}
}
