package com.tambo.sistematambo.response;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String sku,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        Integer stock,
        Integer stockMinimo,
        Long categoriaId,
        String categoriaNombre,
        Long almacenId,
        String almacenNombre,
        String imagen,
        Integer estado) {
}
