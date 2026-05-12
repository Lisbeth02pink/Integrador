package com.tambo.sistematambo.response;

public record AlmacenResponse(
        Long id,
        String nombre,
        String ciudad,
        String responsable,
        String direccion,
        String tipo,
        Integer capacidad,
        Integer ocupacion,
        Integer estado) {
}
