package com.notif.api.admin;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.decision.DecisionResult;
import com.notif.common.dto.decision.GoldenScore;
import com.notif.common.dto.decision.SwitchUpdateRequest;
import com.notif.common.dto.decision.SwitchView;
import com.notif.decision.eval.GoldenEvaluator;
import com.notif.decision.service.DecisionService;
import com.notif.scrape.service.ScrapeService;

@RestController
@RequestMapping("/api/admin/decision")
public class AdminDecisionController {

    private final DecisionService decisions;
    private final GoldenEvaluator golden;
    private final ScrapeService scrapeService;

    public AdminDecisionController(DecisionService decisions, GoldenEvaluator golden, ScrapeService scrapeService) {
        this.decisions = decisions;
        this.golden = golden;
        this.scrapeService = scrapeService;
    }

    @GetMapping("/switch")
    public SwitchView getSwitch() {
        return decisions.switchView();
    }

    @PutMapping("/switch")
    public SwitchView setSwitch(@Valid @RequestBody SwitchUpdateRequest request) {
        return decisions.setMode(request.mode());
    }

    @GetMapping("/events/{eventId}")
    public List<DecisionResult> trail(@PathVariable UUID eventId) {
        return decisions.trail(eventId);
    }

    @PostMapping("/events/{eventId}/decide")
    public List<DecisionResult> decide(@PathVariable UUID eventId) {
        return decisions.evaluate(scrapeService.requireEvent(eventId));
    }

    @GetMapping("/recent")
    public List<DecisionResult> recent() {
        return decisions.listRecent();
    }

    @PostMapping("/score")
    public GoldenScore score() {
        return golden.score();
    }
}
