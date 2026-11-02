package com.backend.ms_security.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyTwoFactorRequestDTO {
    @NotBlank(message = "The email is mandatory.")
    @Email(message = "The email format is invalid.")
    private String email;

    @NotBlank(message = "The challenge token is mandatory.")
    private String challengeToken;

    @NotBlank(message = "The 2FA code is mandatory.")
    private String code;
}
