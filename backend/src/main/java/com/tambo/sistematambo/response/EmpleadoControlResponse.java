package com.tambo.sistematambo.response;

public record EmpleadoControlResponse(
        Long id,
        String nombre,
        String cargo,
        String entrada,
        String salida,
        Integer tardanzas,
        Integer faltas,
        Integer asistencias,
        String estado) {
}
