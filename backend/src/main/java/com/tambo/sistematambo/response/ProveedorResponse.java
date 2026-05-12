package com.tambo.sistematambo.response;

public record ProveedorResponse(
        Long id,
        String ruc,
        String razonSocial,
        String contacto,
        String telefono,
        String correo,
        String direccion,
        String productosSuministrados,
        String historialEntregas,
        Integer estado) {
}
