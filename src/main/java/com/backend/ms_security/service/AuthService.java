package com.backend.ms_security.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.dto.auth.ForgotPasswordRequestDTO;
import com.backend.ms_security.dto.auth.LoginRequestDTO;
import com.backend.ms_security.dto.auth.ResetPasswordRequestDTO;
import com.backend.ms_security.dto.auth.VerifyTwoFactorRequestDTO;
import com.backend.ms_security.entity.PasswordResetToken;
import com.backend.ms_security.entity.Session;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.repository.PasswordResetTokenRepository;
import com.backend.ms_security.repository.SessionRepository;
import com.backend.ms_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int TWO_FA_CODE_LENGTH = 6;
    private static final int TWO_FA_TTL_MINUTES = 3;
    private static final int RESET_TOKEN_TTL_MINUTES = 15;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[\\S]{8,72}$");

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleOAuthService googleOAuthService;
    private final GitHubOAuthService gitHubOAuthService;
    private final RestTemplate restTemplate;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(this::unauthorized);

        boolean validPassword = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!validPassword) {
            throw unauthorized();
        }

        return createTwoFactorChallenge(user);
    }

    public AuthResponseDTO verify2fa(VerifyTwoFactorRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(this::unauthorized);

        Optional<Session> pendingChallenge = Optional.ofNullable(dto.getChallengeToken())
                .filter(token -> !token.isBlank())
                .map(token -> sessionRepository.findByUserIdAndToken(user.getId(), token))
                .orElseGet(() -> sessionRepository.findTopByUserIdAndCode2FAIsNotNullOrderByExpirationDesc(user.getId()));

        if (pendingChallenge.isEmpty()) {
            throw unauthorized();
        }

        Session session = pendingChallenge.get();
        if (session.getExpiration() == null || !session.getExpiration().isAfter(ZonedDateTime.now())) {
            sessionRepository.delete(session);
            throw unauthorized();
        }

        if (session.getCode2FA() == null || !session.getCode2FA().equals(dto.getCode().trim())) {
            sessionRepository.delete(session);
            throw unauthorized();
        }

        sessionRepository.delete(session);
        return jwtService.generateToken(user);
    }

    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim();
        if (email.isBlank()) {
            throw new ApplicationException(ErrorCase.INVALID_OPERATION, "The email is mandatory.");
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.findTopByUserIdAndUsedFalseOrderByExpirationDesc(user.getId())
                    .ifPresent(passwordResetTokenRepository::delete);

            String token = UUID.randomUUID().toString();
            ZonedDateTime expiration = ZonedDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setExpiration(expiration);
            resetToken.setUsed(false);
            resetToken.setUser(user);
            passwordResetTokenRepository.save(resetToken);

            sendRecoveryEmail(user.getEmail(), token);
        });
    }

    public void resetPassword(ResetPasswordRequestDTO dto) {
        String token = dto.getToken() == null ? "" : dto.getToken().trim();
        String newPassword = dto.getNewPassword() == null ? "" : dto.getNewPassword();

        if (token.isBlank()) {
            throw new ApplicationException(ErrorCase.UNAUTHORIZED, "Invalid or expired reset token.");
        }

        if (!isPasswordValid(newPassword)) {
            throw new ApplicationException(ErrorCase.INVALID_OPERATION, "The password must contain uppercase, lowercase, number, special character, and be between 8 and 72 characters.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApplicationException(ErrorCase.UNAUTHORIZED, "Invalid or expired reset token."));

        if (resetToken.isUsed()) {
            throw new ApplicationException(ErrorCase.UNAUTHORIZED, "This reset token has already been used.");
        }

        if (resetToken.getExpiration().isBefore(ZonedDateTime.now())) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            throw new ApplicationException(ErrorCase.UNAUTHORIZED, "Reset token expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public AuthResponseDTO loginWithGoogle(String credential) {
        return googleOAuthService.loginWithGoogleToken(credential);
    }

    public AuthResponseDTO loginWithGitHub(String code) {
        String accessToken = gitHubOAuthService.exchangeCodeForAccessToken(code);
        return gitHubOAuthService.loginWithGitHubToken(accessToken);
    }

    public void logout(String token) {
        String normalizedToken = token == null ? "" : token.trim();
        if (normalizedToken.startsWith("Bearer ")) {
            normalizedToken = normalizedToken.substring("Bearer ".length()).trim();
        }

        if (normalizedToken.isBlank()) {
            return;
        }

        sessionRepository.findByToken(normalizedToken)
                .ifPresent(sessionRepository::delete);
    }

    private AuthResponseDTO createTwoFactorChallenge(User user) {
        sessionRepository.findAllByUserId(user.getId()).stream()
                .filter(session -> session.getCode2FA() != null && !session.getCode2FA().isBlank())
                .forEach(sessionRepository::delete);

        String code = generateCode();
        String challengeToken = java.util.UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(TWO_FA_TTL_MINUTES * 60L);

        Session challenge = new Session();
        challenge.setUser(user);
        challenge.setToken(challengeToken);
        challenge.setCode2FA(code);
        challenge.setExpiration(ZonedDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
        sessionRepository.save(challenge);

        sendTwoFactorCodeEmail(user.getEmail(), code);

        return new AuthResponseDTO(challengeToken, "Bearer", expiresAt, true, challengeToken);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }

    private void sendTwoFactorCodeEmail(String to, String code) {
        if (restTemplate == null) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String message = "Tu código de verificación es: " + code + "\n"
                + "Será válido durante 3 minutos.\n"
                + "No compartas este código con nadie.";

        Map<String, String> payload = Map.of(
                "to", to,
                "subject", "Código de verificación 2FA",
                "message", message
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity("http://localhost:5000/send-email", request, Map.class);
    }

    private void sendRecoveryEmail(String to, String token) {
        if (restTemplate == null) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String message = "Hola,\n\n"
                + "Has solicitado recuperar tu contraseña.\n"
                + "Usa este token para continuar:\n\n"
                + token + "\n\n"
                + "Este token será válido durante 15 minutos.\n"
                + "Si no solicitaste este cambio, puedes ignorar este correo.";

        Map<String, String> payload = Map.of(
                "to", to,
                "subject", "Recuperación de contraseña",
                "message", message
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity("http://localhost:5000/send-email", request, Map.class);
    }

    private boolean isPasswordValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(ErrorCase.UNAUTHORIZED, "Invalid credentials.");
    }
}
