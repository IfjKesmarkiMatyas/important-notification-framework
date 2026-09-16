package com.notif.delivery.channel;

import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.dto.delivery.ChannelResult;
import com.notif.common.entity.delivery.DeliveryJob;

public interface DeliveryChannel {
    DeliveryChannelType type();

    ChannelResult send(DeliveryJob job);
}
