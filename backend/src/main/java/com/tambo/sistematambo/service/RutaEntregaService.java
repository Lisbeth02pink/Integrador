package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.RutaEntregaRequest;
import com.tambo.sistematambo.model.PedidoInterno;
import com.tambo.sistematambo.model.PedidoInternoItem;
import com.tambo.sistematambo.model.RutaEntrega;
import com.tambo.sistematambo.model.Transferencia;
import com.tambo.sistematambo.repository.RutaEntregaRepository;
import com.tambo.sistematambo.repository.TransferenciaRepository;
import com.tambo.sistematambo.response.RutaEntregaResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RutaEntregaService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "pendiente", "preparado", "cargando", "en ruta", "detenido", "retrasado", "entregado", "cancelado");
    private static final Set<String> ESTADOS_SALIDA_ACTIVA = Set.of("cargando", "en ruta", "detenido", "retrasado", "entregado");

    private final RutaEntregaRepository rutaEntregaRepository;
    private final PedidoInternoService pedidoInternoService;
    private final TransferenciaRepository transferenciaRepository;

    public RutaEntregaService(
            RutaEntregaRepository rutaEntregaRepository,
            PedidoInternoService pedidoInternoService,
            TransferenciaRepository transferenciaRepository) {
        this.rutaEntregaRepository = rutaEntregaRepository;
        this.pedidoInternoService = pedidoInternoService;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<RutaEntregaResponse> listar() {
        return rutaEntregaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RutaEntregaResponse crear(RutaEntregaRequest request) {
        RutaEntrega ruta = new RutaEntrega();
        mapRequest(ruta, request);
        return toResponse(rutaEntregaRepository.save(ruta));
    }

    @Transactional
    public RutaEntregaResponse actualizar(Long rutaId, RutaEntregaRequest request) {
        RutaEntrega ruta = rutaEntregaRepository.findById(rutaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));
        mapRequest(ruta, request);
        return toResponse(rutaEntregaRepository.save(ruta));
    }

    public void eliminar(Long rutaId) {
        if (!rutaEntregaRepository.existsById(rutaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada");
        }
        rutaEntregaRepository.deleteById(rutaId);
    }

    private void mapRequest(RutaEntrega ruta, RutaEntregaRequest request) {
        PedidoInterno pedido = request.pedidoId() != null
                ? pedidoInternoService.buscarEntidad(request.pedidoId())
                : null;
        Transferencia transferencia = request.transferenciaId() != null
                ? transferenciaRepository.findById(request.transferenciaId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La transferencia indicada no existe"))
                : null;

        String estadoNormalizado = normalizarEstado(request.estado());
        validarReglas(request, pedido, transferencia, estadoNormalizado);

        ruta.setNombre(request.nombre().trim());
        ruta.setZona(request.zona().trim());
        ruta.setRepartidor(request.repartidor().trim());
        ruta.setPedidoInterno(pedido);
        ruta.setTransferencia(transferencia);
        ruta.setVehiculo(safeTrim(request.vehiculo()));
        ruta.setTipoVehiculo(safeTrim(request.tipoVehiculo()));
        ruta.setPlaca(request.placa() != null ? request.placa().trim().toUpperCase() : null);
        ruta.setCapacidadVehiculo(request.capacidadVehiculo());
        ruta.setCantidadCarga(request.cantidadCarga());
        ruta.setOrigen(safeTrim(request.origen()));
        ruta.setDestino(safeTrim(request.destino()));
        ruta.setFechaEntrega(request.fechaEntrega());
        ruta.setPedidos(request.pedidos());
        ruta.setEstado(estadoNormalizado);
        ruta.setHoraSalida(request.horaSalida().trim());
        ruta.setHoraEstimadaLlegada(safeTrim(request.horaEstimadaLlegada()));
        ruta.setHoraEntregaReal(safeTrim(request.horaEntregaReal()));
        ruta.setUbicacionActual(safeTrim(request.ubicacionActual()));
        ruta.setObservaciones(safeTrim(request.observaciones()));
        ruta.setIncidencias(safeTrim(request.incidencias()));
        ruta.setEstadoGps(safeTrim(request.estadoGps()));
        ruta.setEvidenciaEntrega(safeTrim(request.evidenciaEntrega()));
        ruta.setFirmaDigital(safeTrim(request.firmaDigital()));
        ruta.setFotoEntrega(safeTrim(request.fotoEntrega()));
        ruta.setVehiculoActivo(request.vehiculoActivo() == null ? Boolean.TRUE : request.vehiculoActivo());
        ruta.setConductorBloqueado(request.conductorBloqueado() == null ? Boolean.FALSE : request.conductorBloqueado());
        ruta.setConfirmacionEntrega(request.confirmacionEntrega() == null ? Boolean.FALSE : request.confirmacionEntrega());
        ruta.setProgreso(request.progreso());
    }

    private void validarReglas(
            RutaEntregaRequest request,
            PedidoInterno pedido,
            Transferencia transferencia,
            String estadoNormalizado) {
        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado logistico no valido para la ruta");
        }

        if (request.fechaEntrega() == null || request.fechaEntrega().isBefore(LocalDate.now().minusDays(1))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de entrega programada no es valida");
        }

        if (request.origen() == null || request.destino() == null
                || request.origen().trim().isBlank() || request.destino().trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta debe tener origen y destino");
        }

        if (request.origen().trim().equalsIgnoreCase(request.destino().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El origen y el destino deben ser diferentes");
        }

        if (Boolean.TRUE.equals(request.conductorBloqueado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede asignar un conductor bloqueado");
        }

        if (ESTADOS_SALIDA_ACTIVA.contains(estadoNormalizado)) {
            if (request.repartidor() == null || request.repartidor().trim().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se permite salida sin conductor asignado");
            }
            if (request.vehiculo() == null || request.vehiculo().trim().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se permite salida sin vehiculo asignado");
            }
            if (Boolean.FALSE.equals(request.vehiculoActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede despachar un vehiculo inactivo");
            }
        }

        if (request.capacidadVehiculo() != null && request.cantidadCarga() != null
                && request.cantidadCarga() > request.capacidadVehiculo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La carga excede la capacidad maxima del vehiculo");
        }

        if (pedido == null && transferencia == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta debe estar asociada a un pedido o una transferencia");
        }

        if (pedido != null) {
            if (pedido.getItems() == null || pedido.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta debe tener productos asociados en el pedido");
            }

            for (PedidoInternoItem item : pedido.getItems()) {
                if (item.getCantidad() == null || item.getCantidad() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta contiene cantidades no validas");
                }
                if (item.getProducto().getStock() < item.getCantidad()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe stock suficiente para despachar el pedido asociado");
                }
            }
        }

        if (transferencia != null && (transferencia.getDetalles() == null || transferencia.getDetalles().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La transferencia asociada no tiene productos para enrutar");
        }

        if ("entregado".equals(estadoNormalizado)) {
            if (!Boolean.TRUE.equals(request.confirmacionEntrega())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cerrar la ruta sin confirmacion de entrega");
            }
            if (request.horaEntregaReal() == null || request.horaEntregaReal().trim().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe registrar la hora real de entrega");
            }
        }
    }

    private String normalizarEstado(String estado) {
        return estado == null ? "" : estado.trim().toLowerCase();
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private RutaEntregaResponse toResponse(RutaEntrega ruta) {
        return new RutaEntregaResponse(
                ruta.getId(),
                ruta.getNombre(),
                ruta.getZona(),
                ruta.getRepartidor(),
                ruta.getPedidoInterno() != null ? ruta.getPedidoInterno().getId() : null,
                ruta.getTransferencia() != null ? ruta.getTransferencia().getId() : null,
                ruta.getVehiculo(),
                ruta.getTipoVehiculo(),
                ruta.getPlaca(),
                ruta.getCapacidadVehiculo(),
                ruta.getCantidadCarga(),
                ruta.getOrigen(),
                ruta.getDestino(),
                ruta.getFechaEntrega() != null ? ruta.getFechaEntrega().toString() : null,
                ruta.getPedidos(),
                ruta.getEstado(),
                ruta.getHoraSalida(),
                ruta.getHoraEstimadaLlegada(),
                ruta.getHoraEntregaReal(),
                ruta.getUbicacionActual(),
                ruta.getObservaciones(),
                ruta.getIncidencias(),
                ruta.getEstadoGps(),
                ruta.getEvidenciaEntrega(),
                ruta.getFirmaDigital(),
                ruta.getFotoEntrega(),
                ruta.getVehiculoActivo(),
                ruta.getConductorBloqueado(),
                ruta.getConfirmacionEntrega(),
                ruta.getProgreso());
    }
}
