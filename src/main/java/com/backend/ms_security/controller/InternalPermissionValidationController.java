package com.backend.ms_security.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_security.service.PermissionService;

import lombok.RequiredArgsConstructor;

/**
 * Usado por ms_negocio para delegar la autorizacion: valida si el usuario del JWT
 * tiene permiso (method, url) sobre un endpoint del negocio.
 */
@RestController
@RequestMapping("/api/internal/security")
@RequiredArgsConstructor
public class InternalPermissionValidationController {
    private final PermissionService permissionService;

    public record PermissionValidationRequest(String url, String method) {}

    @PostMapping("/permissions-validation")
    public Map<String, Boolean> validate(
            Authentication authentication,
            @RequestBody PermissionValidationRequest request
    ) {
        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return Map.of("allowed", false);
        }
        return Map.of("allowed", permissionService.hasPermission(userId, request.method(), request.url()));
    }
}
