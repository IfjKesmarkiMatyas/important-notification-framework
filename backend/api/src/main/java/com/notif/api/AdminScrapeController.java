package com.notif.api;

import com.notif.scrape.NormalizedEvent;
import com.notif.scrape.RawIntake;
import com.notif.scrape.ScrapeRun;
import com.notif.scrape.ScrapeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/scrape")
public class AdminScrapeController {

    private static final int RAW_EXCERPT = 2000;

    private final ScrapeService scrapeService;

    public AdminScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @GetMapping("/sources")
    public List<ScrapeService.SourceHealthView> sources() {
        return scrapeService.sources();
    }

    @PostMapping("/runs")
    public List<RunView> run(@Valid @RequestBody RunRequest request) {
        String sourceId = request.sourceId() == null ? "all" : request.sourceId();
        if ("all".equalsIgnoreCase(sourceId)) {
            return scrapeService.runAll().stream().map(RunView::from).toList();
        }
        return List.of(RunView.from(scrapeService.run(scrapeService.requireSource(sourceId))));
    }

    @GetMapping("/runs")
    public List<RunView> runs() {
        return scrapeService.recentRuns().stream().map(RunView::from).toList();
    }

    @GetMapping("/events")
    public List<EventView> events(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String sourceId,
            @RequestParam(required = false) Integer limit
    ) {
        return scrapeService.listEvents(family, sourceId, limit).stream().map(EventView::from).toList();
    }

    @GetMapping("/events/export")
    public List<Map<String, Object>> export(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String sourceId,
            @RequestParam(required = false) Integer limit
    ) {
        return scrapeService.exportEvents(family, sourceId, limit);
    }

    @GetMapping("/events/{id}")
    public EventDetailView event(@PathVariable UUID id) {
        NormalizedEvent event = scrapeService.requireEvent(id);
        String excerpt = scrapeService.intake(event.getRawIntakeId())
                .map(RawIntake::getBody)
                .map(body -> body == null ? "" : body.substring(0, Math.min(body.length(), RAW_EXCERPT)))
                .orElse("");
        return EventDetailView.from(event, excerpt);
    }

    public record RunRequest(String sourceId) {}

    public record RunView(
            UUID id,
            String sourceId,
            String status,
            int fetched,
            int normalized,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt
    ) {
        static RunView from(ScrapeRun run) {
            return new RunView(
                    run.getId(),
                    run.getSourceId().name(),
                    run.getStatus().name(),
                    run.getFetched(),
                    run.getNormalized(),
                    run.getErrorMessage(),
                    run.getStartedAt(),
                    run.getFinishedAt()
            );
        }
    }

    public record EventView(
            UUID id,
            String family,
            String sourceId,
            String externalId,
            Instant occurredAt,
            Instant ingestedAt,
            String locale,
            String headline,
            String summary,
            String canonicalUrl,
            Map<String, Object> payload
    ) {
        static EventView from(NormalizedEvent event) {
            return new EventView(
                    event.getId(),
                    event.getFamily().name(),
                    event.getSourceId().name(),
                    event.getExternalId(),
                    event.getOccurredAt(),
                    event.getIngestedAt(),
                    event.getLocale(),
                    event.getHeadline(),
                    event.getSummary(),
                    event.getCanonicalUrl(),
                    event.getPayload()
            );
        }
    }

    public record EventDetailView(
            UUID id,
            String family,
            String sourceId,
            String externalId,
            Instant occurredAt,
            Instant ingestedAt,
            String locale,
            String headline,
            String summary,
            String canonicalUrl,
            Map<String, Object> payload,
            UUID rawIntakeId,
            String rawExcerpt
    ) {
        static EventDetailView from(NormalizedEvent event, String rawExcerpt) {
            return new EventDetailView(
                    event.getId(),
                    event.getFamily().name(),
                    event.getSourceId().name(),
                    event.getExternalId(),
                    event.getOccurredAt(),
                    event.getIngestedAt(),
                    event.getLocale(),
                    event.getHeadline(),
                    event.getSummary(),
                    event.getCanonicalUrl(),
                    event.getPayload(),
                    event.getRawIntakeId(),
                    rawExcerpt
            );
        }
    }
}
