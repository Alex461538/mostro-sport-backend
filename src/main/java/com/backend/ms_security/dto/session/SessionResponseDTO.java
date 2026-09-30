package com.backend.ms_security.dto.session;

import java.util.Date;

import lombok.Value;

@Value
public class SessionResponseDTO {
    Long id;
    String token;
    Date expiration;
    String code2FA;
}
