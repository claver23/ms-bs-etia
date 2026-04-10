# 👑 FASE G — Overlord + Plan Forge — Admin Dashboard (M5)

> **Módulos:** Overlord (Dashboard Admin) + Plan Forge (Gestión de Planes)  
> **Paquete:** `com.etlions.ms_bs_etia.admin`  
> **Endpoint base:** `/api/v1/admin`  
> **Prioridad:** 🟡 Alta  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA, requiere ADMIN_ETIA role)  
> **Puede paralelizarse con:** FASE C, D, E, F, H (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Dashboard de administración para ETLIONS: visualizar clientes, asignar/modificar planes, activar/desactivar módulos por empresa, gestionar incidencias de soporte, y tener visibilidad de las métricas del negocio (ETLIONS como SaaS).

---

## 📋 Tareas

### TG-01 — Modelo de datos adicional

```sql
CREATE TABLE support_tickets (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    tipo VARCHAR(50),           -- BUG, FEATURE_REQUEST, SOPORTE, CONSULTA_PLAN
    titulo VARCHAR(500),
    descripcion TEXT,
    prioridad VARCHAR(20),      -- BAJA, MEDIA, ALTA, CRITICA
    status VARCHAR(30),         -- ABIERTO, EN_PROGRESO, RESUELTO, CERRADO
    asignado_a VARCHAR(255),    -- user_id del admin que atiende
    notas_internas TEXT,
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    fecha_resolucion TIMESTAMP
);

CREATE TABLE module_assignments (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    module_name VARCHAR(100) NOT NULL,  -- facturacion, pos, campaigns, radar, etc.
    activo BOOLEAN DEFAULT TRUE,
    fecha_activacion TIMESTAMP,
    fecha_desactivacion TIMESTAMP
);
```

---

### TG-02 — Endpoints REST (solo ADMIN_ETIA)

```yaml
# ==================== OVERLORD ADMIN ====================
/admin/companies:
  get:
    summary: "Listar todas las empresas-cliente"
    operationId: listarEmpresasAdmin
    tags: [admin]
    parameters:
      - name: plan { enum: [FREE, STARTER, PRO, BUSINESS] }
      - name: estado { enum: [ACTIVO, INACTIVO, SUSPENDIDO] }
      - name: page / size

/admin/companies/{companyId}:
  get:
    summary: "Detalle de empresa-cliente"
    operationId: obtenerEmpresaAdmin
    tags: [admin]

/admin/companies/{companyId}/plan:
  put:
    summary: "Cambiar plan de una empresa"
    operationId: cambiarPlanEmpresa
    tags: [admin]
    requestBody:
      schema:
        properties:
          planId: { type: string, example: "plan-pro" }

/admin/companies/{companyId}/modules:
  get:
    summary: "Listar módulos de una empresa"
    operationId: listarModulosEmpresa
    tags: [admin]
  put:
    summary: "Activar/desactivar módulos"
    operationId: gestionarModulosEmpresa
    tags: [admin]
    requestBody:
      schema:
        properties:
          modules:
            type: array
            items:
              type: object
              properties:
                moduleName: { type: string }
                activo: { type: boolean }

/admin/tickets:
  post: { operationId: crearTicketSoporte }
  get:  { operationId: listarTicketsSoporte, params: [status, prioridad, companyId] }

/admin/tickets/{ticketId}:
  get: { operationId: obtenerTicket }
  put: { operationId: actualizarTicket }

/admin/tickets/{ticketId}/assign:
  put:
    summary: "Asignar ticket a admin"
    operationId: asignarTicket

/admin/dashboard:
  get:
    summary: "Métricas del dashboard admin"
    operationId: obtenerDashboardAdmin
    tags: [admin]
    responses:
      "200":
        schema:
          $ref: '#/components/schemas/AdminDashboardData'
```

**AdminDashboardData:**
```yaml
AdminDashboardData:
  type: object
  properties:
    totalEmpresas: { type: integer, example: 150 }
    empresasPorPlan:
      type: object
      properties:
        FREE: { type: integer, example: 80 }
        STARTER: { type: integer, example: 40 }
        PRO: { type: integer, example: 25 }
        BUSINESS: { type: integer, example: 5 }
    ingresoMensualEstimado: { type: number, example: 8500.00 }
    ticketsAbiertos: { type: integer, example: 12 }
    ticketsCriticos: { type: integer, example: 2 }
    empresasNuevasEsteMes: { type: integer, example: 15 }
    documentosEmitidosHoy: { type: integer, example: 342 }
    campanasActivasTotal: { type: integer, example: 18 }
```

---

### TG-03 — Vista Overlord

**Nueva vista:** `templates/admin/dashboard.html`  
**View Controller:** `AdminViewController.java` (`GET /admin`)  
**Rutas:** `/admin`, `/admin/companies`, `/admin/tickets`

```
Vista Admin (Overlord)
├── Header: "OVERLORD — Panel de Control ETLIONS"
├── Dashboard (cards KPI):
│   ├── 🏢 Total Empresas: 150
│   ├── 💰 MRR: S/ 8,500
│   ├── 🎫 Tickets Abiertos: 12 (2 críticos ⚠️)
│   └── 📊 Docs emitidos hoy: 342
├── Tabla de Empresas:
│   ├── Nombre | RUC | Plan | Módulos | Estado | Docs/mes | Última actividad
│   ├── Click → Modal detalle con:
│   │   ├── Cambiar plan (dropdown)
│   │   ├── Checkboxes de módulos
│   │   └── Historial de incidencias
│   └── [Exportar a Excel]
├── Tickets de Soporte:
│   ├── Lista con status badges y prioridad
│   ├── Alertas para tickets CRITICOS (banner rojo)
│   └── Asignar a admin
└── Solo accesible con role ADMIN_ETIA
```

---

### TG-04 — Planes y precios

| Plan | Precio | Módulos | Docs/mes | Almacenamiento |
|------|--------|---------|----------|----------------|
| **FREE** | S/ 0 | Recibos por honorarios | 10 | Compartido |
| **STARTER** | S/ 29.90 | Facturación + Clientes + Productos + Sedes | 100 | Compartido |
| **PRO** | S/ 79.90 | STARTER + POS + Campañas + Radar + Compras + Reportes | 500 | Compartido |
| **BUSINESS** | Desde S/ 199.90 | Todo + Overlord CRM + GeoInsight + Soporte dedicado | Ilimitado | Dedicado |

**Nota:** BUSINESS requiere agendar cita → se crea ticket tipo `CONSULTA_PLAN`.

---

### TG-05 — Seguridad del módulo admin

```java
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN_ETIA')")
public class AdminViewController {
    // Solo ADMIN_ETIA puede acceder
}
```

**En SecurityConfig:**
```java
.requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN_ETIA")
```

---

### TG-06 — Data de prueba admin

NO crear data de tickets de prueba (por instrucción). Solo:
- Usuario admin `claveralvaro@gmail.com` (ya en FASE B)
- Empresa ETLIONS con plan BUSINESS
- Módulos asignados para empresa demo

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/admin_plan_management.feature`

```gherkin
Feature: Overlord — Administración de clientes y planes

  Scenario: Admin ve dashboard con métricas
    Given estoy logueado como ADMIN_ETIA
    When accedo a /admin/dashboard
    Then veo total empresas, MRR, tickets abiertos

  Scenario: Cambiar plan de empresa
    Given empresa "ETLIONS" con plan FREE
    When cambio su plan a PRO
    Then la empresa tiene plan PRO
    And los módulos de PRO se activan automáticamente

  Scenario: Downgrade de plan desactiva módulos extra
    Given empresa con plan PRO (módulos: pos, campaigns, radar)
    When cambio su plan a STARTER
    Then los módulos pos, campaigns, radar se desactivan
    And los datos NO se eliminan (acceso se corta, datos se conservan)

  Scenario: Activar módulo extra
    Given empresa "ETLIONS" con plan STARTER
    When activo el módulo "campaigns" como extra
    Then la empresa puede usar Alpha Campaigns

  Scenario: Desactivar módulo extra
    Given empresa con módulo extra "campaigns" activo
    When desactivo el módulo
    Then la empresa pierde acceso a Alpha Campaigns
    And los datos de campañas se conservan (solo se ocultan)

  Scenario: Usuario no admin no accede a Overlord
    Given estoy logueado como usuario EMPRESA
    When intento acceder a /admin
    Then recibo error 403

  Scenario: Ticket de soporte como alerta
    Given un ticket de prioridad CRITICA
    Then aparece un banner rojo en el dashboard admin

  Scenario: Suspender empresa
    Given una empresa activa
    When el admin la suspende
    Then la empresa queda con estado SUSPENDIDO
    And ningún usuario de esa empresa puede operar

  Scenario: Reactivar empresa suspendida
    Given una empresa suspendida
    When el admin la reactiva
    Then la empresa vuelve a estado ACTIVO
    And los usuarios recuperan acceso

  Scenario: Exportar lista de empresas a Excel
    Given empresas registradas en el sistema
    When exporto la lista desde Overlord
    Then recibo un .xlsx con nombre, RUC, plan, estado, docs/mes

  Scenario: Solicitud de plan BUSINESS genera ticket
    Given un usuario que selecciona plan BUSINESS
    When envía el formulario de contacto
    Then se crea un ticket tipo CONSULTA_PLAN en Overlord
    And aparece en la lista de tickets del admin

  Scenario: Log de auditoría de acciones admin
    Given el admin cambia el plan de una empresa
    Then se registra en admin_audit_log: quién, qué, cuándo, empresa afectada
```

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag admin, +endpoints, +schemas |
| `src/main/java/.../admin/**` | 🆕 Paquete completo (~12 archivos) |
| `src/main/java/.../admin/openapi/AdminOpenApi.java` | 🆕 |
| `src/main/java/.../admin/service/AdminService.java` | 🆕 |
| `src/main/java/.../admin/service/impl/AdminServiceImpl.java` | 🆕 |
| `src/main/java/.../admin/persistence/SupportTicketEntity.java` | 🆕 |
| `src/main/java/.../admin/persistence/SupportTicketRepository.java` | 🆕 |
| `src/main/java/.../admin/persistence/ModuleAssignmentEntity.java` | 🆕 |
| `src/main/java/.../admin/persistence/ModuleAssignmentRepository.java` | 🆕 |
| `src/main/java/.../admin/persistence/AdminAuditLogEntity.java` | 🆕 |
| `src/main/java/.../admin/persistence/AdminAuditLogRepository.java` | 🆕 |
| `src/main/java/.../view/AdminViewController.java` | 🆕 |
| `src/main/resources/templates/admin/dashboard.html` | 🆕 |
| `src/main/resources/templates/admin/companies.html` | 🆕 |
| `src/main/resources/templates/admin/tickets.html` | 🆕 |
| `src/main/resources/static/js/admin-view.js` | 🆕 |
| `src/main/resources/data.sql` | 🔧 +module_assignments para empresa demo |
| `src/test/resources/features/admin_plan_management.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Decisión G1 (PREGUNTAS_DEFINICION):** Admin NO puede impersonar usuarios. Solo ver datos y gestionar planes/módulos.
- **Decisión G2 (PREGUNTAS_DEFINICION):** Tickets NO generan email. Solo alertas en el dashboard.
- **Auditoría admin:** Crear tabla `admin_audit_log` con campos: `id, admin_user_id, accion, entity_type, entity_id, detalles_json, fecha`. Cada acción del admin (cambiar plan, suspender empresa, etc.) se registra.
- **Downgrade de plan:** Los datos del módulo desactivado se CONSERVAN pero el acceso se corta. Si la empresa vuelve a un plan superior, los datos siguen ahí.
- **BUSINESS requiere cita:** El formulario genera un ticket `CONSULTA_PLAN` que el admin atiende.

---

## ✅ Criterios de Aceptación

- [ ] Dashboard admin con KPIs funcionales (total empresas, MRR, tickets, docs/día)
- [ ] CRUD de tickets de soporte con prioridades y asignación
- [ ] Cambio de plan por empresa (upgrade y downgrade)
- [ ] Activar/desactivar módulos por empresa (datos se conservan)
- [ ] Suspender/reactivar empresas
- [ ] Tabla de empresas exportable a Excel
- [ ] Solo ADMIN_ETIA puede acceder (403 para otros roles)
- [ ] Alertas para tickets críticos (banner rojo)
- [ ] Log de auditoría para acciones admin
- [ ] Solicitud BUSINESS genera ticket automáticamente
- [ ] Company isolation funcional en todos los endpoints admin
- [ ] Compilación limpia + tests pasan

---

*Fase G — Overlord. El que controla el tablero, controla el juego.*




