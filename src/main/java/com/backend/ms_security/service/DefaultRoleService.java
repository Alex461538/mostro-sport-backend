package com.backend.ms_security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.entity.UserRole;
import com.backend.ms_security.repository.RoleRepository;
import com.backend.ms_security.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

/**
 * Asigna el rol por defecto (AFICIONADO) a los usuarios que se registran
 * o que entran por primera vez con OAuth.
 */
@Service
@RequiredArgsConstructor
public class DefaultRoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Value("${security.default-role:AFICIONADO}")
    private String defaultRoleName;

    public void assignDefaultRole(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        Role role = roleRepository.findByNameIgnoreCase(defaultRoleName)
                .orElseGet(() -> {
                    Role created = new Role();
                    created.setName(defaultRoleName);
                    created.setDescription("Rol por defecto para usuarios registrados");
                    return roleRepository.save(created);
                });

        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }
}
