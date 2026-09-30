package com.backend.ms_security.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDTO extends BaseUserDTO
{
    @Size(  min = 8,
            message = "The password should be at least 8 chars long."
    )
    private String password;
}
