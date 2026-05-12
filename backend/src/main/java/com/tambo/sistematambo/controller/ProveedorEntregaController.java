package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.ProveedorEntregaRequest;
import com.tambo.sistematambo.response.ProveedorEntregaResponse;
import com.tambo.sistematambo.service.ProveedorEntregaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores/entregas")
public class ProveedorEntregaController {

    private final ProveedorEntregaService proveedorEntregaService;

    public ProveedorEntregaController(ProveedorEntregaService proveedorEntregaService) {
        this.proveedorEntregaService = proveedorEntregaService;
    }

    @GetMapping
    public List<ProveedorEntregaResponse> listar() {
        return proveedorEntregaService.listar();
    }

    @PostMapping
    public ProveedorEntregaResponse registrar(@Valid @RequestBody ProveedorEntregaRequest request) {
        return proveedorEntregaService.registrar(request);
    }
}
