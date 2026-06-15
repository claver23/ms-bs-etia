# 🤝 FASE I — Deal Flow CRM (M8)

> **Módulo:** Deal Flow  
> **Paquete:** `com.etlions.ms_bs_etia.crm`  
> **Endpoint base:** `/api/v1/crm`  
> **Prioridad:** 🟢 Media  
> **Dependencias:** FASE A, FASE B (Security — COMPLETADA), FASE D (Radar), FASE E (Campaigns)  
> **Puede paralelizarse con:** FASE G, H, J, K (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

CRM ligero para gestionar negociaciones post-contacto (Radar o directo). Funnel de oportunidades: LEAD → CONTACTADO → NEGOCIANDO → CERRADO_GANADO / CERRADO_PERDIDO. Integración con campañas y módulo admin para negociaciones de ETLIONS con sus propios clientes.

---

## 📋 Tareas

### TI-01 — Modelo de datos

```sql
CREATE TABLE deals (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    codigo VARCHAR(50),                  -- DEAL-20260409-0001
    titulo VARCHAR(300),
    contact_id VARCHAR(255),             -- FK a potential_business_contacts o clients
    contact_type VARCHAR(20),            -- LEAD, CLIENTE, PROVEEDOR
    campaign_id VARCHAR(255),            -- Si viene de campaña
    product_id VARCHAR(255),             -- Producto/servicio de interés
    valor_estimado DECIMAL(12,2),
    stage VARCHAR(30),                   -- LEAD, CONTACTADO, NEGOCIANDO, PROPUESTA, CERRADO_GANADO, CERRADO_PERDIDO
    probabilidad INTEGER,                -- 0-100%
    canal_contacto VARCHAR(30),          -- WHATSAPP, EMAIL, TELEFONO, PRESENCIAL, WEB
    notas TEXT,
    actividades_json TEXT,               -- JSON de historial de actividades
    fecha_siguiente_contacto DATE,
    motivo_perdida VARCHAR(500),         -- Solo si CERRADO_PERDIDO
    fecha_cierre DATE,
    created_by VARCHAR(255),
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);
```

### TI-02 — Endpoints REST

```yaml
/crm/deals:
  post: { operationId: crearDeal }
  get:  { operationId: listarDeals, params: [companyId, stage, campaignId] }

/crm/deals/{dealId}:
  get: { operationId: obtenerDeal }
  put: { operationId: actualizarDeal }

/crm/deals/{dealId}/advance:
  post:
    summary: "Avanzar deal al siguiente stage del funnel"
    operationId: avanzarDeal

/crm/deals/{dealId}/activity:
  post:
    summary: "Registrar actividad (llamada, email, reunión)"
    operationId: registrarActividad

/crm/funnel:
  get:
    summary: "Obtener funnel de deals (pipeline)"
    operationId: obtenerFunnel

/crm/deals/export:
  get: { operationId: exportarDealsExcel }
```

### TI-03 — Vista Deal Flow

```
Vista CRM
├── Kanban Board (drag & drop con Vanilla JS):
│   ├── Columna LEAD (azul)
│   ├── Columna CONTACTADO (púrpura)
│   ├── Columna NEGOCIANDO (amarillo)
│   ├── Columna PROPUESTA (naranja)
│   ├── Columna CERRADO_GANADO (verde)
│   └── Columna CERRADO_PERDIDO (gris)
├── Cada card muestra:
│   ├── Título + Valor estimado
│   ├── Contacto + Canal
│   ├── Probabilidad (progress bar)
│   └── Días en esta etapa
├── Sidebar: Detalle del deal + actividades
└── Botones de contacto: [WhatsApp] [Email] [Llamar]
```

**Drag & Drop (Vanilla JS):** Implementar con HTML5 DragEvent API. Cada columna es un drop target. Al soltar una card en nueva columna → `PUT /crm/deals/{id}` con nuevo `stage`.

---

### TI-04 — Estructura de actividades (JSON definido)

**Estructura de `actividades_json`:**
```json
[
  {
    "id": "act_001",
    "tipo": "LLAMADA",
    "descripcion": "Llamada de seguimiento. Interesado en producto.",
    "resultado": "POSITIVO",
    "fecha": "2026-04-09T14:30:00",
    "createdBy": "usr-001"
  },
  {
    "id": "act_002",
    "tipo": "EMAIL",
    "descripcion": "Enviada cotización formal.",
    "resultado": "PENDIENTE",
    "fecha": "2026-04-10T09:00:00",
    "createdBy": "usr-001"
  }
]
```

**Tipos de actividad:** `LLAMADA`, `EMAIL`, `REUNION`, `WHATSAPP`, `NOTA_INTERNA`  
**Resultados:** `POSITIVO`, `NEGATIVO`, `PENDIENTE`, `SIN_RESPUESTA`

---

### TI-05 — Permisos y planes

| Rol | Puede crear deals | Puede ver deals | Puede avanzar/cerrar |
|-----|-------------------|-----------------|---------------------|
| ADMIN_ETIA | ✅ | ✅ (todos) | ✅ |
| OWNER | ✅ | ✅ (su empresa) | ✅ |
| MANAGER | ✅ | ✅ (su empresa) | ✅ |
| EMPLOYEE | ✅ (solo asignados) | ✅ (su empresa) | ✅ (solo asignados) |
| VIEWER | ❌ | ✅ (su empresa) | ❌ |

**Planes:** CRM solo disponible en **BUSINESS** (o como módulo extra).

---

### TI-06 — Integración con facturación al cerrar deal

```
Deal CERRADO_GANADO
  ↓
Modal: "¿Desea crear una factura vinculada?"
  → SÍ: Redirige al wizard con datos pre-cargados:
        - Cliente = contact del deal
        - Producto = product del deal
        - Monto = valor_estimado del deal
        - campaignId = campaign del deal (si hay)
  → NO: Solo cierra el deal, no genera factura
```

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/crm_deal_flow.feature`

```gherkin
Feature: Deal Flow CRM

  Scenario: Crear deal desde lead del Radar
    Given un lead "Ferretería San Martín" guardado
    When creo un deal vinculado a ese lead
    Then el deal se crea en stage LEAD
    And tiene código "DEAL-20260409-0001"

  Scenario: Avanzar deal por el funnel
    Given un deal en stage CONTACTADO
    When avanzo al siguiente stage
    Then el stage es NEGOCIANDO
    And se registra actividad automática "Avance de stage"

  Scenario: No se puede retroceder de stage
    Given un deal en stage NEGOCIANDO
    When intento retroceder a CONTACTADO
    Then recibo error 400 "No se puede retroceder de stage"

  Scenario: Deal cerrado ganado sugiere crear factura
    Given un deal en stage PROPUESTA con valor S/ 5,000
    When lo cierro como GANADO
    Then se sugiere crear una factura vinculada
    And el deal queda con fecha_cierre = hoy

  Scenario: Deal cerrado perdido requiere motivo
    Given un deal en stage NEGOCIANDO
    When intento cerrar como PERDIDO sin motivo
    Then recibo error 400 "El motivo de pérdida es obligatorio"

  Scenario: Deal cerrado perdido con motivo
    Given un deal en stage NEGOCIANDO
    When lo cierro como PERDIDO con motivo "Precio fuera de presupuesto"
    Then el deal tiene stage CERRADO_PERDIDO y motivo_perdida registrado

  Scenario: Crear deal desde campaña
    Given una campaña ACTIVE "Promo Consultoría"
    When creo un deal vinculado a esa campaña
    Then el deal tiene campaign_id de la campaña

  Scenario: Funnel stats
    Given 10 deals: 3 LEAD, 2 CONTACTADO, 2 NEGOCIANDO, 1 PROPUESTA, 1 GANADO, 1 PERDIDO
    When consulto /crm/funnel
    Then recibo conteo por stage y valor estimado total por stage

  Scenario: Registrar actividad en deal
    Given un deal en stage CONTACTADO
    When registro una actividad tipo LLAMADA con resultado POSITIVO
    Then la actividad se agrega al historial del deal

  Scenario: Exportar deals a Excel
    Given deals en distintos stages
    When exporto
    Then recibo .xlsx con título, stage, valor, contacto, días en stage

  Scenario: Drag & drop en Kanban cambia stage
    Given un deal en columna LEAD del Kanban
    When lo arrastro a columna CONTACTADO
    Then el deal se actualiza a stage CONTACTADO

  Scenario: Días en etapa se calculan correctamente
    Given un deal que lleva 5 días en NEGOCIANDO
    When consulto el deal
    Then el campo "días en esta etapa" muestra 5

  Scenario: Plan FREE no puede usar CRM
    Given un usuario con plan FREE
    When intento acceder a /crm
    Then recibo error 403 "Módulo no incluido en tu plan"
```

---

## ✅ Criterios de Aceptación

- [ ] CRUD de deals con código correlativo
- [ ] Funnel completo: LEAD → CONTACTADO → NEGOCIANDO → PROPUESTA → CERRADO_GANADO/PERDIDO
- [ ] No se puede retroceder de stage
- [ ] CERRADO_PERDIDO requiere motivo obligatorio
- [ ] CERRADO_GANADO sugiere crear factura vinculada
- [ ] Kanban Board drag & drop funcional (Vanilla JS)
- [ ] Registro de actividades con tipos y resultados
- [ ] Integración con Radar (crear deal desde lead)
- [ ] Integración con Campaigns (deal desde campaña)
- [ ] Exportar deals a Excel
- [ ] Permisos por rol (VIEWER no crea deals)
- [ ] Solo plan BUSINESS (o módulo extra) accede a CRM
- [ ] Company isolation en todos los endpoints
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag crm, +endpoints, +schemas |
| `src/main/java/.../crm/**` | 🆕 Paquete completo (~10 archivos) |
| `src/main/java/.../crm/openapi/CrmOpenApi.java` | 🆕 |
| `src/main/java/.../crm/service/DealService.java` | 🆕 |
| `src/main/java/.../crm/service/impl/DealServiceImpl.java` | 🆕 |
| `src/main/java/.../crm/mapper/DealMapper.java` | 🆕 |
| `src/main/java/.../crm/persistence/DealEntity.java` | 🆕 |
| `src/main/java/.../crm/persistence/DealRepository.java` | 🆕 |
| `src/main/java/.../view/CrmViewController.java` | 🆕 |
| `src/main/resources/templates/crm.html` | 🆕 |
| `src/main/resources/static/js/crm-view.js` | 🆕 |
| `src/main/resources/static/js/components/kanban-board.js` | 🆕 |
| `src/main/resources/data.sql` | 🔧 +deals demo en distintos stages |
| `src/test/resources/features/crm_deal_flow.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Kanban Vanilla JS:** Usar HTML5 Drag & Drop API (`draggable="true"`, `dragstart`, `dragover`, `drop`). No requiere librería externa.
- **Probabilidad:** Se calcula automáticamente según stage: LEAD=10%, CONTACTADO=25%, NEGOCIANDO=50%, PROPUESTA=75%, CERRADO_GANADO=100%, CERRADO_PERDIDO=0%.
- **Días en etapa:** Calculado como `DATEDIFF(today, fecha_actualizacion)`. Se actualiza `fecha_actualizacion` al cambiar de stage.
- **Actividades JSON vs tabla:** Se mantiene JSON por simplicidad. Si en futuro se necesita buscar por tipo de actividad, migrar a tabla `deal_activities`.

---

*Fase I — Deal Flow. Cada contacto es una oportunidad. Cada oportunidad tiene un precio.*



