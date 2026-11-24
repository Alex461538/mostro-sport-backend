# Informe para ms_security — Permisos del Microservicio de Negocio

Fecha: 2026-11-23 · Origen: `ms_negocio` (NestJS, puerto 3000) · Destino: `ms_security` (Spring Boot, puerto 8181)

## 1. Qué debe hacer ms_security

ms_negocio **no tiene roles propios**: delega toda la autorización. Por cada request HTTP entrante llama a:

```
POST http://localhost:8181/api/internal/security/permissions-validation
Authorization: Bearer <mismo JWT del usuario>
Content-Type: application/json

{ "url": "/api/v1/equipos/5", "method": "GET" }

→ 200 { "allowed": true | false }
```

`PermissionService.hasPermission` normaliza la URL (segmentos numéricos → `{id}`) y busca coincidencia **exacta** por `(method, url)` en `permissions` → `role_permissions` → `user_roles` del usuario (`sub` del JWT). Por tanto:

- Cada endpoint del negocio debe existir en `permissions` con la **URL ya normalizada** (ej. `/api/v1/equipos/{id}`), método en mayúsculas y `model = 'negocio'`.
- No hay comodines: `/api/v1/equipos/{id}` y `/api/v1/equipos/{id}/jugadores` son permisos distintos.
- La query string no forma parte de la URL validada; no se registra.
- Sin permiso, el negocio responde **403**.

### Estado actual del seeder (`DatabaseSeeder.java`)
Solo crea el rol `ADMIN` con permisos del propio ms_security (`/api/users`, `/api/roles`, …). **No existe ningún permiso `/api/v1/*`**: con `DEV_SECURITY_BYPASS=false` toda llamada al negocio devuelve 403 hasta cargar los permisos de este informe.

## 2. Roles propuestos

| Rol | Descripción | Permisos |
|---|---|---|
| `ADMIN` (o `ADMIN_DEPORTIVO`) | Gestión completa del negocio | Los 149 permisos de la sección 4 |
| `AFICIONADO` | Consulta + gestión de lo propio (perfil, favoritos, métodos de pago, boletos, apuestas) | 76 permisos marcados ✔ en la sección 4 |

El plan de trabajo habla de `ADMIN_DEPORTIVO` / `AFICIONADO`; el seeder actual usa `ADMIN`. Usen el nombre que decidan, pero el mismo en el script SQL.

**Limitación:** el modelo `(método, url)` no distingue la *propiedad del recurso*. Un AFICIONADO con `GET /api/v1/aficionados/{id}/apuestas` podría consultar las de otro `id`. Si se requiere, hay que validar en el negocio `aficionado.usuarioId` contra el `sub` del JWT (pendiente, no implementado) o ampliar el contrato de validación.

## 3. Endpoints excluidos (no dar permiso)

| Método | URL | Motivo |
|---|---|---|
| POST | `/api/v1/stubs/seed` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/health` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/torneos` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/fases/{id}` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/escenarios` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/equipos` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/jugadores` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/pelotas` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/habilidades` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |
| GET | `/api/v1/stubs/metodos-pago` | Utilidad de desarrollo (`stubs`); `POST /stubs/seed` pobla datos. Deshabilitar el módulo en producción |

Los eventos WebSocket (Socket.IO) tampoco pasan por esta validación.

## 4. Matriz de permisos (149)

✔ = también para AFICIONADO. ADMIN recibe todos.

