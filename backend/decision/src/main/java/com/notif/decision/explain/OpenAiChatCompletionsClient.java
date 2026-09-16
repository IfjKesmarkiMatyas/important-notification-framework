package com.notif.decision.explain;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.notif.decision.config.DecisionProperties;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OpenAiChatCompletionsClient implements ChatCompletionsClient {

    private final DecisionProperties properties;
    private final JsonMapper jsonMapper;
    private final RestClient restClient;

    public OpenAiChatCompletionsClient(DecisionProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com").build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (!properties.openaiConfigured()) {
            throw new IllegalStateException("GPT_API_KEY missing");
        }
        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "temperature", 0,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
        String json = restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getOpenaiApiKey())
                .body(body)
                .retrieve()
                .body(String.class);
        Map<String, Object> parsed = jsonMapper.readValue(json == null ? "{}" : json, new TypeReference<>() {});
        List<?> choices = parsed.get("choices") instanceof List<?> list ? list : List.of();
        if (choices.isEmpty() || !(choices.getFirst() instanceof Map<?, ?> choice)) {
            throw new IllegalStateException("OpenAI returned no choices");
        }
        Object message = choice.get("message");
        if (message instanceof Map<?, ?> msg) {
            Object content = msg.get("content");
            if (content != null) {
                return String.valueOf(content);
            }
        }
        throw new IllegalStateException("OpenAI returned empty content");
    }
}
