package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProveedorEntregaRequest(
        @NotNull Long proveedorId,
        @NotNull Long productoId,
        @NotNull Long almacenDestinoId,
        @NotNull @Min(1) Integer cantidad,
        @Size(max = 400) String observaciones) {
}