| # | Método | URL (normalizada) | Módulo | ADMIN | AFICIONADO |
|---|---|---|---|:-:|:-:|
| 1 | POST | `/api/v1/aficionados/{id}/equipos/{id}` | aficionados-equipos | ✔ | ✔ |
| 2 | DELETE | `/api/v1/aficionados/{id}/equipos/{id}` | aficionados-equipos | ✔ | ✔ |
| 3 | GET | `/api/v1/aficionados/{id}/equipos` | aficionados-equipos | ✔ | ✔ |
| 4 | POST | `/api/v1/aficionados-equipos` | aficionados-equipos | ✔ | ✔ |
| 5 | GET | `/api/v1/aficionados-equipos/{id}` | aficionados-equipos | ✔ | ✔ |
| 6 | DELETE | `/api/v1/aficionados-equipos/{id}/{id}` | aficionados-equipos | ✔ | ✔ |
| 7 | POST | `/api/v1/aficionados` | aficionados | ✔ | ✔ |
| 8 | GET | `/api/v1/aficionados` | aficionados | ✔ | — |
| 9 | GET | `/api/v1/aficionados/usuario/{id}` | aficionados | ✔ | ✔ |
| 10 | GET | `/api/v1/aficionados/{id}` | aficionados | ✔ | ✔ |
| 11 | PATCH | `/api/v1/aficionados/{id}` | aficionados | ✔ | ✔ |
| 12 | DELETE | `/api/v1/aficionados/{id}` | aficionados | ✔ | — |
| 13 | POST | `/api/v1/partidos/{id}/anotaciones` | anotaciones | ✔ | — |
| 14 | GET | `/api/v1/partidos/{id}/anotaciones` | anotaciones | ✔ | ✔ |
| 15 | POST | `/api/v1/anotaciones` | anotaciones | ✔ | — |
| 16 | GET | `/api/v1/anotaciones/partido/{id}` | anotaciones | ✔ | ✔ |
| 17 | DELETE | `/api/v1/anotaciones/{id}` | anotaciones | ✔ | — |
| 18 | POST | `/api/v1/apuestas` | apuestas | ✔ | ✔ |
| 19 | GET | `/api/v1/apuestas` | apuestas | ✔ | — |
| 20 | GET | `/api/v1/aficionados/{id}/apuestas` | apuestas | ✔ | ✔ |
| 21 | GET | `/api/v1/partidos/{id}/apuestas` | apuestas | ✔ | — |
| 22 | GET | `/api/v1/apuestas/partido/{id}/cuota` | apuestas | ✔ | ✔ |
| 23 | PATCH | `/api/v1/apuestas/{id}/cancelar` | apuestas | ✔ | ✔ |
| 24 | POST | `/api/v1/boletos` | boletos | ✔ | ✔ |
| 25 | GET | `/api/v1/partidos/{id}/boletos` | boletos | ✔ | — |
| 26 | GET | `/api/v1/boletos/partido/{id}` | boletos | ✔ | — |
| 27 | GET | `/api/v1/aficionados/{id}/boletos` | boletos | ✔ | ✔ |
| 28 | PATCH | `/api/v1/boletos/{id}/estado` | boletos | ✔ | — |
| 29 | POST | `/api/v1/deportes` | deportes | ✔ | — |
| 30 | GET | `/api/v1/deportes` | deportes | ✔ | ✔ |
| 31 | GET | `/api/v1/deportes/{id}` | deportes | ✔ | ✔ |
| 32 | GET | `/api/v1/deportes/{id}/subdeportes` | deportes | ✔ | ✔ |
| 33 | PUT | `/api/v1/deportes/{id}` | deportes | ✔ | — |
| 34 | PATCH | `/api/v1/deportes/{id}/estado` | deportes | ✔ | — |
| 35 | DELETE | `/api/v1/deportes/{id}` | deportes | ✔ | — |
| 36 | POST | `/api/v1/equipos/{id}/deportes/{id}` | equipos-deportes | ✔ | — |
| 37 | DELETE | `/api/v1/equipos/{id}/deportes/{id}` | equipos-deportes | ✔ | — |
| 38 | GET | `/api/v1/equipos/{id}/deportes` | equipos-deportes | ✔ | ✔ |
| 39 | GET | `/api/v1/deportes/{id}/equipos` | equipos-deportes | ✔ | ✔ |
| 40 | POST | `/api/v1/equipos/{id}/jugadores` | equipos-jugadores | ✔ | — |
| 41 | PUT | `/api/v1/equipos/{id}/jugadores/{id}` | equipos-jugadores | ✔ | — |
| 42 | DELETE | `/api/v1/equipos/{id}/jugadores/{id}` | equipos-jugadores | ✔ | — |
| 43 | GET | `/api/v1/equipos/{id}/jugadores` | equipos-jugadores | ✔ | ✔ |
| 44 | GET | `/api/v1/jugadores/{id}/equipos` | equipos-jugadores | ✔ | ✔ |
| 45 | POST | `/api/v1/equipos` | equipos | ✔ | — |
| 46 | GET | `/api/v1/equipos` | equipos | ✔ | ✔ |
| 47 | GET | `/api/v1/equipos/{id}` | equipos | ✔ | ✔ |
| 48 | PUT | `/api/v1/equipos/{id}` | equipos | ✔ | — |
| 49 | PATCH | `/api/v1/equipos/{id}/estado` | equipos | ✔ | — |
| 50 | DELETE | `/api/v1/equipos/{id}` | equipos | ✔ | — |
| 51 | POST | `/api/v1/torneos/{id}/escenarios/{id}` | escenarios-torneos | ✔ | — |
| 52 | DELETE | `/api/v1/torneos/{id}/escenarios/{id}` | escenarios-torneos | ✔ | — |
| 53 | GET | `/api/v1/torneos/{id}/escenarios` | escenarios-torneos | ✔ | ✔ |
| 54 | GET | `/api/v1/escenarios/{id}/torneos` | escenarios-torneos | ✔ | ✔ |
| 55 | POST | `/api/v1/escenarios` | escenarios | ✔ | — |
| 56 | GET | `/api/v1/escenarios` | escenarios | ✔ | ✔ |
| 57 | GET | `/api/v1/escenarios/{id}` | escenarios | ✔ | ✔ |
| 58 | GET | `/api/v1/escenarios/{id}/subescenarios` | escenarios | ✔ | ✔ |
| 59 | POST | `/api/v1/escenarios/{id}/subescenarios` | escenarios | ✔ | — |
| 60 | PUT | `/api/v1/escenarios/{id}` | escenarios | ✔ | — |
| 61 | PATCH | `/api/v1/escenarios/{id}/estado` | escenarios | ✔ | — |
| 62 | DELETE | `/api/v1/escenarios/{id}` | escenarios | ✔ | — |
| 63 | POST | `/api/v1/torneos/{id}/fase-final` | fases-finales | ✔ | — |
| 64 | GET | `/api/v1/torneos/{id}/fase-final` | fases-finales | ✔ | ✔ |
| 65 | PUT | `/api/v1/torneos/{id}/fase-final` | fases-finales | ✔ | — |
| 66 | DELETE | `/api/v1/torneos/{id}/fase-final` | fases-finales | ✔ | — |
| 67 | POST | `/api/v1/torneos/{id}/fases` | fases | ✔ | — |
| 68 | GET | `/api/v1/torneos/{id}/fases` | fases | ✔ | ✔ |
| 69 | GET | `/api/v1/fases/{id}` | fases | ✔ | ✔ |
| 70 | PUT | `/api/v1/fases/{id}` | fases | ✔ | — |
| 71 | DELETE | `/api/v1/fases/{id}` | fases | ✔ | — |
| 72 | POST | `/api/v1/fases/{id}/grupos` | grupos | ✔ | — |
| 73 | GET | `/api/v1/fases/{id}/grupos` | grupos | ✔ | ✔ |
| 74 | GET | `/api/v1/grupos/{id}` | grupos | ✔ | ✔ |
| 75 | PUT | `/api/v1/grupos/{id}` | grupos | ✔ | — |
| 76 | DELETE | `/api/v1/grupos/{id}` | grupos | ✔ | — |
| 77 | GET | `/api/v1/grupos/{id}/equipos` | grupos | ✔ | ✔ |
| 78 | POST | `/api/v1/grupos/{id}/equipos` | grupos | ✔ | — |
| 79 | DELETE | `/api/v1/grupos/{id}/equipos/{id}` | grupos | ✔ | — |
| 80 | POST | `/api/v1/jugadores/{id}/habilidades/{id}` | habilidades-jugador | ✔ | — |
| 81 | DELETE | `/api/v1/jugadores/{id}/habilidades/{id}` | habilidades-jugador | ✔ | — |
| 82 | GET | `/api/v1/jugadores/{id}/habilidades` | habilidades-jugador | ✔ | ✔ |
| 83 | GET | `/api/v1/habilidades/{id}/jugadores` | habilidades-jugador | ✔ | ✔ |
| 84 | POST | `/api/v1/habilidades-jugador` | habilidades-jugador | ✔ | — |
| 85 | GET | `/api/v1/habilidades-jugador/{id}` | habilidades-jugador | ✔ | ✔ |
| 86 | DELETE | `/api/v1/habilidades-jugador/{id}/{id}` | habilidades-jugador | ✔ | — |
| 87 | POST | `/api/v1/habilidades` | habilidades | ✔ | — |
| 88 | GET | `/api/v1/habilidades` | habilidades | ✔ | ✔ |
| 89 | GET | `/api/v1/habilidades/{id}` | habilidades | ✔ | ✔ |
| 90 | PUT | `/api/v1/habilidades/{id}` | habilidades | ✔ | — |
| 91 | DELETE | `/api/v1/habilidades/{id}` | habilidades | ✔ | — |
| 92 | POST | `/api/v1/torneos/{id}/inscripciones` | inscripciones | ✔ | — |
| 93 | DELETE | `/api/v1/torneos/{id}/inscripciones/{id}` | inscripciones | ✔ | — |
| 94 | GET | `/api/v1/torneos/{id}/inscripciones` | inscripciones | ✔ | ✔ |
| 95 | GET | `/api/v1/torneos/{id}/equipos` | inscripciones | ✔ | ✔ |
| 96 | GET | `/api/v1/equipos/{id}/torneos` | inscripciones | ✔ | ✔ |
| 97 | POST | `/api/v1/jugadores` | jugadores | ✔ | — |
| 98 | GET | `/api/v1/jugadores` | jugadores | ✔ | ✔ |
| 99 | GET | `/api/v1/jugadores/{id}` | jugadores | ✔ | ✔ |
| 100 | GET | `/api/v1/jugadores/{id}/perfil` | jugadores | ✔ | ✔ |
| 101 | PUT | `/api/v1/jugadores/{id}` | jugadores | ✔ | — |
| 102 | PATCH | `/api/v1/jugadores/{id}/estado` | jugadores | ✔ | — |
| 103 | DELETE | `/api/v1/jugadores/{id}` | jugadores | ✔ | — |
| 104 | POST | `/api/v1/localidades` | localidades | ✔ | — |
| 105 | GET | `/api/v1/escenarios/{id}/localidades` | localidades | ✔ | ✔ |
| 106 | GET | `/api/v1/localidades/escenario/{id}` | localidades | ✔ | ✔ |
| 107 | GET | `/api/v1/localidades/{id}` | localidades | ✔ | ✔ |
| 108 | DELETE | `/api/v1/localidades/{id}` | localidades | ✔ | — |
| 109 | POST | `/api/v1/aficionados/{id}/metodos-pago` | metodos-pago-aficionado | ✔ | ✔ |
| 110 | GET | `/api/v1/aficionados/{id}/metodos-pago` | metodos-pago-aficionado | ✔ | ✔ |
| 111 | DELETE | `/api/v1/aficionados/{id}/metodos-pago/{id}` | metodos-pago-aficionado | ✔ | ✔ |
| 112 | POST | `/api/v1/metodos-pago-aficionado` | metodos-pago-aficionado | ✔ | ✔ |
| 113 | GET | `/api/v1/metodos-pago-aficionado/{id}` | metodos-pago-aficionado | ✔ | ✔ |
| 114 | DELETE | `/api/v1/metodos-pago-aficionado/{id}` | metodos-pago-aficionado | ✔ | ✔ |
| 115 | POST | `/api/v1/metodos-pago` | metodos-pago | ✔ | — |
| 116 | GET | `/api/v1/metodos-pago` | metodos-pago | ✔ | ✔ |
| 117 | GET | `/api/v1/metodos-pago/{id}` | metodos-pago | ✔ | ✔ |
| 118 | PUT | `/api/v1/metodos-pago/{id}` | metodos-pago | ✔ | — |
| 119 | DELETE | `/api/v1/metodos-pago/{id}` | metodos-pago | ✔ | — |
| 120 | POST | `/api/v1/partidos` | partidos | ✔ | — |
| 121 | GET | `/api/v1/partidos/disponibles-apuesta` | partidos | ✔ | ✔ |
| 122 | GET | `/api/v1/partidos` | partidos | ✔ | ✔ |
| 123 | GET | `/api/v1/partidos/{id}` | partidos | ✔ | ✔ |
| 124 | PATCH | `/api/v1/partidos/{id}/estado` | partidos | ✔ | — |
| 125 | POST | `/api/v1/partidos/{id}/finalizar` | partidos | ✔ | — |
| 126 | POST | `/api/v1/partidos/{id}/resultado` | partidos | ✔ | — |
| 127 | POST | `/api/v1/partidos/{id}/posiciones` | partidos | ✔ | — |
| 128 | GET | `/api/v1/partidos/{id}/posiciones/ultimas` | partidos | ✔ | ✔ |
| 129 | GET | `/api/v1/partidos/{id}/posiciones` | partidos | ✔ | ✔ |
| 130 | POST | `/api/v1/pelotas` | pelotas | ✔ | — |
| 131 | GET | `/api/v1/pelotas` | pelotas | ✔ | ✔ |
| 132 | GET | `/api/v1/pelotas/{id}` | pelotas | ✔ | ✔ |
| 133 | PATCH | `/api/v1/pelotas/{id}` | pelotas | ✔ | — |
| 134 | DELETE | `/api/v1/pelotas/{id}` | pelotas | ✔ | — |
| 135 | POST | `/api/v1/posiciones-espaciales` | posiciones-espaciales | ✔ | — |
| 136 | GET | `/api/v1/posiciones-espaciales` | posiciones-espaciales | ✔ | ✔ |
| 137 | GET | `/api/v1/posiciones-espaciales/{id}` | posiciones-espaciales | ✔ | ✔ |
| 138 | DELETE | `/api/v1/posiciones-espaciales/{id}` | posiciones-espaciales | ✔ | — |
| 139 | GET | `/api/v1/torneos/{id}/desarrollo` | torneos-desarrollo | ✔ | ✔ |
| 140 | GET | `/api/v1/torneos-desarrollo/{id}/desarrollo` | torneos-desarrollo | ✔ | ✔ |
| 141 | GET | `/api/v1/torneos/{id}/tabla-posiciones` | torneos-desarrollo | ✔ | ✔ |
| 142 | GET | `/api/v1/torneos-desarrollo/{id}/tabla-posiciones` | torneos-desarrollo | ✔ | ✔ |
| 143 | POST | `/api/v1/torneos` | torneos | ✔ | — |
| 144 | GET | `/api/v1/torneos` | torneos | ✔ | ✔ |
| 145 | GET | `/api/v1/torneos/{id}` | torneos | ✔ | ✔ |
| 146 | GET | `/api/v1/torneos/{id}/estructura` | torneos | ✔ | ✔ |
| 147 | PUT | `/api/v1/torneos/{id}` | torneos | ✔ | — |
| 148 | PATCH | `/api/v1/torneos/{id}/estado` | torneos | ✔ | — |
| 149 | DELETE | `/api/v1/torneos/{id}` | torneos | ✔ | — |

