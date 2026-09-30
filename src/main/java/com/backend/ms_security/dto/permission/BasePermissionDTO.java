package com.backend.ms_security.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePermissionDTO {
    @NotBlank(message = "The URL is mandatory.")
    private String url;

    @NotBlank(message = "The HTTP method is mandatory.")
    private String method;

    @NotBlank(message = "The model name is mandatory.")
    private String model;
}
