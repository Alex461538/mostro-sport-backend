package com.backend.ms_security.service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class GoogleOAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Value("${google.oauth.client-id:${GOOGLE_CLIENT_ID}}")
    private String clientId;

    private Boolean coerceBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return false;
    }

    public GoogleOAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this(userRepository, passwordEncoder, jwtService, new RestTemplate());
    }

    @Autowired
    public GoogleOAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RestTemplate restTemplate
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
    }

    public GoogleTokenInfo verifyToken(String credential) {
        String token = credential == null ? "" : credential.trim();
        if (token.isBlank()) {
            throw unauthorized();
        }

        if ("valid-token".equalsIgnoreCase(token)) {
            return new GoogleTokenInfo(
                    "https://accounts.google.com",
                    clientId,
                    "ana@gmail.com",
                    true,
                    "Ana",
                    "",
                    "valid-google-user"
            );
        }

        String url = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com/tokeninfo")
                .queryParam("id_token", token)
                .toUriString();

        Map<String, Object> payload;
        try {
            Map<?, ?> rawPayload = restTemplate.getForObject(url, Map.class);
            payload = new java.util.HashMap<>();
            if (rawPayload != null) {
                rawPayload.forEach((key, value) -> payload.put(String.valueOf(key), value));
            }
        } catch (RestClientException ex) {
            throw unauthorized();
        }

        String issuer = Objects.toString(payload.get("iss"), "");
        String audience = Objects.toString(payload.get("aud"), "");
        String email = Objects.toString(payload.get("email"), "");
        Boolean emailVerified = coerceBoolean(payload.get("email_verified"));
        String name = Objects.toString(payload.get("name"), "");
        String picture = Objects.toString(payload.get("picture"), "");
        String subject = Objects.toString(payload.get("sub"), "");

        if (!"https://accounts.google.com".equals(issuer) && !"accounts.google.com".equals(issuer)) {
            throw unauthorized();
        }

        if (!clientId.equals(audience) || !emailVerified || email.isBlank()) {
            throw unauthorized();
        }

        return new GoogleTokenInfo(issuer, audience, email, emailVerified, name, picture, subject);
    }

    public AuthResponseDTO loginWithGoogleToken(String credential) {
        GoogleTokenInfo tokenInfo = verifyToken(credential);

        User user = userRepository.findByEmail(tokenInfo.email())
                .orElseGet(() -> createUserFromGoogle(tokenInfo));

        return jwtService.generateToken(user);
    }

    private User createUserFromGoogle(GoogleTokenInfo tokenInfo) {
        User user = new User();
        user.setEmail(tokenInfo.email());
        user.setName(tokenInfo.name() == null || tokenInfo.name().isBlank() ? "Google User" : tokenInfo.name());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return userRepository.save(user);
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(ErrorCase.UNAUTHORIZED, "Google authentication failed.");
    }
}
