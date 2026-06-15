# ESPECIFICACION 02 - PAGINACION REAL EN LISTADOS (PRIORIDAD ALTA)

## Contexto
Los listados principales deben escalar con volumen alto sin cargar todo en memoria.

## Tipo de alcance
- Alcance **modular progresivo**.
- Iniciar por factura y boleta; luego replicar en modulos equivalentes.

## Problema a resolver
Actualmente existen respuestas paginadas a nivel DTO, pero se requiere paginacion real en consulta para evitar:
- consumo excesivo de memoria,
- tiempos de respuesta altos,
- degradacion en ambientes con alto volumen.

## Objetivo funcional
Implementar paginacion efectiva en BD para endpoints de listado (facturas, boletas y otros modulos equivalentes).

## Mapa de archivos impactados (fase inicial ya ejecutada)
1. `src/main/java/com/etlions/ms_bs_etia/doc_factura/persistence/FacturaRepository.java`
2. `src/main/java/com/etlions/ms_bs_etia/doc_boleta/persistence/BoletaRepository.java`
3. `src/main/java/com/etlions/ms_bs_etia/doc_factura/service/impl/DocFacturaServiceImpl.java`
4. `src/main/java/com/etlions/ms_bs_etia/doc_boleta/service/impl/DocBoletaServiceImpl.java`
5. `src/main/resources/db/migration/V4__indices_paginacion_listados.sql`
6. `src/test/java/com/etlions/ms_bs_etia/doc_factura/service/impl/DocFacturaPaginationTest.java`
7. `src/test/java/com/etlions/ms_bs_etia/doc_boleta/service/impl/DocBoletaPaginationTest.java`

## Extension obligatoria del patron (siguiente ola)
Aplicar el mismo patron tecnico (Repository `Pageable` + Service con metadata real + tests + indices) a modulos que hoy listan con `List` y `totalPages(1)`:

### OLA A (prioridad alta)
1. `client`
   - `src/main/java/com/etlions/ms_bs_etia/client/persistence/ClientRepository.java`
   - `src/main/java/com/etlions/ms_bs_etia/client/service/impl/ClientServiceImpl.java`
2. `doc_notacred`
   - `src/main/java/com/etlions/ms_bs_etia/doc_notacred/persistence/NotaCreditoRepository.java`
   - `src/main/java/com/etlions/ms_bs_etia/doc_notacred/service/impl/DocNotacredServiceImpl.java`
3. `doc_notadeb`
   - `src/main/java/com/etlions/ms_bs_etia/doc_notadeb/persistence/NotaDebitoRepository.java`
   - `src/main/java/com/etlions/ms_bs_etia/doc_notadeb/service/impl/DocNotadebServiceImpl.java`
4. `resumen_diario`
   - `src/main/java/com/etlions/ms_bs_etia/resumen_diario/persistence/ResumenDiarioRepository.java`
   - `src/main/java/com/etlions/ms_bs_etia/resumen_diario/service/impl/ResumenDiarioServiceImpl.java`

### OLA B (prioridad media)
- `company`, `employee` y otros listados que aun no usen `Pageable` real.

## Criterios de aceptacion
- El backend consulta solo la pagina solicitada.
- `totalElements`, `totalPages`, `currentPage`, `pageSize` reflejan datos reales.
- Se soportan filtros combinados con paginacion.
- En modulos migrados no debe existir `totalPages(1)` hardcodeado.

## Orden de ejecucion recomendado
1. Definir limite maximo de `size` por endpoint y comportamiento default.
2. Migrar repositorios a consultas `Pageable`.
3. Ajustar services para mapear `Page<Entity>` a wrappers OpenAPI.
4. Crear migracion de indices por filtros mas usados.
5. Crear tests de paginacion + filtros (pagina 0, intermedia, ultima, sin resultados).
6. Ejecutar compilacion + tests.

## Metricas esperadas
- Reduccion de memoria por request en listados.
- Menor latencia en paginas iniciales y profundas.
- Respuesta estable bajo crecimiento de datos.

## Definicion de terminado (DoD)
- Tests de repositorio y servicio para paginacion + filtros.
- Prueba con dataset grande sin degradacion critica.
- Documentacion de limites recomendados de `size` por endpoint.
- Eliminacion de patrones de paginacion fija (ej: `totalPages=1`) en modulos migrados.
