package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.PerfilPermisosRequest;
import com.tambo.sistematambo.dto.PerfilRequest;
import com.tambo.sistematambo.model.Modulo;
import com.tambo.sistematambo.model.Perfil;
import com.tambo.sistematambo.repository.ModuloRepository;
import com.tambo.sistematambo.repository.PerfilRepository;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.response.PerfilResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final ModuloRepository moduloRepository;
    private final UserRepository userRepository;

    public PerfilService(
            PerfilRepository perfilRepository,
            ModuloRepository moduloRepository,
            UserRepository userRepository) {
        this.perfilRepository = perfilRepository;
        this.moduloRepository = moduloRepository;
        this.userRepository = userRepository;
    }

    public List<PerfilResponse> listar() {
        return perfilRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PerfilResponse crear(PerfilRequest request) {
        validarNombreDuplicado(null, request.nombre());

        Perfil perfil = new Perfil();
        perfil.setNombre(request.nombre().trim());
        perfil.setDescripcion(request.descripcion());
        perfil.setEstado(true);

        return toResponse(perfilRepository.save(perfil));
    }

    public PerfilResponse actualizar(Long perfilId, PerfilRequest request) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        validarNombreDuplicado(perfilId, request.nombre());

        perfil.setNombre(request.nombre().trim());
        perfil.setDescripcion(request.descripcion());

        return toResponse(perfilRepository.save(perfil));
    }

    public PerfilResponse cambiarEstado(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        perfil.setEstado(!perfil.isEstado());
        return toResponse(perfilRepository.save(perfil));
    }

    public void eliminar(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        if (userRepository.countByPerfilId(perfilId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede eliminar el perfil porque tiene usuarios asignados");
        }

        perfilRepository.delete(perfil);
    }

    @Transactional
    public PerfilResponse actualizarPermisos(Long perfilId, PerfilPermisosRequest request) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        Set<Modulo> modulos = new LinkedHashSet<>(moduloRepository.findAllById(request.moduloIds()));
        if (modulos.size() != request.moduloIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o mas modulos no existen");
        }

        perfil.getModulos().clear();
        perfil.getModulos().addAll(modulos);

        Perfil perfilActualizado = perfilRepository.saveAndFlush(perfil);
        return toResponse(perfilActualizado);
    }

    private void validarNombreDuplicado(Long perfilId, String nombre) {
        boolean duplicado = perfilId == null
                ? perfilRepository.existsByNombreIgnoreCase(nombre)
                : perfilRepository.existsByNombreIgnoreCaseAndIdNot(nombre, perfilId);

        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil ya existe");
        }
    }

    private PerfilResponse toResponse(Perfil perfil) {
        List<Long> moduloIds = perfil.getModulos().stream().map(Modulo::getId).toList();
        List<String> modulos = perfil.getModulos().stream().map(Modulo::getNombre).toList();

        return new PerfilResponse(
                perfil.getId(),
                perfil.getNombre(),
                perfil.getDescripcion(),
                perfil.isEstado(),
                moduloIds,
                modulos);
    }
}
