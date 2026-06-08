package com.tambo.sistematambo.security;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenHashService {

    public String hash(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            return "";
        }

        return Hashing.sha256()
                .hashString(refreshToken, StandardCharsets.UTF_8)
                .toString();
    }

    public boolean matches(String rawToken, String expectedHash) {
        if (StringUtils.isBlank(rawToken) || StringUtils.isBlank(expectedHash)) {
            return false;
        }

        byte[] calculated = hash(rawToken).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculated, expected);
    }
}
