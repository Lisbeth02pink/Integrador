package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AsistenciaPerfilRequest(
        Long userId,

        @NotEmpty(message = "El descriptor facial es obligatorio")
        List<Float> descriptor) {
}
