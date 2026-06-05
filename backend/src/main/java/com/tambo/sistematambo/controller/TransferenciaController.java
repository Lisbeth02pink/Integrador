package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.CrearTransferenciaRequest;
import com.tambo.sistematambo.response.TransferenciaResponse;
import com.tambo.sistematambo.service.TransferenciaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @GetMapping
    public List<TransferenciaResponse> listar() {
        return transferenciaService.listar();
    }

    @PostMapping
    public TransferenciaResponse crear(@Valid @RequestBody CrearTransferenciaRequest request) {
        return transferenciaService.crear(request);
    }

    @PostMapping("/{id}/enviar")
    public TransferenciaResponse enviar(@PathVariable Long id) {
        return transferenciaService.enviar(id);
    }

    @PostMapping("/{id}/recibir")
    public TransferenciaResponse recibir(@PathVariable Long id) {
        return transferenciaService.recibir(id);
    }

    @PostMapping("/{id}/rechazar")
    public TransferenciaResponse rechazar(@PathVariable Long id) {
        return transferenciaService.rechazar(id);
    }
}
