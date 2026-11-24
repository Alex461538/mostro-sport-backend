package com.backend.ms_security.config;

import java.util.List;

/**
 * Permisos de partidos, aficionados, boletos, apuestas y seguimiento (Participante B).
 */
final class BusinessOperationsPermissions {

    private BusinessOperationsPermissions() {
    }

    static List<BusinessPermissionDef> all() {
        return List.of(
            new BusinessPermissionDef("POST", "/api/v1/partidos", false),
            new BusinessPermissionDef("GET", "/api/v1/partidos/disponibles-apuesta", true),
            new BusinessPermissionDef("GET", "/api/v1/partidos", true),
            new BusinessPermissionDef("GET", "/api/v1/partidos/{id}", true),
            new BusinessPermissionDef("GET", "/api/v1/fases/{id}/partidos", true),
            new BusinessPermissionDef("PATCH", "/api/v1/partidos/{id}/estado", false),
            new BusinessPermissionDef("POST", "/api/v1/partidos/{id}/finalizar", false),
            new BusinessPermissionDef("POST", "/api/v1/partidos/{id}/resultado", false),
            new BusinessPermissionDef("PUT", "/api/v1/partidos/{id}/resultado", false),
            new BusinessPermissionDef("POST", "/api/v1/partidos/{id}/posiciones", false),
            new BusinessPermissionDef("GET", "/api/v1/partidos/{id}/posiciones/ultimas", true),
            new BusinessPermissionDef("GET", "/api/v1/partidos/{id}/posiciones", true),
            new BusinessPermissionDef("POST", "/api/v1/partidos/{id}/anotaciones", false),
            new BusinessPermissionDef("GET", "/api/v1/partidos/{id}/anotaciones", true),
            new BusinessPermissionDef("POST", "/api/v1/anotaciones", false),
            new BusinessPermissionDef("GET", "/api/v1/anotaciones/partido/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/anotaciones/{id}", false),
            new BusinessPermissionDef("POST", "/api/v1/pelotas", false),
            new BusinessPermissionDef("GET", "/api/v1/pelotas", true),
            new BusinessPermissionDef("GET", "/api/v1/pelotas/{id}", true),
            new BusinessPermissionDef("PATCH", "/api/v1/pelotas/{id}", false),
            new BusinessPermissionDef("DELETE", "/api/v1/pelotas/{id}", false),
            new BusinessPermissionDef("POST", "/api/v1/posiciones-espaciales", false),
            new BusinessPermissionDef("GET", "/api/v1/posiciones-espaciales", true),
            new BusinessPermissionDef("GET", "/api/v1/posiciones-espaciales/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/posiciones-espaciales/{id}", false),
            new BusinessPermissionDef("POST", "/api/v1/jugadores/{id}/habilidades/{id}", false),
            new BusinessPermissionDef("DELETE", "/api/v1/jugadores/{id}/habilidades/{id}", false),
            new BusinessPermissionDef("GET", "/api/v1/jugadores/{id}/habilidades", true),
            new BusinessPermissionDef("GET", "/api/v1/habilidades/{id}/jugadores", true),
            new BusinessPermissionDef("POST", "/api/v1/habilidades-jugador", false),
            new BusinessPermissionDef("GET", "/api/v1/habilidades-jugador/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/habilidades-jugador/{id}/{id}", false),
            new BusinessPermissionDef("POST", "/api/v1/localidades", false),
            new BusinessPermissionDef("GET", "/api/v1/escenarios/{id}/localidades", true),
            new BusinessPermissionDef("GET", "/api/v1/localidades/escenario/{id}", true),
            new BusinessPermissionDef("GET", "/api/v1/localidades/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/localidades/{id}", false),
            new BusinessPermissionDef("POST", "/api/v1/boletos", true),
            new BusinessPermissionDef("GET", "/api/v1/partidos/{id}/boletos", false),
            new BusinessPermissionDef("GET", "/api/v1/boletos/partido/{id}", false),
            new BusinessPermissionDef("GET", "/api/v1/aficionados/{id}/boletos", true),
            new BusinessPermissionDef("PATCH", "/api/v1/boletos/{id}/estado", false),
            new BusinessPermissionDef("POST", "/api/v1/aficionados", true),
            new BusinessPermissionDef("GET", "/api/v1/aficionados", false),
            new BusinessPermissionDef("GET", "/api/v1/aficionados/usuario/{id}", true),
            new BusinessPermissionDef("GET", "/api/v1/aficionados/{id}", true),
            new BusinessPermissionDef("PATCH", "/api/v1/aficionados/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/aficionados/{id}", false),
            new BusinessPermissionDef("POST", "/api/v1/aficionados/{id}/equipos/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/aficionados/{id}/equipos/{id}", true),
            new BusinessPermissionDef("GET", "/api/v1/aficionados/{id}/equipos", true),
            new BusinessPermissionDef("POST", "/api/v1/aficionados-equipos", true),
            new BusinessPermissionDef("GET", "/api/v1/aficionados-equipos/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/aficionados-equipos/{id}/{id}", true),
            new BusinessPermissionDef("POST", "/api/v1/aficionados/{id}/metodos-pago", true),
            new BusinessPermissionDef("GET", "/api/v1/aficionados/{id}/metodos-pago", true),
            new BusinessPermissionDef("DELETE", "/api/v1/aficionados/{id}/metodos-pago/{id}", true),
            new BusinessPermissionDef("POST", "/api/v1/metodos-pago-aficionado", true),
            new BusinessPermissionDef("GET", "/api/v1/metodos-pago-aficionado/{id}", true),
            new BusinessPermissionDef("DELETE", "/api/v1/metodos-pago-aficionado/{id}", true),
            new BusinessPermissionDef("POST", "/api/v1/apuestas", true),
            new BusinessPermissionDef("GET", "/api/v1/apuestas", false),
            new BusinessPermissionDef("GET", "/api/v1/apuestas/mis-apuestas", true),
            new BusinessPermissionDef("GET", "/api/v1/aficionados/{id}/apuestas", true),
            new BusinessPermissionDef("GET", "/api/v1/partidos/{id}/apuestas", false),
            new BusinessPermissionDef("GET", "/api/v1/apuestas/partido/{id}/cuota", true),
            new BusinessPermissionDef("PATCH", "/api/v1/apuestas/{id}/cancelar", true),
            new BusinessPermissionDef("POST", "/api/v1/apuestas/{id}/cancelar", true),
            new BusinessPermissionDef("GET", "/api/v1/torneos/{id}/desarrollo", true),
            new BusinessPermissionDef("GET", "/api/v1/torneos-desarrollo/{id}/desarrollo", true),
            new BusinessPermissionDef("GET", "/api/v1/torneos/{id}/tabla-posiciones", true),
            new BusinessPermissionDef("GET", "/api/v1/torneos-desarrollo/{id}/tabla-posiciones", true)
        );
    }
}
