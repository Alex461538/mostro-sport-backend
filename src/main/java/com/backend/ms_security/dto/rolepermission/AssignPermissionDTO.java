package com.backend.ms_security.dto.rolepermission;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignPermissionDTO {
    @NotNull(message = "The role identifier is mandatory.")
    private Long roleId;

    @NotNull(message = "The permission identifier is mandatory.")
    private Long permissionId;
}
