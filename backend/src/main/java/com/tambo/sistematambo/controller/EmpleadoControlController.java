package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.response.EmpleadoControlResponse;
import com.tambo.sistematambo.service.EmpleadoControlService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoControlController {

    private final EmpleadoControlService empleadoControlService;

    public EmpleadoControlController(EmpleadoControlService empleadoControlService) {
        this.empleadoControlService = empleadoControlService;
    }

    @GetMapping("/control")
    public List<EmpleadoControlResponse> listar() {
        return empleadoControlService.listar();
    }
}
