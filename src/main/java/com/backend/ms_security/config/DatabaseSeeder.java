package com.backend.ms_security.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${security.seed.admin-email:admin@uc.edu.co}")
    private String adminEmail;

    @Value("${security.seed.admin-password:}")
    private String adminPassword;

    @Bean
    CommandLineRunner seedDatabase() {
        return args -> {
            Role adminRole = findOrCreateRole("ADMIN", "Administrador del sistema");
            User adminUser = findOrCreateAdminUser();
            if (adminUser != null) {
                assignRole(adminUser, adminRole);
            }

            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/users"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}/detail-with-roles"));
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
            assignPermission(adminRole, findOrCreatePermission( "GET", "/api/profiles/{id}"));
            assignPermission(adminRole, findOrCreatePermission( "POST", "/api/profiles"));
            assignPermission(adminRole, findOrCreatePermission( "GET", "/api/profiles"));
            assignPermission(adminRole, findOrCreatePermission( "PUT", "/api/profiles/{id}"));
            assignPermission(adminRole, findOrCreatePermission( "DELETE", "/api/profiles/{id}"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/users/{id}/profile"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}/profile"));
            assignPermission(adminRole, findOrCreatePermission("PUT", "/api/users/{id}/profile"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/users/{id}/profile"));

            // Administracion de asignaciones y sesiones (panel del front)
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}/detail-with-sessions"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/user-roles"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/user-roles"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/user-roles/users/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/user-roles/roles/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/user-roles/{id}/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/role-permissions"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/role-permissions"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/role-permissions/roles/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/role-permissions/permissions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/role-permissions/{id}/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/sessions"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/sessions"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/sessions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("PUT", "/api/sessions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/sessions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}/sessions"));
            assignPermission(adminRole, findOrCreatePermission("POST", "/api/users/{id}/sessions"));
            assignPermission(adminRole, findOrCreatePermission("GET", "/api/users/{id}/sessions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("PUT", "/api/users/{id}/sessions/{id}"));
            assignPermission(adminRole, findOrCreatePermission("DELETE", "/api/users/{id}/sessions/{id}"));
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

    // Si no hay contrasena semilla configurada, no se crea el admin por defecto.
    private User findOrCreateAdminUser() {
        return userRepository.findByEmail(adminEmail)
                .orElseGet(() -> {
                    if (adminPassword == null || adminPassword.isBlank()) {
                        return null;
                    }
                    User user = new User();
                    user.setName("Administrador");
                    user.setEmail(adminEmail);
                    user.setPassword(passwordEncoder.encode(adminPassword));
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
