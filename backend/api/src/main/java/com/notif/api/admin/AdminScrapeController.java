package com.notif.api.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.scrape.EventView;
import com.notif.common.dto.scrape.RunRequest;
import com.notif.common.dto.scrape.RunView;
import com.notif.common.dto.scrape.SourceHealthView;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.common.entity.scrape.RawIntake;
import com.notif.scrape.service.ScrapeService;

@RestController
@RequestMapping("/api/admin/scrape")
public class AdminScrapeController {

    private static final int RAW_EXCERPT = 2000;

    private final ScrapeService scrapeService;

    public AdminScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @GetMapping("/sources")
    public List<SourceHealthView> sources() {
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

    @GetMapping("/event-details")
    public Map<String, Object> eventDetail(@RequestParam String eventId) {
        NormalizedEvent event = scrapeService.requireEvent(UUID.fromString(eventId));
        String excerpt = scrapeService.intake(event.getRawIntakeId())
                .map(RawIntake::getBody)
                .map(body -> body == null ? "" : body.substring(0, Math.min(body.length(), RAW_EXCERPT)))
                .orElse("");
        Map<String, Object> row = new java.util.LinkedHashMap<>(scrapeService.toExport(event));
        row.put("rawExcerpt", excerpt);
        return row;
    }
}
