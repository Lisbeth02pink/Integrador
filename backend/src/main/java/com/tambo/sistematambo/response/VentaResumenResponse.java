package com.tambo.sistematambo.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaResumenResponse(
        Long id,
        LocalDate fecha,
        String canal,
        BigDecimal ingresos,
        BigDecimal egresos,
        String productoMasVendido) {
}
