package com.tambo.sistematambo.response;

import java.time.LocalDateTime;

public record AsistenciaRegistroResponse(
        Long id,
        Long userId,
        String codigo,
        String usuario,
        String nombre,
        Double coincidencia,
        LocalDateTime fecha,
        String tipo) {
}
