package com.notif.decision.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.notif.common.domain.decision.DecisionEngineMode;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.dto.decision.DecisionResult;
import com.notif.common.dto.decision.SwitchView;
import com.notif.common.dto.delivery.EnqueueDeliveryCommand;
import com.notif.common.entity.decision.DecisionRun;
import com.notif.common.entity.decision.DecisionSettings;
import com.notif.common.entity.decision.UserDecision;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.common.exception.DecisionException;
import com.notif.common.port.DeliveryDispatcher;
import com.notif.common.port.NormalizedEventListener;
import com.notif.decision.config.DecisionProperties;
import com.notif.decision.explain.AiExplainer;
import com.notif.decision.parse.Maps;
import com.notif.decision.repository.DecisionRunRepository;
import com.notif.decision.repository.DecisionSettingsRepository;
import com.notif.decision.repository.UserDecisionRepository;
import com.notif.identity.service.UserService;

@Service
public class DecisionService implements NormalizedEventListener {

    private final DecisionSettingsRepository settings;
    private final DecisionRunRepository runs;
    private final UserDecisionRepository decisions;
    private final UserService users;
    private final NativeDecisionEngine engine;
    private final AiExplainer ai;
    private final DecisionProperties properties;
    private final DeliveryDispatcher delivery;

    public DecisionService(
            DecisionSettingsRepository settings,
            DecisionRunRepository runs,
            UserDecisionRepository decisions,
            UserService users,
            NativeDecisionEngine engine,
            AiExplainer ai,
            DecisionProperties properties,
            DeliveryDispatcher delivery
    ) {
        this.settings = settings;
        this.runs = runs;
        this.decisions = decisions;
        this.users = users;
        this.engine = engine;
        this.ai = ai;
        this.properties = properties;
        this.delivery = delivery;
    }

    public SwitchView switchView() {
        return new SwitchView(requireSettings().getMode().toWire(), properties.openaiConfigured());
    }

