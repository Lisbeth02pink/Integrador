package com.tambo.sistematambo.security;

import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            UserRepository userRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {

            boolean isAccess = jwtTokenService.isAccessToken(token);

            if (!isAccess) {
                logger.warn("JWT rechazado por tipo invalido en {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username =
                    jwtTokenService.extractClaims(token).getSubject();

            User user = userRepository
                    .findByCorreoOrUsuario(username, username)
                    .orElse(null);

            if (user == null) {
                logger.warn("JWT valido para usuario inexistente");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (user.getEstado() == null || user.getEstado() != 1) {
                logger.warn("JWT rechazado por usuario inactivo");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String role = "ROLE_USER";

            if (user.getPerfil() != null &&
                    user.getPerfil().getNombre() != null) {

                role = "ROLE_" +
                        user.getPerfil()
                                .getNombre()
                        .replace(" ", "_")
                                .toUpperCase();
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            logger.warn("JWT rechazado por validacion fallida en {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
