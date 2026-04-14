package com.tambo.sistematambo.perfil;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/{perfilId}/modulos")
    public PerfilResponse actualizarPermisos(
            @PathVariable Long perfilId,
            @Valid @RequestBody PerfilPermisosRequest request) {
        return perfilService.actualizarPermisos(perfilId, request);
    }
}
