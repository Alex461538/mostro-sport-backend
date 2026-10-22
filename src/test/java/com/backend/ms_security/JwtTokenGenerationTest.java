package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.service.JwtService;

class JwtTokenGenerationTest {

    @Test
    void shouldGenerateJwtForAuthenticatedUser() {
        SecretKey secretKey = new SecretKeySpec(
                Base64.getEncoder().encodeToString("clave-local-educativa-de-32-bytes".getBytes(StandardCharsets.UTF_8))
                        .getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtService jwtService = new JwtService(new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(
                new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(
                        new com.nimbusds.jose.jwk.JWKSet(
                                new com.nimbusds.jose.jwk.OctetSequenceKey.Builder(secretKey.getEncoded()).build()
                        )
                )
        ));

        User user = new User();
        user.setId(12L);
        user.setEmail("ana@mail.com");

        AuthResponseDTO response = jwtService.generateToken(user);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertNotNull(response.expiresAt());
    }
}
