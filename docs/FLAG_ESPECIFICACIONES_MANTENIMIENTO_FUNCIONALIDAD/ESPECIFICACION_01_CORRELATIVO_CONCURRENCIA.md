# ESPECIFICACION 01 - CORRELATIVO Y CONCURRENCIA (PRIORIDAD ALTA)

## Contexto
Al emitir comprobantes de forma concurrente (misma empresa + serie), existe riesgo de duplicidad de numero.

## Tipo de alcance
- Alcance **transversal** a emision de documentos.
- Implementacion en componentes base + modulos que consumen correlativo.

## Problema a resolver
Evitar que dos solicitudes paralelas obtengan el mismo correlativo para la misma combinacion:
- `companyId`
- `serie`

## Objetivo funcional
Garantizar **unicidad estricta** y monotonia del correlativo por `companyId + serie`.

## Mapa de archivos impactados (estado actual)
1. `src/main/java/com/etlions/ms_bs_etia/app_util/service/impl/CorrelativoServiceImpl.java`
2. `src/main/java/com/etlions/ms_bs_etia/app_util/persistence/CorrelativoRepository.java`
3. `src/main/resources/db/migration/V1__initial_schema.sql` (referencia de estado base)
4. `src/main/resources/db/migration/V3__correlativo_concurrencia.sql` (nuevo)
5. `src/main/java/com/etlions/ms_bs_etia/doc_factura/service/impl/DocFacturaServiceImpl.java` (consumidor)
6. `src/main/java/com/etlions/ms_bs_etia/doc_boleta/service/impl/DocBoletaServiceImpl.java` (consumidor)
7. `src/test/java/com/etlions/ms_bs_etia/app_util/service/` (test concurrente nuevo)

## Criterios de aceptacion
- Nunca se repite `serie-numero` en concurrencia alta.
- Se mantiene incremento secuencial por empresa/serie.
- Si hay colision, el sistema reintenta o falla controladamente sin inconsistencia.

## Orden de ejecucion recomendado
1. Crear migracion `V3__correlativo_concurrencia.sql` con restricciones unicas en documentos.
2. Ajustar `CorrelativoRepository` para lectura con lock pesimista.
3. Ajustar `CorrelativoServiceImpl` para flujo seguro create/read/update concurrente.
4. Definir manejo de reintento ante violacion de unique.
5. Crear test concurrente (N hilos, misma empresa+serie).
6. Ejecutar compilacion + tests.

## Riesgo de no ejecutar
- Duplicidad de comprobantes.
- Rechazo/observacion por validaciones externas.
- Incidencias graves de auditoria.

## Definicion de terminado (DoD)
- Pruebas concurrentes automatizadas pasan con N hilos.
- Validacion en base de datos sin duplicados.
- Documentacion de estrategia de lock y fallback actualizada.
- Confirmacion de compatibilidad en factura y boleta.
