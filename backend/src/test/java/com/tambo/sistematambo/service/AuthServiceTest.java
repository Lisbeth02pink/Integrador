package com.tambo.sistematambo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tambo.sistematambo.dto.AuthRefreshRequest;
import com.tambo.sistematambo.dto.AuthRequest;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.response.AuthResponse;
import com.tambo.sistematambo.security.JwtTokenService;
import com.tambo.sistematambo.security.RefreshTokenHashService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private RefreshTokenHashService refreshTokenHashService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        refreshTokenHashService = new RefreshTokenHashService();
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenService,
                refreshTokenHashService);
    }

    @Test
    void loginStoresOnlyRefreshTokenHash() {
        User user = activeUser();
        when(userRepository.findByCorreoOrUsuario("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-password")).thenReturn(true);
        when(jwtTokenService.generateAccessToken(any(), anyString(), any(), anyList())).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken(any(), anyString())).thenReturn("refresh-token");
        when(jwtTokenService.getAccessExpirationMs()).thenReturn(3600000L);
        when(jwtTokenService.getRefreshExpirationMs()).thenReturn(604800000L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(new AuthRequest("admin", "secret"));

        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(user.getRefreshTokenHash()).isNotEqualTo("refresh-token");
        assertThat(refreshTokenHashService.matches("refresh-token", user.getRefreshTokenHash())).isTrue();
        verify(userRepository, org.mockito.Mockito.times(2)).save(user);
    }

    @Test
    void refreshAcceptsRawTokenOnlyWhenStoredHashMatches() {
        User user = activeUser();
        user.setRefreshTokenHash(refreshTokenHashService.hash("refresh-token"));
        user.setRefreshTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        Claims claims = Jwts.claims().subject("admin").build();

        when(jwtTokenService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtTokenService.extractClaims("refresh-token")).thenReturn(claims);
        when(userRepository.findByCorreoOrUsuario("admin", "admin")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessToken(any(), anyString(), any(), anyList())).thenReturn("new-access-token");
        when(jwtTokenService.generateRefreshToken(any(), anyString())).thenReturn("new-refresh-token");
        when(jwtTokenService.getAccessExpirationMs()).thenReturn(3600000L);
        when(jwtTokenService.getRefreshExpirationMs()).thenReturn(604800000L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.refresh(new AuthRefreshRequest("refresh-token"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRefreshTokenHash()).isNotEqualTo("new-refresh-token");
        assertThat(refreshTokenHashService.matches("new-refresh-token", captor.getValue().getRefreshTokenHash())).isTrue();
    }

    private User activeUser() {
        return new User(
                "Administrador",
                "admin",
                "encoded-password",
                "admin@tambo.com",
                1);
    }
}
