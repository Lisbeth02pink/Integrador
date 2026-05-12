package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.ProveedorEntregaRequest;
import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.model.MovimientoInventario;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.model.Proveedor;
import com.tambo.sistematambo.model.ProveedorEntrega;
import com.tambo.sistematambo.repository.MovimientoInventarioRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.repository.ProveedorEntregaRepository;
import com.tambo.sistematambo.repository.ProveedorRepository;
import com.tambo.sistematambo.response.ProveedorEntregaResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProveedorEntregaService {

    private final ProveedorEntregaRepository proveedorEntregaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoService productoService;
    private final AlmacenService almacenService;

    public ProveedorEntregaService(
            ProveedorEntregaRepository proveedorEntregaRepository,
            ProveedorRepository proveedorRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            ProductoService productoService,
            AlmacenService almacenService) {
        this.proveedorEntregaRepository = proveedorEntregaRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoService = productoService;
        this.almacenService = almacenService;
    }

    @Transactional(readOnly = true)
    public List<ProveedorEntregaResponse> listar() {
        return proveedorEntregaRepository.findAllByOrderByFechaEntregaDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProveedorEntregaResponse registrar(ProveedorEntregaRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        Producto producto = productoService.buscarEntidad(request.productoId());
        Almacen almacenDestino = almacenService.buscarEntidad(request.almacenDestinoId());

        validarDestinoCentral(almacenDestino);

        if (!producto.getAlmacen().getId().equals(almacenDestino.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El producto seleccionado debe pertenecer al almacen central que recibe la entrega");
        }

        producto.setStock(producto.getStock() + request.cantidad());
        productoRepository.save(producto);

        ProveedorEntrega entrega = new ProveedorEntrega();
        entrega.setProveedor(proveedor);
        entrega.setProducto(producto);
        entrega.setAlmacenDestino(almacenDestino);
        entrega.setCantidad(request.cantidad());
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setObservaciones(request.observaciones() != null ? request.observaciones().trim() : "");
        ProveedorEntrega saved = proveedorEntregaRepository.save(entrega);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFecha(saved.getFechaEntrega());
        movimiento.setProducto(producto);
        movimiento.setTipo("Ingreso");
        movimiento.setCantidad(request.cantidad());
        movimiento.setAlmacenOrigen(null);
        movimiento.setAlmacenDestino(almacenDestino);
        movimiento.setReferencia("Ingreso proveedor " + proveedor.getRazonSocial());
        movimientoInventarioRepository.save(movimiento);

        return toResponse(saved);
    }

    private void validarDestinoCentral(Almacen almacenDestino) {
        if (!almacenDestino.getNombre().toLowerCase().contains("central")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Las entregas de proveedor solo pueden registrarse hacia el almacen central");
        }
    }

    private ProveedorEntregaResponse toResponse(ProveedorEntrega entrega) {
        return new ProveedorEntregaResponse(
                entrega.getId(),
                entrega.getProveedor().getId(),
                entrega.getProveedor().getRazonSocial(),
                entrega.getProducto().getId(),
                entrega.getProducto().getNombre(),
                entrega.getProducto().getSku(),
                entrega.getAlmacenDestino().getNombre(),
                entrega.getCantidad(),
                entrega.getFechaEntrega(),
                entrega.getObservaciones());
    }
}
