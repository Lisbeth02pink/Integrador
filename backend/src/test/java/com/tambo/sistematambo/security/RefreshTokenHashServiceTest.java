package com.tambo.sistematambo.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RefreshTokenHashServiceTest {

    private final RefreshTokenHashService refreshTokenHashService = new RefreshTokenHashService();

    @Test
    void hashDoesNotExposeRawRefreshToken() {
        String rawToken = "refresh-token-value";

        String hash = refreshTokenHashService.hash(rawToken);

        assertThat(hash).isNotBlank();
        assertThat(hash).isNotEqualTo(rawToken);
        assertThat(hash).hasSize(64);
    }

    @Test
    void matchesOnlyAcceptsOriginalRefreshToken() {
        String rawToken = "refresh-token-value";
        String hash = refreshTokenHashService.hash(rawToken);

        assertThat(refreshTokenHashService.matches(rawToken, hash)).isTrue();
        assertThat(refreshTokenHashService.matches("another-token", hash)).isFalse();
    }
}
