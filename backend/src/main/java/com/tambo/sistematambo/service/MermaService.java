package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.MermaRequest;
import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.model.Merma;
import com.tambo.sistematambo.model.MovimientoInventario;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.repository.MermaRepository;
import com.tambo.sistematambo.repository.MovimientoInventarioRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.response.MermaResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MermaService {

    private final MermaRepository mermaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public MermaService(MermaRepository mermaRepository, ProductoRepository productoRepository, MovimientoInventarioRepository movimientoRepository) {
        this.mermaRepository = mermaRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<MermaResponse> listarMermas() {
        return mermaRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public MermaResponse registrarMerma(MermaRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStock() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para registrar la merma. Stock actual: " + producto.getStock());
        }

        // Restar stock
        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);

        // Crear la Merma
        Merma merma = new Merma();
        merma.setFecha(LocalDateTime.now());
        merma.setProducto(producto);
        merma.setCantidad(request.getCantidad());
        merma.setMotivo(request.getMotivo());
        merma.setResponsable(request.getResponsable());
        merma.setObservaciones(request.getObservaciones());
        Merma mermaGuardada = mermaRepository.save(merma);

        // Crear movimiento en kardex (MovimientoInventario)
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFecha(mermaGuardada.getFecha());
        movimiento.setProducto(producto);
        movimiento.setTipo("SALIDA_MERMA");
        movimiento.setCantidad(request.getCantidad());
        movimiento.setAlmacenOrigen(producto.getAlmacen());
        movimiento.setReferencia("Merma: " + request.getMotivo() + " - Resp: " + request.getResponsable());
        movimientoRepository.save(movimiento);

        return mapToResponse(mermaGuardada);
    }

    private MermaResponse mapToResponse(Merma merma) {
        MermaResponse res = new MermaResponse();
        res.setMovimientoId(merma.getId());
        res.setFecha(merma.getFecha());
        res.setProductoId(merma.getProducto().getId());
        res.setProductoNombre(merma.getProducto().getNombre());
        
        Almacen almacen = merma.getProducto().getAlmacen();
        res.setAlmacenNombre(almacen != null ? almacen.getNombre() : "Almacén Central");
        
        res.setCantidad(merma.getCantidad());
        res.setMotivo(merma.getMotivo());
        res.setResponsable(merma.getResponsable());
        res.setObservaciones(merma.getObservaciones());
        return res;
    }
}
