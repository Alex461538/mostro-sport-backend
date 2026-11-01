package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.repository.UserRepository;
import com.backend.ms_security.service.GitHubOAuthService;
import com.backend.ms_security.service.GoogleOAuthService;
import com.backend.ms_security.service.JwtService;

class GoogleOAuthLoginTest {

    @Test
    void shouldCreateInternalJwtWhenGoogleTokenIsValid() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId(42L);
        user.setEmail("ana@gmail.com");
        user.setName("Ana");
        user.setPassword(new BCryptPasswordEncoder(12).encode("google-random-password"));

        when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(user));

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

        GoogleOAuthService service = new GoogleOAuthService(userRepository, new BCryptPasswordEncoder(12), jwtService);

        AuthResponseDTO response = service.loginWithGoogleToken("valid-token");

        assertNotNull(response);
        assertEquals("Bearer", response.tokenType());
        assertNotNull(response.accessToken());
        assertNotNull(response.expiresAt());
        assertTrue(response.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void shouldCreateInternalJwtWhenGitHubTokenIsValid() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId(44L);
        user.setEmail("ana@github.com");
        user.setName("Ana");
        user.setPassword(new BCryptPasswordEncoder(12).encode("github-random-password"));

        when(userRepository.findByEmail("ana@github.com")).thenReturn(Optional.of(user));

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

        RestTemplate gitHubRestTemplate = mock(RestTemplate.class);
        when(gitHubRestTemplate.exchange(
                ArgumentMatchers.eq("https://api.github.com/user"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of("email", "ana@github.com", "login", "ana", "name", "Ana")));

        GitHubOAuthService service = new GitHubOAuthService(userRepository, new BCryptPasswordEncoder(12), jwtService, gitHubRestTemplate);
        AuthResponseDTO response = service.loginWithGitHubToken("valid-github-token");

        assertNotNull(response);
        assertEquals("Bearer", response.tokenType());
        assertNotNull(response.accessToken());
        assertNotNull(response.expiresAt());
        assertTrue(response.expiresAt().isAfter(Instant.now()));
    }
}
