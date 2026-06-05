package com.tambo.sistematambo.service;

import com.tambo.sistematambo.model.MovimientoInventario;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.model.Transferencia;
import com.tambo.sistematambo.repository.MovimientoInventarioRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.repository.TransferenciaRepository;
import com.tambo.sistematambo.response.AlertaResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlertaService {

    private final ProductoRepository productoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public AlertaService(
            ProductoRepository productoRepository,
            TransferenciaRepository transferenciaRepository,
            MovimientoInventarioRepository movimientoInventarioRepository) {
        this.productoRepository = productoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    public List<AlertaResponse> listar() {
        List<AlertaResponse> alertas = new ArrayList<>();

        for (Producto producto : productoRepository.findAll()) {
            if (producto.getStock() == 0) {
                alertas.add(new AlertaResponse(
                        "STOCK_AGOTADO",
                        producto.getNombre() + " sin stock",
                        producto.getAlmacen().getNombre() + " no tiene unidades disponibles",
                        "CRITICA",
                        LocalDateTime.now()));
            } else if (producto.getStock() <= producto.getStockMinimo()) {
                alertas.add(new AlertaResponse(
                        "BAJO_STOCK",
                        producto.getNombre() + " con bajo stock",
                        producto.getStock() + " und disponibles en " + producto.getAlmacen().getNombre(),
                        "ALTA",
                        LocalDateTime.now()));
            }
        }

        for (Transferencia transferencia : transferenciaRepository.findAll()) {
            if ("PENDIENTE".equalsIgnoreCase(transferencia.getEstado())
                    || "ENVIADA".equalsIgnoreCase(transferencia.getEstado())) {
                alertas.add(new AlertaResponse(
                        "TRANSFERENCIA_PENDIENTE",
                        "Transferencia #" + transferencia.getId() + " " + transferencia.getEstado().toLowerCase(),
                        transferencia.getAlmacenOrigen().getNombre() + " a " + transferencia.getAlmacenDestino().getNombre(),
                        "MEDIA",
                        transferencia.getFecha()));
            }
        }

        movimientoInventarioRepository.findAllByOrderByFechaDesc().stream()
                .filter(movimiento -> "Merma".equalsIgnoreCase(movimiento.getTipo()))
                .limit(5)
                .map(this::toMermaAlerta)
                .forEach(alertas::add);

        return alertas.stream()
                .sorted(Comparator.comparing(AlertaResponse::fecha).reversed())
                .toList();
    }

    private AlertaResponse toMermaAlerta(MovimientoInventario movimiento) {
        return new AlertaResponse(
                "MERMA",
                "Merma registrada: " + movimiento.getProducto().getNombre(),
                movimiento.getCantidad() + " und descontadas. " + movimiento.getReferencia(),
                "ALTA",
                movimiento.getFecha());
    }
}
