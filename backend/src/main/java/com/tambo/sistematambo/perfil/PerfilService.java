package com.tambo.sistematambo.perfil;

import com.tambo.sistematambo.modulo.Modulo;
import com.tambo.sistematambo.modulo.ModuloRepository;
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

    public PerfilService(PerfilRepository perfilRepository, ModuloRepository moduloRepository) {
        this.perfilRepository = perfilRepository;
        this.moduloRepository = moduloRepository;
    }

    public List<PerfilResponse> listar() {
        return perfilRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PerfilResponse crear(PerfilRequest request) {
        if (perfilRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil ya existe");
        }

        Perfil perfil = new Perfil();
        perfil.setNombre(request.nombre().trim());
        perfil.setDescripcion(request.descripcion());
        perfil.setEstado(true);

        return toResponse(perfilRepository.save(perfil));
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
