package com.notif.common.port;

import java.util.UUID;
import com.notif.common.dto.delivery.EnqueueDeliveryCommand;

public interface DeliveryDispatcher {
    UUID enqueue(EnqueueDeliveryCommand command);
}
