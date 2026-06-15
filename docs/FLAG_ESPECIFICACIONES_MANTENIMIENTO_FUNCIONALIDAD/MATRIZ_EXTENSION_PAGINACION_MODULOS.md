# MATRIZ DE EXTENSION - PATRON DE PAGINACION REAL

## Proposito
Estandarizar la extension del patron de paginacion real a todos los modulos que listan datos.

## Patron tecnico obligatorio
1. Repository con `Page<Entidad>` + `Pageable` + filtros opcionales.
2. Service que convierte `Page<Entidad>` a `ListResponseDto` con metadata real.
3. Limites seguros de entrada (`page >= 0`, `size` default y maximo).
4. Tests unitarios de paginacion y filtros.
5. Indices de BD segun campos de filtro/orden.

## Estado por modulo
| Modulo | Estado | Service actual | Repository actual | Observacion |
|---|---|---|---|---|
| doc_factura | Implementado | `DocFacturaServiceImpl#listarFacturas` | `FacturaRepository#buscarConFiltros` | Fase 2 inicial completa |
| doc_boleta | Implementado | `DocBoletaServiceImpl#listarBoletas` | `BoletaRepository#buscarConFiltros` | Fase 2 inicial completa |
| client | Implementado | `ClientServiceImpl#listarClientes` | `ClientRepository#buscarConFiltros` | OLA A completada |
| doc_notacred | Implementado | `DocNotacredServiceImpl#listarNotasCredito` | `NotaCreditoRepository#buscarConFiltros` | OLA A completada |
| doc_notadeb | Implementado | `DocNotadebServiceImpl#listarNotasDebito` | `NotaDebitoRepository#buscarConFiltros` | OLA A completada |
| resumen_diario | Implementado | `ResumenDiarioServiceImpl#listarResumenesDiarios` | `ResumenDiarioRepository#findByCompanyId(..., Pageable)` | OLA A completada |

## Orden de ejecucion recomendado
### OLA A (alta)
1. `client`
2. `doc_notacred`
3. `doc_notadeb`
4. `resumen_diario`

### OLA B (media)
1. `company`
2. `employee`
3. Otros listados detectados en auditoria incremental

## Checklist por modulo
- [x] Repository migrado a `Pageable`
- [x] Service sin `totalPages(1)` hardcodeado
- [x] Tests de pagina vacia/intermedia/ultima
- [x] Prueba con filtros combinados
- [x] Indices de BD agregados y documentados
- [x] `mvn compile` y tests en verde
