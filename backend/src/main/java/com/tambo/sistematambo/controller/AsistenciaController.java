package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.AsistenciaPerfilRequest;
import com.tambo.sistematambo.dto.AsistenciaRegistroRequest;
import com.tambo.sistematambo.response.AsistenciaPerfilResponse;
import com.tambo.sistematambo.response.AsistenciaRegistroResponse;
import com.tambo.sistematambo.service.AsistenciaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/perfiles")
    public List<AsistenciaPerfilResponse> listarPerfiles() {
        return asistenciaService.listarPerfiles();
    }

    @PostMapping("/perfiles")
    public AsistenciaPerfilResponse guardarPerfil(@Valid @RequestBody AsistenciaPerfilRequest request) {
        return asistenciaService.guardarPerfil(request);
    }

    @DeleteMapping("/perfiles/{perfilId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPerfil(@PathVariable Long perfilId) {
        asistenciaService.eliminarPerfil(perfilId);
    }

    @GetMapping("/registros")
    public List<AsistenciaRegistroResponse> listarRegistros() {
        return asistenciaService.listarRegistros();
    }

    @PostMapping("/registros")
    public AsistenciaRegistroResponse registrarAsistencia(@Valid @RequestBody AsistenciaRegistroRequest request) {
        return asistenciaService.registrarAsistencia(request);
    }
}
