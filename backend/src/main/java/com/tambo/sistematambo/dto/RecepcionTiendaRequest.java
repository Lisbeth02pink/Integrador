package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecepcionTiendaRequest(
        @NotNull Long transferenciaId,
        @Size(max = 120) String responsable,
        @Size(max = 180) String observaciones) {
}
