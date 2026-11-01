package com.backend.ms_security.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthRequestDTO(
        @NotBlank(message = "El credential de Google es obligatorio")
        String credential
) {
}
