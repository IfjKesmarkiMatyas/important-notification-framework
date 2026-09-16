package com.notif.identity.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.dto.delivery.EnqueueDeliveryCommand;
import com.notif.common.exception.IdentityException;
import com.notif.common.port.DeliveryDispatcher;

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
