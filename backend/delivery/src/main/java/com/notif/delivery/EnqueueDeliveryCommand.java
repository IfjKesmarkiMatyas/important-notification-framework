package com.notif.delivery;

import java.util.Map;

public record EnqueueDeliveryCommand(
        DeliveryPurpose purpose,
        DeliveryChannelType channel,
        String recipient,
        String locale,
        String subject,
        String bodyText,
        String bodyHtml,
        Map<String, Object> payload
) {}
