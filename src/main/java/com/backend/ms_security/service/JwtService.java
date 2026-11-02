package com.backend.ms_security.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.auth.AuthResponseDTO;
import com.backend.ms_security.entity.Session;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.repository.SessionRepository;

@Service
public class JwtService {
    private static final Duration TOKEN_DURATION = Duration.ofMinutes(15);

    private final JwtEncoder jwtEncoder;
    private final SessionRepository sessionRepository;

    public JwtService(JwtEncoder jwtEncoder) {
        this(jwtEncoder, null);
    }

    @Autowired
    public JwtService(JwtEncoder jwtEncoder, SessionRepository sessionRepository) {
        this.jwtEncoder = jwtEncoder;
        this.sessionRepository = sessionRepository;
    }

    public AuthResponseDTO generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(TOKEN_DURATION);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ms-security")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
        createSessionRecord(user, jwt.getTokenValue(), expiresAt);

        return new AuthResponseDTO(jwt.getTokenValue(), "Bearer", expiresAt);
    }

    private void createSessionRecord(User user, String token, Instant expiresAt) {
        if (sessionRepository == null || user == null || user.getId() == null) {
            return;
        }

        Session session = new Session();
        session.setToken(token);
        session.setExpiration(ZonedDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
        session.setCode2FA("");
        session.setUser(user);
        sessionRepository.save(session);
    }
}
