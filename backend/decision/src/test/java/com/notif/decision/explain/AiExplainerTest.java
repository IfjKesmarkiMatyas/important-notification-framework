package com.notif.decision.explain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.decision.AlertLevel;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.config.DecisionProperties;
import com.notif.decision.match.InterestHit;
import com.notif.decision.service.NativeDecisionEngine;
import tools.jackson.databind.json.JsonMapper;

class AiExplainerTest {

    @Test
    void readsReasonFromMockedJson() {
        DecisionProperties properties = new DecisionProperties();
        properties.setOpenaiApiKey("test-key");
        ChatCompletionsClient chat = (system, user) -> "{\"reason\":\"Peru quake crossed Ada's M6 threshold.\"}";
        AiExplainer explainer = new AiExplainer(chat, properties, new JsonMapper());

        String reason = explainer.explain(event(), user(), verdict());

        assertThat(reason).contains("Peru quake");
    }

    @Test
    void failClosedWithoutKey() {
        DecisionProperties properties = new DecisionProperties();
        AiExplainer explainer = new AiExplainer((s, u) -> "{}", properties, new JsonMapper());
        assertThat(explainer.configured()).isFalse();
        assertThatThrownBy(() -> explainer.explain(event(), user(), verdict()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GPT_API_KEY");
    }

    private static NativeDecisionEngine.Verdict verdict() {
        return new NativeDecisionEngine.Verdict(
                DecisionOutcome.fire,
                AlertLevel.critical,
                "native",
                List.of("email"),
                new InterestHit("disaster", "disaster.earthquake.minMagnitude", "earthquake", null, 6.7, 6.0, List.of())
        );
    }

    private static AppUser user() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("ada@notif.local");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setKit(Map.of());
        return user;
    }

    private static NormalizedEvent event() {
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.randomUUID());
        event.setFamily(EventFamily.disaster);
        event.setSourceId(SourceId.usgs);
        event.setExternalId("us1");
        event.setOccurredAt(Instant.parse("2026-09-16T12:00:00Z"));
        event.setIngestedAt(event.getOccurredAt());
        event.setLocale("en");
        event.setHeadline("6.7 Peru");
        event.setPayload(Map.of("kind", "earthquake", "magnitude", 6.7));
        return event;
    }
}
