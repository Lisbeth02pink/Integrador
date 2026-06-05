package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetalleTransferenciaRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad) {
}
