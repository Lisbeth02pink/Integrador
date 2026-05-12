package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransferenciaInventarioRequest(
        @NotNull Long productoId,
        @NotNull Long almacenOrigenId,
        @NotNull Long almacenDestinoId,
        @NotNull @Min(1) Integer cantidad) {
}
