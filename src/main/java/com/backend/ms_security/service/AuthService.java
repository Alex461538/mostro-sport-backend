package com.backend.ms_security.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.dto.auth.LoginRequestDTO;
import com.backend.ms_security.dto.auth.VerifyTwoFactorRequestDTO;
import com.backend.ms_security.entity.Session;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.repository.SessionRepository;
import com.backend.ms_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int TWO_FA_CODE_LENGTH = 6;
    private static final int TWO_FA_TTL_MINUTES = 3;

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleOAuthService googleOAuthService;
    private final GitHubOAuthService gitHubOAuthService;
    private final RestTemplate restTemplate;

    @Value("${auth.email-service.url:http://localhost:5000/send-email}")
    private String emailServiceUrl = "http://localhost:5000/send-email";

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

        try {
            sendTwoFactorCodeEmail(user.getEmail(), code);
        } catch (RestClientException exception) {
            sessionRepository.delete(challenge);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The two-factor email service is unavailable.",
                    exception
            );
        }

        return new AuthResponseDTO(challengeToken, "Bearer", expiresAt, true, challengeToken);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }

    private void sendTwoFactorCodeEmail(String to, String code) {
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
        restTemplate.postForEntity(emailServiceUrl, request, Map.class);
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(ErrorCase.UNAUTHORIZED, "Invalid credentials.");
    }
}
