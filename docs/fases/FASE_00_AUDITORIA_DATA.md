# 🔍 FASE 00 — AUDITORÍA DE DATA Y CONSISTENCIA DE FLUJOS

> **Módulo:** Transversal (Pre-requisito)  
> **Prioridad:** 🔴 CRÍTICA — Ejecutar ANTES de cualquier otra fase  
> **Dependencias:** Ninguna  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Realizar una auditoría completa del estado actual de la data, flujos de negocio y consistencia del sistema. Identificar brechas, datos huérfanos, flujos incompletos y oportunidades de mejora antes de construir los nuevos módulos.

---

## 📋 Tareas

### T00-01 — Auditoría de Data Existente (data.sql + entidades)

**Qué revisar:**
1. **Consistencia referencial:** Verificar que todos los `company_id`, `client_id`, `factura_ref` en seed data apuntan a registros existentes.
2. **Estados coherentes:** Revisar que los estados de facturas/boletas en data.sql sean coherentes con la máquina de estados.
3. **Correlativos:** Validar que `correlativos.ultimo_numero` coincide con el máximo número de cada serie en facturas/boletas.
4. **XML huérfanos:** Verificar que facturas con `xml_content` poblado tienen estado ≥ ENVIADA.
5. **Cuotas y cargos:** Confirmar que cuotas_credito y allowance_charges referencian facturas que existen.
6. **Clientes sin uso:** Identificar clientes de seed que nunca se referencian en documentos.
7. **Productos sin uso:** Identificar productos de seed sin referencia en detalle de facturas/boletas.

**Resultado esperado:** Documento `AUDITORIA_DATA_RESULTADO.md` con hallazgos y acciones correctivas.

---

### T00-02 — Auditoría de Flujos de Negocio

**Flujos a auditar basándose en `src/main/resources/scenarios/`:**

```gherkin
Scenario: Flujo completo Factura BORRADOR → ACEPTADA
  Given una empresa activa con productos y clientes configurados
  When el usuario crea una factura con emitir=false
  Then la factura se guarda como BORRADOR
  And se puede validar → VALIDADO
  And se puede emitir → ENVIADA (genera XML)
  And SUNAT acepta → ACEPTADA
  And se puede anular → ANULADA (estado terminal)

Scenario: Flujo completo Boleta con Resumen Diario
  Given boletas en estado VALIDADO
  When se genera un Resumen Diario
  Then las boletas pasan a ENVIADA
  And se consulta el ticket → ACEPTADO
  And las boletas pasan a ACEPTADA

Scenario: Coherencia de notas de crédito/débito
  Given una factura ACEPTADA referenciada
  When se crea una nota de crédito
  Then la nota debe referenciar correctamente la factura
  And los montos de la nota no deben exceder los de la factura original

Scenario: Nota de crédito NO puede exceder monto de factura referenciada
  Given una factura ACEPTADA por S/ 1,000.00
  When se intenta crear una NC por S/ 1,500.00
  Then el sistema rechaza con error "Monto excede factura referenciada"

Scenario: Empresa INACTIVA no puede emitir documentos
  Given una empresa con estado INACTIVO
  When intenta crear una factura con emitir=true
  Then el sistema rechaza con error "Empresa no está activa"

Scenario: Duplicidad de serie+número rechazada
  Given una factura existente con serie "F001" y número 1
  When se intenta crear otra factura con misma serie y número
  Then el sistema rechaza con error "Serie y número ya existen"

Scenario: Flujo de eliminación protegida
  Given documentos en diferentes estados
  When se intenta eliminar cada uno
  Then solo BORRADOR y CON_ERROR deben permitir eliminación
  And los demás estados deben rechazar con error claro

Scenario: Tipo de cambio registrado al emitir en USD
  Given una factura en moneda USD
  When se emite la factura
  Then se registra el tipo de cambio vigente del día
```

**Verificar que NO se están considerando:**
- [ ] ¿Existe flujo de comunicación de baja real? (no solo anulación interna)
- [ ] ¿Se valida que una nota de crédito no exceda el monto de la factura referenciada?
- [ ] ¿Existe validación de duplicidad de serie+número?
- [ ] ¿Se maneja el caso de empresa INACTIVA intentando emitir?
- [ ] ¿Los empleados tienen algún vínculo con las operaciones? (actualmente no)
- [ ] ¿Los productos se validan contra catálogo SUNAT al emitir?
- [ ] ¿El tipo de cambio se registra automáticamente al emitir en USD?

---

### T00-03 — Auditoría de Escenarios vs Tests

| Archivo Escenario | Tests asociados | ¿Cubierto? |
|-------------------|-----------------|------------|
| `ESCENARIOS_FACTURA.md` (33 escenarios) | `DocFacturaServiceImplTest` | Verificar cobertura |
| `ESCENARIOS_BOLETA.md` (54 escenarios) | `DocBoletaServiceImplTest` | Verificar cobertura |
| Notas Crédito (sin escenarios) | `—` | ❌ Crear escenarios |
| Notas Débito (sin escenarios) | `—` | ❌ Crear escenarios |
| Resumen Diario | `ResumenDiarioServiceImplTest` | Verificar cobertura |

---

### T00-04 — Reporte de Brechas Funcionales

**Brechas identificadas a priori (validar durante auditoría):**

| # | Brecha | Impacto | Acción |
|---|--------|---------|--------|
| 1 | No existe módulo de compras | No se registran gastos | FASE F lo resuelve |
| 2 | No existe autenticación | Cualquier usuario accede a todo | FASE B lo resuelve |
| 3 | No existe aislamiento por empresa | Multi-tenant sin seguridad | FASE B lo resuelve |
| 4 | Empleados no vinculados a ventas | No hay trazabilidad de desempeño | FASE C lo resuelve |
| 5 | No existe comunicación de baja SUNAT | Solo anulación interna | FASE H lo resuelve |
| 6 | No hay módulo de clientes CRM | Solo datos de facturación | FASE I lo resuelve |
| 7 | No hay reportes exportables | No hay inteligencia de negocio | FASE J lo resuelve |
| 8 | Campo `website_url` no existe en Company | Falta dato comercial clave | FASE A lo resuelve |
| 9 | Fechas sin formato estándar dd/MM/yyyy | Inconsistencia en presentación | FASE A lo resuelve |
| 10 | No hay escenarios para NC/ND | Falta cobertura de pruebas | FASE 00 crea los escenarios |

---

## ✅ Criterios de Aceptación

- [ ] Documento `AUDITORIA_DATA_RESULTADO.md` creado con hallazgos
- [ ] Todas las inconsistencias de data.sql corregidas
- [ ] Escenarios para Notas de Crédito y Notas de Débito creados en `src/main/resources/scenarios/`
- [ ] Tests existentes pasan: `.\mvnw.cmd test`
- [ ] Compilación limpia: `.\mvnw.cmd compile`

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `docs/AUDITORIA_DATA_RESULTADO.md` | 🆕 Crear |
| `src/main/resources/scenarios/ESCENARIOS_NOTA_CREDITO.md` | 🆕 Crear |
| `src/main/resources/scenarios/ESCENARIOS_NOTA_DEBITO.md` | 🆕 Crear |
| `src/main/resources/data.sql` | 🔧 Corregir inconsistencias encontradas |

---

*Fase 00 — Auditoría completa antes de construir. Sin esta base, construir encima es construir sobre arena.*



