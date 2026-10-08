package com.backend.ms_security.dto.userrole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleDTO {
    @NotNull(message = "The user id is mandatory.")
    private Long userId;

    @NotNull(message = "The role id is mandatory.")
    private Long roleId;
}
