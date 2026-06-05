package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.RecepcionTiendaRequest;
import com.tambo.sistematambo.response.TransferenciaResponse;
import com.tambo.sistematambo.service.RecepcionTiendaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recepcion-tienda")
public class RecepcionTiendaController {

    private final RecepcionTiendaService recepcionTiendaService;

    public RecepcionTiendaController(RecepcionTiendaService recepcionTiendaService) {
        this.recepcionTiendaService = recepcionTiendaService;
    }

    @PostMapping
    public TransferenciaResponse confirmar(@Valid @RequestBody RecepcionTiendaRequest request) {
        return recepcionTiendaService.confirmar(request);
    }
}
