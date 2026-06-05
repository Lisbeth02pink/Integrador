package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.response.AlertaResponse;
import com.tambo.sistematambo.service.AlertaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public List<AlertaResponse> listar() {
        return alertaService.listar();
    }
}
