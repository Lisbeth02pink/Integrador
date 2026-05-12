package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank @Pattern(regexp = "\\d{11}") String ruc,
        @NotBlank @Size(max = 150) String razonSocial,
        @NotBlank @Size(max = 120) String contacto,
        @Size(max = 20) String telefono,
        @Email @Size(max = 120) String correo,
        @Size(max = 180) String direccion,
        @Size(max = 400) String productosSuministrados,
        @Size(max = 400) String historialEntregas) {
}
