package com.backend.ms_security.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.ms_security.dto.permission.PermissionResponseDTO;
import com.backend.ms_security.dto.role.RoleResponseDTO;
import com.backend.ms_security.dto.rolepermission.PermissionRoleResponseDTO;
import com.backend.ms_security.dto.rolepermission.RolePermissionResponseDTO;
import com.backend.ms_security.entity.RolePermission;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RolePermissionMapper {
    private final PermissionMapper permissionMapper;

    public RolePermissionResponseDTO toResponseDTO(RolePermission rolePermission) {
        return new RolePermissionResponseDTO(
                rolePermission.getId(),
                rolePermission.getRole().getId(),
                permissionMapper.toResponseDTO(rolePermission.getPermission())
        );
    }

    public List<RolePermissionResponseDTO> toResponseDTOList(List<RolePermission> rolePermissions) {
        return rolePermissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PermissionRoleResponseDTO toPermissionRoleResponseDTO(RolePermission rolePermission) {
        var role = rolePermission.getRole();
        return new PermissionRoleResponseDTO(
                rolePermission.getId(),
                rolePermission.getPermission().getId(),
                new RoleResponseDTO(role.getId(), role.getName(), role.getDescription())
        );
    }

    public List<PermissionRoleResponseDTO> toPermissionRoleResponseDTOList(List<RolePermission> rolePermissions) {
        return rolePermissions.stream()
                .map(this::toPermissionRoleResponseDTO)
                .toList();
    }
}
