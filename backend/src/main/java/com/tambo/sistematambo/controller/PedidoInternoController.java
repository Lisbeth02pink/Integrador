package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.PedidoInternoEstadoRequest;
import com.tambo.sistematambo.dto.PedidoInternoRequest;
import com.tambo.sistematambo.response.MovimientoInventarioResponse;
import com.tambo.sistematambo.response.PedidoInternoResponse;
import com.tambo.sistematambo.service.PedidoInternoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos-internos")
public class PedidoInternoController {

    private final PedidoInternoService pedidoInternoService;

    public PedidoInternoController(PedidoInternoService pedidoInternoService) {
        this.pedidoInternoService = pedidoInternoService;
    }

    @GetMapping
    public List<PedidoInternoResponse> listar() {
        return pedidoInternoService.listar();
    }

    @PostMapping
    public PedidoInternoResponse crear(@Valid @RequestBody PedidoInternoRequest request) {
        return pedidoInternoService.crear(request);
    }

    @PatchMapping("/{pedidoId}/estado")
    public PedidoInternoResponse actualizarEstado(@PathVariable Long pedidoId, @Valid @RequestBody PedidoInternoEstadoRequest request) {
        return pedidoInternoService.actualizarEstado(pedidoId, request);
    }

    @PostMapping("/{pedidoId}/transferencia")
    public List<MovimientoInventarioResponse> generarTransferencia(@PathVariable Long pedidoId) {
        return pedidoInternoService.generarTransferencia(pedidoId);
    }
}
