package com.notif.common.dto.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteRequest(@Email @NotBlank String email, String locale) {}
