package com.backend.ms_security.dto.auth;

import java.time.Instant;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        Boolean requiresTwoFactor,
        String challengeToken
) {
    public AuthResponseDTO(String accessToken, String tokenType, Instant expiresAt) {
        this(accessToken, tokenType, expiresAt, false, null);
    }
}
