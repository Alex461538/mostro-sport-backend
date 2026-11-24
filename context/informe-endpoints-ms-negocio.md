# Informe de endpoints — Microservicio de Negocio (`ms_negocio`)

Fecha: 2026-11-23 · Rama: `juanfe` · Total: **159 endpoints** en 27 módulos (generado desde los controladores reales).

## 1. Convenciones generales

| Aspecto | Valor |
|---|---|
| Base URL (dev) | `http://localhost:3000` (variable `PORT`) |
| Prefijo | `/api/v1` (declarado en cada controlador) |
| Formato | JSON, campos en `camelCase`; dominio en español |
| Autenticación | `Authorization: Bearer <JWT emitido por ms_security>` en **todas** las rutas HTTP (guard global `SecurityGuard`) |
| Autorización | El guard llama a `POST {MS_SECURITY}/api/internal/security/permissions-validation` con `{url, method}` y espera `{allowed}` |
| Paginación | `?page=&size=` → `{ content, totalElements }` (solo los listados que lo indican) |
| Errores | `{ timestamp, status, error, message, path }`: 400 validación, 401 sin token, 403 sin permiso, 404 no existe, 409 duplicado/con asociaciones |
| WebSocket | Socket.IO en el mismo puerto. Eventos emitidos: `nuevo_partido`, `partido_finalizado`, `nueva_anotacion`, `apuesta_registrada`, `partido_{id}_posiciones`. El cliente puede enviar `ping` y recibe `pong`. **No pasa por el guard.** |
| Notificaciones | Crear partido, finalizarlo, registrar anotación o apuesta llaman a ms_notificaciones (`POST /send-email`). Si falla, **no** revierte la operación |
| CORS | Origen permitido: `FRONTEND_URL` (dev: `http://localhost:5173`) |

**Body/Query:** `*` = obligatorio, `?` = opcional. Los `{id}` son enteros. Las rutas marcadas *(legacy)* son alias antiguos de la ruta anidada; el frontend debería usar la anidada.

## 2. Variables de entorno

Plantilla en `backend/.env.example`; el `.env` real está en `.gitignore`.

| Variable | Uso | Valor dev |
|---|---|---|
| `PORT` | Puerto HTTP del negocio | 3000 |
| `DB_HOST` `DB_PORT` `DB_USER` `DB_PASS` `DB_NAME` | MySQL | localhost / 3306 / mostro / (secreto) / torneos_deportivos |
| `MS_SECURITY` | URL base de ms_security (Spring) | http://localhost:8181 |
| `DEV_SECURITY_BYPASS` | `true` omite la validación (solo local) | false |
| `MS_NOTIFICATIONS` | URL base de ms_notificaciones (Flask) | http://localhost:5000 |
| `NOTIFICATIONS_DEFAULT_RECIPIENT` | Correo destino de avisos masivos | aficionados@torneo.com |
| `FRONTEND_URL` | Origen(es) CORS, separados por coma | http://localhost:5173 |

## 3. Deportes

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/deportes` | Crear deporte (nombre único dentro del mismo padre) | `{nombre*, descripcion?, deportePadreId?}` |
| GET | `/api/v1/deportes` | Listar deportes paginado | `?page&size&nombre&activo&soloRaiz` |
| GET | `/api/v1/deportes/{id}` | Detalle de deporte | — |
| GET | `/api/v1/deportes/{id}/subdeportes` | Subdeportes de un deporte | `?page&size` |
| PUT | `/api/v1/deportes/{id}` | Actualizar deporte | `{nombre?, descripcion?, deportePadreId?}` |
| PATCH | `/api/v1/deportes/{id}/estado` | Activar/desactivar deporte | `{activo*: boolean}` |
| DELETE | `/api/v1/deportes/{id}` | Eliminar deporte (409 si tiene subdeportes/equipos/torneos) | — |

## 4. Equipos

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/equipos` | Crear equipo | `{nombre*, genero?: MASCULINO|FEMENINO|MIXTO, fechaCreacion?}` |
| GET | `/api/v1/equipos` | Listar equipos paginado | `?page&size&nombre&genero&activo` |
| GET | `/api/v1/equipos/{id}` | Detalle de equipo | — |
| PUT | `/api/v1/equipos/{id}` | Actualizar equipo | `{nombre?, genero?, fechaCreacion?}` |
| PATCH | `/api/v1/equipos/{id}/estado` | Activar/desactivar equipo | `{activo*}` |
| DELETE | `/api/v1/equipos/{id}` | Eliminar equipo (409 si tiene asociaciones) | — |

