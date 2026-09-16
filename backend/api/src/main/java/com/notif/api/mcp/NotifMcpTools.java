package com.notif.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.entity.scrape.ScrapeRun;
import com.notif.delivery.service.DeliveryService;
import com.notif.api.station.StationService;
import com.notif.decision.eval.GoldenEvaluator;
import com.notif.decision.service.DecisionService;
import com.notif.identity.service.InviteService;
import com.notif.identity.service.TestDeliveryService;
import com.notif.identity.service.UserService;
import com.notif.scrape.service.ScrapeService;
import tools.jackson.databind.json.JsonMapper;

@Service
public class NotifMcpTools {

    private final InviteService invites;
    private final UserService users;
    private final DeliveryService deliveryService;
    private final TestDeliveryService testDelivery;
    private final ScrapeService scrapeService;
    private final DecisionService decisions;
    private final GoldenEvaluator golden;
    private final StationService station;
    private final JsonMapper jsonMapper;

    public NotifMcpTools(
            InviteService invites,
            UserService users,
            DeliveryService deliveryService,
            TestDeliveryService testDelivery,
            ScrapeService scrapeService,
            DecisionService decisions,
            GoldenEvaluator golden,
            StationService station,
            JsonMapper jsonMapper
    ) {
        this.invites = invites;
        this.users = users;
        this.deliveryService = deliveryService;
        this.testDelivery = testDelivery;
        this.scrapeService = scrapeService;
        this.decisions = decisions;
        this.golden = golden;
        this.station = station;
        this.jsonMapper = jsonMapper;
    }

    @McpTool(name = "invite_user", description = "Invite a user by email. Sends an INVITE via the delivery engine.")
    public String inviteUser(
            @McpToolParam(description = "Email address", required = true) String email,
            @McpToolParam(description = "Locale hu or en") String locale
    ) {
        var result = invites.invite(email, locale == null ? "hu" : locale, "MCP");
        return "invited " + result.userId() + " job " + result.deliveryJobId();
    }

    @McpTool(name = "list_users", description = "List all users")
    public String listUsers() {
        return users.list().stream()
                .map(u -> u.getEmail() + " " + u.getStatus() + " " + u.getRole())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }

    @McpTool(name = "update_kit", description = "Replace a user's kit JSON object")
    public String updateKit(
            @McpToolParam(description = "User UUID", required = true) String userId,
            @McpToolParam(description = "Kit JSON as a map", required = true) Map<String, Object> kit
    ) {
        users.updateKit(UUID.fromString(userId), kit, null, null);
        return "updated kit for " + userId;
    }

    @McpTool(name = "resend_invite", description = "Resend invite email with a new token")
    public String resendInvite(
            @McpToolParam(description = "User UUID", required = true) String userId
    ) {
        var result = invites.resend(UUID.fromString(userId), "hu", "MCP");
        return "resent job " + result.deliveryJobId();
    }

    @McpTool(name = "send_test_delivery", description = "Enqueue a TEST delivery job on email, slack, or pushover")
    public String sendTestDelivery(
            @McpToolParam(description = "Channel: email, slack, pushover", required = true) String channel,
            @McpToolParam(description = "Recipient email, Slack id, or Pushover user key") String recipient
    ) {
        UUID id = testDelivery.send(DeliveryChannelType.valueOf(channel), recipient);
        return "queued " + id;
    }

    @McpTool(name = "list_delivery_jobs", description = "List recent delivery jobs")
    public String listDeliveryJobs() {
        return deliveryService.listRecent().stream()
                .map(j -> j.getCreatedAt() + " " + j.getPurpose() + " " + j.getChannel() + " " + j.getStatus()
                        + " " + j.getRecipient())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }

