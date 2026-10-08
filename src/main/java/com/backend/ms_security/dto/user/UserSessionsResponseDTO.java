package com.backend.ms_security.dto.user;

import java.util.List;

import com.backend.ms_security.dto.session.SessionResponseDTO;

import lombok.Value;

@Value
public class UserSessionsResponseDTO {
    Long id;
    String name;
    String email;
    List<SessionResponseDTO> sessions;
}
