# PLAN MAESTRO DE EJECUCION - FASE 01 Y FASE 02

## Objetivo
Reducir riesgo de errores de implementacion definiendo orden, alcance y criterio de validacion unico.

## Regla de decision
- Si una tarea afecta integridad de numeracion/documentos -> ejecutar en Fase 01.
- Si una tarea afecta rendimiento de listados/consulta -> ejecutar en Fase 02.

## Fase 01 (integridad) - orden por commit sugerido
1. `db`: crear `V3__correlativo_concurrencia.sql`.
2. `repository`: lock pesimista en `CorrelativoRepository`.
3. `service`: endurecer `CorrelativoServiceImpl`.
4. `tests`: agregar test concurrente.
5. `run`: compilar + tests.

## Fase 02 (escalabilidad) - orden por commit sugerido
1. `repository`: pageable + filtros en factura/boleta.
2. `service`: mapeo de `Page<Entity>` a DTO paginado.
3. `db`: crear `V4__indices_paginacion_listados.sql`.
4. `tests`: paginacion + filtros + bordes.
5. `run`: compilar + tests.

## Estado actual
- Fase 01: **IMPLEMENTADA**
- Fase 02: **IMPLEMENTADA (factura + boleta + OLA A)**

## Enfasis de continuidad Fase 2
La Fase 2 no termina en factura/boleta. Se debe extender el mismo patron a modulos que listan con `List` y metadata fija.

Referencias obligatorias:
- `docs/FLAG_ESPECIFICACIONES_MANTENIMIENTO_FUNCIONALIDAD/ESPECIFICACION_02_PAGINACION_REAL.md`
- `docs/FLAG_ESPECIFICACIONES_MANTENIMIENTO_FUNCIONALIDAD/MATRIZ_EXTENSION_PAGINACION_MODULOS.md`

## Evidencia de validacion
- `mvn compile` OK
- `CorrelativoServiceConcurrencyTest` OK
- `DocFacturaPaginationTest` OK
- `DocBoletaPaginationTest` OK
- `ClientPaginationTest` OK
- `DocNotacredPaginationTest` OK
- `DocNotadebPaginationTest` OK

## Checklist de salida por fase
- [x] Criterios de aceptacion completos
- [x] DoD completo (iteracion actual factura/boleta)
- [x] Sin regresiones en compilacion
- [x] Evidencia en tests automatizados
- [x] Extension de Fase 2 completada en modulos OLA A

## Nota de alcance
La carpeta `FLAG_ESPECIFICACIONES_MANTENIMIENTO_FUNCIONALIDAD` es la referencia global para cualquier nueva especificacion. Este plan debe actualizarse cuando cambie prioridad o alcance.
