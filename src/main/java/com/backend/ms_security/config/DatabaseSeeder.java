package com.backend.ms_security.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.ms_security.entity.Permission;
import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.RolePermission;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.entity.UserRole;
import com.backend.ms_security.repository.PermissionRepository;
import com.backend.ms_security.repository.RolePermissionRepository;
import com.backend.ms_security.repository.RoleRepository;
import com.backend.ms_security.repository.UserRepository;
import com.backend.ms_security.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDatabase() {
        return args -> {
            Role adminRole = findOrCreateRole("ADMIN", "Administrador del sistema");
            User adminUser = findOrCreateAdminUser();
            assignRole(adminUser, adminRole);

            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/users"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}"));
            assignPermission(adminRole, findOrCreatePermission("PUT", "/api/users/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/users/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/roles"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/roles"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/roles/{id}"));
            assignPermission(adminRole, findOrCreatePermission("PUT", "/api/roles/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/roles/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/permissions"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/permissions"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/permissions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("PUT", "/api/permissions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/permissions/{id}"));
        };
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private User findOrCreateAdminUser() {
        return userRepository.findByEmail("admin@uc.edu.co")
                .orElseGet(() -> {
                    User user = new User();
                    user.setName("Administrador");
                    user.setEmail("admin@uc.edu.co");
                    user.setPassword(passwordEncoder.encode("Admin123!"));
                    return userRepository.save(user);
                });
    }

    private Permission findOrCreatePermission(String method, String url) {
        return permissionRepository.findByUrlAndMethod(url, method)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setUrl(url);
                    permission.setMethod(method);
                    permission.setModel("core");
                    return permissionRepository.save(permission);
                });
    }

    private void assignRole(User user, Role role) {
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    private void assignPermission(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }
}
