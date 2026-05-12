package com.tambo.sistematambo.response;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoInternoResponse(
        Long id,
        Long storeId,
        String storeName,
        String requestedBy,
        String priority,
        LocalDateTime requestedAt,
        String status,
        String notes,
        Boolean transferGenerated,
        List<PedidoInternoItemResponse> items) {
}
