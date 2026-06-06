package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.MermaRequest;
import com.tambo.sistematambo.response.MermaResponse;
import com.tambo.sistematambo.service.MermaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mermas")
public class MermaController {

    private final MermaService mermaService;

    public MermaController(MermaService mermaService) {
        this.mermaService = mermaService;
    }

    @GetMapping
    public List<MermaResponse> listarMermas() {
        return mermaService.listarMermas();
    }

    @PostMapping
    public MermaResponse registrarMerma(@Valid @RequestBody MermaRequest request) {
        return mermaService.registrarMerma(request);
    }
}
