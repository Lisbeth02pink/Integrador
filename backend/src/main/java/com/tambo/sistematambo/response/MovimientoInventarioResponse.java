package com.tambo.sistematambo.response;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long id,
        LocalDateTime fecha,
        Long productoId,
        String productoSku,
        String productoNombre,
        String tipo,
        Integer cantidad,
        String almacenOrigen,
        String almacenDestino,
        String referencia) {
}
