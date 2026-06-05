package com.tambo.sistematambo.response;

import java.time.LocalDateTime;
import java.util.List;

public record TransferenciaResponse(
        Long id,
        Long almacenOrigenId,
        String almacenOrigenNombre,
        Long almacenDestinoId,
        String almacenDestinoNombre,
        LocalDateTime fecha,
        String estado,
        String responsable,
        String referencia,
        List<TransferenciaDetalleResponse> detalles) {
}
