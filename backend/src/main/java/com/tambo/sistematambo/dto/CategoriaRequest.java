package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 40) String codigo,
        @Size(max = 250) String descripcion,
        @Size(max = 500) String imagen,
        @NotNull Integer estado) {
}
