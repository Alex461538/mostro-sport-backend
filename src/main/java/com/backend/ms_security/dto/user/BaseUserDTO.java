package com.backend.ms_security.dto.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Setter
@Getter
public abstract class BaseUserDTO {
    @NotBlank(
        message = "The name is mandatory."
    )
    @Size(
        min = 2,
        max = 100,
        message = "The name has to be at least 2, and at most 100 chars long."
    )
    private String name;

    @NotBlank(
        message = "The email is mandatory."
    )
    @Email(
        message = "Invalid email format."
    )
    private String email;
}
