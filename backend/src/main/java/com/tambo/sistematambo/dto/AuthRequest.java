package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El usuario o correo es obligatorio") String username,
        @NotBlank(message = "La clave es obligatoria") String password) {
}
