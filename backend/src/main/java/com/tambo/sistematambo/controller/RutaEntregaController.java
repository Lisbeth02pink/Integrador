package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.RutaEntregaRequest;
import com.tambo.sistematambo.response.RutaEntregaResponse;
import com.tambo.sistematambo.service.RutaEntregaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rutas")
public class RutaEntregaController {

    private final RutaEntregaService rutaEntregaService;

    public RutaEntregaController(RutaEntregaService rutaEntregaService) {
        this.rutaEntregaService = rutaEntregaService;
    }

    @GetMapping
    public List<RutaEntregaResponse> listar() {
        return rutaEntregaService.listar();
    }

    @PostMapping
    public RutaEntregaResponse crear(@Valid @RequestBody RutaEntregaRequest request) {
        return rutaEntregaService.crear(request);
    }

    @PutMapping("/{rutaId}")
    public RutaEntregaResponse actualizar(@PathVariable Long rutaId, @Valid @RequestBody RutaEntregaRequest request) {
        return rutaEntregaService.actualizar(rutaId, request);
    }

    @DeleteMapping("/{rutaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long rutaId) {
        rutaEntregaService.eliminar(rutaId);
    }
}
