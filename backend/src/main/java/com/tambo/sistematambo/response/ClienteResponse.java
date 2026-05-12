package com.tambo.sistematambo.response;

public record ClienteResponse(
        Long id,
        String tipoDocumento,
        String documento,
        String nombre,
        String telefono,
        String correo,
        String direccion,
        Integer estado) {
}
