package com.backend.ms_security.service;

import com.backend.ms_security.dto.auth.AuthResponseDTO;

public interface GitHubOAuthAuthenticator {
    AuthResponseDTO authenticate(String authorizationCode);
}
