# 🔎 REVISIÓN CRÍTICA DEL PLAN DE FASES — ETIA Suite

> **Fecha:** 09/04/2026  
> **Propósito:** Auditoría del plan antes de ejecutar. Identificar ambigüedades, huecos, dependencias rotas, escenarios faltantes y riesgos.  
> **Veredicto general:** El plan es ambicioso y bien estructurado, pero tiene **huecos críticos** que harán que la ejecución falle o genere retrabajo si no se corrigen ANTES de empezar.  
> **Estado:** ✅ MEJORAS APLICADAS — Los hallazgos corregibles han sido aplicados directamente en cada archivo de fase (09/04/2026).

---

## ✅ MEJORAS YA APLICADAS EN ARCHIVOS DE FASE

| Mejora | Archivos Afectados |
|--------|-------------------|
| Formato fecha corregido a `dd/MM/yyyy` | FASE_00, FASE_A (TA-02) |
| Conteo tests corregido a 295 | FASE_A notas |
| FASE B marcada como BLOQUEANTE (no paralela) | FASE_B header, FASE_C-M headers |
| Tarea TB-10: Adaptar tests a Security | FASE_B |
| Tarea TB-11: Env vars documentadas | FASE_B |
| Env vars consolidadas | PREGUNTAS_DEFINICION |
| Escenarios Gherkin agregados (80+) | TODAS las fases |
| Sección "Archivos a crear" agregada | FASE_F, G, H, I, J, K, L |
| Sección "Notas de implementación" agregada | FASE_F, G, H, I, J, K, L |
| Criterios de aceptación expandidos | FASE_A, F, G, I, J, K, L, N |
| FASE_I expandida significativamente | FASE_I (de 127 → ~300+ líneas) |
| Mock de Google Places definido | FASE_D (TD-08) |
| CronJob de campaigns definido | FASE_E (TE-10) |
| Nota de Flyway para futuro | FASE_N (TN-06) |
| PREGUNTAS_DEFINICION resueltas | PREGUNTAS_DEFINICION (todas ✅) |

---

## 🔴 HALLAZGOS CRÍTICOS (Bloquean producción)

### 1. NO EXISTE FASE DE FIRMA DIGITAL + ENVÍO SUNAT

**Gravedad:** 🔴🔴🔴 BLOQUEANTE PARA PRODUCCIÓN

El propio README del proyecto tiene un roadmap con:
```
3. Firma + Envío → XMLDSig, certificado .p12, Web Service SUNAT, CDR (🔴 Alta)
4. Resumen + Baja → Resumen diario (boletas), comunicación de baja, tickets (🟡 Media)
```

**Sin embargo, NINGUNA de las 15 fases (00 a N) cubre esto.** La Opinión de Producto lo dice textualmente:

> *"Sin firma digital + envío SOAP a SUNAT, las facturas generadas son XMLs bonitos pero **inválidos ante la SUNAT**."*

**Lo que falta concretamente:**
- Firma XMLDSig con certificado `.p12` del contribuyente
- Cliente SOAP para Web Services de SUNAT (beta y producción)
- Recepción y parseo del CDR (Constancia de Recepción)
- Envío real de Resumen Diario (ticket + consulta)
- Envío real de Comunicación de Baja (ticket + consulta)
- Manejo de estados SUNAT reales (aceptado, rechazado, con observaciones, anulado)
- Perfil `beta` vs `producción` de SUNAT

**Impacto:** Sin esto, ETIA es una demo. NINGUNA empresa puede facturar legalmente con ETIA. Todas las fases de negocio (POS, Campaigns, Supply Chain, CRM) son irrelevantes si la factura no es válida ante SUNAT.

**Recomendación:** Crear **FASE B_SUNAT** (o renombrar FASE B a seguridad y crear una nueva) con prioridad CRÍTICA, a ejecutarse antes o en paralelo con la seguridad. Debe cubrir:
1. `SunatSigningService` — firma XMLDSig del XML UBL ya generado
2. `SunatSoapClient` — envío SOAP a SUNAT (beta/prod configurable)
3. `CdrParserService` — parseo de la respuesta SUNAT
4. Actualización de estados de documento según respuesta real
5. Reintento automático en caso de timeout
6. Tests con mock de SUNAT (WireMock)

