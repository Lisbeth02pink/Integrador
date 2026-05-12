package com.tambo.sistematambo.response;

import java.util.List;

public record AuthResponse(
        Long id,
        String usuario,
        String correo,
        String nombre,
        String perfil,
        List<String> modulos,
        String accessToken,
        String refreshToken,
        long expiresAt,
        String message) {
}
