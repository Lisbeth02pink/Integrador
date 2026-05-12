package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.AuthLogoutRequest;
import com.tambo.sistematambo.dto.AuthRefreshRequest;
import com.tambo.sistematambo.dto.AuthRequest;
import com.tambo.sistematambo.model.Modulo;
import com.tambo.sistematambo.model.Perfil;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.response.AuthResponse;
import com.tambo.sistematambo.security.JwtTokenService;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse login(AuthRequest request) {

        try {

            System.out.println("========== LOGIN ==========");
            System.out.println("REQUEST USERNAME: " + request.username());
            System.out.println("REQUEST PASSWORD: " + request.password());

            User user = userRepository.findByCorreoOrUsuario(
                    request.username(),
                    request.username()
            ).orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Credenciales incorrectas"
                    )
            );

            System.out.println("USUARIO ENCONTRADO: " + user.getUsuario());
            System.out.println("ESTADO: " + user.getEstado());
            System.out.println("CLAVE BD: " + user.getClave());

            validarUsuarioActivo(user);
            validarBloqueo(user);

            boolean passwordOk = passwordEncoder.matches(
                    request.password(),
                    user.getClave()
            );

            System.out.println("PASSWORD OK: " + passwordOk);

            if (!passwordOk) {

                registrarIntentoFallido(user);

                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales incorrectas"
                );
            }

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);

            return emitirTokens(user, "Login correcto");

        } catch (Exception e) {

            e.printStackTrace();

            throw e;
        }
    }

    public AuthResponse refresh(AuthRefreshRequest request) {

        try {

            if (!jwtTokenService.isRefreshToken(request.refreshToken())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Refresh token invalido"
                );
            }

        } catch (JwtException ex) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token invalido"
            );
        }

        String username = jwtTokenService
                .extractClaims(request.refreshToken())
                .getSubject();

        User user = userRepository.findByCorreoOrUsuario(
                username,
                username
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                )
        );

        validarUsuarioActivo(user);

        if (user.getRefreshTokenHash() == null
                || user.getRefreshTokenExpiresAt() == null
                || user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())
                || !request.refreshToken().equals(user.getRefreshTokenHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expirado o invalido"
            );
        }

        return emitirTokens(user, "Sesion renovada");
    }

    public void logout(AuthLogoutRequest request) {

        try {

            if (!jwtTokenService.isRefreshToken(request.refreshToken())) {
                return;
            }

            String username = jwtTokenService
                    .extractClaims(request.refreshToken())
                    .getSubject();

            userRepository.findByCorreoOrUsuario(username, username)
                    .ifPresent(user -> {

                        user.setRefreshTokenHash(null);
                        user.setRefreshTokenExpiresAt(null);

                        userRepository.save(user);
                    });

        } catch (JwtException ignored) {
            // logout idempotente
        }
    }

    private AuthResponse emitirTokens(User user, String message) {

        Perfil perfil = user.getPerfil();

        List<String> modulos = perfil == null
                ? List.of()
                : perfil.getModulos()
                        .stream()
                        .map(Modulo::getNombre)
                        .toList();

        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getUsuario(),
                perfil != null ? perfil.getNombre() : null,
                modulos
        );

        String refreshToken = jwtTokenService.generateRefreshToken(
                user.getId(),
                user.getUsuario()
        );

        long expiresAt = System.currentTimeMillis()
                + jwtTokenService.getAccessExpirationMs();

        // CAMBIO IMPORTANTE
        user.setRefreshTokenHash(refreshToken);

        user.setRefreshTokenExpiresAt(
                LocalDateTime.now().plusNanos(
                        jwtTokenService.getRefreshExpirationMs() * 1_000_000
                )
        );

        userRepository.save(user);

        return new AuthResponse(
                user.getId(),
                user.getUsuario(),
                user.getCorreo(),
                user.getNombre(),
                perfil != null ? perfil.getNombre() : null,
                modulos,
                accessToken,
                refreshToken,
                expiresAt,
                message
        );
    }

    private void validarUsuarioActivo(User user) {

        if (user.getEstado() == null || user.getEstado() != 1) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario inactivo"
            );
        }
    }

    private void validarBloqueo(User user) {

        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Usuario bloqueado temporalmente por intentos fallidos"
            );
        }
    }

    private void registrarIntentoFallido(User user) {

        int attempts = (
                user.getFailedLoginAttempts() == null
                        ? 0
                        : user.getFailedLoginAttempts()
        ) + 1;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {

            user.setLockedUntil(
                    LocalDateTime.now().plusMinutes(15)
            );

            user.setFailedLoginAttempts(0);
        }

        userRepository.save(user);
    }
}