package com.backend.ms_security.dto.session;

import java.time.ZonedDateTime;

import lombok.Value;

@Value
public class SessionResponseDTO {
    Long id;
    String token;
    ZonedDateTime expiration;
    String code2FA;
}
