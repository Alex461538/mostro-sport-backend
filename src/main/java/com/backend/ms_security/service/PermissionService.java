package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.permission.CreatePermissionDTO;
import com.backend.ms_security.dto.permission.PermissionResponseDTO;
import com.backend.ms_security.dto.permission.UpdatePermissionDTO;
import com.backend.ms_security.entity.Permission;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.PermissionMapper;
import com.backend.ms_security.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponseDTO create(CreatePermissionDTO dto) {
        Permission permission = permissionMapper.toEntity(dto);
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponseDTO(savedPermission);
    }

    public List<PermissionResponseDTO> findAll() {
        return permissionMapper.toResponseDTOList(permissionRepository.findAll());
    }

    private Permission findPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Permission not found with id: " + id
                ));
    }

    public PermissionResponseDTO findById(Long id) {
        return permissionMapper.toResponseDTO(findPermission(id));
    }

    public PermissionResponseDTO update(Long id, UpdatePermissionDTO dto) {
        Permission permission = findPermission(id);
        permissionMapper.updateEntity(dto, permission);
        Permission updatedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponseDTO(updatedPermission);
    }

    public void delete(Long id) {
        permissionRepository.delete(findPermission(id));
    }
}
