package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.backend.ms_security.controller.PermissionValidationController;
import com.backend.ms_security.dto.permission.PermissionValidationRequestDTO;
import com.backend.ms_security.dto.permission.PermissionValidationResponseDTO;
import com.backend.ms_security.service.PermissionService;

import jakarta.validation.Validation;

class PermissionValidationControllerTest {

    @Test
    void shouldReturnPermissionDecisionForUserFromJwtSubject() {
        PermissionService permissionService = mock(PermissionService.class);
        when(permissionService.hasPermission(42L, "post", "/cinema/movies")).thenReturn(true);
        PermissionValidationController controller = new PermissionValidationController(permissionService);
        PermissionValidationRequestDTO request = request("/cinema/movies", "post");

        PermissionValidationResponseDTO response = controller.validate(
                request,
                authentication("42")
        );

        assertTrue(response.allowed());
        verify(permissionService).hasPermission(42L, "post", "/cinema/movies");
    }

    @Test
    void shouldReturnFalseWhenPermissionIsMissing() {
        PermissionService permissionService = mock(PermissionService.class);
        when(permissionService.hasPermission(42L, "DELETE", "/cinema/movies/15")).thenReturn(false);
        PermissionValidationController controller = new PermissionValidationController(permissionService);

        PermissionValidationResponseDTO response = controller.validate(
                request("/cinema/movies/15", "DELETE"),
                authentication("42")
        );

        assertFalse(response.allowed());
    }

    @Test
    void shouldValidateRequestUrlAndHttpMethod() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            PermissionValidationRequestDTO invalidRequest = request("", "OPTIONS");

            Set<?> violations = validator.validate(invalidRequest);

            assertEquals(2, violations.size());
            assertTrue(validator.validate(request("/cinema/movies", "post")).isEmpty());
            assertEquals(1, validator.validate(request("a".repeat(256), "GET")).size());
        }
    }

    private PermissionValidationRequestDTO request(String url, String method) {
        PermissionValidationRequestDTO request = new PermissionValidationRequestDTO();
        request.setUrl(url);
        request.setMethod(method);
        return request;
    }

    private JwtAuthenticationToken authentication(String subject) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
