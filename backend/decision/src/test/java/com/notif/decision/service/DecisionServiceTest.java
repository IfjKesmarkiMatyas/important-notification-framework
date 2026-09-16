package com.notif.decision.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.notif.common.domain.decision.DecisionEngineMode;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.entity.decision.DecisionSettings;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.common.port.DeliveryDispatcher;
import com.notif.decision.config.DecisionProperties;
import com.notif.decision.explain.AiExplainer;
import com.notif.decision.explain.LevelResolver;
import com.notif.decision.explain.NativeExplainer;
import com.notif.decision.match.InterestMatcher;
import com.notif.decision.policy.DecisionPolicy;
import com.notif.decision.repository.DecisionRunRepository;
import com.notif.decision.repository.DecisionSettingsRepository;
import com.notif.decision.repository.UserDecisionRepository;
import com.notif.identity.service.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecisionServiceTest {

    @Mock
    private DecisionSettingsRepository settings;

    @Mock
    private DecisionRunRepository runs;

    @Mock
    private UserDecisionRepository decisions;

    @Mock
    private UserService users;

    @Mock
    private AiExplainer ai;

    @Mock
    private DeliveryDispatcher delivery;

    private DecisionProperties properties;
    private DecisionService service;

    @BeforeEach
    void setUp() {
        properties = new DecisionProperties();
        NativeDecisionEngine engine = new NativeDecisionEngine(
                new InterestMatcher(new DecisionPolicy()),
                new LevelResolver(),
                new NativeExplainer()
        );
        service = new DecisionService(settings, runs, decisions, users, engine, ai, properties, delivery);
        when(runs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(decisions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(settings.findById((short) 1)).thenReturn(Optional.of(nativeSettings()));
    }

    @Test
    void fireEnqueuesAlertOnEnabledChannel() {
        AppUser ada = ada();
        when(users.list()).thenReturn(List.of(ada));
        when(decisions.findByUserIdAndSourceIdAndExternalId(any(), any(), any())).thenReturn(Optional.empty());
        when(delivery.enqueue(any())).thenReturn(UUID.randomUUID());

        var results = service.evaluate(quake(6.7));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().outcome()).isEqualTo(DecisionOutcome.fire);
        verify(delivery).enqueue(any());
    }

    @Test
    void aiSwitchWithoutKeyIsFailClosed() {
        DecisionSettings aiSettings = nativeSettings();
        aiSettings.setMode(DecisionEngineMode.ai);
        when(settings.findById((short) 1)).thenReturn(Optional.of(aiSettings));
        when(users.list()).thenReturn(List.of(ada()));
        when(decisions.findByUserIdAndSourceIdAndExternalId(any(), any(), any())).thenReturn(Optional.empty());

        var results = service.evaluate(quake(6.7));

        assertThat(results.getFirst().outcome()).isEqualTo(DecisionOutcome.error);
        assertThat(results.getFirst().errorMessage()).contains("GPT_API_KEY");
        verify(delivery, never()).enqueue(any());
    }

    @Test
    void inactiveUserDoesNotEnqueue() {
        AppUser denes = ada();
        denes.setEmail("denes@notif.local");
        denes.setStatus(UserStatus.INACTIVE);
        when(users.list()).thenReturn(List.of(denes));
        when(decisions.findByUserIdAndSourceIdAndExternalId(any(), any(), any())).thenReturn(Optional.empty());

        var results = service.evaluate(quake(6.7));

        assertThat(results.getFirst().outcome()).isEqualTo(DecisionOutcome.no);
        verify(delivery, never()).enqueue(any());
    }

    @Test
    void duplicateUserSourceExternalIsSkipped() {
        AppUser ada = ada();
        when(users.list()).thenReturn(List.of(ada));
        com.notif.common.entity.decision.UserDecision existing = new com.notif.common.entity.decision.UserDecision();
        existing.setId(UUID.randomUUID());
        existing.setUserId(ada.getId());
        existing.setOutcome(DecisionOutcome.fire);
        existing.setEngine(DecisionEngineMode.native_);
        when(decisions.findByUserIdAndSourceIdAndExternalId(any(), any(), any())).thenReturn(Optional.of(existing));

        service.evaluate(quake(6.7));

        verify(decisions, never()).save(any());
        verify(delivery, never()).enqueue(any());
    }

    private static DecisionSettings nativeSettings() {
        DecisionSettings row = new DecisionSettings();
        row.setId((short) 1);
        row.setMode(DecisionEngineMode.native_);
        row.setUpdatedAt(Instant.now());
        return row;
    }

    private static AppUser ada() {
        AppUser user = new AppUser();
        user.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        user.setEmail("ada@notif.local");
        user.setDisplayName("ada");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setKit(Map.of(
                "preferences", Map.of(
                        "channels", Map.of("email", true, "slack", false, "pushover", false),
                        "email", "ada@notif.local",
                        "slack", Map.of("userId", "")
                ),
                "interests", List.of(Map.of("type", "disaster", "kind", "earthquake", "minMagnitude", 6))
        ));
        user.setRulesEn(Map.of("locale", "en", "rules", List.of(
                Map.of("when", "disaster.earthquake.minMagnitude", "level", "critical")
        )));
        return user;
    }

    private static NormalizedEvent quake(double mag) {
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.randomUUID());
        event.setFamily(EventFamily.disaster);
        event.setSourceId(SourceId.usgs);
        event.setExternalId("us-test");
        event.setOccurredAt(Instant.parse("2026-09-16T12:00:00Z"));
        event.setIngestedAt(event.getOccurredAt());
        event.setLocale("en");
        event.setHeadline("M" + mag);
        event.setCanonicalUrl("https://example.test");
        event.setPayload(Map.of("kind", "earthquake", "magnitude", mag, "place", "Peru"));
        return event;
    }
}
