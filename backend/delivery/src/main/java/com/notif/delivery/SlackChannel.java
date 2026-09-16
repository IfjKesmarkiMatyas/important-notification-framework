package com.notif.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SlackChannel implements DeliveryChannel {

    private final RestClient restClient;
    private final String botToken;

    public SlackChannel(
            RestClient.Builder restClientBuilder,
            @Value("${notif.slack.bot-token:}") String botToken
    ) {
        this.restClient = restClientBuilder.baseUrl("https://slack.com/api").build();
        this.botToken = botToken == null ? "" : botToken;
    }

    @Override
    public DeliveryChannelType type() {
        return DeliveryChannelType.slack;
    }

    @Override
    public ChannelResult send(DeliveryJob job) {
        if (botToken.isBlank()) {
            return ChannelResult.failure("Slack bot token is not configured");
        }
        try {
            String channel = job.getRecipient();
            if (channel != null && channel.startsWith("U")) {
                Map<?, ?> open = restClient.post()
                        .uri("/conversations.open")
                        .headers(h -> h.setBearerAuth(botToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("users", channel))
                        .retrieve()
                        .body(Map.class);
                if (open == null || !Boolean.TRUE.equals(open.get("ok"))) {
                    return ChannelResult.failure(slackError(open, "conversations.open failed"));
                }
                Object ch = open.get("channel");
                if (ch instanceof Map<?, ?> cmap && cmap.get("id") instanceof String id) {
                    channel = id;
                }
            }
            String text = job.getBodyText() == null ? (job.getSubject() == null ? "Notif" : job.getSubject()) : job.getBodyText();
            Map<?, ?> posted = restClient.post()
                    .uri("/chat.postMessage")
                    .headers(h -> h.setBearerAuth(botToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("channel", channel, "text", text))
                    .retrieve()
                    .body(Map.class);
            if (posted == null || !Boolean.TRUE.equals(posted.get("ok"))) {
                return ChannelResult.failure(slackError(posted, "chat.postMessage failed"));
            }
            return ChannelResult.success();
        } catch (Exception ex) {
            return ChannelResult.failure(ex.getMessage() == null ? "Slack send failed" : ex.getMessage());
        }
    }

    private static String slackError(Map<?, ?> body, String fallback) {
        if (body != null && body.get("error") instanceof String err) {
            return "Slack: " + err;
        }
        return fallback;
    }
}
