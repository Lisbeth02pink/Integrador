package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PedidoInternoEstadoRequest(
        @NotBlank @Size(max = 20) String estado) {
}
