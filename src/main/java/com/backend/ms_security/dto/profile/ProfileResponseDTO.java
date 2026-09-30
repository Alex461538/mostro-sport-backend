package com.backend.ms_security.dto.profile;

import java.util.Date;

import lombok.Value;

@Value
public class ProfileResponseDTO {
    Long id;
    String phone;
    Date birthDate;
}
