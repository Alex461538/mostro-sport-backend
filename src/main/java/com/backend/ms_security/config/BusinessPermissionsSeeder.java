package com.backend.ms_security.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.backend.ms_security.entity.Permission;
import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.RolePermission;
import com.backend.ms_security.repository.PermissionRepository;
import com.backend.ms_security.repository.RolePermissionRepository;
import com.backend.ms_security.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

/**
 * Seeder separado para permisos del microservicio de negocio (/api/v1/...).
 * Carga existentes en lote para arrancar en pocos segundos incluso con BD remota.
 */
@Configuration
@RequiredArgsConstructor
public class BusinessPermissionsSeeder {

    private static final String MODEL_NEGOCIO = "negocio";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Bean
    @Order(2)
    CommandLineRunner seedBusinessPermissions() {
        return args -> {
            Role adminRole = findOrCreateRole("ADMIN", "Administrador del sistema");
            Role aficionadoRole = findOrCreateRole("AFICIONADO", "Aficionado deportivo");

            Map<String, Permission> existingPerms = new HashMap<>();
            for (Permission p : permissionRepository.findAll()) {
                existingPerms.put(p.getMethod() + "::" + p.getUrl(), p);
            }

            List<BusinessPermissionDef> catalog = new ArrayList<>();
            catalog.addAll(BusinessCorePermissions.all());
            catalog.addAll(BusinessOperationsPermissions.all());

            List<Permission> toInsert = new ArrayList<>();
            for (BusinessPermissionDef item : catalog) {
                String key = item.method() + "::" + item.url();
                if (!existingPerms.containsKey(key)) {
                    Permission p = new Permission();
                    p.setMethod(item.method());
                    p.setUrl(item.url());
                    p.setModel(MODEL_NEGOCIO);
                    toInsert.add(p);
                }
            }
            if (!toInsert.isEmpty()) {
                for (Permission saved : permissionRepository.saveAll(toInsert)) {
                    existingPerms.put(saved.getMethod() + "::" + saved.getUrl(), saved);
                }
            }

            Set<String> existingLinks = new HashSet<>();
            for (RolePermission rp : rolePermissionRepository.findAll()) {
                existingLinks.add(rp.getRole().getId() + "::" + rp.getPermission().getId());
            }

            List<RolePermission> newLinks = new ArrayList<>();
            for (BusinessPermissionDef item : catalog) {
                Permission perm = existingPerms.get(item.method() + "::" + item.url());
                if (perm == null) continue;
                addLinkIfMissing(adminRole, perm, existingLinks, newLinks);
                if (item.aficionado()) {
                    addLinkIfMissing(aficionadoRole, perm, existingLinks, newLinks);
                }
            }
            if (!newLinks.isEmpty()) {
                rolePermissionRepository.saveAll(newLinks);
            }
        };
    }

    private void addLinkIfMissing(Role role, Permission perm, Set<String> existing, List<RolePermission> batch) {
        String key = role.getId() + "::" + perm.getId();
        if (existing.add(key)) {
            RolePermission rp = new RolePermission();
            rp.setRole(role);
            rp.setPermission(perm);
            batch.add(rp);
        }
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }
}
