package com.notif.common.dto.delivery;

import java.util.Map;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryPurpose;

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
