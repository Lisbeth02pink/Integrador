package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.repository.ModuloRepository;
import com.tambo.sistematambo.response.ModuloResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modulos")
public class ModuloController {

    private final ModuloRepository moduloRepository;

    public ModuloController(ModuloRepository moduloRepository) {
        this.moduloRepository = moduloRepository;
    }

    @GetMapping
    public List<ModuloResponse> listar() {
        return moduloRepository.findAll().stream().map(ModuloResponse::fromEntity).toList();
    }
}
