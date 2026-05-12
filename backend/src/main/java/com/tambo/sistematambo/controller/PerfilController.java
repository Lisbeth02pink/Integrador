package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.PerfilPermisosRequest;
import com.tambo.sistematambo.dto.PerfilRequest;
import com.tambo.sistematambo.response.PerfilResponse;
import com.tambo.sistematambo.service.PerfilService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public List<PerfilResponse> listar() {
        return perfilService.listar();
    }

    @PostMapping
    public PerfilResponse crear(@Valid @RequestBody PerfilRequest request) {
        return perfilService.crear(request);
    }

    @PutMapping("/{perfilId}")
    public PerfilResponse actualizar(@PathVariable Long perfilId, @Valid @RequestBody PerfilRequest request) {
        return perfilService.actualizar(perfilId, request);
    }

    @PutMapping("/{perfilId}/modulos")
    public PerfilResponse actualizarPermisos(
            @PathVariable Long perfilId,
            @Valid @RequestBody PerfilPermisosRequest request) {
        return perfilService.actualizarPermisos(perfilId, request);
    }

    @PatchMapping("/{perfilId}/estado")
    public PerfilResponse cambiarEstado(@PathVariable Long perfilId) {
        return perfilService.cambiarEstado(perfilId);
    }

    @DeleteMapping("/{perfilId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long perfilId) {
        perfilService.eliminar(perfilId);
    }
}
