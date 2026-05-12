package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.ClienteRequest;
import com.tambo.sistematambo.model.Cliente;
import com.tambo.sistematambo.repository.ClienteRepository;
import com.tambo.sistematambo.response.ClienteResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ClienteResponse crear(ClienteRequest request) {
        validarDocumento(null, request.documento(), request.tipoDocumento());

        Cliente cliente = new Cliente();
        mapRequest(cliente, request);
        cliente.setEstado(1);

        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse actualizar(Long clienteId, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        validarDocumento(clienteId, request.documento(), request.tipoDocumento());
        mapRequest(cliente, request);

        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse cambiarEstado(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        cliente.setEstado(cliente.getEstado() != null && cliente.getEstado() == 1 ? 0 : 1);
        return toResponse(clienteRepository.save(cliente));
    }

    public void eliminar(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }

        clienteRepository.deleteById(clienteId);
    }

    private void validarDocumento(Long clienteId, String documento, String tipoDocumento) {
        boolean formatoValido = ("DNI".equals(tipoDocumento) && documento.matches("\\d{8}"))
                || ("RUC".equals(tipoDocumento) && documento.matches("\\d{11}"));

        if (!formatoValido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El documento no coincide con el tipo seleccionado");
        }

        boolean duplicado = clienteId == null
                ? clienteRepository.existsByDocumento(documento)
                : clienteRepository.existsByDocumentoAndIdNot(documento, clienteId);

        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un cliente con ese documento");
        }
    }

    private void mapRequest(Cliente cliente, ClienteRequest request) {
        cliente.setTipoDocumento(request.tipoDocumento().trim());
        cliente.setDocumento(request.documento().trim());
        cliente.setNombre(request.nombre().trim().toUpperCase());
        cliente.setTelefono(request.telefono() != null ? request.telefono().trim() : "");
        cliente.setCorreo(request.correo() != null ? request.correo().trim() : "");
        cliente.setDireccion(request.direccion() != null ? request.direccion().trim() : "");
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getTipoDocumento(),
                cliente.getDocumento(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getDireccion(),
                cliente.getEstado());
    }
}
