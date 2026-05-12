package com.tambo.sistematambo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PedidoInternoRequest(
        @NotNull Long tiendaId,
        @NotBlank @Size(max = 120) String solicitadoPor,
        @NotBlank @Size(max = 20) String prioridad,
        @Size(max = 400) String observaciones,
        @NotEmpty List<@Valid PedidoInternoItemRequest> items) {
}
