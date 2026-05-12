package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.TransferenciaInventarioRequest;
import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.model.MovimientoInventario;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.model.Transferencia;
import com.tambo.sistematambo.model.TransferenciaDetalle;
import com.tambo.sistematambo.repository.MovimientoInventarioRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.repository.TransferenciaRepository;
import com.tambo.sistematambo.response.InventarioResumenResponse;
import com.tambo.sistematambo.response.MovimientoInventarioResponse;
import com.tambo.sistematambo.response.ProductoResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final ProductoService productoService;
    private final AlmacenService almacenService;

    public InventarioService(
            MovimientoInventarioRepository movimientoInventarioRepository,
            ProductoRepository productoRepository,
            TransferenciaRepository transferenciaRepository,
            ProductoService productoService,
            AlmacenService almacenService) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.productoService = productoService;
        this.almacenService = almacenService;
    }

    public List<MovimientoInventarioResponse> listarMovimientos() {
        return movimientoInventarioRepository.findAllByOrderByFechaDesc().stream().map(this::toResponse).toList();
    }

    public List<ProductoResponse> listarStockBajo() {
        return productoRepository.findAll().stream()
                .filter(producto -> producto.getStock() <= producto.getStockMinimo())
                .map(productoService::toResponse)
                .toList();
    }

    public List<InventarioResumenResponse> resumenPorAlmacen() {
        return almacenService.listar().stream()
                .map(almacen -> {
                    List<Producto> productos = productoRepository.findAllByAlmacenId(almacen.id());
                    long totalStock = productos.stream().mapToLong(Producto::getStock).sum();
                    return new InventarioResumenResponse(almacen, (long) productos.size(), totalStock);
                })
                .toList();
    }

    @Transactional
    public MovimientoInventarioResponse registrarTransferencia(TransferenciaInventarioRequest request) {
        return registrarTransferenciaInterna(
                request.productoId(),
                request.almacenOrigenId(),
                request.almacenDestinoId(),
                request.cantidad(),
                null);
    }

    @Transactional
    public MovimientoInventarioResponse registrarTransferenciaInterna(
            Long productoId,
            Long almacenOrigenId,
            Long almacenDestinoId,
            Integer cantidad,
            String referencia) {
        Producto productoOrigen = productoService.buscarEntidad(productoId);
        Almacen origen = almacenService.buscarEntidad(almacenOrigenId);
        Almacen destino = almacenService.buscarEntidad(almacenDestinoId);
        String referenciaFinal = referencia != null && !referencia.isBlank()
                ? referencia
                : "Transferencia " + origen.getNombre() + " a " + destino.getNombre();

        if (origen.getId().equals(destino.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El almacen destino debe ser distinto al origen");
        }

        if (!productoOrigen.getAlmacen().getId().equals(origen.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no pertenece al almacen origen");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad a transferir debe ser mayor a cero");
        }

        if (productoOrigen.getStock() < cantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para la transferencia");
        }

        productoOrigen.setStock(productoOrigen.getStock() - cantidad);
        productoRepository.save(productoOrigen);

        Producto productoDestino = productoRepository.findBySkuAndAlmacenId(productoOrigen.getSku(), destino.getId())
                .map(existing -> {
                    existing.setStock(existing.getStock() + cantidad);
                    return existing;
                })
                .orElseGet(() -> clonarProductoEnDestino(productoOrigen, destino, cantidad));

        productoRepository.save(productoDestino);

        Transferencia transferencia = new Transferencia();
        transferencia.setFecha(LocalDateTime.now());
        transferencia.setAlmacenOrigen(origen);
        transferencia.setAlmacenDestino(destino);
        transferencia.setEstado("COMPLETADA");
        transferencia.setResponsable("SISTEMA");
        transferencia.setReferencia(referenciaFinal);

        TransferenciaDetalle detalle = new TransferenciaDetalle();
        detalle.setTransferencia(transferencia);
        detalle.setProducto(productoDestino);
        detalle.setCantidad(cantidad);
        transferencia.getDetalles().add(detalle);
        transferenciaRepository.save(transferencia);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setProducto(productoDestino);
        movimiento.setTipo("Transferencia");
        movimiento.setCantidad(cantidad);
        movimiento.setAlmacenOrigen(origen);
        movimiento.setAlmacenDestino(destino);
        movimiento.setReferencia(referenciaFinal);

        return toResponse(movimientoInventarioRepository.save(movimiento));
    }

    private Producto clonarProductoEnDestino(Producto origen, Almacen destino, Integer cantidad) {
        Producto producto = new Producto();
        producto.setNombre(origen.getNombre());
        producto.setSku(origen.getSku());
        producto.setPrecioCompra(origen.getPrecioCompra());
        producto.setPrecioVenta(origen.getPrecioVenta());
        producto.setStock(cantidad);
        producto.setStockMinimo(origen.getStockMinimo());
        producto.setCategoria(origen.getCategoria());
        producto.setAlmacen(destino);
        producto.setImagen(origen.getImagen());
        producto.setEstado(origen.getEstado());
        return producto;
    }

    MovimientoInventarioResponse toResponse(MovimientoInventario movimiento) {
        return new MovimientoInventarioResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getSku(),
                movimiento.getProducto().getNombre(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getAlmacenOrigen() != null ? movimiento.getAlmacenOrigen().getNombre() : null,
                movimiento.getAlmacenDestino() != null ? movimiento.getAlmacenDestino().getNombre() : null,
                movimiento.getReferencia());
    }
}
