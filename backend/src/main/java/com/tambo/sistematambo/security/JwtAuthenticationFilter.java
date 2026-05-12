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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        System.out.println("========== JWT FILTER ==========");
        System.out.println("URI => " + request.getRequestURI());

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        System.out.println("HEADER => " + header);

        if (header == null || !header.startsWith("Bearer ")) {

            System.out.println("SIN TOKEN");

            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {

            System.out.println("TOKEN RECIBIDO");

            boolean isAccess = jwtTokenService.isAccessToken(token);

            System.out.println("IS ACCESS TOKEN => " + isAccess);

            if (!isAccess) {

                System.out.println("TOKEN INVALIDO");

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

                return;
            }

            String username =
                    jwtTokenService.extractClaims(token).getSubject();

            System.out.println("USERNAME => " + username);

            User user = userRepository
                    .findByCorreoOrUsuario(username, username)
                    .orElse(null);

            if (user == null) {

                System.out.println("USUARIO NO ENCONTRADO");

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

                return;
            }

            System.out.println("USUARIO OK");

            if (user.getEstado() == null || user.getEstado() != 1) {

                System.out.println("USUARIO INACTIVO");

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

            System.out.println("ROLE => " + role);

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

            System.out.println("AUTHENTICATION OK");

            filterChain.doFilter(request, response);

        } catch (Exception ex) {

            System.out.println("ERROR JWT");
            ex.printStackTrace();

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}