---

### 2. FORMATO DE FECHA MM/DD/YYYY ES INCORRECTO PARA PERÚ

**Gravedad:** 🔴 ERROR DE PRODUCTO

La FASE A estandariza fechas como `MM/dd/yyyy` (formato estadounidense). Pero ETIA es un producto para el **mercado peruano**, donde el formato es `dd/MM/yyyy`.

La Opinión de Producto ya lo señala:
> *"Un software para el mercado peruano que muestra 04/09/2026 cuando el usuario espera 09/04/2026 genera confusión."*

**Y sin embargo, la FASE A implementa exactamente lo contrario de la recomendación.**

La tarea TA-02 dice:
```java
builder.simpleDateFormat("MM/dd/yyyy");
builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
```

**Recomendación:** 
- Backend: Mantener ISO 8601 (`yyyy-MM-dd`) en el API REST (es el estándar internacional para APIs)
- Frontend: Formatear como `dd/MM/yyyy` para la UI peruana
- Esto facilita internacionalización futura sin romper el contrato API

---

### 3. NO EXISTE FASE DE MIGRACIÓN A POSTGRESQL + FLYWAY

**Gravedad:** 🔴 DEUDA TÉCNICA QUE CRECE EXPONENCIALMENTE

Con las 15 fases se crearán **18+ tablas nuevas** (users, plans, user_company_roles, puntos_venta, exploration_cache, potential_business_contacts, campaigns, campaign_audiences, campaign_metrics, campaign_conversions, suppliers, purchase_orders, purchase_invoices, support_tickets, module_assignments, cancelation_requests, deals, sequence_control).

Todo con `ddl-auto: update` y `data.sql`. Esto es:
- Sin versionamiento de esquema
- Sin rollback
- Sin idempotencia garantizada  
- Diferencias H2 vs PostgreSQL no detectadas (funciones SQL, tipos, secuencias)

**Ninguna fase introduce Flyway ni PostgreSQL**, a pesar de que el README lo lista como roadmap prioritario y la Opinión de Producto lo señala explícitamente.

**Recomendación:** Crear **FASE A.5_PERSISTENCIA** entre FASE A y FASE B:
1. Agregar Flyway como dependencia
2. Migrar `data.sql` a scripts Flyway versionados (`V1__initial_schema.sql`, `V2__seed_data.sql`)
3. Crear perfil `application-prod.yaml` con PostgreSQL
4. Agregar Testcontainers para tests de integración con PostgreSQL real
5. Cada fase posterior agrega su migración Flyway (`V{N}__add_{module}_tables.sql`)

---

## 🟠 HALLAZGOS GRAVES (Generan retrabajo significativo)

### 4. FASE B — Scope demasiado grande, debe dividirse

**Problema:** FASE B pretende implementar EN UNA SOLA FASE:
- Spring Security completo
- Autenticación nativa (email + password)
- Google OAuth2
- JWT (generación, validación, refresh)
- Company Isolation Filter
- 3 tablas nuevas (users, plans, user_company_roles)
- 3 entidades JPA + repositorios + mappers
- 3 vistas Thymeleaf nuevas (login, register, seleccionar-plan)
- Sistema de planes y pricing
- Gestión de módulos activos por plan
- Última ruta guardada
- ~15 archivos Java nuevos

**Estimación realista:** Esto es **3-4 semanas** de trabajo para un desarrollador, no una "fase".

**Recomendación:** Dividir en:
- **FASE B1 — Autenticación básica:** Spring Security + JWT + login nativo + tabla users. Solo autenticar. Sin OAuth2, sin planes.
- **FASE B2 — Company Isolation:** Filter de aislamiento por empresa + autorización por roles. Que todos los endpoints existentes validen ownership.
- **FASE B3 — Planes y módulos:** Sistema de planes, tabla plans + user_company_roles + module_assignments. Restricción por plan.
- **FASE B4 — OAuth2 Google:** Integración OAuth2 como flujo alternativo al login nativo.