## 5. Equipos–Deportes

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/equipos/{equipoId}/deportes/{deporteId}` | Asociar equipo a deporte (409 si ya existe) | — |
| DELETE | `/api/v1/equipos/{equipoId}/deportes/{deporteId}` | Quitar asociación equipo–deporte | — |
| GET | `/api/v1/equipos/{equipoId}/deportes` | Deportes de un equipo | — |
| GET | `/api/v1/deportes/{deporteId}/equipos` | Equipos de un deporte | — |

## 6. Jugadores

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/jugadores` | Crear jugador (documento único) | `{nombre*, apellido?, documento*, fechaNacimiento?, posicionId?}` |
| GET | `/api/v1/jugadores` | Listar jugadores paginado | `?page&size&nombre&documento&activo` |
| GET | `/api/v1/jugadores/{id}` | Detalle de jugador | — |
| GET | `/api/v1/jugadores/{id}/perfil` | Perfil: datos + equipos + habilidades (HU-GD-08) | — |
| PUT | `/api/v1/jugadores/{id}` | Actualizar jugador | `{nombre?, apellido?, documento?, fechaNacimiento?, posicionId?}` |
| PATCH | `/api/v1/jugadores/{id}/estado` | Activar/desactivar jugador | `{activo*}` |
| DELETE | `/api/v1/jugadores/{id}` | Eliminar jugador (409 si tiene asociaciones) | — |

## 7. Equipos–Jugadores (historial)

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/equipos/{equipoId}/jugadores` | Asociar jugador a equipo (vínculo con fechas) | `{jugadorId*, fechaInicio?, fechaFin?}` |
| PUT | `/api/v1/equipos/{equipoId}/jugadores/{jugadorId}` | Actualizar/cerrar vínculo jugador–equipo | `{fechaInicio?, fechaFin?}` |
| DELETE | `/api/v1/equipos/{equipoId}/jugadores/{jugadorId}` | Retirar jugador (cierra el vínculo, conserva historial) | — |
| GET | `/api/v1/equipos/{equipoId}/jugadores` | Jugadores de un equipo | `?vigentes=true|false` |
| GET | `/api/v1/jugadores/{jugadorId}/equipos` | Historial de equipos de un jugador | `?vigentes=true|false` |

## 8. Escenarios

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/escenarios` | Crear escenario | `{nombre*, direccion?, escenarioPadreId?}` |
| GET | `/api/v1/escenarios` | Listar escenarios paginado | `?page&size&nombre&activo&soloRaiz` |
| GET | `/api/v1/escenarios/{id}` | Detalle de escenario | — |
| GET | `/api/v1/escenarios/{id}/subescenarios` | Subescenarios (jerarquía) | `?page&size` |
| POST | `/api/v1/escenarios/{id}/subescenarios` | Crear subescenario dentro de un escenario | `{nombre*, direccion?}` |
| PUT | `/api/v1/escenarios/{id}` | Actualizar escenario | `{nombre?, direccion?, escenarioPadreId?}` |
| PATCH | `/api/v1/escenarios/{id}/estado` | Activar/desactivar escenario | `{activo*}` |
| DELETE | `/api/v1/escenarios/{id}` | Eliminar escenario (409 si tiene asociaciones) | — |

## 9. Torneos

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/torneos` | Crear torneo | `{nombre*, fechaInicio*, fechaFin*, fechaCierreInscripciones?, deporteId*}` |
| GET | `/api/v1/torneos` | Listar torneos paginado | `?page&size&nombre&estado&deporteId&activo` |
| GET | `/api/v1/torneos/{id}` | Detalle de torneo | — |
| GET | `/api/v1/torneos/{id}/estructura` | Estructura: fases ordenadas, grupos y equipos | — |
| PUT | `/api/v1/torneos/{id}` | Actualizar torneo | `{nombre?, fechaInicio?, fechaFin?, fechaCierreInscripciones?, deporteId?}` |
| PATCH | `/api/v1/torneos/{id}/estado` | Cambiar estado del torneo | `{estado*: INSCRIPCIONES_ABIERTAS|EN_CURSO|FINALIZADO|CANCELADO}` |
| DELETE | `/api/v1/torneos/{id}` | Eliminar torneo (valida fases/partidos) | — |

## 10. Inscripciones

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/torneos/{torneoId}/inscripciones` | Inscribir equipo en torneo | `{equipoId*}` |
| DELETE | `/api/v1/torneos/{torneoId}/inscripciones/{equipoId}` | Retirar inscripción de un equipo | — |
| GET | `/api/v1/torneos/{torneoId}/inscripciones` | Inscripciones de un torneo | `?page&size` |
| GET | `/api/v1/torneos/{torneoId}/equipos` | Equipos inscritos en un torneo | — |
| GET | `/api/v1/equipos/{equipoId}/torneos` | Torneos en los que está inscrito un equipo | — |

