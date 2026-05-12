package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.ProveedorRequest;
import com.tambo.sistematambo.response.ProveedorResponse;
import com.tambo.sistematambo.response.SunatRucResponse;
import com.tambo.sistematambo.service.ProveedorService;
import com.tambo.sistematambo.service.SunatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final SunatService sunatService;

    public ProveedorController(ProveedorService proveedorService, SunatService sunatService) {
        this.proveedorService = proveedorService;
        this.sunatService = sunatService;
    }

    @GetMapping
    public List<ProveedorResponse> listar() {
        return proveedorService.listar();
    }

    @GetMapping("/sunat/{ruc}")
    public SunatRucResponse buscarSunat(@PathVariable String ruc) {
        return sunatService.buscarPorRuc(ruc);
    }

    @PostMapping
    public ProveedorResponse crear(@Valid @RequestBody ProveedorRequest request) {
        return proveedorService.crear(request);
    }

    @PutMapping("/{proveedorId}")
    public ProveedorResponse actualizar(@PathVariable Long proveedorId, @Valid @RequestBody ProveedorRequest request) {
        return proveedorService.actualizar(proveedorId, request);
    }

    @PatchMapping("/{proveedorId}/estado")
    public ProveedorResponse cambiarEstado(@PathVariable Long proveedorId) {
        return proveedorService.cambiarEstado(proveedorId);
    }

    @DeleteMapping("/{proveedorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long proveedorId) {
        proveedorService.eliminar(proveedorId);
    }
}
