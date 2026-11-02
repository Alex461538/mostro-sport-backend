package com.backend.ms_security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.dto.auth.LoginRequestDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleOAuthAuthenticator googleOAuthAuthenticator;
    private final GitHubOAuthAuthenticator gitHubOAuthAuthenticator;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(this::unauthorized);

        boolean validPassword = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!validPassword) {
            throw unauthorized();
        }

        return jwtService.generateToken(user);
    }

    public AuthResponseDTO loginWithGoogle(String credential) {
        return googleOAuthAuthenticator.authenticate(credential);
    }

    public AuthResponseDTO loginWithGitHub(String code) {
        return gitHubOAuthAuthenticator.authenticate(code);
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(ErrorCase.UNAUTHORIZED, "Invalid credentials.");
    }
}
