package com.notif.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PushoverChannel implements DeliveryChannel {

    private final RestClient restClient;
    private final String appToken;

    public PushoverChannel(
            RestClient.Builder restClientBuilder,
            @Value("${notif.pushover.app-token:}") String appToken
    ) {
        this.restClient = restClientBuilder.baseUrl("https://api.pushover.net").build();
        this.appToken = appToken == null ? "" : appToken;
    }

    @Override
    public DeliveryChannelType type() {
        return DeliveryChannelType.pushover;
    }

    @Override
    public ChannelResult send(DeliveryJob job) {
        if (appToken.isBlank()) {
            return ChannelResult.failure("Pushover app token is not configured");
        }
        if (job.getRecipient() == null || job.getRecipient().isBlank()) {
            return ChannelResult.failure("Pushover user key is missing");
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("token", appToken);
            form.add("user", job.getRecipient());
            form.add("title", job.getSubject() == null ? "Notif" : job.getSubject());
            form.add("message", job.getBodyText() == null || job.getBodyText().isBlank()
                    ? "Notif" : job.getBodyText());
            Map<?, ?> body = restClient.post()
                    .uri("/1/messages.json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
            if (body != null && Integer.valueOf(1).equals(body.get("status"))) {
                return ChannelResult.success();
            }
            Object errors = body == null ? null : body.get("errors");
            return ChannelResult.failure(errors == null ? "Pushover send failed" : String.valueOf(errors));
        } catch (Exception ex) {
            return ChannelResult.failure(ex.getMessage() == null ? "Pushover send failed" : ex.getMessage());
        }
    }
}