Esto permite ir desplegando seguridad incrementalmente y reducir el riesgo de una mega-fase que falla en bloque.

---

### 5. FASE L (Códigos Correlativos) está mal posicionada — debería ser FASE A

**Problema:** FASE L está posicionada como la fase #12 de 15, con prioridad "Media". Sin embargo:
- FASE E (Campaigns) genera códigos `CAMP-20260409-0001`
- FASE F (Supply Chain) genera `OC-20260409-0001`
- FASE I (Deal Flow) genera `DEAL-20260409-0001`
- FASE H (Adjuster) genera cancelation requests

**Todas esas fases necesitan códigos correlativos centralizados**, pero se ejecutan antes de FASE L. Esto significa que:
- Cada fase implementará su propia lógica de generación de códigos ad-hoc
- Cuando llegue FASE L, hay que refactorizar todo para usar el servicio centralizado
- Retrabajo garantizado

**Recomendación:** Mover la lógica de `CorrelativeCodeService` a **FASE A (Core)** como servicio transversal. Es un servicio utility, no un módulo independiente. La vista de panel de administración de secuencias puede quedar para después.

---

### 6. FASE H — Comunicación de Baja sin integración SUNAT real

**Problema:** FASE H describe que 100% de cancelación genera "Comunicación de Baja". Pero:
- Una comunicación de baja no es solo un registro en base de datos
- Requiere generar XML de Voided Documents (SUNAT)
- Requiere enviarlo por SOAP a SUNAT
- Requiere consultar ticket de respuesta
- Requiere actualizar estado según respuesta SUNAT

**FASE H asume que la comunicación de baja es un cambio de estado interno.** En realidad, es un flujo completo con SUNAT que hoy NO existe en ninguna fase.