    @McpTool(name = "list_scrape_sources", description = "List scrape sources and health (ok, silent, error, idle)")
    public String listScrapeSources() {
        return scrapeService.sources().stream()
                .map(s -> s.sourceId() + " " + s.family() + " " + s.health()
                        + (s.lastOkAt() == null ? "" : " lastOk=" + s.lastOkAt())
                        + (s.lastError() == null ? "" : " error=" + s.lastError()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }

    @McpTool(name = "run_scrape", description = "Run scrape now for one source or all")
    public String runScrape(
            @McpToolParam(description = "Source id: telex, bbc, usgs, coingecko, frankfurter, or all") String sourceId
    ) {
        List<ScrapeRun> runs = sourceId == null || sourceId.isBlank() || "all".equalsIgnoreCase(sourceId)
                ? scrapeService.runAll()
                : List.of(scrapeService.run(scrapeService.requireSource(sourceId)));
        return runs.stream()
                .map(r -> r.getSourceId() + " " + r.getStatus() + " fetched=" + r.getFetched()
                        + " new=" + r.getNormalized()
                        + (r.getErrorMessage() == null ? "" : " " + r.getErrorMessage()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }

    @McpTool(name = "list_normalized_events", description = "List recent normalized events for the decision engine")
    public String listNormalizedEvents(
            @McpToolParam(description = "Family: breaking, market, disaster, or all") String family,
            @McpToolParam(description = "Source id or all") String sourceId,
            @McpToolParam(description = "Max rows, up to 100") Integer limit
    ) {
        return scrapeService.listEvents(family, sourceId, limit).stream()
                .map(e -> e.getId() + " " + e.getFamily() + " " + e.getSourceId() + " " + e.getHeadline())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }

    @McpTool(name = "get_normalized_event", description = "Get one normalized event by UUID")
    public String getNormalizedEvent(
            @McpToolParam(description = "Event UUID", required = true) String id
    ) {
        return jsonMapper.writeValueAsString(scrapeService.toExport(scrapeService.requireEvent(UUID.fromString(id))));
    }

    @McpTool(name = "export_normalized_events", description = "Export normalized events as JSON for decision-engine fixtures")
    public String exportNormalizedEvents(
            @McpToolParam(description = "Family: breaking, market, disaster, or all") String family,
            @McpToolParam(description = "Source id or all") String sourceId,
            @McpToolParam(description = "Max rows, up to 100") Integer limit
    ) {
        return jsonMapper.writeValueAsString(scrapeService.exportEvents(family, sourceId, limit));
    }

    @McpTool(name = "get_decision_switch", description = "Get native/ai decision engine switch")
    public String getDecisionSwitch() {
        var view = decisions.switchView();
        return view.mode() + " openai=" + view.openaiConfigured();
    }

    @McpTool(name = "set_decision_switch", description = "Set decision engine switch to native or ai")
    public String setDecisionSwitch(
            @McpToolParam(description = "native or ai", required = true) String mode
    ) {
        var view = decisions.setMode(mode);
        return view.mode() + " openai=" + view.openaiConfigured();
    }

    @McpTool(name = "decide_event", description = "Run the decision engine for one normalized event UUID")
    public String decideEvent(
            @McpToolParam(description = "Event UUID", required = true) String eventId
    ) {
        return jsonMapper.writeValueAsString(decisions.evaluate(scrapeService.requireEvent(UUID.fromString(eventId))));
    }

    @McpTool(name = "list_user_decisions", description = "List decision trail rows for an event, or recent if eventId omitted")
    public String listUserDecisions(
            @McpToolParam(description = "Event UUID") String eventId
    ) {
        var rows = eventId == null || eventId.isBlank()
                ? decisions.listRecent()
                : decisions.trail(UUID.fromString(eventId));
        return jsonMapper.writeValueAsString(rows);
    }

    @McpTool(name = "score_golden", description = "Score the native decision engine against the 65-sample golden corpus")
    public String scoreGolden() {
        return jsonMapper.writeValueAsString(golden.score());
    }

    @McpTool(name = "load_station", description = "Load the repo test station: Ada/Béla/Cora/Dénes/Elena plus scraped golden events, then run decisions")
    public String loadStation(
            @McpToolParam(description = "If true, run the decision engine after insert") Boolean decide
    ) {
        return jsonMapper.writeValueAsString(station.load(decide == null || decide));
    }
}
