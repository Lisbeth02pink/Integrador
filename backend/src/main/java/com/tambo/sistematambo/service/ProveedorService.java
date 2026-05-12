package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.ProveedorRequest;
import com.tambo.sistematambo.model.Proveedor;
import com.tambo.sistematambo.repository.ProveedorRepository;
import com.tambo.sistematambo.response.ProveedorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<ProveedorResponse> listar() {
        return proveedorRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProveedorResponse crear(ProveedorRequest request) {
        validarRuc(null, request.ruc());

        Proveedor proveedor = new Proveedor();
        mapRequest(proveedor, request);
        proveedor.setEstado(1);

        return toResponse(proveedorRepository.save(proveedor));
    }

    public ProveedorResponse actualizar(Long proveedorId, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        validarRuc(proveedorId, request.ruc());
        mapRequest(proveedor, request);

        return toResponse(proveedorRepository.save(proveedor));
    }

    public ProveedorResponse cambiarEstado(Long proveedorId) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        proveedor.setEstado(proveedor.getEstado() != null && proveedor.getEstado() == 1 ? 0 : 1);
        return toResponse(proveedorRepository.save(proveedor));
    }

    public void eliminar(Long proveedorId) {
        if (!proveedorRepository.existsById(proveedorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado");
        }

        proveedorRepository.deleteById(proveedorId);
    }

    private void validarRuc(Long proveedorId, String ruc) {
        String normalized = ruc.trim();
        if (!normalized.matches("\\d{11}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC debe tener 11 digitos");
        }

        boolean duplicado = proveedorId == null
                ? proveedorRepository.existsByRuc(normalized)
                : proveedorRepository.existsByRucAndIdNot(normalized, proveedorId);

        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un proveedor con ese RUC");
        }
    }

    private void mapRequest(Proveedor proveedor, ProveedorRequest request) {
        proveedor.setRuc(request.ruc().trim());
        proveedor.setRazonSocial(request.razonSocial().trim().toUpperCase());
        proveedor.setContacto(request.contacto().trim());
        proveedor.setTelefono(request.telefono() != null ? request.telefono().trim() : "");
        proveedor.setCorreo(request.correo() != null ? request.correo().trim() : "");
        proveedor.setDireccion(request.direccion() != null ? request.direccion().trim() : "");
        proveedor.setProductosSuministrados(request.productosSuministrados() != null ? request.productosSuministrados().trim() : "");
        proveedor.setHistorialEntregas(request.historialEntregas() != null ? request.historialEntregas().trim() : "");
    }

    private ProveedorResponse toResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getRuc(),
                proveedor.getRazonSocial(),
                proveedor.getContacto(),
                proveedor.getTelefono(),
                proveedor.getCorreo(),
                proveedor.getDireccion(),
                proveedor.getProductosSuministrados(),
                proveedor.getHistorialEntregas(),
                proveedor.getEstado());
    }
}
