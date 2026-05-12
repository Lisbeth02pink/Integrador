package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLogoutRequest(@NotBlank String refreshToken) {
}
