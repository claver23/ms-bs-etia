# 🔗 FASE N — Integración Final + Escenarios + Data Alineada

> **Módulo:** Transversal  
> **Prioridad:** 🔴 Ejecutar AL FINAL de todas las fases  
> **Dependencias:** TODAS las fases A-M completadas  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Fusionar todos los módulos, verificar compilación global, alinear data.sql con todos los escenarios nuevos, actualizar escenarios en `scenarios/`, ejecutar suite completa de tests, actualizar README y documentación.

---

## 📋 Tareas

### TN-01 — Compilación global

```bash
.\mvnw.cmd clean compile
.\mvnw.cmd test
```

Arreglar TODOS los errores de importación, dependencias circulares, y conflictos entre módulos.

---

### TN-02 — Data de prueba completa (data.sql)

Alinear data.sql para que incluya datos coherentes de TODOS los módulos:

| Entidad | Datos |
|---------|-------|
| Users | Admin ETLIONS + 2 usuarios empresa + 1 natural |
| Plans | FREE, STARTER, PRO, BUSINESS |
| UserCompanyRoles | Roles asignados |
| PuntosVenta | 2 POS en sedes existentes |
| Suppliers | 2 proveedores (1 manual, 1 del radar) |
| PurchaseOrders | 1 OC de prueba |
| Campaigns | 1 campaña DRAFT, 1 ACTIVE |
| CampaignAudiences | Audiencias para las campañas |
| Deals | 2 deals en diferentes stages |
| SequenceControl | Secuencias para cada entidad |
| ModuleAssignments | Módulos de ETLIONS (todos activos) |

**Empresa demo ETLIONS:**
- RUC: 20601234567
- Admin: claveralvaro@gmail.com / Etia2025!
- Plan: BUSINESS
- Productos configurados = módulos de ETIA que se ofrecen al usuario

---

### TN-03 — Actualizar escenarios en `src/main/resources/scenarios/`

| Archivo | Acción |
|---------|--------|
| `ESCENARIOS_FACTURA.md` | 🔧 Agregar escenarios de campaignId, employeeId |
| `ESCENARIOS_BOLETA.md` | 🔧 Agregar escenarios de POS |
| `ESCENARIOS_NOTA_CREDITO.md` | 🆕 Crear (FASE 00) |
| `ESCENARIOS_NOTA_DEBITO.md` | 🆕 Crear (FASE 00) |
| `ESCENARIOS_POS.md` | 🆕 Crear |
| `ESCENARIOS_RADAR.md` | 🆕 Crear |
| `ESCENARIOS_CAMPAIGNS.md` | 🆕 Crear |
| `ESCENARIOS_COMPRAS.md` | 🆕 Crear |
| `ESCENARIOS_CRM.md` | 🆕 Crear |
| `ESCENARIOS_ADMIN.md` | 🆕 Crear |
| `ESCENARIOS_ADJUSTER.md` | 🆕 Crear |
| `ESCENARIOS_AUTH.md` | 🆕 Crear |

---

### TN-04 — Habilitar módulos en el menú

Actualizar `menu.html` para mostrar TODOS los módulos con:
- Cards por grupo (Facturación, Operaciones, Inteligencia, Configuración)
- Candado si módulo no incluido en plan
- Badge "NUEVO" en módulos recién implementados
- Contador de registros por módulo (facturas: 23, campañas: 2, etc.)

---

### TN-05 — Suite de tests de integración

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegrationTestSuite {
    // Test que verifica que todos los endpoints responden
    // Test de aislamiento por empresa
    // Test de plan/módulo restriction
    // Test de flujo completo: login → crear factura → asignar a campaña → exportar reporte
}
```

**Tests de integración cross-module:**

```java
// 1. Flujo completo Radar → Proveedor → OC → Factura Compra
@Test void testRadarToSupplyChainFlow() { ... }

// 2. Flujo completo Campaign → Venta POS → Conversión → ROI
@Test void testCampaignToSalesFlow() { ... }

// 3. Flujo completo Lead (Radar) → Deal (CRM) → Cerrado Ganado → Factura
@Test void testRadarToCrmToInvoiceFlow() { ... }

// 4. Flujo Adjuster: Factura → Cancelación parcial → NC → Segunda cancelación → Baja
@Test void testAdjusterMultipleCancelations() { ... }

