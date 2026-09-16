package com.notif.identity;

import com.notif.delivery.DeliveryChannelType;
import com.notif.delivery.DeliveryDispatcher;
import com.notif.delivery.DeliveryPurpose;
import com.notif.delivery.EnqueueDeliveryCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class TestDeliveryService {

    private final DeliveryDispatcher delivery;
    private final String slackTestChannel;
    private final String pushoverTestUser;

    public TestDeliveryService(
            DeliveryDispatcher delivery,
            @Value("${notif.slack.test-channel-id:}") String slackTestChannel,
            @Value("${notif.pushover.test-user-key:}") String pushoverTestUser
    ) {
        this.delivery = delivery;
        this.slackTestChannel = slackTestChannel;
        this.pushoverTestUser = pushoverTestUser;
    }

    public UUID send(DeliveryChannelType channel, String recipient) {
        String to = recipient;
        if (to == null || to.isBlank()) {
            to = switch (channel) {
                case slack -> slackTestChannel;
                case pushover -> pushoverTestUser;
                case email -> null;
            };
        }
        if (to == null || to.isBlank()) {
            throw new IdentityException(HttpStatus.BAD_REQUEST, "Recipient required for " + channel);
        }
        return delivery.enqueue(new EnqueueDeliveryCommand(
                DeliveryPurpose.TEST,
                channel,
                to,
                "hu",
                null,
                null,
                null,
                Map.of("channel", channel.name())
        ));
    }
}
