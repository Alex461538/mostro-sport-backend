package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.role.CreateRoleDTO;
import com.backend.ms_security.dto.role.RoleResponseDTO;
import com.backend.ms_security.dto.role.UpdateRoleDTO;
import com.backend.ms_security.entity.Role;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.RoleMapper;
import com.backend.ms_security.repository.RoleRepository;
import com.backend.ms_security.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;

    public RoleResponseDTO create(CreateRoleDTO dto) {
        if (roleRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already a role with this name."
            );
        }

        Role role = roleMapper.toEntity(dto);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponseDTO(savedRole);
    }

    public List<RoleResponseDTO> findAll() {
        return roleMapper.toResponseDTOList(roleRepository.findAll());
    }

    private Role findRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Role not found with id: " + id
                ));
    }

    public RoleResponseDTO findById(Long id) {
        return roleMapper.toResponseDTO(findRole(id));
    }

    public RoleResponseDTO update(Long id, UpdateRoleDTO dto) {
        Role role = findRole(id);

        if (roleRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already another role with this name."
            );
        }

        roleMapper.updateEntity(dto, role);
        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponseDTO(updatedRole);
    }

    public void delete(Long id) {
        Role role = findRole(id);

        if (userRoleRepository.existsByRoleId(id)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "The role cannot be deleted because it is assigned to one or more users."
            );
        }

        roleRepository.delete(role);
    }
}
