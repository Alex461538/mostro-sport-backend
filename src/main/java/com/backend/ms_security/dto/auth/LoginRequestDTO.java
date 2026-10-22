package com.backend.ms_security.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    @NotBlank(message = "The email is mandatory.")
    @Email(message = "The email format is invalid.")
    private String email;

    @NotBlank(message = "The password is mandatory.")
    private String password;
}
