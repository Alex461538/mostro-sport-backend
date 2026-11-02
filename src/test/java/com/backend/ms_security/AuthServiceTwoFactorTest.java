package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.dto.auth.LoginRequestDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.repository.SessionRepository;
import com.backend.ms_security.repository.UserRepository;
import com.backend.ms_security.service.AuthService;
import com.backend.ms_security.service.GoogleOAuthService;
import com.backend.ms_security.service.GitHubOAuthService;
import com.backend.ms_security.service.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTwoFactorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private GitHubOAuthService gitHubOAuthService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldGenerateTwoFactorChallengeForValidCredentials() {
        User user = new User();
        user.setId(7L);
        user.setEmail("alice@example.com");
        user.setPassword("hashed-password");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("alice@example.com");
        dto.setPassword("Secret123!");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "hashed-password")).thenReturn(true);

        AuthResponseDTO response = authService.login(dto);

        assertTrue(response.requiresTwoFactor());
        assertNotNull(response.challengeToken());
        assertNotNull(response.expiresAt());
        verify(jwtService, never()).generateToken(any(User.class));
    }
}
