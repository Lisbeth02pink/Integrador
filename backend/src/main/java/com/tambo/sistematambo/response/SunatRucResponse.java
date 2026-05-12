package com.tambo.sistematambo.response;

public record SunatRucResponse(
        String razonSocial,
        String numeroDocumento,
        String estado,
        String condicion,
        String direccion,
        String distrito,
        String provincia,
        String departamento,
        Boolean agenteRetencion,
        Boolean buenContribuyente) {
}
