package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlmacenRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 80) String ciudad,
        @NotBlank @Size(max = 120) String responsable,
        @NotBlank @Size(max = 180) String direccion,
        @Size(max = 20) String tipo,
        @NotNull Integer capacidad,
        @NotNull Integer ocupacion,
        @NotNull Integer estado) {
}
