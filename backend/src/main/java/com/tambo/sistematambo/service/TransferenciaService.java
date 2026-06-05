package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.CrearTransferenciaRequest;
import com.tambo.sistematambo.dto.DetalleTransferenciaRequest;
import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.model.EstadoTransferencia;
import com.tambo.sistematambo.model.MovimientoInventario;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.model.Transferencia;
import com.tambo.sistematambo.model.TransferenciaDetalle;
import com.tambo.sistematambo.repository.MovimientoInventarioRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.repository.TransferenciaRepository;
import com.tambo.sistematambo.response.TransferenciaDetalleResponse;
import com.tambo.sistematambo.response.TransferenciaResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoService productoService;
    private final AlmacenService almacenService;

    public TransferenciaService(
            TransferenciaRepository transferenciaRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            ProductoService productoService,
            AlmacenService almacenService) {
        this.transferenciaRepository = transferenciaRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoService = productoService;
        this.almacenService = almacenService;
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponse> listar() {
        return transferenciaRepository.findAllByOrderByFechaDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public TransferenciaResponse crear(CrearTransferenciaRequest request) {
        Almacen origen = almacenService.buscarEntidad(request.almacenOrigenId());
        Almacen destino = almacenService.buscarEntidad(request.almacenDestinoId());

        if (origen.getId().equals(destino.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El almacen destino debe ser distinto al origen");
        }

        Transferencia transferencia = new Transferencia();
        transferencia.setAlmacenOrigen(origen);
        transferencia.setAlmacenDestino(destino);
        transferencia.setFecha(LocalDateTime.now());
        transferencia.setEstado(EstadoTransferencia.PENDIENTE.name());
        transferencia.setResponsable(textoOValor(request.responsable(), "SISTEMA"));
        transferencia.setReferencia(textoOValor(
                request.referencia(),
                "Transferencia " + origen.getNombre() + " a " + destino.getNombre()));

        for (DetalleTransferenciaRequest detalleRequest : request.detalles()) {
            Producto productoOrigen = productoService.buscarEntidad(detalleRequest.productoId());
            validarProductoOrigen(productoOrigen, origen);

            TransferenciaDetalle detalle = new TransferenciaDetalle();
            detalle.setTransferencia(transferencia);
            detalle.setProducto(productoOrigen);
            detalle.setCantidad(detalleRequest.cantidad());
            transferencia.getDetalles().add(detalle);
        }

        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse enviar(Long transferenciaId) {
        Transferencia transferencia = buscarEntidad(transferenciaId);
        validarEstado(transferencia, EstadoTransferencia.PENDIENTE, "Solo se pueden enviar transferencias pendientes");

        for (TransferenciaDetalle detalle : transferencia.getDetalles()) {
            Producto productoOrigen = detalle.getProducto();
            validarProductoOrigen(productoOrigen, transferencia.getAlmacenOrigen());

            if (productoOrigen.getStock() < detalle.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para " + productoOrigen.getNombre());
            }

            productoOrigen.setStock(productoOrigen.getStock() - detalle.getCantidad());
            productoRepository.save(productoOrigen);
            registrarMovimiento(transferencia, productoOrigen, "Salida transferencia", detalle.getCantidad());
        }

        transferencia.setEstado(EstadoTransferencia.ENVIADA.name());
        transferencia.setFechaEnvio(LocalDateTime.now());
        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse recibir(Long transferenciaId) {
        Transferencia transferencia = buscarEntidad(transferenciaId);
        validarEstado(transferencia, EstadoTransferencia.ENVIADA, "Solo se pueden recibir transferencias enviadas");

        for (TransferenciaDetalle detalle : transferencia.getDetalles()) {
            Producto productoDestino = productoRepository
                    .findBySkuAndAlmacenId(detalle.getProducto().getSku(), transferencia.getAlmacenDestino().getId())
                    .map(existing -> {
                        existing.setStock(existing.getStock() + detalle.getCantidad());
                        return existing;
                    })
                    .orElseGet(() -> clonarProductoEnDestino(
                            detalle.getProducto(),
                            transferencia.getAlmacenDestino(),
                            detalle.getCantidad()));

            productoRepository.save(productoDestino);
            registrarMovimiento(transferencia, productoDestino, "Recepcion transferencia", detalle.getCantidad());
        }

        transferencia.setEstado(EstadoTransferencia.RECIBIDA.name());
        transferencia.setFechaRecepcion(LocalDateTime.now());
        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse rechazar(Long transferenciaId) {
        Transferencia transferencia = buscarEntidad(transferenciaId);
        validarEstado(transferencia, EstadoTransferencia.PENDIENTE, "Solo se pueden rechazar transferencias pendientes");
        transferencia.setEstado(EstadoTransferencia.RECHAZADA.name());
        return toResponse(transferenciaRepository.save(transferencia));
    }

    private Transferencia buscarEntidad(Long transferenciaId) {
        return transferenciaRepository.findById(transferenciaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transferencia no encontrada"));
    }

    private void validarProductoOrigen(Producto producto, Almacen origen) {
        if (!producto.getAlmacen().getId().equals(origen.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no pertenece al almacen origen");
        }
    }

    private void validarEstado(Transferencia transferencia, EstadoTransferencia estado, String mensaje) {
        if (!estado.name().equals(transferencia.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private void registrarMovimiento(
            Transferencia transferencia,
            Producto producto,
            String tipo,
            Integer cantidad) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setAlmacenOrigen(transferencia.getAlmacenOrigen());
        movimiento.setAlmacenDestino(transferencia.getAlmacenDestino());
        movimiento.setReferencia(transferencia.getReferencia());
        movimientoInventarioRepository.save(movimiento);
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

    private TransferenciaResponse toResponse(Transferencia transferencia) {
        return new TransferenciaResponse(
                transferencia.getId(),
                transferencia.getAlmacenOrigen().getId(),
                transferencia.getAlmacenOrigen().getNombre(),
                transferencia.getAlmacenDestino().getId(),
                transferencia.getAlmacenDestino().getNombre(),
                transferencia.getFecha(),
                transferencia.getEstado(),
                transferencia.getResponsable(),
                transferencia.getReferencia(),
                transferencia.getDetalles().stream()
                        .map(detalle -> new TransferenciaDetalleResponse(
                                detalle.getProducto().getId(),
                                detalle.getProducto().getNombre(),
                                detalle.getProducto().getSku(),
                                detalle.getCantidad()))
                        .toList());
    }

    private String textoOValor(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
