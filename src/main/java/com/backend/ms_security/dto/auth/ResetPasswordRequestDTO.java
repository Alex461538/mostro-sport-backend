package com.backend.ms_security.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDTO {
    @NotBlank(message = "The reset token is mandatory.")
    private String token;

    @NotBlank(message = "The password is mandatory.")
    @Size(min = 8, max = 72, message = "The password must have between 8 and 72 characters.")
    private String newPassword;
}
