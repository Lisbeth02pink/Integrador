package com.tambo.sistematambo.response;

import java.time.LocalDateTime;

public record ProveedorEntregaResponse(
        Long id,
        Long supplierId,
        String supplierName,
        Long productId,
        String productName,
        String productSku,
        String warehouseName,
        Integer quantity,
        LocalDateTime deliveredAt,
        String notes) {
}
