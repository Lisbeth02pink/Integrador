package com.tambo.sistematambo.response;

import java.util.List;

public record PerfilResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean estado,
        List<Long> moduloIds,
        List<String> modulos) {
}
