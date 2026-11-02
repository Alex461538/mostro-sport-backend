package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.ms_security.dto.auth.ForgotPasswordRequestDTO;
import com.backend.ms_security.dto.auth.ResetPasswordRequestDTO;
import com.backend.ms_security.entity.PasswordResetToken;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.repository.PasswordResetTokenRepository;
import com.backend.ms_security.repository.UserRepository;
import com.backend.ms_security.service.AuthService;
import com.backend.ms_security.service.GoogleOAuthService;
import com.backend.ms_security.service.GitHubOAuthService;
import com.backend.ms_security.service.JwtService;
import com.backend.ms_security.repository.SessionRepository;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AuthPasswordRecoveryTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private GitHubOAuthService gitHubOAuthService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    @Test
    void forgotPasswordShouldPersistResetToken() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("encoded");

        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO();
        dto.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authService.forgotPassword(dto));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPasswordShouldUpdatePasswordIfTokenIsValid() {
        User user = new User();
        user.setId(10L);
        user.setEmail("alice@example.com");
        user.setPassword("old-password");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("token-123");
        token.setExpiration(ZonedDateTime.now().plusMinutes(15));
        token.setUsed(false);
        token.setUser(user);

        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setToken("token-123");
        dto.setNewPassword("NewPass123!");

        when(passwordResetTokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encoded-new-password");
        when(userRepository.save(user)).thenReturn(user);

        assertDoesNotThrow(() -> authService.resetPassword(dto));
        assertTrue(token.isUsed());
        verify(passwordResetTokenRepository).save(token);
    }
}