## 5. Script de carga (MySQL, BD `security_ms_db`)

Idempotente (`INSERT IGNORE` sobre los únicos `uk_permission_url_method` y `uk_role_permission`). Las tablas/columnas salen de las entidades JPA (`roles(name, description)`, `permissions(url, method, model)`, `role_permissions(role_id, permission_id)`); confirmar con `SHOW CREATE TABLE` antes de ejecutar.

```sql
INSERT IGNORE INTO roles (name, description) VALUES
  ('ADMIN', 'Administrador del sistema'),
  ('AFICIONADO', 'Aficionado: consulta y gestion de sus propios datos');

INSERT IGNORE INTO permissions (method, url, model) VALUES
  ('POST', '/api/v1/aficionados/{id}/equipos/{id}', 'negocio'),
  ('DELETE', '/api/v1/aficionados/{id}/equipos/{id}', 'negocio'),
  ('GET', '/api/v1/aficionados/{id}/equipos', 'negocio'),
  ('POST', '/api/v1/aficionados-equipos', 'negocio'),
  ('GET', '/api/v1/aficionados-equipos/{id}', 'negocio'),
  ('DELETE', '/api/v1/aficionados-equipos/{id}/{id}', 'negocio'),
  ('POST', '/api/v1/aficionados', 'negocio'),
  ('GET', '/api/v1/aficionados', 'negocio'),
  ('GET', '/api/v1/aficionados/usuario/{id}', 'negocio'),
  ('GET', '/api/v1/aficionados/{id}', 'negocio'),
  ('PATCH', '/api/v1/aficionados/{id}', 'negocio'),
  ('DELETE', '/api/v1/aficionados/{id}', 'negocio'),
  ('POST', '/api/v1/partidos/{id}/anotaciones', 'negocio'),
  ('GET', '/api/v1/partidos/{id}/anotaciones', 'negocio'),
  ('POST', '/api/v1/anotaciones', 'negocio'),
  ('GET', '/api/v1/anotaciones/partido/{id}', 'negocio'),
  ('DELETE', '/api/v1/anotaciones/{id}', 'negocio'),
  ('POST', '/api/v1/apuestas', 'negocio'),
  ('GET', '/api/v1/apuestas', 'negocio'),
  ('GET', '/api/v1/aficionados/{id}/apuestas', 'negocio'),
  ('GET', '/api/v1/partidos/{id}/apuestas', 'negocio'),
  ('GET', '/api/v1/apuestas/partido/{id}/cuota', 'negocio'),
  ('PATCH', '/api/v1/apuestas/{id}/cancelar', 'negocio'),
  ('POST', '/api/v1/boletos', 'negocio'),
  ('GET', '/api/v1/partidos/{id}/boletos', 'negocio'),
  ('GET', '/api/v1/boletos/partido/{id}', 'negocio'),
  ('GET', '/api/v1/aficionados/{id}/boletos', 'negocio'),
  ('PATCH', '/api/v1/boletos/{id}/estado', 'negocio'),
  ('POST', '/api/v1/deportes', 'negocio'),
  ('GET', '/api/v1/deportes', 'negocio'),
  ('GET', '/api/v1/deportes/{id}', 'negocio'),
  ('GET', '/api/v1/deportes/{id}/subdeportes', 'negocio'),
  ('PUT', '/api/v1/deportes/{id}', 'negocio'),
  ('PATCH', '/api/v1/deportes/{id}/estado', 'negocio'),
  ('DELETE', '/api/v1/deportes/{id}', 'negocio'),
  ('POST', '/api/v1/equipos/{id}/deportes/{id}', 'negocio'),
  ('DELETE', '/api/v1/equipos/{id}/deportes/{id}', 'negocio'),
  ('GET', '/api/v1/equipos/{id}/deportes', 'negocio'),
  ('GET', '/api/v1/deportes/{id}/equipos', 'negocio'),
  ('POST', '/api/v1/equipos/{id}/jugadores', 'negocio'),
  ('PUT', '/api/v1/equipos/{id}/jugadores/{id}', 'negocio'),
  ('DELETE', '/api/v1/equipos/{id}/jugadores/{id}', 'negocio'),
  ('GET', '/api/v1/equipos/{id}/jugadores', 'negocio'),
  ('GET', '/api/v1/jugadores/{id}/equipos', 'negocio'),
  ('POST', '/api/v1/equipos', 'negocio'),
  ('GET', '/api/v1/equipos', 'negocio'),
  ('GET', '/api/v1/equipos/{id}', 'negocio'),
  ('PUT', '/api/v1/equipos/{id}', 'negocio'),
  ('PATCH', '/api/v1/equipos/{id}/estado', 'negocio'),
  ('DELETE', '/api/v1/equipos/{id}', 'negocio'),
  ('POST', '/api/v1/torneos/{id}/escenarios/{id}', 'negocio'),
  ('DELETE', '/api/v1/torneos/{id}/escenarios/{id}', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/escenarios', 'negocio'),
  ('GET', '/api/v1/escenarios/{id}/torneos', 'negocio'),
  ('POST', '/api/v1/escenarios', 'negocio'),
  ('GET', '/api/v1/escenarios', 'negocio'),
  ('GET', '/api/v1/escenarios/{id}', 'negocio'),
  ('GET', '/api/v1/escenarios/{id}/subescenarios', 'negocio'),
  ('POST', '/api/v1/escenarios/{id}/subescenarios', 'negocio'),
  ('PUT', '/api/v1/escenarios/{id}', 'negocio'),
  ('PATCH', '/api/v1/escenarios/{id}/estado', 'negocio'),
  ('DELETE', '/api/v1/escenarios/{id}', 'negocio'),
  ('POST', '/api/v1/torneos/{id}/fase-final', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/fase-final', 'negocio'),
  ('PUT', '/api/v1/torneos/{id}/fase-final', 'negocio'),
  ('DELETE', '/api/v1/torneos/{id}/fase-final', 'negocio'),
  ('POST', '/api/v1/torneos/{id}/fases', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/fases', 'negocio'),
  ('GET', '/api/v1/fases/{id}', 'negocio'),
  ('PUT', '/api/v1/fases/{id}', 'negocio'),
  ('DELETE', '/api/v1/fases/{id}', 'negocio'),
  ('POST', '/api/v1/fases/{id}/grupos', 'negocio'),
  ('GET', '/api/v1/fases/{id}/grupos', 'negocio'),
  ('GET', '/api/v1/grupos/{id}', 'negocio'),
  ('PUT', '/api/v1/grupos/{id}', 'negocio'),
  ('DELETE', '/api/v1/grupos/{id}', 'negocio'),
  ('GET', '/api/v1/grupos/{id}/equipos', 'negocio'),
  ('POST', '/api/v1/grupos/{id}/equipos', 'negocio'),
  ('DELETE', '/api/v1/grupos/{id}/equipos/{id}', 'negocio'),
  ('POST', '/api/v1/jugadores/{id}/habilidades/{id}', 'negocio'),
  ('DELETE', '/api/v1/jugadores/{id}/habilidades/{id}', 'negocio'),
  ('GET', '/api/v1/jugadores/{id}/habilidades', 'negocio'),
  ('GET', '/api/v1/habilidades/{id}/jugadores', 'negocio'),
  ('POST', '/api/v1/habilidades-jugador', 'negocio'),
  ('GET', '/api/v1/habilidades-jugador/{id}', 'negocio'),
  ('DELETE', '/api/v1/habilidades-jugador/{id}/{id}', 'negocio'),
  ('POST', '/api/v1/habilidades', 'negocio'),
  ('GET', '/api/v1/habilidades', 'negocio'),
  ('GET', '/api/v1/habilidades/{id}', 'negocio'),
  ('PUT', '/api/v1/habilidades/{id}', 'negocio'),
  ('DELETE', '/api/v1/habilidades/{id}', 'negocio'),
  ('POST', '/api/v1/torneos/{id}/inscripciones', 'negocio'),
  ('DELETE', '/api/v1/torneos/{id}/inscripciones/{id}', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/inscripciones', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/equipos', 'negocio'),
  ('GET', '/api/v1/equipos/{id}/torneos', 'negocio'),
  ('POST', '/api/v1/jugadores', 'negocio'),
  ('GET', '/api/v1/jugadores', 'negocio'),
  ('GET', '/api/v1/jugadores/{id}', 'negocio'),
  ('GET', '/api/v1/jugadores/{id}/perfil', 'negocio'),
  ('PUT', '/api/v1/jugadores/{id}', 'negocio'),
  ('PATCH', '/api/v1/jugadores/{id}/estado', 'negocio'),
  ('DELETE', '/api/v1/jugadores/{id}', 'negocio'),
  ('POST', '/api/v1/localidades', 'negocio'),
  ('GET', '/api/v1/escenarios/{id}/localidades', 'negocio'),
  ('GET', '/api/v1/localidades/escenario/{id}', 'negocio'),
  ('GET', '/api/v1/localidades/{id}', 'negocio'),
  ('DELETE', '/api/v1/localidades/{id}', 'negocio'),
  ('POST', '/api/v1/aficionados/{id}/metodos-pago', 'negocio'),
  ('GET', '/api/v1/aficionados/{id}/metodos-pago', 'negocio'),
  ('DELETE', '/api/v1/aficionados/{id}/metodos-pago/{id}', 'negocio'),
  ('POST', '/api/v1/metodos-pago-aficionado', 'negocio'),
  ('GET', '/api/v1/metodos-pago-aficionado/{id}', 'negocio'),
  ('DELETE', '/api/v1/metodos-pago-aficionado/{id}', 'negocio'),
  ('POST', '/api/v1/metodos-pago', 'negocio'),
  ('GET', '/api/v1/metodos-pago', 'negocio'),
  ('GET', '/api/v1/metodos-pago/{id}', 'negocio'),
  ('PUT', '/api/v1/metodos-pago/{id}', 'negocio'),
  ('DELETE', '/api/v1/metodos-pago/{id}', 'negocio'),
  ('POST', '/api/v1/partidos', 'negocio'),
  ('GET', '/api/v1/partidos/disponibles-apuesta', 'negocio'),
  ('GET', '/api/v1/partidos', 'negocio'),
  ('GET', '/api/v1/partidos/{id}', 'negocio'),
  ('PATCH', '/api/v1/partidos/{id}/estado', 'negocio'),
  ('POST', '/api/v1/partidos/{id}/finalizar', 'negocio'),
  ('POST', '/api/v1/partidos/{id}/resultado', 'negocio'),
  ('POST', '/api/v1/partidos/{id}/posiciones', 'negocio'),
  ('GET', '/api/v1/partidos/{id}/posiciones/ultimas', 'negocio'),
  ('GET', '/api/v1/partidos/{id}/posiciones', 'negocio'),
  ('POST', '/api/v1/pelotas', 'negocio'),
  ('GET', '/api/v1/pelotas', 'negocio'),
  ('GET', '/api/v1/pelotas/{id}', 'negocio'),
  ('PATCH', '/api/v1/pelotas/{id}', 'negocio'),
  ('DELETE', '/api/v1/pelotas/{id}', 'negocio'),
  ('POST', '/api/v1/posiciones-espaciales', 'negocio'),
  ('GET', '/api/v1/posiciones-espaciales', 'negocio'),
  ('GET', '/api/v1/posiciones-espaciales/{id}', 'negocio'),
  ('DELETE', '/api/v1/posiciones-espaciales/{id}', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/desarrollo', 'negocio'),
  ('GET', '/api/v1/torneos-desarrollo/{id}/desarrollo', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/tabla-posiciones', 'negocio'),
  ('GET', '/api/v1/torneos-desarrollo/{id}/tabla-posiciones', 'negocio'),
  ('POST', '/api/v1/torneos', 'negocio'),
  ('GET', '/api/v1/torneos', 'negocio'),
  ('GET', '/api/v1/torneos/{id}', 'negocio'),
  ('GET', '/api/v1/torneos/{id}/estructura', 'negocio'),
  ('PUT', '/api/v1/torneos/{id}', 'negocio'),
  ('PATCH', '/api/v1/torneos/{id}/estado', 'negocio'),
  ('DELETE', '/api/v1/torneos/{id}', 'negocio');

-- ADMIN: todos los permisos del negocio
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.model = 'negocio'
WHERE r.name = 'ADMIN';

-- AFICIONADO: subconjunto
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.model = 'negocio'
WHERE r.name = 'AFICIONADO' AND (p.method, p.url) IN (
  ('POST', '/api/v1/aficionados/{id}/equipos/{id}'),
  ('DELETE', '/api/v1/aficionados/{id}/equipos/{id}'),
  ('GET', '/api/v1/aficionados/{id}/equipos'),
  ('POST', '/api/v1/aficionados-equipos'),
  ('GET', '/api/v1/aficionados-equipos/{id}'),
  ('DELETE', '/api/v1/aficionados-equipos/{id}/{id}'),
  ('POST', '/api/v1/aficionados'),
  ('GET', '/api/v1/aficionados/usuario/{id}'),
  ('GET', '/api/v1/aficionados/{id}'),
  ('PATCH', '/api/v1/aficionados/{id}'),
  ('GET', '/api/v1/partidos/{id}/anotaciones'),
  ('GET', '/api/v1/anotaciones/partido/{id}'),
  ('POST', '/api/v1/apuestas'),
  ('GET', '/api/v1/aficionados/{id}/apuestas'),
  ('GET', '/api/v1/apuestas/partido/{id}/cuota'),
  ('PATCH', '/api/v1/apuestas/{id}/cancelar'),
  ('POST', '/api/v1/boletos'),
  ('GET', '/api/v1/aficionados/{id}/boletos'),
  ('GET', '/api/v1/deportes'),
  ('GET', '/api/v1/deportes/{id}'),
  ('GET', '/api/v1/deportes/{id}/subdeportes'),
  ('GET', '/api/v1/equipos/{id}/deportes'),
  ('GET', '/api/v1/deportes/{id}/equipos'),
  ('GET', '/api/v1/equipos/{id}/jugadores'),
  ('GET', '/api/v1/jugadores/{id}/equipos'),
  ('GET', '/api/v1/equipos'),
  ('GET', '/api/v1/equipos/{id}'),
  ('GET', '/api/v1/torneos/{id}/escenarios'),
  ('GET', '/api/v1/escenarios/{id}/torneos'),
  ('GET', '/api/v1/escenarios'),
  ('GET', '/api/v1/escenarios/{id}'),
  ('GET', '/api/v1/escenarios/{id}/subescenarios'),
  ('GET', '/api/v1/torneos/{id}/fase-final'),
  ('GET', '/api/v1/torneos/{id}/fases'),
  ('GET', '/api/v1/fases/{id}'),
  ('GET', '/api/v1/fases/{id}/grupos'),
  ('GET', '/api/v1/grupos/{id}'),
  ('GET', '/api/v1/grupos/{id}/equipos'),
  ('GET', '/api/v1/jugadores/{id}/habilidades'),
  ('GET', '/api/v1/habilidades/{id}/jugadores'),
  ('GET', '/api/v1/habilidades-jugador/{id}'),
  ('GET', '/api/v1/habilidades'),
  ('GET', '/api/v1/habilidades/{id}'),
  ('GET', '/api/v1/torneos/{id}/inscripciones'),
  ('GET', '/api/v1/torneos/{id}/equipos'),
  ('GET', '/api/v1/equipos/{id}/torneos'),
  ('GET', '/api/v1/jugadores'),
  ('GET', '/api/v1/jugadores/{id}'),
  ('GET', '/api/v1/jugadores/{id}/perfil'),
  ('GET', '/api/v1/escenarios/{id}/localidades'),
  ('GET', '/api/v1/localidades/escenario/{id}'),
  ('GET', '/api/v1/localidades/{id}'),
  ('POST', '/api/v1/aficionados/{id}/metodos-pago'),
  ('GET', '/api/v1/aficionados/{id}/metodos-pago'),
  ('DELETE', '/api/v1/aficionados/{id}/metodos-pago/{id}'),
  ('POST', '/api/v1/metodos-pago-aficionado'),
  ('GET', '/api/v1/metodos-pago-aficionado/{id}'),
  ('DELETE', '/api/v1/metodos-pago-aficionado/{id}'),
  ('GET', '/api/v1/metodos-pago'),
  ('GET', '/api/v1/metodos-pago/{id}'),
  ('GET', '/api/v1/partidos/disponibles-apuesta'),
  ('GET', '/api/v1/partidos'),
  ('GET', '/api/v1/partidos/{id}'),
  ('GET', '/api/v1/partidos/{id}/posiciones/ultimas'),
  ('GET', '/api/v1/partidos/{id}/posiciones'),
  ('GET', '/api/v1/pelotas'),
  ('GET', '/api/v1/pelotas/{id}'),
  ('GET', '/api/v1/posiciones-espaciales'),
  ('GET', '/api/v1/posiciones-espaciales/{id}'),
  ('GET', '/api/v1/torneos/{id}/desarrollo'),
  ('GET', '/api/v1/torneos-desarrollo/{id}/desarrollo'),
  ('GET', '/api/v1/torneos/{id}/tabla-posiciones'),
  ('GET', '/api/v1/torneos-desarrollo/{id}/tabla-posiciones'),
  ('GET', '/api/v1/torneos'),
  ('GET', '/api/v1/torneos/{id}'),
  ('GET', '/api/v1/torneos/{id}/estructura')
);
```

