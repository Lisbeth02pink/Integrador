package com.tambo.sistematambo.response;

import java.time.LocalDateTime;
import java.util.List;

public record AsistenciaPerfilResponse(
        Long id,
        Long userId,
        String codigo,
        String usuario,
        String nombre,
        List<Float> descriptor,
        LocalDateTime creadoEn) {
}
