package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.AlmacenRequest;
import com.tambo.sistematambo.response.AlmacenResponse;
import com.tambo.sistematambo.service.AlmacenService;
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
@RequestMapping("/api/almacenes")
public class AlmacenController {

    private final AlmacenService almacenService;

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @GetMapping
    public List<AlmacenResponse> listar() {
        return almacenService.listar();
    }

    @PostMapping
    public AlmacenResponse crear(@Valid @RequestBody AlmacenRequest request) {
        return almacenService.crear(request);
    }

    @PutMapping("/{almacenId}")
    public AlmacenResponse actualizar(@PathVariable Long almacenId, @Valid @RequestBody AlmacenRequest request) {
        return almacenService.actualizar(almacenId, request);
    }

    @DeleteMapping("/{almacenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long almacenId) {
        almacenService.eliminar(almacenId);
    }
}