Alternativa sin SQL: añadir estas filas al `DatabaseSeeder` con `findOrCreatePermission(method, url)` + `assignPermission(role, permission)` y `setModel("negocio")` (hoy el seeder fija `"core"`), o cargarlas vía `POST /api/permissions` y `POST /api/role-permissions`.

## 6. Otros puntos de integración

1. **JWT**: el `sub` debe ser el `userId` numérico (lo exige `PermissionValidationController`). El negocio reenvía el token sin interpretarlo.
2. **Aficionado ↔ usuario**: `aficionado.usuarioId` (negocio) = `users.id` (seguridad). Tras registrar un usuario con rol AFICIONADO se crea el aficionado con `POST /api/v1/aficionados`; `GET /api/v1/aficionados/usuario/{id}` lo resuelve.
3. **CORS** de ms_security solo permite `http://localhost:5173`; el negocio acepta el mismo origen vía `FRONTEND_URL`.
4. **Disponibilidad**: si ms_security no responde, el negocio **deniega** (403) salvo `DEV_SECURITY_BYPASS=true`. Timeout: 4 s.
5. **Hallazgos en ms_security (fuera de este repo):** `application.properties` y los `@Value` por defecto contienen contraseña de BD, `JWT_SECRET` y *client secrets* de Google/GitHub en texto plano versionado; deben moverse a variables de entorno y rotarse. Además hay `System.out.println` de userId/URL en `DynamicAuthorizationManager` y `PermissionService`.
