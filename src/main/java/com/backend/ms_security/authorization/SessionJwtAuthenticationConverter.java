package com.backend.ms_security.authorization;

import java.util.List;
import java.time.ZonedDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

import com.backend.ms_security.entity.Session;
import com.backend.ms_security.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
    private final SessionRepository sessionRepository;

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        String token = jwt.getTokenValue();
        Session session = sessionRepository.findByToken(token)
                .orElse(null);

        if (session == null) {
            throw new BadCredentialsException("JWT token has no matching active session.");
        }

        if (session.getUser() == null || !String.valueOf(session.getUser().getId()).equals(jwt.getSubject())) {
            sessionRepository.delete(session);
            throw new BadCredentialsException("JWT token user does not match the session owner.");
        }

        if (!session.getExpiration().isAfter(ZonedDateTime.now())) {
            sessionRepository.delete(session);
            throw new BadCredentialsException("Session expired.");
        }

        return new JwtAuthenticationToken(jwt, List.of(), jwt.getSubject());
    }
}
