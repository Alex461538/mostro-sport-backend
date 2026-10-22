package com.backend.ms_security.dto.rolepermission;

import com.backend.ms_security.dto.permission.PermissionResponseDTO;

import lombok.Value;

@Value
public class RolePermissionResponseDTO {
    Long id;
    Long roleId;
    PermissionResponseDTO permission;
}
