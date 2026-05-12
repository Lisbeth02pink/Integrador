package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.UserRequest;
import com.tambo.sistematambo.dto.UserUpdateRequest;
import com.tambo.sistematambo.model.Perfil;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.PerfilRepository;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.response.UserResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PerfilRepository perfilRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listar() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse crear(UserRequest request) {
        validarDuplicados(null, request.usuario(), request.correo());

        Perfil perfil = perfilRepository.findById(request.idPerfil())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil no existe"));

        User user = new User();
        user.setNombre(request.nombre().trim());
        user.setUsuario(request.usuario().trim());
        user.setClave(passwordEncoder.encode(request.clave()));
        user.setCorreo(request.correo().trim());
        user.setEstado(1);
        user.setPerfil(perfil);

        return toResponse(userRepository.save(user));
    }

    public UserResponse actualizar(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarDuplicados(userId, request.usuario(), request.correo());

        Perfil perfil = perfilRepository.findById(request.idPerfil())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil no existe"));

        user.setNombre(request.nombre().trim());
        user.setUsuario(request.usuario().trim());
        user.setCorreo(request.correo().trim());
        user.setPerfil(perfil);

        if (request.clave() != null && !request.clave().isBlank()) {
            if (request.clave().trim().length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave debe tener al menos 6 caracteres");
            }
            user.setClave(passwordEncoder.encode(request.clave()));
        }

        return toResponse(userRepository.save(user));
    }

    public UserResponse cambiarEstado(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.setEstado(user.getEstado() != null && user.getEstado() == 1 ? 0 : 1);
        return toResponse(userRepository.save(user));
    }

    public void eliminar(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        userRepository.deleteById(userId);
    }

    private void validarDuplicados(Long userId, String usuario, String correo) {
        boolean usuarioDuplicado = userId == null
                ? userRepository.existsByUsuarioIgnoreCase(usuario)
                : userRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, userId);

        if (usuarioDuplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya existe");
        }

        boolean correoDuplicado = userId == null
                ? userRepository.existsByCorreoIgnoreCase(correo)
                : userRepository.existsByCorreoIgnoreCaseAndIdNot(correo, userId);

        if (correoDuplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya existe");
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNombre(),
                user.getUsuario(),
                user.getCorreo(),
                user.getEstado(),
                user.getPerfil() != null ? user.getPerfil().getId() : null,
                user.getPerfil() != null ? user.getPerfil().getNombre() : null);
    }
}
