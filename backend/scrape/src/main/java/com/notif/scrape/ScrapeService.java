package com.notif.scrape;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScrapeService {

    static final Duration SILENT_AFTER = Duration.ofMinutes(30);

    private final ScrapeClient client;
    private final ScrapeRunRepository runs;
    private final RawIntakeRepository intakes;
    private final NormalizedEventRepository events;
    private final MarketSnapshotRepository snapshots;
    private final Map<SourceId, SourceConnector> connectors = new EnumMap<>(SourceId.class);

    public ScrapeService(
            ScrapeClient client,
            ScrapeRunRepository runs,
            RawIntakeRepository intakes,
            NormalizedEventRepository events,
            MarketSnapshotRepository snapshots,
            List<SourceConnector> connectorList
    ) {
        this.client = client;
        this.runs = runs;
        this.intakes = intakes;
        this.events = events;
        this.snapshots = snapshots;
        for (SourceConnector connector : connectorList) {
            connectors.put(connector.id(), connector);
        }
    }

    public List<ScrapeRun> runAll() {
        List<ScrapeRun> result = new ArrayList<>();
        for (SourceId sourceId : SourceId.values()) {
            try {
                result.add(run(sourceId));
            } catch (Exception ex) {
                ScrapeRun failed = new ScrapeRun();
                failed.setId(UUID.randomUUID());
                failed.setSourceId(sourceId);
                failed.setStartedAt(Instant.now());
                failed.setFinishedAt(Instant.now());
                failed.setStatus(ScrapeRunStatus.error);
                failed.setFetched(0);
                failed.setNormalized(0);
                failed.setErrorMessage(ex.getMessage() == null ? "scrape failed" : ex.getMessage());
                result.add(runs.save(failed));
            }
        }
        return result;
    }

    @Transactional
    public ScrapeRun run(SourceId sourceId) {
        SourceConnector connector = connectors.get(sourceId);
        if (connector == null) {
            throw new ScrapeException(HttpStatus.BAD_REQUEST, "Unknown source: " + sourceId);
        }
        Instant started = Instant.now();
        ScrapeRun run = new ScrapeRun();
        run.setId(UUID.randomUUID());
        run.setSourceId(sourceId);
        run.setStartedAt(started);
        run.setStatus(ScrapeRunStatus.error);
        run.setFetched(0);
        run.setNormalized(0);
        runs.save(run);

        FetchResult fetch = client.get(connector.fetchUrl());
        RawIntake intake = new RawIntake();
        intake.setId(UUID.randomUUID());
        intake.setRunId(run.getId());
        intake.setSourceId(sourceId);
        intake.setFetchedAt(started);
        intake.setHttpStatus(fetch.httpStatus());
        intake.setContentType(fetch.contentType());
        intake.setBody(fetch.body());
        intake.setErrorMessage(fetch.errorMessage());
        intakes.save(intake);

        if (!fetch.ok() || fetch.body() == null || fetch.body().isBlank()) {
            run.setFinishedAt(Instant.now());
            if (!fetch.ok()) {
                run.setStatus(ScrapeRunStatus.error);
                run.setErrorMessage(fetch.errorMessage());
            } else {
                run.setStatus(ScrapeRunStatus.empty);
            }
            return runs.save(run);
        }

        try {
            List<NormalizedDraft> drafts = connector.normalize(fetch.body());
            run.setFetched(drafts.size());
            int created = 0;
            for (NormalizedDraft draft : drafts) {
                if (persist(draft, intake.getId(), started)) {
                    created++;
                }
            }
            run.setNormalized(created);
            run.setStatus(created == 0 ? ScrapeRunStatus.empty : ScrapeRunStatus.ok);
            run.setFinishedAt(Instant.now());
            return runs.save(run);
        } catch (Exception ex) {
            run.setStatus(ScrapeRunStatus.error);
            run.setErrorMessage(ex.getMessage() == null ? "normalize failed" : ex.getMessage());
            run.setFinishedAt(Instant.now());
            return runs.save(run);
        }
    }

    public List<SourceHealthView> sources() {
        Instant now = Instant.now();
        List<SourceHealthView> views = new ArrayList<>();
        for (SourceId sourceId : SourceId.values()) {
            SourceConnector connector = connectors.get(sourceId);
            Optional<ScrapeRun> last = runs.findFirstBySourceIdOrderByStartedAtDesc(sourceId);
            Optional<ScrapeRun> lastOk = runs.findFirstBySourceIdAndStatusInOrderByStartedAtDesc(
                    sourceId, List.of(ScrapeRunStatus.ok, ScrapeRunStatus.empty));
            SourceHealthStatus health = health(last, lastOk, now);
            views.add(new SourceHealthView(
                    sourceId.name(),
                    connector == null ? EventFamily.breaking.name() : connector.family().name(),
                    connector == null ? "" : connector.locale(),
                    connector == null ? "" : connector.fetchUrl(),
                    health.name(),
                    last.map(ScrapeRun::getStatus).map(Enum::name).orElse(null),
                    lastOk.map(ScrapeRun::getFinishedAt).orElse(null),
                    last.map(ScrapeRun::getErrorMessage).orElse(null),
                    last.map(ScrapeRun::getNormalized).orElse(0)
            ));
        }
        return views;
    }

    public List<ScrapeRun> recentRuns() {
        return runs.findTop50ByOrderByStartedAtDesc();
    }

    public List<NormalizedEvent> listEvents(String family, String sourceId) {
        return listEvents(family, sourceId, null);
    }

    public List<NormalizedEvent> listEvents(String family, String sourceId, Integer limit) {
        EventFamily parsedFamily = parseFamily(family);
        SourceId parsedSource = parseSource(sourceId);
        List<NormalizedEvent> rows;
        if (parsedFamily != null && parsedSource != null) {
            rows = events.findTop100ByFamilyAndSourceIdOrderByIngestedAtDesc(parsedFamily, parsedSource);
        } else if (parsedFamily != null) {
            rows = events.findTop100ByFamilyOrderByIngestedAtDesc(parsedFamily);
        } else if (parsedSource != null) {
            rows = events.findTop100BySourceIdOrderByIngestedAtDesc(parsedSource);
        } else {
            rows = events.findTop100ByOrderByIngestedAtDesc();
        }
        int cap = cap(limit);
        return rows.size() <= cap ? rows : rows.subList(0, cap);
    }

    public NormalizedEvent requireEvent(UUID id) {
        return events.findById(id).orElseThrow(() -> new ScrapeException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    public Optional<RawIntake> intake(UUID id) {
        return intakes.findById(id);
    }

    public List<Map<String, Object>> exportEvents(String family, String sourceId) {
        return exportEvents(family, sourceId, null);
    }

    public List<Map<String, Object>> exportEvents(String family, String sourceId, Integer limit) {
        return listEvents(family, sourceId, limit).stream().map(this::toExport).toList();
    }

    public Map<String, Object> toExport(NormalizedEvent event) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", event.getId().toString());
        row.put("family", event.getFamily().name());
        row.put("sourceId", event.getSourceId().name());
        row.put("externalId", event.getExternalId());
        row.put("occurredAt", event.getOccurredAt().toString());
        row.put("ingestedAt", event.getIngestedAt().toString());
        row.put("locale", event.getLocale());
        row.put("headline", event.getHeadline());
        row.put("summary", event.getSummary());
        row.put("canonicalUrl", event.getCanonicalUrl());
        row.put("payload", event.getPayload());
        row.put("rawIntakeId", event.getRawIntakeId() == null ? null : event.getRawIntakeId().toString());
        return row;
    }

    public SourceId requireSource(String raw) {
        SourceId source = parseSource(raw);
        if (source == null) {
            throw new ScrapeException(HttpStatus.BAD_REQUEST, "Unknown source: " + raw);
        }
        return source;
    }

    private boolean persist(NormalizedDraft draft, UUID rawIntakeId, Instant ingestedAt) {
        NormalizedDraft prepared = applyMarketSnapshot(draft, ingestedAt);
        if (prepared == null) {
            return false;
        }
        if (events.existsBySourceIdAndExternalId(prepared.sourceId(), prepared.externalId())) {
            return false;
        }
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.randomUUID());
        event.setFamily(prepared.family());
        event.setSourceId(prepared.sourceId());
        event.setExternalId(prepared.externalId());
        event.setOccurredAt(prepared.occurredAt());
        event.setIngestedAt(ingestedAt);
        event.setLocale(prepared.locale());
        event.setHeadline(prepared.headline());
        event.setSummary(prepared.summary());
        event.setCanonicalUrl(prepared.canonicalUrl());
        event.setPayload(prepared.payload());
        event.setRawIntakeId(rawIntakeId);
        events.save(event);
        return true;
    }

    private NormalizedDraft applyMarketSnapshot(NormalizedDraft draft, Instant ingestedAt) {
        if (draft.family() != EventFamily.market) {
            return draft;
        }
        String instrument = JsonMaps.str(draft.payload().get("instrument"));
        if (instrument.isBlank()) {
            return draft;
        }
        Double usd = JsonMaps.num(draft.payload().get("priceUsd"));
        Double huf = JsonMaps.num(draft.payload().get("priceHuf"));
        Optional<MarketSnapshot> previous = snapshots.findById(instrument);
        Map<String, Object> payload = new LinkedHashMap<>(draft.payload());
        if (previous.isPresent()) {
            Double prevUsd = previous.get().getPriceUsd();
            Double prevHuf = previous.get().getPriceHuf();
            payload.put("previousUsd", prevUsd);
            payload.put("previousHuf", prevHuf);
            Double current = usd != null ? usd : huf;
            Double prior = prevUsd != null ? prevUsd : prevHuf;
            if (current != null && prior != null && prior != 0) {
                payload.put("movePercent", Math.round(Math.abs(current - prior) / prior * 1000.0) / 10.0);
            } else {
                payload.put("movePercent", 0);
            }
        } else {
            saveSnapshot(instrument, usd, huf, ingestedAt);
            return null;
        }
        saveSnapshot(instrument, usd, huf, ingestedAt);
        return new NormalizedDraft(
                draft.family(),
                draft.sourceId(),
                draft.externalId(),
                draft.occurredAt(),
                draft.locale(),
                draft.headline(),
                draft.summary(),
                draft.canonicalUrl(),
                payload
        );
    }

    private void saveSnapshot(String instrument, Double usd, Double huf, Instant seenAt) {
        MarketSnapshot snapshot = snapshots.findById(instrument).orElseGet(MarketSnapshot::new);
        snapshot.setInstrument(instrument);
        snapshot.setPriceUsd(usd);
        snapshot.setPriceHuf(huf);
        snapshot.setSeenAt(seenAt);
        snapshots.save(snapshot);
    }

    static SourceHealthStatus health(Optional<ScrapeRun> last, Optional<ScrapeRun> lastOk, Instant now) {
        if (last.isEmpty()) {
            return SourceHealthStatus.idle;
        }
        if (last.get().getStatus() == ScrapeRunStatus.error) {
            return SourceHealthStatus.error;
        }
        if (lastOk.isEmpty()
                || lastOk.get().getFinishedAt() == null
                || lastOk.get().getFinishedAt().isBefore(now.minus(SILENT_AFTER))) {
            return SourceHealthStatus.silent;
        }
        if (last.get().getStatus() == ScrapeRunStatus.empty) {
            return SourceHealthStatus.empty;
        }
        return SourceHealthStatus.ok;
    }

    static int cap(Integer limit) {
        if (limit == null || limit <= 0) {
            return 100;
        }
        return Math.min(limit, 100);
    }

    private static EventFamily parseFamily(String raw) {
        if (raw == null || raw.isBlank() || "all".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return EventFamily.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new ScrapeException(HttpStatus.BAD_REQUEST, "Unknown family: " + raw);
        }
    }

    private static SourceId parseSource(String raw) {
        if (raw == null || raw.isBlank() || "all".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return SourceId.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new ScrapeException(HttpStatus.BAD_REQUEST, "Unknown source: " + raw);
        }
    }

    public record SourceHealthView(
            String sourceId,
            String family,
            String locale,
            String url,
            String health,
            String lastStatus,
            Instant lastOkAt,
            String lastError,
            int lastNormalized
    ) {}
}
