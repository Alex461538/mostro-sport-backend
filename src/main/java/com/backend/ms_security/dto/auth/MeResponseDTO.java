package com.backend.ms_security.dto.auth;

import java.util.List;

public record MeResponseDTO(Long id, String name, String email, List<String> roles) {
}
