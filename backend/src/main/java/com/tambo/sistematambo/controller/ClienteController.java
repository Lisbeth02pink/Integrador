package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.ClienteRequest;
import com.tambo.sistematambo.response.ClienteResponse;
import com.tambo.sistematambo.service.ClienteService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @PostMapping
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return clienteService.crear(request);
    }

    @PutMapping("/{clienteId}")
    public ClienteResponse actualizar(@PathVariable Long clienteId, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(clienteId, request);
    }

    @PatchMapping("/{clienteId}/estado")
    public ClienteResponse cambiarEstado(@PathVariable Long clienteId) {
        return clienteService.cambiarEstado(clienteId);
    }

    @DeleteMapping("/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long clienteId) {
        clienteService.eliminar(clienteId);
    }
}
