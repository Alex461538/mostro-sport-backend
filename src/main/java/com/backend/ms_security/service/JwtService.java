package com.backend.ms_security.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
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
import com.backend.ms_security.repository.UserRoleRepository;

@Service
public class JwtService {
    private static final Duration TOKEN_DURATION = Duration.ofMinutes(15);

    private final JwtEncoder jwtEncoder;
    private final SessionRepository sessionRepository;
    private final UserRoleRepository userRoleRepository;

    public JwtService(JwtEncoder jwtEncoder) {
        this(jwtEncoder, null, null);
    }

    public JwtService(JwtEncoder jwtEncoder, SessionRepository sessionRepository) {
        this(jwtEncoder, sessionRepository, null);
    }

    @Autowired
    public JwtService(JwtEncoder jwtEncoder, SessionRepository sessionRepository, UserRoleRepository userRoleRepository) {
        this.jwtEncoder = jwtEncoder;
        this.sessionRepository = sessionRepository;
        this.userRoleRepository = userRoleRepository;
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
                .claim("roles", roleNames(user))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
        createSessionRecord(user, jwt.getTokenValue(), expiresAt);

        return new AuthResponseDTO(jwt.getTokenValue(), "Bearer", expiresAt);
    }

    // Informativo para el front; la autorizacion real sigue siendo por permisos en BD.
    private List<String> roleNames(User user) {
        if (userRoleRepository == null || user == null || user.getId() == null) {
            return List.of();
        }
        return userRoleRepository.findAllByUserId(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();
    }

    private void createSessionRecord(User user, String token, Instant expiresAt) {
        if (sessionRepository == null || user == null || user.getId() == null) {
            return;
        }

        Session session = new Session();
        session.setToken(token);
        session.setExpiration(ZonedDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
        session.setCode2FA(null);
        session.setUser(user);
        sessionRepository.save(session);
    }
}