## 11. Fases

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/torneos/{torneoId}/fases` | Crear fase del torneo | `{nombre*, tipo*: GRUPO|GRUPOS|FINAL, orden?>=1}` |
| GET | `/api/v1/torneos/{torneoId}/fases` | Fases de un torneo (ordenadas) | — |
| GET | `/api/v1/fases/{id}` | Detalle de fase | — |
| PUT | `/api/v1/fases/{id}` | Actualizar fase | `{nombre?, tipo?, orden?}` |
| DELETE | `/api/v1/fases/{id}` | Eliminar fase (409 si tiene información asociada) | — |

## 12. Fase final

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/torneos/{torneoId}/fase-final` | Definir fase final del torneo | `{faseId*, modalidad*: OCTAVOS|CUARTOS|SEMIFINAL|FINAL}` |
| GET | `/api/v1/torneos/{torneoId}/fase-final` | Consultar fase final | — |
| PUT | `/api/v1/torneos/{torneoId}/fase-final` | Actualizar modalidad de la fase final | `{modalidad*}` |
| DELETE | `/api/v1/torneos/{torneoId}/fase-final` | Eliminar fase final | — |

## 13. Grupos

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/fases/{faseId}/grupos` | Crear grupo en una fase | `{nombreGrupo*}` |
| GET | `/api/v1/fases/{faseId}/grupos` | Grupos de una fase | — |
| GET | `/api/v1/grupos/{id}` | Detalle de grupo | — |
| PUT | `/api/v1/grupos/{id}` | Actualizar grupo | `{nombreGrupo?}` |
| DELETE | `/api/v1/grupos/{id}` | Eliminar grupo (no borra equipos) | — |
| GET | `/api/v1/grupos/{id}/equipos` | Equipos de un grupo | — |
| POST | `/api/v1/grupos/{id}/equipos` | Agregar equipo a grupo (sin duplicar en la fase) | `{equipoId*}` |
| DELETE | `/api/v1/grupos/{id}/equipos/{equipoId}` | Quitar equipo de un grupo | — |

## 14. Escenarios–Torneos

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/torneos/{torneoId}/escenarios/{escenarioId}` | Asignar escenario a torneo | — |
| DELETE | `/api/v1/torneos/{torneoId}/escenarios/{escenarioId}` | Quitar escenario de torneo | — |
| GET | `/api/v1/torneos/{torneoId}/escenarios` | Escenarios de un torneo | — |
| GET | `/api/v1/escenarios/{escenarioId}/torneos` | Torneos de un escenario | — |

## 15. Partidos y posiciones

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/partidos` | Crear partido (equipos distintos, fase, escenario). Notifica | `{fechaHora*, torneoId*, faseId?, escenarioId*, equipoLocalId*, equipoVisitanteId*, pelotaId?, estado?}` |
| GET | `/api/v1/partidos/disponibles-apuesta` | Partidos que aún admiten apuestas | — |
| GET | `/api/v1/partidos` | Listar partidos | `?torneoId` |
| GET | `/api/v1/partidos/{id}` | Detalle del partido con equipos y marcador | — |
| PATCH | `/api/v1/partidos/{id}/estado` | Cambiar estado del partido | `{estado*: PROGRAMADO|EN_CURSO|FINALIZADO|CANCELADO}` |
| POST | `/api/v1/partidos/{id}/finalizar` | Finalizar partido y liquidar apuestas. Notifica | — |
| POST | `/api/v1/partidos/{id}/resultado` | Registrar marcador final | `{marcadorLocal*>=0, marcadorVisitante*>=0}` |
| POST | `/api/v1/partidos/{id}/posiciones` | Registrar posiciones de jugadores/pelota (lote). Emite WebSocket | `{posiciones*: [{jugadorId?, esPelota?, x*, y*, timestamp?}]}` |
| GET | `/api/v1/partidos/{id}/posiciones/ultimas` | Últimas posiciones (refresco de pizarra) | — |
| GET | `/api/v1/partidos/{id}/posiciones` | Historial de posiciones | `?desde` |

## 16. Desarrollo del torneo

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| GET | `/api/v1/torneos/{id}/desarrollo` | Desarrollo del torneo (HU-GC-08) | — |
| GET | `/api/v1/torneos-desarrollo/{id}/desarrollo` | Alias de desarrollo del torneo | — |
| GET | `/api/v1/torneos/{id}/tabla-posiciones` | Tabla de posiciones del torneo | — |
| GET | `/api/v1/torneos-desarrollo/{id}/tabla-posiciones` | Alias de tabla de posiciones | — |

## 17. Anotaciones

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/partidos/{partidoId}/anotaciones` | Registrar anotación (emite WebSocket + notifica) | `{jugadorId*, minuto*: 0-200, tipo?, descripcion?}` |
| GET | `/api/v1/partidos/{partidoId}/anotaciones` | Anotaciones de un partido | — |
| POST | `/api/v1/anotaciones` | Registrar anotación (legacy) | `{partidoId, jugadorId, minuto, tipo?, descripcion?}` |
| GET | `/api/v1/anotaciones/partido/{partidoId}` | Anotaciones de un partido (legacy) | — |
| DELETE | `/api/v1/anotaciones/{id}` | Eliminar anotación | — |

