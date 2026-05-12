package com.tambo.sistematambo.response;

import com.tambo.sistematambo.model.Modulo;

public record ModuloResponse(Long id, String nombre, String ruta, String icono) {

    public static ModuloResponse fromEntity(Modulo modulo) {
        return new ModuloResponse(modulo.getId(), modulo.getNombre(), modulo.getRuta(), modulo.getIcono());
    }
}
