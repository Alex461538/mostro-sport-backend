package com.backend.ms_security.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseRoleDTO {
    @NotBlank(message = "The role name is mandatory.")
    @Size(min = 2, max = 50, message = "The role name must be between 2 and 50 characters long.")
    private String name;

    @NotBlank(message = "The role description is mandatory.")
    @Size(min = 5, max = 255, message = "The role description must be between 5 and 255 characters long.")
    private String description;
}
