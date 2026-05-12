package com.tambo.sistematambo.response;

public record UserResponse(
        Long id,
        String nombre,
        String usuario,
        String correo,
        Integer estado,
        Long perfilId,
        String perfilNombre) {
}
