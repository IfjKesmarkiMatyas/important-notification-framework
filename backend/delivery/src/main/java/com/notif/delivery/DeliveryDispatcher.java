package com.notif.delivery;

import java.util.UUID;

public interface DeliveryDispatcher {
    UUID enqueue(EnqueueDeliveryCommand command);
}
