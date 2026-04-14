package com.tambo.sistematambo.auth;

import java.util.List;

public record AuthResponse(
        Long id,
        String usuario,
        String correo,
        String nombre,
        String perfil,
        List<String> modulos,
        String message) {
}
