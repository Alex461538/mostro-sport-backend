package com.backend.ms_security.dto.profile;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseProfileDTO {
    @NotBlank(message = "The phone is mandatory.")
    @Size(min = 8, max = 20, message = "The phone must be between 8 and 20 characters long.")
    private String phone;

    @NotNull(message = "The birth date is mandatory.")
    private ZonedDateTime birthDate;
}
