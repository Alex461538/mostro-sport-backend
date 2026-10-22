package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.ms_security.dto.rolepermission.AssignPermissionDTO;
import com.backend.ms_security.dto.rolepermission.PermissionRoleResponseDTO;
import com.backend.ms_security.dto.rolepermission.RolePermissionResponseDTO;
import com.backend.ms_security.entity.Permission;
import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.RolePermission;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.RolePermissionMapper;
import com.backend.ms_security.repository.PermissionRepository;
import com.backend.ms_security.repository.RolePermissionRepository;
import com.backend.ms_security.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolePermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RolePermissionMapper rolePermissionMapper;

    @Transactional
    public RolePermissionResponseDTO assign(AssignPermissionDTO dto) {
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Role not found with id: " + dto.getRoleId()
                ));

        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Permission not found with id: " + dto.getPermissionId()
                ));

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(dto.getRoleId(), dto.getPermissionId())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "The role already has this permission assigned."
            );
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        role.getRolePermissions().add(rolePermission);
        permission.getRolePermissions().add(rolePermission);

        return rolePermissionMapper.toResponseDTO(rolePermissionRepository.save(rolePermission));
    }

    @Transactional(readOnly = true)
    public List<RolePermissionResponseDTO> findAll() {
        return rolePermissionMapper.toResponseDTOList(rolePermissionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<RolePermissionResponseDTO> findByRoleId(Long roleId) {
        return rolePermissionMapper.toResponseDTOList(rolePermissionRepository.findAllByRoleId(roleId));
    }

    @Transactional(readOnly = true)
    public List<PermissionRoleResponseDTO> findByPermissionId(Long permissionId) {
        return rolePermissionMapper.toPermissionRoleResponseDTOList(
                rolePermissionRepository.findAllByPermissionId(permissionId)
        );
    }

    @Transactional
    public void delete(Long roleId, Long permissionId) {
        RolePermission rolePermission = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Permission assignment not found for role " + roleId + " and permission " + permissionId
                ));

        rolePermissionRepository.delete(rolePermission);
    }
}
