package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PerfilPermisosRequest(
        @NotEmpty(message = "Debes seleccionar al menos un modulo")
        List<Long> moduloIds) {
}
