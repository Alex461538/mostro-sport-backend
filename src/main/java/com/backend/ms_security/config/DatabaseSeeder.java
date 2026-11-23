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
            findOrCreateRole("GUEST", "Usuario invitado registrado en la plataforma");
            User adminUser = findOrCreateAdminUser();
            assignRole(adminUser, adminRole);

            seedAuthPermissions(adminRole);
            seedUserPermissions(adminRole);
            seedRolePermissions(adminRole);
            seedPermissionCatalogPermissions(adminRole);
            seedProfilePermissions(adminRole);
            seedSessionPermissions(adminRole);
            seedUserRolePermissions(adminRole);
            seedRolePermissionPermissions(adminRole);
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

    private Permission findOrCreatePermission(String method, String url, String model) {
        String normalizedUrl = normalizeIdPlaceholders(url);

        Permission permission = permissionRepository.findByUrlAndMethod(normalizedUrl, method)
                .orElseGet(() -> permissionRepository.findAll().stream()
                        .filter(existing -> method.equals(existing.getMethod()))
                        .filter(existing -> normalizedUrl.equals(normalizeIdPlaceholders(existing.getUrl())))
                        .findFirst()
                        .orElseGet(() -> {
                            Permission newPermission = new Permission();
                            newPermission.setUrl(normalizedUrl);
                            newPermission.setMethod(method);
                            newPermission.setModel(model);
                            return permissionRepository.save(newPermission);
                        }));

        boolean changed = !normalizedUrl.equals(permission.getUrl()) || !model.equals(permission.getModel());
        if (changed) {
            permission.setUrl(normalizedUrl);
            permission.setModel(model);
            return permissionRepository.save(permission);
        }
        return permission;
    }

    private String normalizeIdPlaceholders(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceAll("(?i)\\{[a-z]*id\\}", "{id}");
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

    private void seedUserPermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/users", "Users"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users", "Users"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users/{id}", "Users"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users/{id}/detail-with-sessions", "Users"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users/{id}/detail-with-roles", "Users"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/users/{id}", "Users"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/users/{id}", "Users"));
        assignPermission(role, findOrCreatePermission("POST", "/api/users/{id}/profile", "Profiles"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/users/{id}/profile", "Profiles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users/{id}/profile", "Profiles"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/users/{id}/profile", "Profiles"));
        assignPermission(role, findOrCreatePermission("POST", "/api/users/{id}/sessions", "User Sessions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users/{id}/sessions", "User Sessions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/users/{id}/sessions/{id}", "User Sessions"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/users/{id}/sessions/{id}", "User Sessions"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/users/{id}/sessions/{id}", "User Sessions"));
    }

    private void seedRolePermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/roles", "Roles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/roles", "Roles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/roles/{id}", "Roles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/roles/{id}/detail-with-permissions", "Roles"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/roles/{id}", "Roles"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/roles/{id}", "Roles"));
    }

    private void seedPermissionCatalogPermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/permissions", "Permissions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/permissions", "Permissions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/permissions/{id}", "Permissions"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/permissions/{id}", "Permissions"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/permissions/{id}", "Permissions"));
    }

    private void seedProfilePermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/profiles", "Profiles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/profiles", "Profiles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/profiles/{id}", "Profiles"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/profiles/{id}", "Profiles"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/profiles/{id}", "Profiles"));
    }

    private void seedSessionPermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/sessions", "Sessions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/sessions", "Sessions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/sessions/{id}", "Sessions"));
        assignPermission(role, findOrCreatePermission("PUT", "/api/sessions/{id}", "Sessions"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/sessions/{id}", "Sessions"));
    }

    private void seedUserRolePermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/user-roles", "User Roles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/user-roles", "User Roles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/user-roles/users/{id}", "User Roles"));
        assignPermission(role, findOrCreatePermission("GET", "/api/user-roles/roles/{id}", "User Roles"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/user-roles/{id}/{id}", "User Roles"));
    }

    private void seedRolePermissionPermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/role-permissions", "Role Permissions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/role-permissions", "Role Permissions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/role-permissions/roles/{id}", "Role Permissions"));
        assignPermission(role, findOrCreatePermission("GET", "/api/role-permissions/permissions/{id}", "Role Permissions"));
        assignPermission(role, findOrCreatePermission("DELETE", "/api/role-permissions/{id}/{id}", "Role Permissions"));
    }

    private void seedAuthPermissions(Role role) {
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/login", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/login/verify-2fa", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/forgot-password", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/reset-password", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/oauth/google", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/oauth/github", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/logout", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/auth/register", "Auth"));
        assignPermission(role, findOrCreatePermission("POST", "/api/internal/security/permissions-validation", "Security"));
    }
}
