package com.notif.common.dto.decision;

import jakarta.validation.constraints.NotBlank;

public record SwitchUpdateRequest(@NotBlank String mode) {}
