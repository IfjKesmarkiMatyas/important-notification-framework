package com.notif.common.dto.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptRequest(
        @NotBlank @Size(min = 1, max = 200) String displayName,
        @NotBlank @Size(min = 8, max = 200) String password
) {}
