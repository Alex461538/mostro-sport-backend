package com.backend.ms_security.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_security.dto.permission.PermissionValidationRequestDTO;
import com.backend.ms_security.dto.permission.PermissionValidationResponseDTO;
import com.backend.ms_security.service.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/security")
@RequiredArgsConstructor
public class PermissionValidationController {
    private final PermissionService permissionService;

    @PostMapping("/permissions-validation")
    public PermissionValidationResponseDTO validate(
            @Valid @RequestBody PermissionValidationRequestDTO dto,
            JwtAuthenticationToken authentication) {

        Long userId = Long.valueOf(authentication.getToken().getSubject());
        String method = dto.getMethod();
        String url = dto.getUrl();

        boolean allowed = permissionService.hasPermission(userId, method, url);
        return new PermissionValidationResponseDTO(allowed);
    }
}