**Recomendación:** FASE H debe depender de la fase de firma/envío SUNAT (hallazgo #1). No puede funcionar sin ella. Reordenar: Firma SUNAT → Adjuster.

---

### 7. Dependencias de paralelismo son irreales

**Problema:** Múltiples fases dicen "Puede paralelizarse con B, C, D, E, F, G, H" mientras simultáneamente dicen "Depende de FASE B (Security)". Si FASE B no está lista, las fases paralelas:
- No tienen company isolation → datos accesibles sin restricción
- No tienen autorización por rol → cualquier usuario hace cualquier cosa
- No tienen restricción por plan → módulos PRO accesibles para FREE

**Mockear auth** para trabajar en paralelo es viable para backend, pero **la integración posterior genera retrabajo considerable** en:
- Todos los fetch del frontend (agregar Bearer token)
- Todos los endpoints (validar companyId vs JWT)
- Todas las vistas (verificar plan/módulos)

**Recomendación:** Ser honesto con las dependencias. El orden real viable es:

```
FASE 00 (Auditoría)
  └→ FASE A (Core) + FASE L lite (Códigos como servicio)
       └→ FASE A.5 (Flyway + PostgreSQL)
            └→ FASE B_SUNAT (Firma + Envío SUNAT) ←── NUEVA
            └→ FASE B1 (Auth básica + JWT)
                 └→ FASE B2 (Company Isolation)
                      └→ FASE H (Adjuster) — depende de B_SUNAT
                      └→ FASE C (POS Pro)
                      └→ FASE F (Supply Chain)
                      └→ FASE J (Reporting — versión básica)
                           └→ FASE B3 (Planes y módulos)
                                └→ FASE G (Overlord Admin)
                                └→ FASE D (Radar)
                                     └→ FASE E (Campaigns)
                                     └→ FASE I (CRM)
                                     └→ FASE K (GeoInsight)
                           └→ FASE B4 (OAuth2 Google)
                 └→ FASE M (Frontend — continuo)
                      └→ FASE N (Integración final)
```

---

## 🟡 HALLAZGOS MODERADOS (Ambigüedades y falta de definición)

### 8. FASE A — `sedeId` en Employee referencia entidad que no se define claramente

**Problema:** TA-03 agrega `sedeId` al Employee, y TA-04 crea PuntoVenta con `sedeId`. Pero:
- ¿Ya existe entidad `Sede` en el sistema? (Parece que sí por el código existente)
- ¿Quién gestiona la relación Sede ↔ PuntoVenta?  
- ¿Qué pasa si se elimina una sede que tiene POS asignados?
- No hay validación de que `sedeId` exista al crear PuntoVenta

**Recomendación:** Agregar a FASE A:
- Validación de existencia de sede al asignar `sedeId` en Employee y PuntoVenta
- Regla: No eliminar sede con POS activos asignados
- Documentar claramente qué campos son FK validados vs strings libres

---

### 9. FASE I (Deal Flow CRM) — Especificación INCOMPLETA

**Problema:** FASE I es la fase con menos detalle de todas:
- ✅ Tiene modelo de datos
- ✅ Tiene endpoints
- ✅ Tiene vista descrita
- ❌ **No tiene tabla de archivos a crear/modificar**
- ❌ **No tiene criterios de aceptación**
- ❌ **No tiene notas de implementación**
- ❌ No define qué pasa cuando un deal se cierra como GANADO (¿se crea factura automáticamente? ¿solo se sugiere?)
- ❌ No define permisos/roles requeridos
- ❌ No define qué planes pueden usar CRM
- ❌ No define cómo se calcula la probabilidad
- ❌ `actividades_json` como TEXT → sin estructura definida de la actividad

**Recomendación:** Completar FASE I al nivel de detalle de las otras fases antes de ejecutar. Definir:
- ¿Quién puede crear/ver deals? (¿OWNER, MANAGER, EMPLOYEE?)
- ¿El Kanban drag & drop usa qué librería? (Vanilla JS para drag & drop es complejo)
- Estructura del JSON de actividades
- Integración con facturación al cerrar deal

---

### 10. FASE E (Campaigns) — Complejidad subestimada del tracking de conversiones

**Problema:** El tracking de conversiones depende de que el usuario **manualmente seleccione** la campaña al crear una venta:
```
Dropdown "¿Esta venta proviene de una campaña?" → lista de campañas ACTIVE
```

**Problemas con este enfoque:**
- El cajero de POS probablemente no sabe/no le importa de qué campaña viene el cliente
- No hay tracking automático (ej: código de descuento, link de campaña, etc.)
- Las métricas de ROI serán tan buenas como la disciplina del usuario al etiquetar ventas
- No hay definición de qué pasa si una campaña ENDED recibe una conversión tardía

**Recomendación:** Agregar al menos:
- Códigos de descuento por campaña → automáticamente vincula la venta
- Período de gracia para conversiones después de ENDED (ej: 7 días)
- Escenario para conversión tardía

---

### 11. FASE D (Radar) — Sin modo de desarrollo definido

**Problema:** Google Places API requiere:
- Cuenta de Google Cloud con billing habilitado
- API Key con restricciones
- Costo real por cada búsqueda ($0.017-$0.040)

**No hay definición de:**
- ¿Cómo se testea en desarrollo sin gastar dinero? 
- ¿Mock predefinido con datos de ejemplo?
- ¿Qué respuesta da el sistema si `GOOGLE_PLACES_API_KEY=demo-key` (el default)?
- ¿Se puede usar el módulo Radar sin Google Places (solo leads manuales)?

**Recomendación:** Definir explícitamente:
- `MockGooglePlacesClient` con datos hardcoded de ~20 negocios ficticios en Lima
- Condición: `@ConditionalOnProperty(name = "google.places.mode", havingValue = "real")`
- En dev/test siempre usa mock
- La vista Radar funciona igualmente con datos mock

---

### 12. FASE F (Supply Chain) — `detalle_json` como TEXT para ítems de compra

**Problema:** Tanto `purchase_orders.detalle_json` como `purchase_invoices.detalle_json` almacenan los ítems como JSON en TEXT. Este patrón:
- No permite queries SQL sobre ítems individuales
- No permite reportes de "producto más comprado" sin parsear JSON
- No permite vincular ítems de compra con productos del inventario
- Rompe normalización necesaria para módulos de inventario futuros

**Contexto:** En facturas/boletas de venta este patrón tiene sentido porque el XML UBL ya es la fuente de verdad del detalle. Pero en compras, el detalle normalizado ES la fuente de verdad.

**Recomendación:** Crear tabla `purchase_order_items` y `purchase_invoice_items` normalizadas con FK a productos. Esto habilita:
- Reportes de compras por producto
- Futuro módulo de inventario
- Conciliación compras vs ventas por producto

---

### 13. FASE G (Overlord) — Sin definición de multi-admin

**Problema:** Se asume un solo admin (claveralvaro@gmail.com). Pero:
- ¿Qué pasa cuando hay un segundo admin?
- ¿Los tickets se asignan entre admins?
- ¿Hay notificaciones cuando un ticket CRITICO se crea?
- ¿Hay auditoría de acciones admin? (quién cambió el plan de quién)

**Recomendación:** Agregar:
- Log de auditoría para acciones admin (`admin_audit_log`)
- Soporte para múltiples usuarios ADMIN_ETIA
- Notificaciones por email para tickets críticos (al menos la definición del escenario)

---

## 🟢 ESCENARIOS FALTANTES POR FASE

### FASE 00 — Auditoría
- ✅ Bien definida. Los checklists son claros.
- ⚠️ Falta: ¿Qué pasa si la auditoría revela que los 36 tests de XML se rompen al corregir data? ¿Se arreglan en FASE 00 o se difieren?

### FASE A — Core
- ❌ Escenario: URL inválida en `websiteUrl` (ej: "no-es-una-url") → ¿Qué validación se aplica?
- ❌ Escenario: PuntoVenta sin sede (¿se permite? ¿es obligatorio `sedeId`?)
- ❌ Escenario: Empleado cambia de sede → ¿qué pasa con su POS asignado?
- ❌ Escenario: Eliminar PuntoVenta que tiene ventas asociadas

### FASE B — Seguridad
- ❌ Escenario: Usuario intenta registrarse con email ya existente en OAuth2 (ej: registró con Google y luego intenta registro nativo con mismo email)
- ❌ Escenario: Token JWT expirado → ¿flujo de refresh automático en frontend?
- ❌ Escenario: Admin cambia plan de empresa → ¿qué pasa con los módulos que ya no están incluidos? ¿datos se mantienen? ¿acceso se corta inmediatamente?
- ❌ Escenario: Usuario pertenece a múltiples empresas → ¿cómo selecciona cuál usar?
- ❌ Escenario: Contraseña olvidada / reset password
- ❌ Escenario: Cuenta bloqueada por intentos fallidos

### FASE C — POS Pro
- ❌ Escenario: Producto sin stock (si futuro inventario) → ¿se puede vender igual?
- ❌ Escenario: Descuento aplicado en POS → ¿cómo afecta la factura?
- ❌ Escenario: Venta mixta (algunos productos con IGV, otros exonerados)
- ❌ Escenario: Devolución desde POS (relación con Adjuster)
- ❌ Escenario: Corte de caja / cierre del día

### FASE D — Radar
- ❌ Escenario: Búsqueda sin resultados
- ❌ Escenario: Ubigeo no encontrado en tabla de ubigeos
- ❌ Escenario: Lead guardado que ya existe como cliente → ¿deduplicación?

### FASE E — Campaigns
- ❌ Escenario: Campaña con presupuesto agotado → ¿se pausa automáticamente?
- ❌ Escenario: Reactivar campaña PAUSED → ¿se resetean métricas o continúan?
- ❌ Escenario: Dos campañas activas para el mismo producto → ¿conflicto de atribución?

### FASE F — Supply Chain
- ❌ Escenario: Orden de compra con moneda distinta a PEN (USD) → tipo de cambio
- ❌ Escenario: Factura de compra con monto distinto a la OC
- ❌ Escenario: Proveedor inactivo → ¿se puede crear OC?
- ❌ Escenario: ¿Cómo se envía la OC al proveedor? (email/WhatsApp mencionado pero no definido)

### FASE H — Adjuster
- ❌ Escenario: Anulación de Nota de Crédito (¿se puede anular una NC?)
- ❌ Escenario: Anulación de boleta (flujo distinto a factura — va por Resumen Diario)
- ❌ Escenario: Porcentaje que resulta en monto < S/ 1.00 → ¿se permite?
- ❌ Escenario: Documento ya anulado al 100% → segundo intento

### FASE J — Reporting
- ❌ Escenario: Reporte sin datos en el rango → ¿Excel vacío o mensaje?
- ❌ Escenario: Reporte de período largo (1 año) → ¿paginación? ¿timeout?
- ❌ Escenario: `tiempoUsoHoy` → ¿cómo se mide? No hay tracking de sesión definido

---

## 🔵 FUNCIONALIDADES HUÉRFANAS (Mencionadas pero sin fase asignada)

| Funcionalidad | Dónde se menciona | Fase asignada |
|---|---|---|
| **Firma digital XMLDSig** | README Roadmap #3 | ❌ NINGUNA |
| **Envío SOAP a SUNAT** | README Roadmap #3 | ❌ NINGUNA |
| **CDR (Constancia de Recepción)** | README Roadmap #3 | ❌ NINGUNA |
| **Flyway migrations** | Opinión de Producto | ❌ NINGUNA |
| **PostgreSQL production profile** | README Roadmap #5 | ❌ NINGUNA |
| **PDF con QR code** | README Roadmap #5 | ❌ NINGUNA |
| **Email de facturas** | Implícito en facturación | ❌ NINGUNA |
| **Reset de contraseña** | Implícito en auth | ❌ NINGUNA |
| **Testcontainers** | Opinión de Producto | ❌ NINGUNA |
| **CI/CD pipeline** | Implícito en producción | ❌ NINGUNA |
| **Monitoreo y logging** | Implícito en producción | ❌ NINGUNA |
| **CORS configuration** | Necesario si frontend separado | ❌ NINGUNA |
| **Notificaciones email** | Tickets soporte, campañas | ❌ NINGUNA |
| **Backup y recovery** | Implícito en producción | ❌ NINGUNA |

---

## 📊 EVALUACIÓN POR FASE — Resumen ejecutivo

| Fase | Claridad | Completitud | Dependencias correctas | Escenarios suficientes | Riesgo | Veredicto |
|------|----------|-------------|------------------------|------------------------|--------|-----------|
| **00** | 🟢 Alta | 🟢 Alta | 🟢 Sí | 🟡 Faltan edge cases | 🟢 Bajo | ✅ Lista para ejecutar |
| **A** | 🟡 Media | 🟡 Media | 🟢 Sí | 🟡 Faltan 4 escenarios | 🟡 Medio | ⚠️ Corregir formato fecha, agregar validaciones sede |
| **B** | 🟡 Media | 🟢 Alta | 🟢 Sí | 🔴 Faltan 6+ escenarios | 🔴 Alto | ⚠️ Dividir en 4 sub-fases |
| **C** | 🟡 Media | 🟡 Media | 🟡 Parcial | 🟡 Faltan 5 escenarios | 🟡 Medio | ⚠️ Definir más escenarios POS |
| **D** | 🟢 Alta | 🟡 Media | 🟢 Sí | 🟡 Faltan 3 escenarios | 🟡 Medio | ⚠️ Definir mock mode explícito |
| **E** | 🟢 Alta | 🟡 Media | 🟢 Sí | 🟡 Faltan 3 escenarios | 🟡 Medio | ⚠️ Tracking de conversión débil |
| **F** | 🟡 Media | 🟡 Media | 🟢 Sí | 🟡 Faltan 4 escenarios | 🟡 Medio | ⚠️ Normalizar detalle de compra |
| **G** | 🟡 Media | 🟡 Media | 🟢 Sí | 🟢 Suficientes | 🟡 Medio | ⚠️ Agregar auditoría admin |
| **H** | 🟢 Alta | 🟡 Media | 🔴 Incompleta | 🟡 Faltan 4 escenarios | 🔴 Alto | ⚠️ Depende de firma SUNAT inexistente |
| **I** | 🔴 Baja | 🔴 Baja | 🟢 Sí | 🔴 Mínimos | 🟡 Medio | ❌ Requiere completar especificación |
| **J** | 🟡 Media | 🟡 Media | 🟡 Parcial | 🟡 Faltan 3 escenarios | 🟡 Medio | ⚠️ Agregar tracking de sesión |
| **K** | 🟡 Media | 🟡 Media | 🟢 Sí | 🟢 Suficientes | 🟡 Medio | ✅ Aceptable para fase tardía |
| **L** | 🟡 Media | 🟢 Alta | 🔴 Mal posicionada | 🟢 Suficientes | 🟡 Medio | ⚠️ Mover a FASE A |
| **M** | 🟡 Media | 🟡 Media | 🟢 Sí | N/A (transversal) | 🟡 Medio | ⚠️ Definir como tarea continua |
| **N** | 🔴 Baja | 🔴 Baja | 🟢 Sí | 🔴 Vagos | 🟡 Medio | ❌ Requiere checklist específico |

---

## 🏗️ PLAN CORREGIDO PROPUESTO — Orden de ejecución

```
TIER 0 — CIMIENTOS (no hay producto sin esto)
├── FASE 00   — Auditoría de data
├── FASE A    — Core (corregir: dd/MM/yyyy, mover Códigos aquí)
└── FASE A.5  — 🆕 Flyway + PostgreSQL + Testcontainers

TIER 1 — TICKET DE ENTRADA (mínimo para operar legalmente)
├── FASE B_SUNAT — 🆕 Firma digital + Envío SOAP + CDR
├── FASE B1      — Auth básica (JWT + login nativo + tabla users)
├── FASE B2      — Company Isolation Filter + roles
└── FASE H       — Adjuster (ahora sí con envío SUNAT real)

TIER 2 — PRODUCTO VENDIBLE (genera revenue)
├── FASE C    — POS Pro
├── FASE F    — Supply Chain (normalizar detalle)
├── FASE J    — Reporting (versión básica: ventas + compras + Excel)
└── FASE B3   — Planes y módulos + restricciones

TIER 3 — DIFERENCIADORES (ventaja competitiva)
├── FASE D    — Radar de Mercado
├── FASE E    — Alpha Campaigns
├── FASE G    — Overlord Admin
└── FASE I    — Deal Flow CRM (completar spec primero)

TIER 4 — PREMIUM (long-term value)
├── FASE K    — GeoInsight
├── FASE B4   — OAuth2 Google
├── FASE M    — Frontend polish final
└── FASE N    — Integración final + suite E2E

TRANSVERSAL (paralelo a todo)
├── Frontend incremental por cada fase
├── Escenarios Gherkin por cada fase
└── Tests unitarios + integración por cada fase
```

---

## ✅ ACCIONES INMEDIATAS ANTES DE EJECUTAR

1. **[ ] Crear FASE B_SUNAT** — Firma digital + envío SOAP + CDR. Prioridad EXISTENCIAL.
2. **[ ] Crear FASE A.5_PERSISTENCIA** — Flyway + PostgreSQL + Testcontainers.
3. **[ ] Corregir TA-02 en FASE A** — Cambiar `MM/dd/yyyy` a `dd/MM/yyyy` (o ISO en backend + dd/MM/yyyy en frontend).
4. **[ ] Mover FASE L (Códigos)** — Integrar `CorrelativeCodeService` como tarea de FASE A.
5. **[ ] Dividir FASE B** — En B1 (auth), B2 (isolation), B3 (planes), B4 (OAuth2).
6. **[ ] Completar FASE I** — Agregar archivos, criterios de aceptación, notas de implementación.
7. **[ ] Completar FASE N** — Convertir de descripción vaga a checklist ejecutable.
8. **[ ] Agregar escenarios faltantes** — Mínimo los 30+ escenarios listados arriba.
9. **[ ] Definir mock mode** para FASE D (Google Places) y FASE E (Ad Platform).
10. **[ ] Normalizar `detalle_json`** en FASE F (crear tablas de items de compra).

---

*Revisión completa. Cero ejecución. Máxima claridad antes de lanzarse.*