    @Transactional
    public SwitchView setMode(String raw) {
        DecisionEngineMode mode;
        try {
            mode = DecisionEngineMode.fromWire(raw);
        } catch (IllegalArgumentException ex) {
            throw new DecisionException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        DecisionSettings row = requireSettings();
        row.setMode(mode);
        row.setUpdatedAt(Instant.now());
        settings.save(row);
        return switchView();
    }

    @Override
    public void onNormalized(NormalizedEvent event) {
        evaluate(event);
    }

    @Transactional
    public List<DecisionResult> evaluate(NormalizedEvent event) {
        DecisionEngineMode mode = requireSettings().getMode();
        DecisionRun run = new DecisionRun();
        run.setId(UUID.randomUUID());
        run.setEventId(event.getId());
        run.setEngine(mode);
        run.setStartedAt(Instant.now());
        run.setEvaluated(0);
        run.setFired(0);
        run.setErrorCount(0);
        runs.save(run);

        List<DecisionResult> results = new ArrayList<>();
        for (AppUser user : users.list()) {
            UserDecision row = decisions
                    .findByUserIdAndSourceIdAndExternalId(user.getId(), event.getSourceId().name(), event.getExternalId())
                    .orElseGet(() -> persist(run.getId(), event, user, decide(event, user, mode)));
            if (row.getRunId() != null && row.getRunId().equals(run.getId())) {
                run.setEvaluated(run.getEvaluated() + 1);
                if (row.getOutcome() == DecisionOutcome.fire) {
                    run.setFired(run.getFired() + 1);
                }
                if (row.getOutcome() == DecisionOutcome.error) {
                    run.setErrorCount(run.getErrorCount() + 1);
                }
            }
            results.add(DecisionResult.from(row, user.getEmail()));
        }
        run.setFinishedAt(Instant.now());
        runs.save(run);
        return results;
    }

    public List<DecisionResult> trail(UUID eventId) {
        Map<UUID, String> emails = new LinkedHashMap<>();
        for (AppUser user : users.list()) {
            emails.put(user.getId(), user.getEmail());
        }
        return decisions.findByEventIdOrderByCreatedAtAsc(eventId).stream()
                .map(row -> DecisionResult.from(row, emails.getOrDefault(row.getUserId(), "")))
                .toList();
    }

    public List<DecisionResult> listRecent() {
        Map<UUID, String> emails = new LinkedHashMap<>();
        for (AppUser user : users.list()) {
            emails.put(user.getId(), user.getEmail());
        }
        return decisions.findTop100ByOrderByCreatedAtDesc().stream()
                .map(row -> DecisionResult.from(row, emails.getOrDefault(row.getUserId(), "")))
                .toList();
    }

    NativeDecisionEngine.Verdict decide(NormalizedEvent event, AppUser user, DecisionEngineMode mode) {
        if (mode == DecisionEngineMode.ai && !properties.openaiConfigured()) {
            List<String> channels = com.notif.decision.explain.LevelResolver.enabledChannels(user.getKit());
            return new NativeDecisionEngine.Verdict(
                    DecisionOutcome.error,
                    null,
                    null,
                    channels,
                    null
            );
        }
        NativeDecisionEngine.Verdict verdict = engine.evaluate(event, user);
        if (mode != DecisionEngineMode.ai || verdict.outcome() != DecisionOutcome.fire) {
            return verdict;
        }
        try {
            String reason = ai.explain(event, user, verdict);
            return new NativeDecisionEngine.Verdict(verdict.outcome(), verdict.level(), reason, verdict.channels(), verdict.hit());
        } catch (RuntimeException ex) {
            return new NativeDecisionEngine.Verdict(
                    DecisionOutcome.error,
                    verdict.level(),
                    null,
                    verdict.channels(),
                    verdict.hit()
            );
        }
    }

    private UserDecision persist(UUID runId, NormalizedEvent event, AppUser user, NativeDecisionEngine.Verdict verdict) {
        UserDecision row = new UserDecision();
        row.setId(UUID.randomUUID());
        row.setRunId(runId);
        row.setUserId(user.getId());
        row.setEventId(event.getId());
        row.setSourceId(event.getSourceId().name());
        row.setExternalId(event.getExternalId());
        row.setOutcome(verdict.outcome());
        row.setLevel(verdict.level());
        row.setReason(verdict.reason());
        row.setEngine(requireSettings().getMode());
        row.setKitSnapshot(user.getKit());
        row.setChannels(verdict.channels());
        row.setCreatedAt(Instant.now());
        if (verdict.outcome() == DecisionOutcome.error) {
            row.setErrorMessage(properties.openaiConfigured() ? "AI explanation failed" : "GPT_API_KEY missing");
        }
        List<String> jobIds = new ArrayList<>();
        if (verdict.outcome() == DecisionOutcome.fire) {
            jobIds.addAll(enqueue(event, user, verdict));
        }
        row.setDeliveryJobIds(jobIds);
        return decisions.save(row);
    }

    private List<String> enqueue(NormalizedEvent event, AppUser user, NativeDecisionEngine.Verdict verdict) {
        List<String> ids = new ArrayList<>();
        Map<String, Object> prefs = Maps.map(user.getKit() == null ? null : user.getKit().get("preferences"));
        String locale = event.getLocale() == null || event.getLocale().isBlank() ? "hu" : event.getLocale();
        for (String channelName : verdict.channels()) {
            DeliveryChannelType channel = DeliveryChannelType.valueOf(channelName);
            String recipient = recipient(channel, prefs, user);
            if (recipient == null || recipient.isBlank()) {
                continue;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("headline", event.getHeadline());
            payload.put("reason", verdict.reason());
            payload.put("url", event.getCanonicalUrl());
            payload.put("level", verdict.level() == null ? "" : verdict.level().name());
            UUID jobId = delivery.enqueue(new EnqueueDeliveryCommand(
                    DeliveryPurpose.ALERT,
                    channel,
                    recipient,
                    locale,
                    null,
                    null,
                    null,
                    payload
            ));
            ids.add(jobId.toString());
        }
        return ids;
    }

    private static String recipient(DeliveryChannelType channel, Map<String, Object> prefs, AppUser user) {
        return switch (channel) {
            case email -> {
                String email = Maps.str(prefs.get("email"));
                yield email.isBlank() ? user.getEmail() : email;
            }
            case slack -> Maps.str(Maps.map(prefs.get("slack")).get("userId"));
            case pushover -> Maps.str(prefs.get("pushoverUserKey"));
        };
    }

    private DecisionSettings requireSettings() {
        return settings.findById((short) 1).orElseGet(() -> {
            DecisionSettings row = new DecisionSettings();
            row.setId((short) 1);
            row.setMode(DecisionEngineMode.native_);
            row.setUpdatedAt(Instant.now());
            return settings.save(row);
        });
    }
}
