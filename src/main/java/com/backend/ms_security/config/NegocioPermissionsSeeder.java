package com.backend.ms_security.config;

import java.util.List;

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
 * Carga (idempotente) los permisos del microservicio de negocio (/api/v1/*) y
 * los roles ADMIN / AFICIONADO. Fuente: context/informe-permisos-ms-security.md.
 * Las URLs ya estan normalizadas ({id}), como las busca PermissionService.
 */
@Configuration
@RequiredArgsConstructor
public class NegocioPermissionsSeeder {
    private static final String MODEL = "negocio";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /** "METODO url" - los 149 endpoints de negocio (ADMIN). */
    private static final List<String> ALL_PERMISSIONS = List.of(

        // Parche para mostro

            "POST /api/v1/stubs/aficionados/{id}/equipos/{id}",
            "DELETE /api/v1/stubs/aficionados/{id}/equipos/{id}",
            "GET /api/v1/stubs/aficionados/{id}/equipos",
            "POST /api/v1/stubs/aficionados-equipos",
            "GET /api/v1/stubs/aficionados-equipos/{id}",
            "DELETE /api/v1/stubs/aficionados-equipos/{id}/{id}",
            "POST /api/v1/stubs/aficionados",
            "GET /api/v1/stubs/aficionados",
            "GET /api/v1/stubs/aficionados/usuario/{id}",
            "GET /api/v1/stubs/aficionados/{id}",
            "PATCH /api/v1/stubs/aficionados/{id}",
            "DELETE /api/v1/stubs/aficionados/{id}",
            "POST /api/v1/stubs/partidos/{id}/anotaciones",
            "GET /api/v1/stubs/partidos/{id}/anotaciones",
            "POST /api/v1/stubs/anotaciones",
            "GET /api/v1/stubs/anotaciones/partido/{id}",
            "DELETE /api/v1/stubs/anotaciones/{id}",
            "POST /api/v1/stubs/apuestas",
            "GET /api/v1/stubs/apuestas",
            "GET /api/v1/stubs/aficionados/{id}/apuestas",
            "GET /api/v1/stubs/partidos/{id}/apuestas",
            "GET /api/v1/stubs/apuestas/partido/{id}/cuota",
            "PATCH /api/v1/stubs/apuestas/{id}/cancelar",
            "POST /api/v1/stubs/boletos",
            "GET /api/v1/stubs/partidos/{id}/boletos",
            "GET /api/v1/stubs/boletos/partido/{id}",
            "GET /api/v1/stubs/aficionados/{id}/boletos",
            "PATCH /api/v1/stubs/boletos/{id}/estado",
            "POST /api/v1/stubs/deportes",
            "GET /api/v1/stubs/deportes",
            "GET /api/v1/stubs/deportes/{id}",
            "GET /api/v1/stubs/deportes/{id}/subdeportes",
            "PUT /api/v1/stubs/deportes/{id}",
            "PATCH /api/v1/stubs/deportes/{id}/estado",
            "DELETE /api/v1/stubs/deportes/{id}",
            "POST /api/v1/stubs/equipos/{id}/deportes/{id}",
            "DELETE /api/v1/stubs/equipos/{id}/deportes/{id}",
            "GET /api/v1/stubs/equipos/{id}/deportes",
            "GET /api/v1/stubs/deportes/{id}/equipos",
            "POST /api/v1/stubs/equipos/{id}/jugadores",
            "PUT /api/v1/stubs/equipos/{id}/jugadores/{id}",
            "DELETE /api/v1/stubs/equipos/{id}/jugadores/{id}",
            "GET /api/v1/stubs/equipos/{id}/jugadores",
            "GET /api/v1/stubs/jugadores/{id}/equipos",
            "POST /api/v1/stubs/equipos",
            "GET /api/v1/stubs/equipos",
            "GET /api/v1/stubs/equipos/{id}",
            "PUT /api/v1/stubs/equipos/{id}",
            "PATCH /api/v1/stubs/equipos/{id}/estado",
            "DELETE /api/v1/stubs/equipos/{id}",
            "POST /api/v1/stubs/torneos/{id}/escenarios/{id}",
            "DELETE /api/v1/stubs/torneos/{id}/escenarios/{id}",
            "GET /api/v1/stubs/torneos/{id}/escenarios",
            "GET /api/v1/stubs/escenarios/{id}/torneos",
            "POST /api/v1/stubs/escenarios",
            "GET /api/v1/stubs/escenarios",
            "GET /api/v1/stubs/escenarios/{id}",
            "GET /api/v1/stubs/escenarios/{id}/subescenarios",
            "POST /api/v1/stubs/escenarios/{id}/subescenarios",
            "PUT /api/v1/stubs/escenarios/{id}",
            "PATCH /api/v1/stubs/escenarios/{id}/estado",
            "DELETE /api/v1/stubs/escenarios/{id}",
            "POST /api/v1/stubs/torneos/{id}/fase-final",
            "GET /api/v1/stubs/torneos/{id}/fase-final",
            "PUT /api/v1/stubs/torneos/{id}/fase-final",
            "DELETE /api/v1/stubs/torneos/{id}/fase-final",
            "POST /api/v1/stubs/torneos/{id}/fases",
            "GET /api/v1/stubs/torneos/{id}/fases",
            "GET /api/v1/stubs/fases/{id}",
            "PUT /api/v1/stubs/fases/{id}",
            "DELETE /api/v1/stubs/fases/{id}",
            "POST /api/v1/stubs/fases/{id}/grupos",
            "GET /api/v1/stubs/fases/{id}/grupos",
            "GET /api/v1/stubs/grupos/{id}",
            "PUT /api/v1/stubs/grupos/{id}",
            "DELETE /api/v1/stubs/grupos/{id}",
            "GET /api/v1/stubs/grupos/{id}/equipos",
            "POST /api/v1/stubs/grupos/{id}/equipos",
            "DELETE /api/v1/stubs/grupos/{id}/equipos/{id}",
            "POST /api/v1/stubs/jugadores/{id}/habilidades/{id}",
            "DELETE /api/v1/stubs/jugadores/{id}/habilidades/{id}",
            "GET /api/v1/stubs/jugadores/{id}/habilidades",
            "GET /api/v1/stubs/habilidades/{id}/jugadores",
            "POST /api/v1/stubs/habilidades-jugador",
            "GET /api/v1/stubs/habilidades-jugador/{id}",
            "DELETE /api/v1/stubs/habilidades-jugador/{id}/{id}",
            "POST /api/v1/stubs/habilidades",
            "GET /api/v1/stubs/habilidades",
            "GET /api/v1/stubs/habilidades/{id}",
            "PUT /api/v1/stubs/habilidades/{id}",
            "DELETE /api/v1/stubs/habilidades/{id}",
            "POST /api/v1/stubs/torneos/{id}/inscripciones",
            "DELETE /api/v1/stubs/torneos/{id}/inscripciones/{id}",
            "GET /api/v1/stubs/torneos/{id}/inscripciones",
            "GET /api/v1/stubs/torneos/{id}/equipos",
            "GET /api/v1/stubs/equipos/{id}/torneos",
            "POST /api/v1/stubs/jugadores",
            "GET /api/v1/stubs/jugadores",
            "GET /api/v1/stubs/jugadores/{id}",
            "GET /api/v1/stubs/jugadores/{id}/perfil",
            "PUT /api/v1/stubs/jugadores/{id}",
            "PATCH /api/v1/stubs/jugadores/{id}/estado",
            "DELETE /api/v1/stubs/jugadores/{id}",
            "POST /api/v1/stubs/localidades",
            "GET /api/v1/stubs/escenarios/{id}/localidades",
            "GET /api/v1/stubs/localidades/escenario/{id}",
            "GET /api/v1/stubs/localidades/{id}",
            "DELETE /api/v1/stubs/localidades/{id}",
            "POST /api/v1/stubs/aficionados/{id}/metodos-pago",
            "GET /api/v1/stubs/aficionados/{id}/metodos-pago",
            "DELETE /api/v1/stubs/aficionados/{id}/metodos-pago/{id}",
            "PUT /api/v1/aficionados/{id}/metodos-pago/{id}",
            "POST /api/v1/stubs/metodos-pago-aficionado",
            "GET /api/v1/stubs/metodos-pago-aficionado/{id}",
            "DELETE /api/v1/stubs/metodos-pago-aficionado/{id}",
            "POST /api/v1/stubs/metodos-pago",
            "GET /api/v1/stubs/metodos-pago",
            "GET /api/v1/stubs/metodos-pago/{id}",
            "PUT /api/v1/stubs/metodos-pago/{id}",
            "DELETE /api/v1/stubs/metodos-pago/{id}",
            "POST /api/v1/stubs/partidos",
            "GET /api/v1/stubs/partidos/disponibles-apuesta",
            "GET /api/v1/stubs/partidos",
            "GET /api/v1/stubs/partidos/{id}",
            "PATCH /api/v1/stubs/partidos/{id}/estado",
            "POST /api/v1/stubs/partidos/{id}/finalizar",
            "POST /api/v1/stubs/partidos/{id}/resultado",
            "POST /api/v1/stubs/partidos/{id}/posiciones",
            "GET /api/v1/stubs/partidos/{id}/posiciones/ultimas",
            "GET /api/v1/stubs/partidos/{id}/posiciones",
            "POST /api/v1/stubs/pelotas",
            "GET /api/v1/stubs/pelotas",
            "GET /api/v1/stubs/pelotas/{id}",
            "PATCH /api/v1/stubs/pelotas/{id}",
            "DELETE /api/v1/stubs/pelotas/{id}",
            "POST /api/v1/stubs/posiciones-espaciales",
            "GET /api/v1/stubs/posiciones-espaciales",
            "GET /api/v1/stubs/posiciones-espaciales/{id}",
            "DELETE /api/v1/stubs/posiciones-espaciales/{id}",
            "GET /api/v1/stubs/torneos/{id}/desarrollo",
            "GET /api/v1/stubs/torneos-desarrollo/{id}/desarrollo",
            "GET /api/v1/stubs/torneos/{id}/tabla-posiciones",
            "GET /api/v1/stubs/torneos-desarrollo/{id}/tabla-posiciones",
            "POST /api/v1/stubs/torneos",
            "GET /api/v1/stubs/torneos",
            "GET /api/v1/stubs/torneos/{id}",
            "GET /api/v1/stubs/torneos/{id}/estructura",
            "PUT /api/v1/stubs/torneos/{id}",
            "PATCH /api/v1/stubs/torneos/{id}/estado",
            "DELETE /api/v1/stubs/torneos/{id}",

        // Rutas normales

            "POST /api/v1/aficionados/{id}/equipos/{id}",
            "DELETE /api/v1/aficionados/{id}/equipos/{id}",
            "GET /api/v1/aficionados/{id}/equipos",
            "POST /api/v1/aficionados-equipos",
            "GET /api/v1/aficionados-equipos/{id}",
            "DELETE /api/v1/aficionados-equipos/{id}/{id}",
            "POST /api/v1/aficionados",
            "GET /api/v1/aficionados",
            "GET /api/v1/aficionados/usuario/{id}",
            "GET /api/v1/aficionados/{id}",
            "PATCH /api/v1/aficionados/{id}",
            "DELETE /api/v1/aficionados/{id}",
            "POST /api/v1/partidos/{id}/anotaciones",
            "GET /api/v1/partidos/{id}/anotaciones",
            "POST /api/v1/anotaciones",
            "GET /api/v1/anotaciones/partido/{id}",
            "DELETE /api/v1/anotaciones/{id}",
            "POST /api/v1/apuestas",
            "GET /api/v1/apuestas",
            "GET /api/v1/aficionados/{id}/apuestas",
            "GET /api/v1/partidos/{id}/apuestas",
            "GET /api/v1/apuestas/partido/{id}/cuota",
            "PATCH /api/v1/apuestas/{id}/cancelar",
            "POST /api/v1/boletos",
            "GET /api/v1/partidos/{id}/boletos",
            "GET /api/v1/boletos/partido/{id}",
            "GET /api/v1/aficionados/{id}/boletos",
            "PATCH /api/v1/boletos/{id}/estado",
            "POST /api/v1/deportes",
            "GET /api/v1/deportes",
            "GET /api/v1/deportes/{id}",
            "GET /api/v1/deportes/{id}/subdeportes",
            "PUT /api/v1/deportes/{id}",
            "PATCH /api/v1/deportes/{id}/estado",
            "DELETE /api/v1/deportes/{id}",
            "POST /api/v1/equipos/{id}/deportes/{id}",
            "DELETE /api/v1/equipos/{id}/deportes/{id}",
            "GET /api/v1/equipos/{id}/deportes",
            "GET /api/v1/deportes/{id}/equipos",
            "POST /api/v1/equipos/{id}/jugadores",
            "PUT /api/v1/equipos/{id}/jugadores/{id}",
            "DELETE /api/v1/equipos/{id}/jugadores/{id}",
            "GET /api/v1/equipos/{id}/jugadores",
            "GET /api/v1/jugadores/{id}/equipos",
            "POST /api/v1/equipos",
            "GET /api/v1/equipos",
            "GET /api/v1/equipos/{id}",
            "PUT /api/v1/equipos/{id}",
            "PATCH /api/v1/equipos/{id}/estado",
            "DELETE /api/v1/equipos/{id}",
            "POST /api/v1/torneos/{id}/escenarios/{id}",
            "DELETE /api/v1/torneos/{id}/escenarios/{id}",
            "GET /api/v1/torneos/{id}/escenarios",
            "GET /api/v1/escenarios/{id}/torneos",
            "POST /api/v1/escenarios",
            "GET /api/v1/escenarios",
            "GET /api/v1/escenarios/{id}",
            "GET /api/v1/escenarios/{id}/subescenarios",
            "POST /api/v1/escenarios/{id}/subescenarios",
            "PUT /api/v1/escenarios/{id}",
            "PATCH /api/v1/escenarios/{id}/estado",
            "DELETE /api/v1/escenarios/{id}",
            "POST /api/v1/torneos/{id}/fase-final",
            "GET /api/v1/torneos/{id}/fase-final",
            "PUT /api/v1/torneos/{id}/fase-final",
            "DELETE /api/v1/torneos/{id}/fase-final",
            "POST /api/v1/torneos/{id}/fases",
            "GET /api/v1/torneos/{id}/fases",
            "GET /api/v1/fases/{id}",
            "PUT /api/v1/fases/{id}",
            "DELETE /api/v1/fases/{id}",
            "POST /api/v1/fases/{id}/grupos",
            "GET /api/v1/fases/{id}/grupos",
            "GET /api/v1/grupos/{id}",
            "PUT /api/v1/grupos/{id}",
            "DELETE /api/v1/grupos/{id}",
            "GET /api/v1/grupos/{id}/equipos",
            "POST /api/v1/grupos/{id}/equipos",
            "DELETE /api/v1/grupos/{id}/equipos/{id}",
            "POST /api/v1/jugadores/{id}/habilidades/{id}",
            "DELETE /api/v1/jugadores/{id}/habilidades/{id}",
            "GET /api/v1/jugadores/{id}/habilidades",
            "GET /api/v1/habilidades/{id}/jugadores",
            "POST /api/v1/habilidades-jugador",
            "GET /api/v1/habilidades-jugador/{id}",
            "DELETE /api/v1/habilidades-jugador/{id}/{id}",
            "POST /api/v1/habilidades",
            "GET /api/v1/habilidades",
            "GET /api/v1/habilidades/{id}",
            "PUT /api/v1/habilidades/{id}",
            "DELETE /api/v1/habilidades/{id}",
            "POST /api/v1/torneos/{id}/inscripciones",
            "DELETE /api/v1/torneos/{id}/inscripciones/{id}",
            "GET /api/v1/torneos/{id}/inscripciones",
            "GET /api/v1/torneos/{id}/equipos",
            "GET /api/v1/equipos/{id}/torneos",
            "POST /api/v1/jugadores",
            "GET /api/v1/jugadores",
            "GET /api/v1/jugadores/{id}",
            "GET /api/v1/jugadores/{id}/perfil",
            "PUT /api/v1/jugadores/{id}",
            "PATCH /api/v1/jugadores/{id}/estado",
            "DELETE /api/v1/jugadores/{id}",
            "POST /api/v1/localidades",
            "GET /api/v1/escenarios/{id}/localidades",
            "GET /api/v1/localidades/escenario/{id}",
            "GET /api/v1/localidades/{id}",
            "DELETE /api/v1/localidades/{id}",
            "POST /api/v1/aficionados/{id}/metodos-pago",
            "GET /api/v1/aficionados/{id}/metodos-pago",
            "DELETE /api/v1/aficionados/{id}/metodos-pago/{id}",
            "PUT /api/v1/aficionados/{id}/metodos-pago/{id}",
            "POST /api/v1/metodos-pago-aficionado",
            "GET /api/v1/metodos-pago-aficionado/{id}",
            "DELETE /api/v1/metodos-pago-aficionado/{id}",
            "POST /api/v1/metodos-pago",
            "GET /api/v1/metodos-pago",
            "GET /api/v1/metodos-pago/{id}",
            "PUT /api/v1/metodos-pago/{id}",
            "DELETE /api/v1/metodos-pago/{id}",
            "POST /api/v1/partidos",
            "GET /api/v1/partidos/disponibles-apuesta",
            "GET /api/v1/partidos",
            "GET /api/v1/partidos/{id}",
            "PATCH /api/v1/partidos/{id}/estado",
            "POST /api/v1/partidos/{id}/finalizar",
            "POST /api/v1/partidos/{id}/resultado",
            "POST /api/v1/partidos/{id}/posiciones",
            "GET /api/v1/partidos/{id}/posiciones/ultimas",
            "GET /api/v1/partidos/{id}/posiciones",
            "POST /api/v1/pelotas",
            "GET /api/v1/pelotas",
            "GET /api/v1/pelotas/{id}",
            "PATCH /api/v1/pelotas/{id}",
            "DELETE /api/v1/pelotas/{id}",
            "POST /api/v1/posiciones-espaciales",
            "GET /api/v1/posiciones-espaciales",
            "GET /api/v1/posiciones-espaciales/{id}",
            "DELETE /api/v1/posiciones-espaciales/{id}",
            "GET /api/v1/torneos/{id}/desarrollo",
            "GET /api/v1/torneos-desarrollo/{id}/desarrollo",
            "GET /api/v1/torneos/{id}/tabla-posiciones",
            "GET /api/v1/torneos-desarrollo/{id}/tabla-posiciones",
            "POST /api/v1/torneos",
            "GET /api/v1/torneos",
            "GET /api/v1/torneos/{id}",
            "GET /api/v1/torneos/{id}/estructura",
            "PUT /api/v1/torneos/{id}",
            "PATCH /api/v1/torneos/{id}/estado",
            "DELETE /api/v1/torneos/{id}"
    );

    /** Subconjunto de 76 permisos para AFICIONADO. */
    private static final List<String> AFICIONADO_PERMISSIONS = List.of(
            "POST /api/v1/aficionados/{id}/equipos/{id}",
            "DELETE /api/v1/aficionados/{id}/equipos/{id}",
            "GET /api/v1/aficionados/{id}/equipos",
            "POST /api/v1/aficionados-equipos",
            "GET /api/v1/aficionados-equipos/{id}",
            "DELETE /api/v1/aficionados-equipos/{id}/{id}",
            "POST /api/v1/aficionados",
            "GET /api/v1/aficionados/usuario/{id}",
            "GET /api/v1/aficionados/{id}",
            "PATCH /api/v1/aficionados/{id}",
            "GET /api/v1/partidos/{id}/anotaciones",
            "GET /api/v1/anotaciones/partido/{id}",
            "POST /api/v1/apuestas",
            "GET /api/v1/aficionados/{id}/apuestas",
            "GET /api/v1/apuestas/partido/{id}/cuota",
            "PATCH /api/v1/apuestas/{id}/cancelar",
            "POST /api/v1/boletos",
            "GET /api/v1/aficionados/{id}/boletos",
            "GET /api/v1/deportes",
            "GET /api/v1/deportes/{id}",
            "GET /api/v1/deportes/{id}/subdeportes",
            "GET /api/v1/equipos/{id}/deportes",
            "GET /api/v1/deportes/{id}/equipos",
            "GET /api/v1/equipos/{id}/jugadores",
            "GET /api/v1/jugadores/{id}/equipos",
            "GET /api/v1/equipos",
            "GET /api/v1/equipos/{id}",
            "GET /api/v1/torneos/{id}/escenarios",
            "GET /api/v1/escenarios/{id}/torneos",
            "GET /api/v1/escenarios",
            "GET /api/v1/escenarios/{id}",
            "GET /api/v1/escenarios/{id}/subescenarios",
            "GET /api/v1/torneos/{id}/fase-final",
            "GET /api/v1/torneos/{id}/fases",
            "GET /api/v1/fases/{id}",
            "GET /api/v1/fases/{id}/grupos",
            "GET /api/v1/grupos/{id}",
            "GET /api/v1/grupos/{id}/equipos",
            "GET /api/v1/jugadores/{id}/habilidades",
            "GET /api/v1/habilidades/{id}/jugadores",
            "GET /api/v1/habilidades-jugador/{id}",
            "GET /api/v1/habilidades",
            "GET /api/v1/habilidades/{id}",
            "GET /api/v1/torneos/{id}/inscripciones",
            "GET /api/v1/torneos/{id}/equipos",
            "GET /api/v1/equipos/{id}/torneos",
            "GET /api/v1/jugadores",
            "GET /api/v1/jugadores/{id}",
            "GET /api/v1/jugadores/{id}/perfil",
            "GET /api/v1/escenarios/{id}/localidades",
            "GET /api/v1/localidades/escenario/{id}",
            "GET /api/v1/localidades/{id}",
            "POST /api/v1/aficionados/{id}/metodos-pago",
            "GET /api/v1/aficionados/{id}/metodos-pago",
            "DELETE /api/v1/aficionados/{id}/metodos-pago/{id}",
            "PUT /api/v1/aficionados/{id}/metodos-pago/{id}",
            "POST /api/v1/metodos-pago-aficionado",
            "GET /api/v1/metodos-pago-aficionado/{id}",
            "DELETE /api/v1/metodos-pago-aficionado/{id}",
            "GET /api/v1/metodos-pago",
            "GET /api/v1/metodos-pago/{id}",
            "GET /api/v1/partidos/disponibles-apuesta",
            "GET /api/v1/partidos",
            "GET /api/v1/partidos/{id}",
            "GET /api/v1/partidos/{id}/posiciones/ultimas",
            "GET /api/v1/partidos/{id}/posiciones",
            "GET /api/v1/pelotas",
            "GET /api/v1/pelotas/{id}",
            "GET /api/v1/posiciones-espaciales",
            "GET /api/v1/posiciones-espaciales/{id}",
            "GET /api/v1/torneos/{id}/desarrollo",
            "GET /api/v1/torneos-desarrollo/{id}/desarrollo",
            "GET /api/v1/torneos/{id}/tabla-posiciones",
            "GET /api/v1/torneos-desarrollo/{id}/tabla-posiciones",
            "GET /api/v1/torneos",
            "GET /api/v1/torneos/{id}",
            "GET /api/v1/torneos/{id}/estructura"
    );

    @Bean
    @Order(2)
    CommandLineRunner seedNegocioPermissions() {
        return args -> {
            Role admin = findOrCreateRole("ADMIN", "Administrador del sistema");
            Role aficionado = findOrCreateRole("AFICIONADO", "Aficionado: consulta y gestion de sus propios datos");

            for (String entry : ALL_PERMISSIONS) {
                Permission permission = findOrCreatePermission(entry);
                assignPermission(admin, permission);
                if (AFICIONADO_PERMISSIONS.contains(entry)) {
                    assignPermission(aficionado, permission);
                }
            }
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

    private Permission findOrCreatePermission(String entry) {
        int separator = entry.indexOf(' ');
        String method = entry.substring(0, separator);
        String url = entry.substring(separator + 1);
        return permissionRepository.findByUrlAndMethod(url, method)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setUrl(url);
                    permission.setMethod(method);
                    permission.setModel(MODEL);
                    return permissionRepository.save(permission);
                });
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
