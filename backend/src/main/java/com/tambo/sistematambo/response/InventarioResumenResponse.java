package com.tambo.sistematambo.response;

public record InventarioResumenResponse(
        AlmacenResponse warehouse,
        Long totalProductos,
        Long stockTotal) {
}
