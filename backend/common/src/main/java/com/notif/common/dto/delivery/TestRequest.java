package com.notif.common.dto.delivery;

import com.notif.common.domain.delivery.DeliveryChannelType;
import jakarta.validation.constraints.NotNull;

public record TestRequest(@NotNull DeliveryChannelType channel, String recipient) {}
