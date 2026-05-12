package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RutaEntregaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 160) String zona,
        @NotBlank @Size(max = 120) String repartidor,
        Long pedidoId,
        Long transferenciaId,
        @Size(max = 80) String vehiculo,
        @Size(max = 30) String tipoVehiculo,
        @Size(max = 20) String placa,
        @Min(1) Integer capacidadVehiculo,
        @Min(1) Integer cantidadCarga,
        @Size(max = 160) String origen,
        @Size(max = 160) String destino,
        LocalDate fechaEntrega,
        @NotNull @Min(1) Integer pedidos,
        @NotBlank @Size(max = 20) String estado,
        @NotBlank @Size(max = 5) String horaSalida,
        @Size(max = 5) String horaEstimadaLlegada,
        @Size(max = 5) String horaEntregaReal,
        @Size(max = 180) String ubicacionActual,
        @Size(max = 500) String observaciones,
        @Size(max = 500) String incidencias,
        @Size(max = 30) String estadoGps,
        @Size(max = 500) String evidenciaEntrega,
        @Size(max = 500) String firmaDigital,
        @Size(max = 500) String fotoEntrega,
        Boolean vehiculoActivo,
        Boolean conductorBloqueado,
        Boolean confirmacionEntrega,
        @NotNull @Min(0) @Max(100) Integer progreso) {
}
