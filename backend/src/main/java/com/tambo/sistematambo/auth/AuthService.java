package com.tambo.sistematambo.auth;

import com.tambo.sistematambo.modulo.Modulo;
import com.tambo.sistematambo.perfil.Perfil;
import com.tambo.sistematambo.user.User;
import com.tambo.sistematambo.user.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByCorreoOrUsuario(request.username(), request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (user.getEstado() == null || user.getEstado() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }

        boolean passwordOk = passwordEncoder.matches(request.password(), user.getClave())
                || request.password().equals(user.getClave());

        if (!passwordOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        if (request.password().equals(user.getClave())) {
            user.setClave(passwordEncoder.encode(request.password()));
            userRepository.save(user);
        }

        Perfil perfil = user.getPerfil();
        List<String> modulos = perfil == null
                ? List.of()
                : perfil.getModulos().stream().map(Modulo::getNombre).toList();

        return new AuthResponse(
                user.getId(),
                user.getUsuario(),
                user.getCorreo(),
                user.getNombre(),
                perfil != null ? perfil.getNombre() : null,
                modulos,
                "Login correcto");
    }
}
