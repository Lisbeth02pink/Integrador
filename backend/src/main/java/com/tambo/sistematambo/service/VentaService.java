package com.tambo.sistematambo.service;

import com.tambo.sistematambo.repository.VentaResumenRepository;
import com.tambo.sistematambo.response.VentaResumenResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VentaService {

    private final VentaResumenRepository ventaResumenRepository;

    public VentaService(VentaResumenRepository ventaResumenRepository) {
        this.ventaResumenRepository = ventaResumenRepository;
    }

    public List<VentaResumenResponse> listarResumen(LocalDate desde, LocalDate hasta) {
        return ventaResumenRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta)
                .stream()
                .map(item -> new VentaResumenResponse(
                        item.getId(),
                        item.getFecha(),
                        item.getCanal(),
                        item.getIngresos(),
                        item.getEgresos(),
                        item.getProductoMasVendido()))
                .toList();
    }
}
