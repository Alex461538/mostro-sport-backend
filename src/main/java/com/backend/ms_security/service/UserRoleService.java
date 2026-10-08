package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.ms_security.dto.userrole.AssignRoleDTO;
import com.backend.ms_security.dto.userrole.RoleUserResponseDTO;
import com.backend.ms_security.dto.userrole.UserRoleResponseDTO;
import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.entity.UserRole;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.UserRoleMapper;
import com.backend.ms_security.repository.RoleRepository;
import com.backend.ms_security.repository.UserRepository;
import com.backend.ms_security.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    @Transactional
    public UserRoleResponseDTO assign(AssignRoleDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "User not found with id: " + dto.getUserId()
                ));

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Role not found with id: " + dto.getRoleId()
                ));

        if (userRoleRepository.existsByUserIdAndRoleId(dto.getUserId(), dto.getRoleId())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "The user already has this role assigned."
            );
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        user.getUserRoles().add(userRole);
        role.getUserRoles().add(userRole);

        return userRoleMapper.toResponseDTO(userRoleRepository.save(userRole));
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponseDTO> findAll() {
        return userRoleMapper.toResponseDTOList(userRoleRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponseDTO> findByUserId(Long userId) {
        return userRoleMapper.toResponseDTOList(userRoleRepository.findAllByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<RoleUserResponseDTO> findByRoleId(Long roleId) {
        return userRoleMapper.toRoleUserResponseDTOList(userRoleRepository.findAllByRoleId(roleId));
    }

    @Transactional
    public void delete(Long userId, Long roleId) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Role assignment not found for user " + userId + " and role " + roleId
                ));
        userRoleRepository.delete(userRole);
    }
}