## 18. Pelotas

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/pelotas` | Crear pelota | `{referencia?, marca?}` |
| GET | `/api/v1/pelotas` | Listar pelotas | — |
| GET | `/api/v1/pelotas/{id}` | Detalle de pelota | — |
| PATCH | `/api/v1/pelotas/{id}` | Actualizar pelota | `{referencia?, marca?}` |
| DELETE | `/api/v1/pelotas/{id}` | Eliminar pelota | — |

## 19. Posiciones espaciales

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/posiciones-espaciales` | Crear posición espacial (catálogo) | `{nombre*, descripcion?}` |
| GET | `/api/v1/posiciones-espaciales` | Listar posiciones espaciales | — |
| GET | `/api/v1/posiciones-espaciales/{id}` | Detalle de posición espacial | — |
| DELETE | `/api/v1/posiciones-espaciales/{id}` | Eliminar posición espacial | — |

## 20. Localidades

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/localidades` | Crear localidad de un escenario | `{nombre*, capacidad?, escenarioId*}` |
| GET | `/api/v1/escenarios/{escenarioId}/localidades` | Localidades de un escenario | — |
| GET | `/api/v1/localidades/escenario/{escenarioId}` | Localidades de un escenario (legacy) | — |
| GET | `/api/v1/localidades/{id}` | Detalle de localidad | — |
| DELETE | `/api/v1/localidades/{id}` | Eliminar localidad | — |

## 21. Habilidades (catálogo)

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/habilidades` | Crear habilidad (catálogo) | `{nombre*, descripcion?}` |
| GET | `/api/v1/habilidades` | Listar habilidades | — |
| GET | `/api/v1/habilidades/{id}` | Detalle de habilidad | — |
| PUT | `/api/v1/habilidades/{id}` | Actualizar habilidad | `{nombre?, descripcion?}` |
| DELETE | `/api/v1/habilidades/{id}` | Eliminar habilidad | — |

## 22. Habilidades de jugador

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/jugadores/{id}/habilidades/{habilidadId}` | Asignar habilidad a jugador | `{nivel?: 1-100}` |
| DELETE | `/api/v1/jugadores/{id}/habilidades/{habilidadId}` | Retirar habilidad de jugador | — |
| GET | `/api/v1/jugadores/{id}/habilidades` | Habilidades de un jugador | — |
| GET | `/api/v1/habilidades/{id}/jugadores` | Jugadores con una habilidad | — |
| POST | `/api/v1/habilidades-jugador` | Asignar habilidad (legacy) | `{jugadorId, habilidadId, nivel?}` |
| GET | `/api/v1/habilidades-jugador/{jugadorId}` | Habilidades de un jugador (legacy) | — |
| DELETE | `/api/v1/habilidades-jugador/{jugadorId}/{habilidadId}` | Retirar habilidad (legacy) | — |

## 23. Métodos de pago (catálogo)

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/metodos-pago` | Crear método de pago (catálogo) | `{nombre*, descripcion?}` |
| GET | `/api/v1/metodos-pago` | Listar métodos de pago | — |
| GET | `/api/v1/metodos-pago/{id}` | Detalle de método de pago | — |
| PUT | `/api/v1/metodos-pago/{id}` | Actualizar método de pago | `{nombre?, descripcion?}` |
| DELETE | `/api/v1/metodos-pago/{id}` | Eliminar método de pago | — |

