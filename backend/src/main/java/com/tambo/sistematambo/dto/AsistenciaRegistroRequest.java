package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AsistenciaRegistroRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long userId,

        @NotNull(message = "La coincidencia es obligatoria")
        @DecimalMin(value = "0.0", message = "La coincidencia no puede ser negativa")
        @DecimalMax(value = "1.0", message = "La coincidencia no puede ser mayor a 1")
        Double coincidencia,

        @NotNull(message = "El tipo es obligatorio")
        TipoMarcacion tipo) {
    public enum TipoMarcacion {
        ENTRADA,
        SALIDA
    }
}
