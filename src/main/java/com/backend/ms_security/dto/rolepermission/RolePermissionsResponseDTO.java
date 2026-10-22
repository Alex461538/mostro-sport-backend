package com.backend.ms_security.dto.rolepermission;

import java.util.List;

import lombok.Value;

@Value
public class RolePermissionsResponseDTO {
    Long id;
    String name;
    String description;
    List<RolePermissionResponseDTO> permissions;
}
