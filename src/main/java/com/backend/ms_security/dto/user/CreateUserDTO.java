package com.backend.ms_security.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDTO extends BaseUserDTO
{
    @NotBlank(
            message = "The password is mandatory."
    )
    @Size(
            min = 8,
            max = 72,
            message = "The password must be between 8 and 72 characters long."
    )
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])\\S{8,72}$",
            message = "The password must contain at least one lowercase letter, one uppercase letter, one number, one special character, and no spaces."
    )
    private String password;
}
