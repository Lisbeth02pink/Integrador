package com.tambo.sistematambo.perfil;

import java.util.List;

public record PerfilResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean estado,
        List<Long> moduloIds,
        List<String> modulos) {
}
