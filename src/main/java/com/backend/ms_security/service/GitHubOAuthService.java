package com.backend.ms_security.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

@Service
public class GitHubOAuthService implements GitHubOAuthAuthenticator {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Value("${github.oauth.client-id:${GITHUB_CLIENT_ID:}}")
    private String githubClientId;

    @Value("${github.oauth.client-secret:${GITHUB_CLIENT_SECRET:}}")
    private String githubClientSecret;

    public GitHubOAuthService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
    }

    public String exchangeCodeForAccessToken(String code) {
        if (githubClientId == null || githubClientId.isBlank()
                || githubClientSecret == null || githubClientSecret.isBlank()) {
            throw unauthorized();
        }

        String oneTimeCode = code == null ? "" : code.trim();
        if (oneTimeCode.isBlank()) {
            throw unauthorized();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("code", oneTimeCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = new RestTemplate().postForEntity(
                    "https://github.com/login/oauth/access_token",
                    request,
                    Map.class
            );

            Map<String, Object> payload = response.getBody();
            if (payload == null) {
                throw unauthorized();
            }

            String accessToken = Objects.toString(payload.get("access_token"), "");
            if (accessToken.isBlank()) {
                throw unauthorized();
            }

            return accessToken;
        } catch (RestClientException ex) {
            throw unauthorized();
        }
    }

    @Override
    public AuthResponseDTO authenticate(String authorizationCode) {
        String accessToken = exchangeCodeForAccessToken(authorizationCode);
        return loginWithGitHubToken(accessToken);
    }

    public AuthResponseDTO loginWithGitHubToken(String credential) {
        OAuthUserInfo userInfo = verifyGitHubToken(credential);
        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> createUserFromGitHub(userInfo));

        return jwtService.generateToken(user);
    }

    private OAuthUserInfo verifyGitHubToken(String credential) {
        String token = credential == null ? "" : credential.trim();
        if (token.isBlank()) {
            throw unauthorized();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> payload = response.getBody();
            if (payload == null) {
                throw unauthorized();
            }

            String email = Objects.toString(payload.get("email"), "");
            String login = Objects.toString(payload.get("login"), "");
            String name = Objects.toString(payload.get("name"), "");

            if (email.isBlank()) {
                email = fetchPrimaryEmail(token);
            }

            if (email.isBlank()) {
                email = login.isBlank() ? "github-user@placeholder.local" : login + "@github.local";
            }

            return new OAuthUserInfo(email, name.isBlank() ? login : name);
        } catch (RestClientException ex) {
            throw unauthorized();
        }
    }

    private String fetchPrimaryEmail(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (response.getBody() == null) {
                return "";
            }

            for (Map<String, Object> item : response.getBody()) {
                Boolean primary = item.get("primary") instanceof Boolean bool && bool;
                if (primary || Boolean.TRUE.equals(item.get("verified"))) {
                    String email = Objects.toString(item.get("email"), "");
                    if (!email.isBlank()) {
                        return email;
                    }
                }
            }
        } catch (RestClientException ignored) {
            // ignore fallback failures and let caller decide final behavior
        }

        return "";
    }

    private User createUserFromGitHub(OAuthUserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.email());
        user.setName(userInfo.name() == null || userInfo.name().isBlank() ? "GitHub User" : userInfo.name());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return userRepository.save(user);
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(ErrorCase.UNAUTHORIZED, "GitHub authentication failed.");
    }
}
