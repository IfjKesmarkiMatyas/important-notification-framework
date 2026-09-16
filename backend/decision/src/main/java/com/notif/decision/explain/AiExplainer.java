package com.notif.decision.explain;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.config.DecisionProperties;
import com.notif.decision.match.InterestHit;
import com.notif.decision.parse.Maps;
import com.notif.decision.service.NativeDecisionEngine.Verdict;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AiExplainer {

    private final ChatCompletionsClient chat;
    private final DecisionProperties properties;
    private final JsonMapper jsonMapper;
    private final String systemPrompt;

    public AiExplainer(ChatCompletionsClient chat, DecisionProperties properties, JsonMapper jsonMapper) {
        this.chat = chat;
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.systemPrompt = readPrompt();
    }

    public boolean configured() {
        return properties.openaiConfigured();
    }

    public String explain(NormalizedEvent event, AppUser user, Verdict verdict) {
        if (!configured()) {
            throw new IllegalStateException("GPT_API_KEY missing");
        }
        String response = chat.complete(systemPrompt, jsonMapper.writeValueAsString(payload(event, user, verdict)));
        Map<String, Object> parsed = jsonMapper.readValue(response, new TypeReference<>() {});
        String reason = Maps.str(parsed.get("reason"));
        if (reason.isBlank()) {
            throw new IllegalStateException("AI explanation missing reason");
        }
        return reason;
    }

    private static Map<String, Object> payload(NormalizedEvent event, AppUser user, Verdict verdict) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("locale", event.getLocale());
        body.put("headline", event.getHeadline());
        body.put("summary", event.getSummary());
        body.put("family", event.getFamily() == null ? null : event.getFamily().name());
        body.put("payload", event.getPayload());
        body.put("userEmail", user.getEmail());
        body.put("channels", verdict.channels());
        body.put("level", verdict.level() == null ? null : verdict.level().name());
        body.put("outcome", verdict.outcome() == null ? null : verdict.outcome().name());
        InterestHit hit = verdict.hit();
        if (hit != null) {
            body.put("matchedInterest", Map.of(
                    "type", hit.type(),
                    "whenKey", hit.whenKey(),
                    "eventValue", hit.eventValue() == null ? "" : hit.eventValue(),
                    "threshold", hit.threshold() == null ? "" : hit.threshold(),
                    "topics", hit.matchedTopics()
            ));
        }
        body.put("kit", user.getKit());
        return body;
    }

    private static String readPrompt() {
        try {
            return new ClassPathResource("prompts/decision.md").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "Return JSON {\"reason\":\"...\"} only. Do not decide FIRE or NO.";
        }
    }
}
