package com.tambo.sistematambo.response;

public record PedidoInternoItemResponse(
        Long productId,
        String productName,
        String sku,
        Integer quantity) {
}
