package com.backend.ms_security.dto.rolepermission;

import com.backend.ms_security.dto.role.RoleResponseDTO;

import lombok.Value;

@Value
public class PermissionRoleResponseDTO {
    Long id;
    Long permissionId;
    RoleResponseDTO role;
}
