package com.backend.ms_security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.dto.auth.OAuthRequestDTO;
import com.backend.ms_security.dto.auth.LoginRequestDTO;
import com.backend.ms_security.dto.auth.VerifyTwoFactorRequestDTO;
import com.backend.ms_security.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/login/verify-2fa")
    public AuthResponseDTO verify2fa(@Valid @RequestBody VerifyTwoFactorRequestDTO dto) {
        return authService.verify2fa(dto);
    }

    @PostMapping("/oauth/google")
    public AuthResponseDTO loginWithGoogle(@Valid @RequestBody OAuthRequestDTO dto) {
        return authService.loginWithGoogle(dto.credential());
    }

    @PostMapping("/oauth/github")
    public AuthResponseDTO loginWithGitHub(@Valid @RequestBody OAuthRequestDTO dto) {
        return authService.loginWithGitHub(dto.credential());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt.getTokenValue());
        return ResponseEntity.noContent().build();
    }
}
