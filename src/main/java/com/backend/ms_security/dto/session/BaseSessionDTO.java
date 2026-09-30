package com.backend.ms_security.dto.session;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseSessionDTO {
    @NotBlank(message = "The token is mandatory.")
    private String token;

    @NotNull(message = "The expiration date is mandatory.")
    private Date expiration;

    @NotBlank(message = "The 2FA code is mandatory.")
    private String code2FA;
}
