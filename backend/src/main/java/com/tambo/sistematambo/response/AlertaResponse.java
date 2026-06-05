package com.tambo.sistematambo.response;

import java.time.LocalDateTime;

public record AlertaResponse(
        String tipo,
        String titulo,
        String detalle,
        String severidad,
        LocalDateTime fecha) {
}
