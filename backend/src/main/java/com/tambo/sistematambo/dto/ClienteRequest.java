package com.tambo.sistematambo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El tipo de documento es obligatorio")
        @Pattern(regexp = "DNI|RUC", message = "El tipo de documento debe ser DNI o RUC")
        String tipoDocumento,

        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = "\\d{8}|\\d{11}", message = "El documento debe tener 8 o 11 digitos")
        String documento,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
        String nombre,

        @Size(max = 20, message = "El telefono no debe exceder 20 caracteres")
        String telefono,

        @Email(message = "El correo debe tener un formato valido")
        @Size(max = 120, message = "El correo no debe exceder 120 caracteres")
        String correo,

        @Size(max = 180, message = "La direccion no debe exceder 180 caracteres")
        String direccion) {
}