// 5. Admin: Cambiar plan → Módulos se desactivan → Usuario pierde acceso
@Test void testPlanDowngradeModuleAccess() { ... }
```

---

### TN-06 — Nota de migración futura (Flyway + PostgreSQL)

> ⚠️ **DEUDA TÉCNICA ACEPTADA:** En esta versión se mantiene H2 en memoria con `data.sql`.
> Para producción se requiere:
> 1. Agregar `spring-boot-starter-data-jpa` con perfil PostgreSQL
> 2. Agregar `org.flywaydb:flyway-core`
> 3. Migrar `data.sql` a scripts Flyway: `V1__initial_schema.sql`, `V2__seed_data.sql`
> 4. Cada módulo nuevo → script Flyway: `V{N}__add_{module}_tables.sql`
> 5. Agregar `Testcontainers` para tests de integración con PostgreSQL real
> 6. Crear `application-prod.yaml` con config PostgreSQL
>
> **Esto debe ser la PRIMERA tarea del siguiente ciclo de desarrollo.**

---

### TN-07 — Bitácora de ejecución

Renombrar cada archivo de fase a `FASE_X_NOMBRE_EJECUTADO.md` tras completar.

---

## 🧪 Escenarios Gherkin de Integración

**Archivo:** `src/test/resources/features/integration_final.feature`

```gherkin
Feature: Integración Final — Cross-module flows

  Scenario: Login → Dashboard → Métricas correctas
    Given usuario admin logueado con plan BUSINESS
    When accedo al dashboard
    Then veo ventas, compras, margen, campañas activas, producto del mes

  Scenario: Radar → Proveedor → Orden Compra → Factura Recibida
    Given busco "Ferreterías" en Radar y guardo "Ferretería San Martín" como lead
    When creo proveedor desde lead y genero OC
    And confirmo OC y registro factura de compra
    Then la compra aparece en reportes de Supply Chain

  Scenario: Campaña → Venta POS → Conversión → ROI
    Given una campaña ACTIVE para "Consultoría TI"
    When un empleado vende desde POS y selecciona la campaña
    Then se crea CampaignConversion y el ROI se actualiza

  Scenario: Lead → Deal CRM → Cerrado Ganado → Factura
    Given un lead del Radar convertido a Deal
    When avanzo el deal hasta CERRADO_GANADO
    And creo la factura vinculada
    Then la factura tiene campaign_id y deal_id trazables

  Scenario: Cancelación parcial → NC → Segunda cancelación → Baja
    Given factura ACEPTADA por S/ 1,000
    When cancelo 30% (NC por S/ 300)
    And cancelo el 100% del restante (baja por S/ 700)
    Then la factura tiene remaining_amount S/ 0
    And existen NC + Comunicación de Baja vinculadas

  Scenario: Admin cambia plan → Módulos se desactivan
    Given empresa "TECH S.A." con plan PRO
    When admin cambia a STARTER
    Then los módulos POS, Campaigns, Radar se desactivan
    And el usuario de "TECH S.A." ve candados en esos módulos
    And los datos se conservan

  Scenario: Compilación limpia de TODO el proyecto
    When ejecuto .\mvnw.cmd clean compile
    Then 0 errores

  Scenario: Suite completa de tests pasa
    When ejecuto .\mvnw.cmd test
    Then 0 fallos en todos los tests (unitarios + integración)

  Scenario: data.sql coherente con todos los módulos
    Given el seed de data.sql cargado
    When consulto cada módulo
    Then cada tabla tiene datos demo coherentes
    And no hay FKs rotas ni datos huérfanos

  Scenario: Menú muestra todos los módulos según plan
    Given usuario con plan PRO
    When accedo al menú
    Then veo módulos de Facturación, POS, Campaigns, Radar, Supply Chain, Reportes desbloqueados
    And veo Overlord, CRM, GeoInsight bloqueados con candado
```

---

## ✅ Criterios de Aceptación

- [ ] `.\mvnw.cmd clean compile` → 0 errores
- [ ] `.\mvnw.cmd test` → 0 fallos
- [ ] data.sql coherente con todos los módulos (sin FKs rotas)
- [ ] Escenarios actualizados para cada módulo en `src/main/resources/scenarios/`
- [ ] Menú con todos los módulos habilitados según plan
- [ ] Tests de integración cross-module pasan
- [ ] README actualizado con estado final
- [ ] Bitácora completada con todas las fases
- [ ] No hay warnings de compilación críticos
- [ ] No hay dependencias circulares entre paquetes

---

*Fase N — Integración. La catedral se ve completa solo cuando se retira el andamio.*


