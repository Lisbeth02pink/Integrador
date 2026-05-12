package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.AlmacenRequest;
import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.repository.AlmacenRepository;
import com.tambo.sistematambo.response.AlmacenResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlmacenService {

    private final AlmacenRepository almacenRepository;

    public AlmacenService(AlmacenRepository almacenRepository) {
        this.almacenRepository = almacenRepository;
    }

    public List<AlmacenResponse> listar() {
        return almacenRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AlmacenResponse crear(AlmacenRequest request) {
        Almacen almacen = new Almacen();
        mapRequest(almacen, request);
        return toResponse(almacenRepository.save(almacen));
    }

    public AlmacenResponse actualizar(Long almacenId, AlmacenRequest request) {
        Almacen almacen = almacenRepository.findById(almacenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Almacen no encontrado"));
        mapRequest(almacen, request);
        return toResponse(almacenRepository.save(almacen));
    }

    public void eliminar(Long almacenId) {
        if (!almacenRepository.existsById(almacenId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Almacen no encontrado");
        }
        almacenRepository.deleteById(almacenId);
    }

    public Almacen buscarEntidad(Long almacenId) {
        return almacenRepository.findById(almacenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Almacen no encontrado"));
    }

    AlmacenResponse toResponse(Almacen almacen) {
        return new AlmacenResponse(
                almacen.getId(),
                almacen.getNombre(),
                almacen.getCiudad(),
                almacen.getResponsable(),
                almacen.getDireccion(),
                almacen.getTipo(),
                almacen.getCapacidad(),
                almacen.getOcupacion(),
                almacen.getEstado());
    }

    private void mapRequest(Almacen almacen, AlmacenRequest request) {
        almacen.setNombre(request.nombre().trim());
        almacen.setCiudad(request.ciudad().trim());
        almacen.setResponsable(request.responsable().trim());
        almacen.setDireccion(request.direccion().trim());
        almacen.setTipo(normalizarTipo(request.tipo(), request.nombre()));
        almacen.setCapacidad(request.capacidad());
        almacen.setOcupacion(request.ocupacion());
        almacen.setEstado(request.estado());
    }

    private String normalizarTipo(String tipo, String nombre) {
        if (tipo != null && !tipo.isBlank()) {
            return tipo.trim().toUpperCase();
        }
        return nombre != null && nombre.toLowerCase().contains("central") ? "CENTRAL" : "TIENDA";
    }
}
