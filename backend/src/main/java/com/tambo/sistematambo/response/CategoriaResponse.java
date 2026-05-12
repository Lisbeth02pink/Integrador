package com.tambo.sistematambo.response;

public record CategoriaResponse(
        Long id,
        String nombre,
        String codigo,
        String descripcion,
        String imagen,
        Integer estado) {
}
