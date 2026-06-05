package com.tambo.sistematambo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CrearTransferenciaRequest(
        @NotNull Long almacenOrigenId,
        @NotNull Long almacenDestinoId,
        String responsable,
        String referencia,
        @NotEmpty List<@Valid DetalleTransferenciaRequest> detalles) {
}
