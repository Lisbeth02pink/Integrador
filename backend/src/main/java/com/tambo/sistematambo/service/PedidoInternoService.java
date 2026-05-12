package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.PedidoInternoEstadoRequest;
import com.tambo.sistematambo.dto.PedidoInternoItemRequest;
import com.tambo.sistematambo.dto.PedidoInternoRequest;
import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.model.PedidoInterno;
import com.tambo.sistematambo.model.PedidoInternoItem;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.repository.PedidoInternoRepository;
import com.tambo.sistematambo.response.PedidoInternoItemResponse;
import com.tambo.sistematambo.response.PedidoInternoResponse;
import com.tambo.sistematambo.response.MovimientoInventarioResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoInternoService {

    private static final List<String> ESTADOS_VALIDOS = List.of("Pendiente", "Aprobado", "Preparando", "En ruta", "Entregado");
    private static final List<String> PRIORIDADES_VALIDAS = List.of("Alta", "Media", "Baja");

    private final PedidoInternoRepository pedidoInternoRepository;
    private final AlmacenService almacenService;
    private final ProductoService productoService;
    private final InventarioService inventarioService;

    public PedidoInternoService(
            PedidoInternoRepository pedidoInternoRepository,
            AlmacenService almacenService,
            ProductoService productoService,
            InventarioService inventarioService) {
        this.pedidoInternoRepository = pedidoInternoRepository;
        this.almacenService = almacenService;
        this.productoService = productoService;
        this.inventarioService = inventarioService;
    }

    @Transactional(readOnly = true)
    public List<PedidoInternoResponse> listar() {
        return pedidoInternoRepository.findAllByOrderByFechaSolicitudDesc().stream().map(this::toResponse).toList();
    }

    public PedidoInterno buscarEntidad(Long pedidoId) {
        return pedidoInternoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido interno no encontrado"));
    }

    @Transactional
    public PedidoInternoResponse crear(PedidoInternoRequest request) {
        validarPrioridad(request.prioridad());
        Almacen tienda = almacenService.buscarEntidad(request.tiendaId());
        validarTienda(tienda);

        PedidoInterno pedido = new PedidoInterno();
        pedido.setTienda(tienda);
        pedido.setSolicitadoPor(request.solicitadoPor().trim());
        pedido.setPrioridad(request.prioridad().trim());
        pedido.setFechaSolicitud(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setTransferenciaGenerada(false);
        pedido.setObservaciones(request.observaciones() != null ? request.observaciones().trim() : "");

        for (PedidoInternoItemRequest itemRequest : request.items()) {
            Producto producto = productoService.buscarEntidad(itemRequest.productoId());
            PedidoInternoItem item = crearItem(pedido, producto, itemRequest.cantidad());
            pedido.getItems().add(item);
        }

        return toResponse(pedidoInternoRepository.save(pedido));
    }

    @Transactional
    public PedidoInternoResponse actualizarEstado(Long pedidoId, PedidoInternoEstadoRequest request) {
        String estado = request.estado().trim();
        validarEstado(estado);

        PedidoInterno pedido = buscarEntidad(pedidoId);
        String estadoAnterior = pedido.getEstado();

        if (!"Entregado".equals(estadoAnterior) && "Entregado".equals(estado) && !Boolean.TRUE.equals(pedido.getTransferenciaGenerada())) {
            completarEntrega(pedido);
        }

        pedido.setEstado(estado);
        return toResponse(pedidoInternoRepository.save(pedido));
    }

    @Transactional
    public List<MovimientoInventarioResponse> generarTransferencia(Long pedidoId) {
        PedidoInterno pedido = buscarEntidad(pedidoId);

        if (Boolean.TRUE.equals(pedido.getTransferenciaGenerada())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ya tiene una transferencia generada");
        }

        if (!List.of("Aprobado", "Preparando").contains(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede generar transferencia para pedidos aprobados o en preparacion");
        }

        List<MovimientoInventarioResponse> movimientos = completarEntrega(pedido);
        pedido.setTransferenciaGenerada(true);
        if ("Aprobado".equals(pedido.getEstado())) {
            pedido.setEstado("Preparando");
        }
        pedidoInternoRepository.save(pedido);
        return movimientos;
    }

    private List<MovimientoInventarioResponse> completarEntrega(PedidoInterno pedido) {
        List<MovimientoInventarioResponse> movimientos = pedido.getItems().stream()
                .map(item -> inventarioService.registrarTransferenciaInterna(
                    item.getProducto().getId(),
                    item.getProducto().getAlmacen().getId(),
                    pedido.getTienda().getId(),
                    item.getCantidad(),
                    "Pedido interno #" + pedido.getId() + " hacia " + pedido.getTienda().getNombre()))
                .toList();

        pedido.setTransferenciaGenerada(true);
        return movimientos;
    }

    private PedidoInternoItem crearItem(PedidoInterno pedido, Producto producto, Integer cantidad) {
        PedidoInternoItem item = new PedidoInternoItem();
        item.setPedidoInterno(pedido);
        item.setProducto(producto);
        item.setCantidad(cantidad);
        return item;
    }

    private void validarEstado(String estado) {
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de pedido interno no valido");
        }
    }

    private void validarPrioridad(String prioridad) {
        if (!PRIORIDADES_VALIDAS.contains(prioridad.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prioridad de pedido interno no valida");
        }
    }

    private void validarTienda(Almacen tienda) {
        if (tienda.getNombre().toLowerCase().contains("central")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La tienda destino no puede ser el almacen central");
        }
    }

    private PedidoInternoResponse toResponse(PedidoInterno pedido) {
        return new PedidoInternoResponse(
                pedido.getId(),
                pedido.getTienda().getId(),
                pedido.getTienda().getNombre(),
                pedido.getSolicitadoPor(),
                pedido.getPrioridad(),
                pedido.getFechaSolicitud(),
                pedido.getEstado(),
                pedido.getObservaciones(),
                Boolean.TRUE.equals(pedido.getTransferenciaGenerada()),
                pedido.getItems().stream()
                        .map(item -> new PedidoInternoItemResponse(
                                item.getProducto().getId(),
                                item.getProducto().getNombre(),
                                item.getProducto().getSku(),
                                item.getCantidad()))
                        .toList());
    }
}
