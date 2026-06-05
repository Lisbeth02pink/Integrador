package com.tambo.sistematambo.response;

public record TransferenciaDetalleResponse(
        Long productoId,
        String productoNombre,
        String productoSku,
        Integer cantidad) {
}
