package com.tambo.sistematambo.user;

import com.tambo.sistematambo.perfil.Perfil;
import com.tambo.sistematambo.perfil.PerfilRepository;
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
        if (userRepository.existsByUsuarioIgnoreCase(request.usuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya existe");
        }

        if (userRepository.existsByCorreoIgnoreCase(request.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya existe");
        }

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