## 24. Aficionados

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/aficionados` | Registrar aficionado | `{usuarioId*, nombre?, apellido?, telefono?}` |
| GET | `/api/v1/aficionados` | Listar aficionados | — |
| GET | `/api/v1/aficionados/usuario/{usuarioId}` | Aficionado por usuarioId (id del ms_security) | — |
| GET | `/api/v1/aficionados/{id}` | Perfil de aficionado | — |
| PATCH | `/api/v1/aficionados/{id}` | Actualizar aficionado | `{usuarioId?, nombre?, apellido?, telefono?}` |
| DELETE | `/api/v1/aficionados/{id}` | Eliminar aficionado | — |

## 25. Aficionados–Equipos favoritos

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/aficionados/{id}/equipos/{equipoId}` | Agregar equipo favorito | `{esFavoritoPrincipal?}` |
| DELETE | `/api/v1/aficionados/{id}/equipos/{equipoId}` | Quitar equipo favorito | — |
| GET | `/api/v1/aficionados/{id}/equipos` | Equipos favoritos | — |
| POST | `/api/v1/aficionados-equipos` | Agregar favorito (legacy) | `{aficionadoId, equipoId, esFavoritoPrincipal?}` |
| GET | `/api/v1/aficionados-equipos/{aficionadoId}` | Favoritos de un aficionado (legacy) | — |
| DELETE | `/api/v1/aficionados-equipos/{aficionadoId}/{equipoId}` | Quitar favorito (legacy) | — |

## 26. Métodos de pago del aficionado

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/aficionados/{id}/metodos-pago` | Registrar método de pago del aficionado | `{metodoPagoId*, referencia?}` |
| GET | `/api/v1/aficionados/{id}/metodos-pago` | Métodos de pago activos del aficionado | — |
| DELETE | `/api/v1/aficionados/{id}/metodos-pago/{mpId}` | Desactivar método de pago (soft-delete) | — |
| POST | `/api/v1/metodos-pago-aficionado` | Registrar método (legacy) | `{aficionadoId, metodoPagoId, referencia?}` |
| GET | `/api/v1/metodos-pago-aficionado/{aficionadoId}` | Métodos de un aficionado (legacy) | — |
| DELETE | `/api/v1/metodos-pago-aficionado/{id}` | Desactivar método (legacy) | — |

## 27. Boletos

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/boletos` | Emitir boleto para partido y localidad | `{codigo*, precio*>=0, partidoId*, localidadId*, aficionadoId?, estado?}` |
| GET | `/api/v1/partidos/{partidoId}/boletos` | Boletos vendidos de un partido | — |
| GET | `/api/v1/boletos/partido/{partidoId}` | Boletos de un partido (legacy) | — |
| GET | `/api/v1/aficionados/{aficionadoId}/boletos` | Boletos de un aficionado | — |
| PATCH | `/api/v1/boletos/{id}/estado` | Cambiar estado de boleto | `{estado}` |

## 28. Apuestas

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/apuestas` | Realizar apuesta (partido no iniciado). Emite WebSocket + notifica | `{aficionadoId*, partidoId*, metodoAficionadoId*, monto*>=1, estado?}` |
| GET | `/api/v1/apuestas` | Listar apuestas | `?aficionadoId&partidoId` |
| GET | `/api/v1/aficionados/{id}/apuestas` | Apuestas de un aficionado | — |
| GET | `/api/v1/partidos/{id}/apuestas` | Apuestas de un partido (admin) | — |
| GET | `/api/v1/apuestas/partido/{partidoId}/cuota` | Cuota actual de un partido | — |
| PATCH | `/api/v1/apuestas/{id}/cancelar` | Cancelar apuesta si el partido no ha iniciado | — |

## 29. Stubs (solo desarrollo)

| Método | Ruta | Descripción | Body / Query |
|---|---|---|---|
| POST | `/api/v1/stubs/seed` | [DEV] Poblar datos de prueba | — |
| GET | `/api/v1/stubs/health` | [DEV] Health de stubs | — |
| GET | `/api/v1/stubs/torneos` | [DEV] Stub de torneos | — |
| GET | `/api/v1/stubs/fases/{torneoId}` | [DEV] Stub de fases | — |
| GET | `/api/v1/stubs/escenarios` | [DEV] Stub de escenarios | — |
| GET | `/api/v1/stubs/equipos` | [DEV] Stub de equipos | — |
| GET | `/api/v1/stubs/jugadores` | [DEV] Stub de jugadores | — |
| GET | `/api/v1/stubs/pelotas` | [DEV] Stub de pelotas | — |
| GET | `/api/v1/stubs/habilidades` | [DEV] Stub de habilidades | — |
| GET | `/api/v1/stubs/metodos-pago` | [DEV] Stub de métodos de pago | — |

