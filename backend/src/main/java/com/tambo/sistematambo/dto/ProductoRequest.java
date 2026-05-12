package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 160) String nombre,
        @NotBlank @Size(max = 50) String sku,
        @NotNull @DecimalMin("0.00") BigDecimal precioCompra,
        @NotNull @DecimalMin("0.00") BigDecimal precioVenta,
        @NotNull @Min(0) Integer stock,
        @NotNull @Min(0) Integer stockMinimo,
        @NotNull Long categoriaId,
        @NotNull Long almacenId,
        @Size(max = 500) String imagen,
        @NotNull Integer estado) {
}
