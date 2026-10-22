package com.backend.ms_security.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.permission.CreatePermissionDTO;
import com.backend.ms_security.dto.permission.PermissionResponseDTO;
import com.backend.ms_security.dto.permission.UpdatePermissionDTO;
import com.backend.ms_security.entity.Permission;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.PermissionMapper;
import com.backend.ms_security.repository.PermissionRepository;
import com.backend.ms_security.repository.RolePermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private static final Pattern ID_SEGMENT_PATTERN = Pattern.compile("(?i)(?<=/)(?:\\d+|[a-f0-9]{24})(?=/|$)");

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponseDTO create(CreatePermissionDTO dto) {
        if (permissionRepository.existsByUrlAndMethod(dto.getUrl(), dto.getMethod())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already a permission with this URL and method."
            );
        }

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

        if (permissionRepository.existsByUrlAndMethodAndIdNot(dto.getUrl(), dto.getMethod(), id)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already another permission with this URL and method."
            );
        }

        permissionMapper.updateEntity(dto, permission);
        Permission updatedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponseDTO(updatedPermission);
    }

    public void delete(Long id) {
        Permission permission = findPermission(id);

        if (rolePermissionRepository.existsByPermissionId(id)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "The permission cannot be deleted because it is assigned to one or more roles."
            );
        }

        permissionRepository.delete(permission);
    }

    public boolean hasPermission(Long userId, String httpMethod, String requestUrl) {
        if (userId == null || httpMethod == null || requestUrl == null) {
            return false;
        }

        String normalizedUrl = normalizeUrl(requestUrl);
        return !permissionRepository.findByUserIdAndMethodAndUrl(userId, httpMethod.toUpperCase(), normalizedUrl)
                .isEmpty();
    }

    String normalizeUrl(String requestUrl) {
        return ID_SEGMENT_PATTERN.matcher(requestUrl).replaceAll("{id}");
    }
}
