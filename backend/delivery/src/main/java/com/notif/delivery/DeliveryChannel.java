package com.notif.delivery;

public interface DeliveryChannel {
    DeliveryChannelType type();

    ChannelResult send(DeliveryJob job);
}
