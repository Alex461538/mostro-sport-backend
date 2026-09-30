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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleResponseDTO create(CreateRoleDTO dto) {
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
        roleMapper.updateEntity(dto, role);
        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponseDTO(updatedRole);
    }

    public void delete(Long id) {
        roleRepository.delete(findRole(id));
    }
}
