package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no debe exceder 50 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripcion no debe exceder 255 caracteres")
        String descripcion) {
}
