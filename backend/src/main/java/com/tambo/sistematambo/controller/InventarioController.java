package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.TransferenciaInventarioRequest;
import com.tambo.sistematambo.response.InventarioResumenResponse;
import com.tambo.sistematambo.response.MovimientoInventarioResponse;
import com.tambo.sistematambo.response.ProductoResponse;
import com.tambo.sistematambo.service.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/movimientos")
    public List<MovimientoInventarioResponse> listarMovimientos() {
        return inventarioService.listarMovimientos();
    }

    @GetMapping("/stock-bajo")
    public List<ProductoResponse> listarStockBajo() {
        return inventarioService.listarStockBajo();
    }

    @GetMapping("/resumen-almacenes")
    public List<InventarioResumenResponse> resumenPorAlmacen() {
        return inventarioService.resumenPorAlmacen();
    }

    @PostMapping("/transferencias")
    public MovimientoInventarioResponse registrarTransferencia(@Valid @RequestBody TransferenciaInventarioRequest request) {
        return inventarioService.registrarTransferencia(request);
    }
}
