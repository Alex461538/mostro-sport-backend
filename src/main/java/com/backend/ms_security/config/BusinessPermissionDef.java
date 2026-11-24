package com.backend.ms_security.config;

/**
 * Definición inmutable de un permiso del microservicio de negocio.
 */
public record BusinessPermissionDef(String method, String url, boolean aficionado) {
}
