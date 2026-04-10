<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenAPI-3.0.3-85EA2D?style=for-the-badge&logo=openapiinitiative&logoColor=white" />
  <img src="https://img.shields.io/badge/MapStruct-1.6.3-FF6B6B?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Lombok-1.18.30-BC4521?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Thymeleaf-Multi%20Page%20App-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" />
  <img src="https://img.shields.io/badge/Leaflet-Map%20Integration-199900?style=for-the-badge&logo=leaflet&logoColor=white" />
  <img src="https://img.shields.io/badge/H2%20|%20PostgreSQL-Ready-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/UBL%202.1-XML%20SUNAT-E74C3C?style=for-the-badge" />
  <img src="https://img.shields.io/badge/OAuth2-JWT%20Security-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />
  <img src="https://img.shields.io/badge/Google%20Places-API%20Integration-4285F4?style=for-the-badge&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/Apache%20POI-Excel%20Export-217346?style=for-the-badge" />
</p>

<h1 align="center">🧾 ETIA Suite — Business Intelligence Platform</h1>

<p align="center">
  <b>Plataforma integral de gestión empresarial para el mercado peruano</b><br/>
  <sub>Facturación Electrónica SUNAT · POS Pro · Radar de Mercado · Alpha Campaigns · Supply Chain · Deal Flow CRM · GeoInsight · Data Vault Reportes · Overlord Admin · The Guillotine · Plan Forge</sub>
</p>

<p align="center">
  <a href="#-etia-suite-v30--plan-de-evolución">ETIA Suite v3.0</a> •
  <a href="#-módulos">Módulos</a> •
  <a href="#-novedades-abril-2026--fase-2-completada-">FASE 2</a> •
  <a href="#-novedades-abril-2026--fase-1-completada-">FASE 1</a> •
  <a href="#-arquitectura">Arquitectura</a> •
  <a href="#-dominios">Dominios</a> •
  <a href="#-api-endpoints">API</a> •
  <a href="#-frontend-multi-page-app">Frontend</a> •
  <a href="#-error-handling">Errores</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 🚀 ETIA Suite v3.0 — Plan de Evolución

> **De facturación electrónica a plataforma integral de inteligencia empresarial.** ETIA evoluciona de un sistema de emisión de comprobantes a una suite completa que cubre ventas, compras, campañas, CRM, análisis geográfico, reportes y administración multi-tenant con planes.

### 📦 Módulos Planificados

| # | Módulo | Nombre UI | Paquete | Endpoint | Descripción |
|---|--------|-----------|---------|----------|-------------|
| M0 | **Core** | — | `ms_bs_etia` (transversal) | — | Cimientos técnicos: dates, website_url, PuntoVenta base |
| M1 | **POS** | POS Pro | `punto_venta` | `/api/v1/punto_venta` | Punto de venta con asignación de empleados y métricas |
| M2 | **Campaigns** | Alpha Campaigns | `campaign` | `/api/v1/campaigns` | Campañas con ROI, audiencia, conversiones y mock ad platform |
| M3 | **Radar** | Radar de Mercado | `radar` | `/api/v1/radar` | Google Places API, leads, caché 30d, rate limiter |
| M4 | **Compras** | Supply Chain | `purchase` | `/api/v1/purchase` | Proveedores, OC, facturas de compra, integración Radar |
| M5 | **Admin** | Overlord | `admin` | `/api/v1/admin` | Dashboard ETLIONS, gestión de planes, tickets de soporte |
| M6 | **Seguridad** | — | `security` | `/api/v1/auth` | OAuth2 + JWT, company isolation, planes, roles |
| M7 | **Anulación** | Adjuster (The Guillotine) | `adjuster` | `/api/v1/adjuster` | Slider de cancelación parcial/total con NC o com. de baja |
| M8 | **CRM** | Deal Flow | `crm` | `/api/v1/crm` | Funnel de oportunidades, kanban, actividades |
| M9 | **Reportes** | Data Vault | `reporting` | `/api/v1/reporting` | Excel export, métricas, dashboards, proyecciones |
| M10 | **Geo** | GeoInsight | `geo` | `/api/v1/geo` | Mapa interactivo, heatmap, auto-ubigeo, análisis zonal |
| M11 | **Códigos** | Códigos | `codigos` | `/api/v1/codigos` | Correlativos centralizados: `{PREFIX}-{DATE}-{N}` |
| M12 | **Frontend** | — | — | — | Modularización JS, menú dinámico, auth flow, slider component |

### 🗂️ Fases de Ejecución

```
FASE 00 (Auditoría Data)
    ↓
FASE A (Core — Cimientos)
    ↓
┌─ FASE B (Seguridad + OAuth2)
├─ FASE C (POS Pro + Team Compass)
├─ FASE D (Radar de Mercado)
├─ FASE E (Alpha Campaigns)
├─ FASE F (Supply Chain)
├─ FASE G (Overlord Admin)
├─ FASE H (Adjuster / Guillotine)
├─ FASE I (Deal Flow CRM)
├─ FASE J (Data Vault Reportes)
├─ FASE K (GeoInsight)
├─ FASE L (Códigos Correlativos)
└─ FASE M (Frontend Modular)
    ↓
FASE N (Integración Final + Tests)
```

> 📁 Plan detallado en [`docs/fases/`](docs/fases/) · Bitácora en [`docs/BITACORA_EJECUCION.md`](docs/BITACORA_EJECUCION.md) · Arquitectura en [`docs/ARQUITECTURA_PATRONES_STACK.md`](docs/ARQUITECTURA_PATRONES_STACK.md)

### 👤 Demo User

| Campo | Valor |
|-------|-------|
| Email | `claveralvaro@gmail.com` |
| Password | `Etia2025!` |
| Rol | `ADMIN_ETIA` |
| Empresa | ETLIONS S.A.C. |
| RUC | 20601234567 |
| Plan | BUSINESS (todos los módulos) |

### 💰 Planes

| Plan | Precio | Módulos |
|------|--------|---------|
| **FREE** | S/ 0 | Recibos por honorarios |
| **STARTER** | S/ 29.90/mes | Facturación + Clientes + Productos + Sedes |
| **PRO** | S/ 79.90/mes | STARTER + POS + Campañas + Radar + Compras + Reportes |
| **BUSINESS** | Desde S/ 199.90/mes | Todo + Overlord + CRM + GeoInsight + Soporte dedicado |

---

## 🔥 Novedades (Abril 2026) — FASE 2 COMPLETADA ✅

> **FASE 2: XML UBL 2.1 SUNAT al ~90%.** Generador XML completo con 36 tests, endpoint de descarga OpenAPI-first, modelo de datos enriquecido con 12 catálogos SUNAT, wizard 5 pasos con edit mode, flujo Borrador/Emisión, 184 tests pasando.

### 🧾 Generador XML UBL 2.1 — `UblInvoiceXmlBuilder`

| Feature | Detalle |
|---------|---------|
| **XML UBL 2.1 completo** | Genera `Invoice` conforme al estándar OASIS UBL 2.1 adaptado por SUNAT con todos los namespaces (`cac:`, `cbc:`, `ext:`, `sac:`) |
| **12 Catálogos SUNAT** | Cat. 01 (Tipo Doc), 02 (Moneda), 03 (UM), 04 (País), 05 (Tributo), 06 (Doc Identidad), 07 (Afectación IGV), 13 (Ubigeo), 16 (Tipo Precio), 51 (Tipo Operación), 52 (Leyendas), 53 (Cargos/Desc.) |
| **Secciones XML** | `AccountingSupplierParty`, `AccountingCustomerParty`, `PaymentTerms`, `AllowanceCharge`, `TaxTotal`, `LegalMonetaryTotal`, `InvoiceLine` |
| **Endpoint descarga** | `GET /factura/{facturaId}/xml` — OpenAPI-first (spec → codegen → service → controller) |
| **Inmutabilidad** | XML descargado = XML generado al emitir. Sin regeneración on-the-fly. Si no existe → HTTP 204. |
| **36 tests unitarios** | Cobertura de todas las secciones del XML, namespaces, atributos `schemeID`, y edge cases |

### 📝 Modelo de Datos UBL 2.1 — Enriquecimiento

| Entidad | Campos nuevos |
|---------|---------------|
| **FacturaEntity** | +9: `tipoOperacion`, `horaEmision`, `exoneradas`, `inafectas`, `gratuitas`, `totalCargos`, `totalDescuentos`, `subtotal`, `totalConImpuestos` |
| **BoletaEntity** | +9: (simétrico a FacturaEntity) |
| **ClientEntity** | +4: `departamento`, `provincia`, `distrito`, `codigoPais` |
| **DetalleItem** | +6: `tipoAfectacionIgv`, `codigoTributo`, `tipoPrecio`, `porcentajeIgv`, `baseImponible`, `montoImpuesto` |
| **CuotaCreditoEntity** | 🆕 Cuotas de crédito con monto, fecha vencimiento, moneda |
| **AllowanceChargeEntity** | 🆕 Cargos/Descuentos globales Cat. 53 (código, factor, base, monto) |

### 🧙 Wizard 5 Steps + Edit Mode

| Feature | Detalle |
|---------|---------|
| **5 pasos** | 📄 Documento → 🏢 Emisor → 👤 Cliente → 📦 Detalle → 🚀 Emitir |
| **Serie como dropdown** | Se carga desde `ETIA.activeSede.series[]` filtrado por tipo de doc |
| **Número con padding** | Formato SUNAT: `F001-00000001` (8 dígitos) |
| **Dropdown afectación** | Gravado(10) / Exonerado(20) / Inafecto(30) por línea |
| **Tipo operación** | Dropdown Cat. 51: 0101, 0200, 0401 |
| **Edit mode** | URL `/wizard?type=FACTURA&id=<facturaId>` — carga borrador completo para edición |
| **Banner edición** | "✏️ Editando F001-00000001" con serie/fecha readonly |
| **Review con tabla** | Step 5 muestra tabla completa de ítems + totales desglosados |

### 📤 Emisión en Envío + Borrador

| Feature | Detalle |
|---------|---------|
| **Toggle por empresa** | `emisionEnEnvio` en `CompanyEntity` — controla comportamiento por defecto |
| **Activado (default)** | Crear factura → genera XML automáticamente → estado EMITIDA |
| **Desactivado** | Factura se guarda como BORRADOR → emitir manualmente |
| **Ambos botones** | 🚀 "Emitir" y 💾 "Guardar Borrador" siempre disponibles |
| **Warning banner** | Si desactivado: "Emisión en Envío desactivada" con link a config |
| **Quick Emit** | Botón 📤 en lista de facturas → `PUT /factura/{id}/emitir` |
| **Re-emisión** | RECHAZADA → ENVIADA directamente (sin pasar por BORRADOR) |

### 📊 Preview y PDF mejorados

| Feature | Detalle |
|---------|---------|
| **Cargos/Descuentos en PDF** | Tabla con tipo, código Cat.53, descripción, factor, base, monto |
| **Cuotas en PDF** | Tabla con # cuota, monto, fecha vencimiento, moneda |
| **Totales desglosados** | Filas "+ Cargos" y "− Descuentos" en resumen antes del TOTAL |
| **Editar desde preview** | Links de edición para BORRADOR y RECHAZADA desde lista |
| **Números formateados** | `ETIA.formatNumero()` con padding 8 dígitos en toda la app |

---

## ✅ Novedades (Abril 2026) — FASE 1 COMPLETADA ✅

> **FASE 1 finalizada oficialmente.** El proyecto pasó de un scaffold básico a una **aplicación de facturación electrónica completa conforme a SUNAT** con arquitectura hexagonal homologada en los 20 dominios.

### ⚡ Performance — Cache inteligente con sessionStorage

| Feature | Detalle |
|---------|---------|
| **Cache con TTL** | `ETIA.init()` cachea company, clients, sedes en `sessionStorage` con TTL de 5 minutos. Reduce 8 API calls a 2-3 en navegación normal. |
| **Stale-while-revalidate** | Muestra datos cacheados inmediatamente y revalida en background (sin bloquear UI). La experiencia se siente instantánea. |
| **Cache helpers** | `_cacheSet(key, data)`, `_cacheGet(key)`, `_cacheInvalidate(key)` — reutilizables para cualquier dato. |
| **TC cacheado** | El tipo de cambio del día se cachea y se refresca silenciosamente. |

### 💱 Tipo de Cambio — Live Badge en Header + Widget en Menú

| Feature | Detalle |
|---------|---------|
| **Header Live Badge** | Chip `💱 3.7520` visible en el header de **todas** las páginas. Click abre popup con compra/venta/fuente/fecha. |
| **Popup interactivo** | Mini popup con compra, venta, fuente (SBS/SUNAT) y botón ⟳ Actualizar. Se cierra al hacer click fuera. |
| **Menu Widget** | Banner horizontal en la página de menú con compra/venta destacados y fuente. |
| **Auto-fetch** | Se consulta automáticamente al iniciar la app usando la fuente configurada (SBS/SUNAT). |

### 🛡️ Validaciones SUNAT pre-emisión

| Validación | Detalle |
|------------|---------|
| **Tipo de documento del cliente** | Factura → solo clientes con RUC (`6`). Boleta → solo DNI (`1`), Carnet de Extranjería (`4`) o Pasaporte (`7`). Valida automáticamente al emitir. |
| **Formato de serie** | Debe cumplir `^[A-Z]\d{3}$` (ej: `F001`, `B001`). Rechaza series mal formateadas. |
| **Prefijo de serie** | Factura debe iniciar con `F`, Boleta con `B`. Validación estricta por tipo de comprobante. |
| **Cuadre de totales** | Verifica que `total == gravadas + IGV` con tolerancia de ±0.01 por redondeo. |
| **Eliminación protegida** | Solo los comprobantes en estado `BORRADOR` pueden eliminarse. Docs emitidos/aceptados/anulados están protegidos. |

### 🔢 Emisión inteligente de comprobantes

| Feature | Detalle |
|---------|---------|
| **Auto-correlativo** | Cada serie mantiene su propio contador atómico (`CorrelativoService`). F001-1, F001-2, F001-3… sin saltos. |
| **Serie por defecto** | Si no se especifica serie, se asigna `F001` (factura) o `B001` (boleta). |
| **Fecha automática** | Si no se envía `fechaEmision`, se asigna la fecha actual. |
| **Forma de pago** | Default `CONTADO`. En modo contado, `fechaVencimiento = fechaEmision` automáticamente. |
| **Estado inicial** | Todo comprobante nace como `BORRADOR`. Flujo: `BORRADOR → ACEPTADA → ANULADA`. |
| **Notificación automática** | Cada emisión y anulación genera una notificación in-memory con serie, número y acción. |

### 🏪 Gestión de sedes y series

| Feature | Detalle |
|---------|---------|
| **Auto-generación de series** | Al crear sede, se generan 4 series automáticas: `F{cod}`, `B{cod}`, `FC{cod2}`, `FD{cod2}`. |
| **Código correlativo** | Cada sede recibe código auto-incremental de 3 dígitos (`001`, `002`, `003`…). |
| **Sede default** | La primera sede creada para una empresa se marca como default. |
| **Eliminación en cascada** | Al desactivar una sede, todas sus series asociadas se desactivan automáticamente. |

### 👤 Consulta RENIEC / SUNAT (mock)

| Feature | Detalle |
|---------|---------|
| **Consulta DNI** | Retorna nombre, apellidos, dirección y ubigeo (mock — en producción conectará con RENIEC). |
| **Consulta RUC** | Retorna razón social, dirección, estado y condición (mock — en producción conectará con SUNAT). |
| **Frontend integrado** | El modal de clientes consulta RENIEC/SUNAT en tiempo real al ingresar DNI o RUC. |

### 🔔 Sistema de notificaciones

| Feature | Detalle |
|---------|---------|
| **Store in-memory** | `ConcurrentLinkedDeque` con notificaciones por empresa (thread-safe). |
| **Badge en tiempo real** | Contador de no-leídas en el header, actualización automática. |
| **Endpoint REST POST** | `POST /api/v1/notification` — Publicar notificación por REST (OpenAPI). |
| **Acciones REST** | Listar, publicar, marcar leída, marcar todas leídas, limpiar por empresa. |
| **Auto-publicación** | Emisión y anulación de cualquier comprobante genera notificación automática. |
| **Patrón homologado** | `openapi/ → service/ → service/impl/ → persistence/` — idéntico a los otros 17 dominios. |

### 🖥️ Frontend SUNAT-aware

| Feature | Detalle |
|---------|---------|
| **Filtro de tipo de documento** | El wizard filtra automáticamente los tipos de documento permitidos para el cliente según el tipo de comprobante. |
| **Cambio de sede** | Al cambiar sede en Config, las series se actualizan automáticamente en el wizard. |
| **14 vistas MPA** | Welcome, Menu, Documents, Facturas, Boletas, Notas C/D, Clients, Products, Employees, Sedes, TC, Resúmenes, Wizard. |
| **10 módulos JS** | Separación de responsabilidades: api-client, app-state, wizard, items, client-modal, product-modal, config-overlay, doc-preview, error-handler, navigation. |

---

## 📐 Arquitectura

El proyecto sigue una **arquitectura hexagonal por dominio**, donde cada uno de los **20 dominios** es autónomo con sus propias capas. El contrato OpenAPI genera las interfaces y DTOs, y la aplicación los implementa en capas limpias. El frontend es una **Multi-Page App (MPA)** con Thymeleaf + Vanilla JS modular.

```
┌─────────────────────────────────────────────────────────────────────┐
│               🖥️ Thymeleaf Multi-Page App  (14 vistas)              │
│   Welcome · Menu · Documents · Facturas · Boletas · Notas Crédito  │
│   Notas Débito · Clientes · Productos · Empleados · Sedes          │
│   Tipo Cambio · Resúmenes Diarios · Wizard (Emisión 5 pasos)       │
│                                                                     │
│   🌙 Dark/Light theme · Leaflet Maps · Config Overlay               │
│   📸 Error screenshot+email · 🔔 Notificaciones en tiempo real      │
│   ✏️ Edit mode (borradores/rechazadas) · 📤 Quick Emit              │
└────────────────────────────┬────────────────────────────────────────┘
                             │  fetch('/api/v1/*')
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        🌐  API Gateway                              │
│              /api/v1/*  (REST JSON · 60+ endpoints)                 │
│        + /api/v1/notification (OpenAPI — in-memory store)           │
│        + /api/v1/factura/{id}/xml (descarga XML UBL 2.1)           │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│              🛡️ GlobalExceptionHandler (@RestControllerAdvice)      │
│   ResourceNotFoundException → 404 · BusinessException → 422         │
│   ValidationException → 400 · SunatValidationException → 422       │
│   MethodArgumentNotValid → 400 · Exception (fallback) → 500        │
│   Wrapper: {isSuccess, status, message, timestamp, data}            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   📜 OpenAPI Contract Layer                         │
│     ms-bs-etia.yaml (2,945 líneas) → codegen (interfaces + DTOs)   │
└────────────────────────────┬────────────────────────────────────────┘
                             │  implements
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│               🎮 Controller Layer  (openapi/)                       │
│      @Controller + @RequestMapping("/api/v1")                       │
│      Recibe HTTP → delega al Service → ResponseEntity.ok()          │
│                                                                     │
│      🔔 NotificationOpenApi — REST controller in-memory             │
│      🏷️ 14 View Controllers — Spring MVC (Thymeleaf)               │
└────────────────────────────┬────────────────────────────────────────┘
                             │  calls
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                🧠 Service Layer  (service/ + service/impl/)         │
│      Interface por operación  →  @Service implementación            │
│      Orquesta: Mapper + Repository + Business Logic                 │
│      🔢 CorrelativoService — auto-increment series                  │
│      ✅ SunatValidator — business rule validation                   │
│      🔔 NotificationService — publish to in-memory store            │
│      🧾 UblInvoiceXmlBuilder — genera XML UBL 2.1 SUNAT            │
└──────────────┬─────────────────────────────────────┬────────────────┘
               │  uses                               │  uses
               ▼                                     ▼
┌──────────────────────────┐   ┌──────────────────────────────────────┐
│  🔄 Mapper (mapper/)     │   │  💾 Persistence (persistence/)       │
│  MapStruct @Mapper       │   │  @Entity + JpaRepository             │
│  DTO ↔ Entity            │   │  H2 (dev) / PostgreSQL (prod)        │
│  Enum ↔ String           │   │                                      │
│  List<DetalleItem> ↔ JSON│   │  data.sql → Seed automático          │
└──────────────────────────┘   └──────────────────────────────────────┘
```

### 🔁 Flujo de una petición (ejemplo: Emitir Factura)

```
POST /api/v1/factura
        │
        ▼
  GlobalExceptionHandler         ← Intercepta cualquier excepción
        │
        ▼
  DocFacturaOpenApi              ← Controller recibe FacturaEmitirRequest
        │
        ▼
  SunatValidator.validate()      ← Validaciones de negocio SUNAT
        │
        ▼
  docFacturaServiceImpl          ← Service orquesta la lógica
        │
        ├──► FacturaMapper.toEntity(request)     ← DTO → Entity (MapStruct)
        │
        ├──► CorrelativoService.next(serie)      ← Auto-correlativo F001-4
        │
        ├──► facturaRepository.save(entity)      ← 1er save: persist entity
        │
        ├──► CuotaCreditoRepo.saveAll(cuotas)    ← Cuotas de crédito (si aplica)
        │
        ├──► AllowanceChargeRepo.saveAll(cargos)  ← Cargos/Desc. Cat.53 (si aplica)
        │
        ├──► UblInvoiceXmlBuilder.build(entity)  ← Genera XML UBL 2.1 ✨
        │
        ├──► facturaRepository.save(entity)      ← 2do save: xmlContent + xmlPath
        │
        ├──► NotificationService.publicar(...)   ← Push notificación in-memory
        │
        ├──► FacturaMapper.toData(saved)         ← Entity → DTO
        │
        └──► Response wrapper
                  .isSuccess(true)
                  .data(facturaData)
        │
        ▼
  ResponseEntity.ok(response)   ← HTTP 200 + JSON body
```

> 💡 Si `emitir=false`, la factura se guarda como **BORRADOR** (sin XML). El usuario puede emitirla después con `PUT /factura/{id}/emitir`.

### 🖥️ Flujo del Frontend (Multi-Page App)

```
GET /                          ← WelcomeViewController → welcome.html
GET /menu                      ← MenuViewController → menu.html (hub central)
GET /documents                 ← DocumentsViewController → documents.html
GET /facturas                  ← FacturasViewController → facturas.html
GET /boletas                   ← BoletasViewController → boletas.html
GET /notas-credito             ← NotasCreditoViewController
GET /notas-debito              ← NotasDebitoViewController
GET /clients                   ← ClientsViewController → clients.html
GET /products                  ← ProductsViewController → products.html
GET /employees                 ← EmployeesViewController → employees.html
GET /sedes                     ← SedesViewController → sedes.html
GET /tipo-cambio               ← TipoCambioViewController → tipo-cambio.html
GET /resumenes-diarios         ← ResumenesDiariosViewController
GET /wizard?type=FACTURA       ← WizardViewController → wizard/factura.html
    │
    └──► JS modular (10 archivos) + CSS unificado (1,606 líneas)
              │
              ├── api-client.js      API REST centralizada
              ├── app-state.js       Estado global + notificaciones
              ├── wizard.js          Lógica del wizard 4 pasos
              ├── items.js           Gestión de ítems + cálculo IGV
              ├── client-modal.js    Búsqueda/registro de clientes
              ├── product-modal.js   Búsqueda de productos
              ├── config-overlay.js  Panel de configuración + Leaflet
              ├── doc-preview.js     Preview de documentos
              ├── error-handler.js   Toast global + screenshot + email
              └── navigation.js      Navegación entre vistas
```

---

## 🔌 API Endpoints

Todos los endpoints REST están bajo **`/api/v1`** y son generados desde el contrato OpenAPI (`~3,300 líneas`). Todas las notificaciones están integradas en el contrato.

### 🏢 Company — Empresas emisoras

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/company` | Registrar nueva empresa emisora |
| `GET` | `/api/v1/company` | Listar empresas (paginado, filtro) |
| `PUT` | `/api/v1/company` | Actualizar datos de empresa |
| `DELETE` | `/api/v1/company` | Eliminar empresa (soft/hard) |

### 👤 Client — Clientes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/client` | Registrar cliente |
| `GET` | `/api/v1/client` | Listar clientes por empresa |
| `PUT` | `/api/v1/client` | Actualizar cliente |
| `DELETE` | `/api/v1/client` | Eliminar cliente |

### 🧾 Factura — Facturas electrónicas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/factura` | Emitir factura (auto-correlativo + XML UBL 2.1) |
| `GET` | `/api/v1/factura` | Listar facturas (filtros) |
| `GET` | `/api/v1/factura/{facturaId}` | Obtener factura por ID |
| `GET` | `/api/v1/factura/{facturaId}/xml` | 🆕 Descargar XML UBL 2.1 (`application/xml`) |
| `PUT` | `/api/v1/factura` | Anular factura |
| `PUT` | `/api/v1/factura/{facturaId}/emitir` | 🆕 Emitir borrador (genera XML + notifica) |
| `DELETE` | `/api/v1/factura` | Eliminar borrador |

### 🎫 Boleta — Boletas de venta

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/boleta` | Emitir boleta (auto-correlativo) |
| `GET` | `/api/v1/boleta` | Listar boletas |
| `GET` | `/api/v1/boleta/{boletaId}` | Obtener boleta por ID |
| `PUT` | `/api/v1/boleta` | Anular boleta |
| `DELETE` | `/api/v1/boleta` | Eliminar borrador |

### 📉 Nota de Crédito

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/nota_credito` | Emitir nota de crédito |
| `GET` | `/api/v1/nota_credito` | Listar notas de crédito |
| `GET` | `/api/v1/nota_credito/{id}` | Obtener nota por ID |
| `PUT` | `/api/v1/nota_credito` | Actualizar borrador |
| `DELETE` | `/api/v1/nota_credito` | Eliminar borrador |

### 📈 Nota de Débito

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/nota_debito` | Emitir nota de débito |
| `GET` | `/api/v1/nota_debito` | Listar notas de débito |
| `GET` | `/api/v1/nota_debito/{id}` | Obtener nota por ID |
| `PUT` | `/api/v1/nota_debito` | Actualizar borrador |
| `DELETE` | `/api/v1/nota_debito` | Eliminar borrador |

### 📦 Product — Productos y servicios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/product` | Buscar productos (paginado + term) |
| `GET` | `/api/v1/product/{productId}` | Obtener producto por ID |
| `POST` | `/api/v1/product` | Crear producto |
| `PUT` | `/api/v1/product` | Actualizar producto |
| `DELETE` | `/api/v1/product` | Eliminar producto (soft/hard) |

### 👔 Employee — Empleados

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/employee` | Listar empleados (filtro estado) |
| `GET` | `/api/v1/employee/{employeeId}` | Obtener empleado por ID |
| `POST` | `/api/v1/employee` | Crear empleado |
| `PUT` | `/api/v1/employee` | Actualizar empleado |
| `DELETE` | `/api/v1/employee` | Eliminar empleado |

### 🏪 Sede — Sedes / Sucursales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/sede` | Listar sedes por empresa |
| `GET` | `/api/v1/sede/{sedeId}` | Obtener sede por ID |
| `POST` | `/api/v1/sede` | Crear sede |
| `PUT` | `/api/v1/sede` | Actualizar sede |
| `DELETE` | `/api/v1/sede` | Eliminar sede |
| `POST` | `/api/v1/sede/{sedeId}/generar_series` | Generar series automáticas para sede |

### 🔢 Serie Config / Ubigeo / Unidad Medida

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/serie_config` | Listar series por empresa/tipo |
| `GET` | `/api/v1/ubigeo` | Catálogo de ubigeos SUNAT |
| `GET` | `/api/v1/unidad_medida` | Catálogo de unidades de medida SUNAT |

### 💱 Tipo de Cambio

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/tipo_cambio` | Consultar TC por fecha/moneda/fuente |
| `POST` | `/api/v1/tipo_cambio` | Registrar tipo de cambio |
| `GET` | `/api/v1/tipo_cambio/list` | Listar tipos de cambio históricos |

### ⚙️ User Preference

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/user_preference` | Listar preferencias por empresa |
| `PUT` | `/api/v1/user_preference` | Guardar preferencia |

### 📚 Libro Electrónico

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/libro_electronico` | Generar libro electrónico |
| `GET` | `/api/v1/libro_electronico` | Listar libros generados |

### 🔔 Notificaciones (OpenAPI)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/notification` | Listar notificaciones por empresa |
| `POST` | `/api/v1/notification` | Publicar notificación de transmisión |
| `PUT` | `/api/v1/notification/read-all` | Marcar todas como leídas |
| `PUT` | `/api/v1/notification/{id}/read` | Marcar una como leída |
| `DELETE` | `/api/v1/notification` | Limpiar notificaciones |

### 🖥️ Vistas (Spring MVC)

| Método | Ruta | Template |
|--------|------|----------|
| `GET` | `/` | `welcome.html` |
| `GET` | `/menu` | `menu.html` |
| `GET` | `/documents` | `documents.html` |
| `GET` | `/facturas` | `facturas.html` |
| `GET` | `/boletas` | `boletas.html` |
| `GET` | `/notas-credito` | `notas-credito.html` |
| `GET` | `/notas-debito` | `notas-debito.html` |
| `GET` | `/clients` | `clients.html` |
| `GET` | `/products` | `products.html` |
| `GET` | `/employees` | `employees.html` |
| `GET` | `/sedes` | `sedes.html` |
| `GET` | `/tipo-cambio` | `tipo-cambio.html` |
| `GET` | `/resumenes-diarios` | `resumenes-diarios.html` |
| `GET` | `/wizard?type=...` | `wizard/factura.html` |

> **Total: 60+ endpoints REST (OpenAPI — incluye notificaciones + XML UBL) + 14 vistas MVC + consola H2 = 75+ rutas**

### 📋 Formato de respuesta estándar

```json
{
  "isSuccess": true,
  "status": "OK",
  "message": "Operación exitosa",
  "timestamp": "2026-04-06T10:30:00Z",
  "data": { ... }
}
```

Errores:

```json
{
  "isSuccess": false,
  "status": "NOT_FOUND",
  "message": "Empresa no encontrado con ID: abc-123",
  "timestamp": "2026-04-06T10:30:00Z"
}
```

Validaciones:

```json
{
  "isSuccess": false,
  "status": "VALIDATION_ERROR",
  "message": "Error de validación en los campos enviados",
  "data": ["ruc: no puede estar vacío", "razonSocial: no puede estar vacío"]
}
```

Listas paginadas:

```json
{
  "isSuccess": true,
  "data": {
    "content": [ ... ],
    "pagination": { "currentPage": 0, "pageSize": 10, "totalElements": 50, "totalPages": 5 }
  }
}
```

---

## 🖥️ Frontend Multi-Page App

La aplicación incluye un **frontend completo** construido con **Thymeleaf + Vanilla JS modular** con las siguientes características:

### Características del Frontend

| Característica | Detalle |
|---------------|---------|
| **Multi-Page App** | 14 vistas con navegación real (server-side routing) |
| **Tema** | 🌙 Dark / ☀️ Light mode con toggle persistente |
| **CSS Design System** | 1,606 líneas — variables CSS, grid, componentes, badges, toasts |
| **Wizard 5 pasos** | Documento → Emisor → Cliente → Detalle → Revisión & Emisión |
| **Edit mode** | 🆕 Carga borrador/rechazada completo para edición en wizard |
| **Borrador / Emisión** | 🆕 Toggle "Emisión en Envío" — ambos botones siempre disponibles |
| **Soporte multi-doc** | Facturas, Boletas, Notas de Crédito, Notas de Débito |
| **Cálculo automático** | IGV 18% en tiempo real + dropdown Afectación (Gravado/Exonerado/Inafecto) |
| **Tipo de cambio** | Consulta automática SBS/SUNAT para moneda extranjera |
| **Forma de pago** | CONTADO / CRÉDITO con fecha de vencimiento automática |
| **Cargos/Descuentos** | 🆕 Tabla de cargos/descuentos globales Cat. 53 SUNAT |
| **Cuotas de crédito** | 🆕 Gestión de cuotas con monto, fecha vencimiento, moneda |
| **Búsqueda de clientes** | Modal con búsqueda en tiempo real + consulta RENIEC/SUNAT |
| **Registro de clientes** | Formulario inline sin salir del wizard |
| **Búsqueda de productos** | Modal con búsqueda live contra API |
| **Preview de documentos** | Modal rich preview con detalle, ítems, cargos/desc., cuotas, totales, estado |
| **PDF export** | 🆕 Incluye cargos/descuentos, cuotas y totales desglosados |
| **Quick Emit** | 🆕 Botón 📤 en lista para emitir borrador directamente |
| **Config overlay** | Panel completo: empresa, series, sede, mapa, tema, emisión en envío |
| **Mapa Leaflet** | Ubicación de sede con geocoding (Nominatim), drag marker |
| **Series por sede** | Cambio automático de series al cambiar de sede |
| **Serie como dropdown** | 🆕 Serie se carga dinámicamente desde sede activa por tipo doc |
| **Formato SUNAT** | 🆕 Números con padding 8 dígitos: `F001-00000001` |
| **Notificaciones** | Badge en tiempo real: docs rechazados + transmisiones |
| **Anulación** | Botón de anular documentos rechazados desde notificaciones |
| **Re-emisión** | 🆕 RECHAZADA → ENVIADA directamente (sin pasar por BORRADOR) |
| **Error handling** | Toast global con detalle + botón screenshot + envío por email |
| **Responsive** | Adaptable a desktop, tablet y mobile |
| **Sample docs** | XMLs y PDFs de ejemplo (factura y boleta reales) |

### Flujo visual del Wizard (5 pasos)

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌─────────────┐
│  PASO 1  │──►│  PASO 2  │──►│  PASO 3  │──►│  PASO 4  │──►│   PASO 5    │
│ 📄 Doc   │   │ 🏢 Emisor│   │ 👤 Cliente│   │ 📦 Items │   │ 🚀 Emitir   │
│(serie,   │   │(readonly)│   │(búsqueda)│   │(productos│   │  (review +  │
│ moneda,  │   │          │   │          │   │ cargos)  │   │  tabla)     │
│ tipoOp)  │   │          │   │          │   │          │   │             │
└──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────┬──────┘
                                 │                │                  │
                                 ▼                ▼            fetch POST
                           🔍 Client Modal  🔍 Product Modal  /api/v1/{tipo}
                           📋 RENIEC/SUNAT  📦 Búsqueda live        │
                           ✏️ Registro                               ▼
                                                              ┌─────────────┐
                                                              │ 📋 Response  │
                                                              │   + 🧾 XML   │
                                                              │   + 🔔 Notif │
                                                              └─────────────┘

Edit Mode:  /wizard?type=FACTURA&id=<facturaId>
            ├── Carga borrador completo (serie, cliente, ítems, cuotas, cargos)
            ├── Serie y fecha readonly
            └── Botón "Emitir Borrador" → PUT /factura/{id}/emitir
```

---

## 🛡️ Error Handling

### Excepciones custom

| Excepción | HTTP Status | Uso |
|-----------|-------------|-----|
| `ResourceNotFoundException` | 404 Not Found | Entidad no existe en BD |
| `BusinessException` | 422 Unprocessable Entity | Regla de negocio violada |
| `ValidationException` | 400 Bad Request | Validaciones custom con lista de errores |
| `SunatValidationException` | 422 Unprocessable Entity | Reglas SUNAT específicas |
| `MethodArgumentNotValidException` | 400 Bad Request | Falla de `@Valid` automática (Jakarta) |
| `Exception` (fallback) | 500 Internal Server Error | Cualquier error no esperado |

### Frontend Error UX

```
   Error HTTP (fetch)
         │
         ▼
   ETIA.showError()          ← Toast con detalle completo
         │
         ├──► 📸 "Capturar pantalla"   → html2canvas → descarga PNG
         │
         └──► 📧 "Enviar por email"    → mailto: con datos del error + referencia al screenshot
```

---

## 📦 Seed Data (data.sql)

La aplicación carga **datos de prueba automáticamente** al iniciar vía `data.sql` (H2 in-memory).

| Entidad | Registros | Ejemplo |
|---------|-----------|---------|
| **Empresa** | 1 | ETLIONS S.A.C. · RUC 20601234567 · San Isidro |
| **Clientes** | 5 | RUC, DNI, Carnet Extranjería, Pasaporte (con depto/prov/dist/país) |
| **Productos** | 3 | Consultoría TI, Licencia ERP, Soporte Técnico |
| **Empleados** | 3 | Contador, Asistente, Cajero |
| **Ubigeos** | 7 | Lima, San Isidro, Miraflores, Surco, Arequipa, Trujillo |
| **Unidades Medida** | 14 | NIU, ZZ, HUR, KGM, LTR, MTR, etc. |
| **Sedes** | 2 | Principal San Isidro + Sucursal Miraflores |
| **Series** | 8 | F001/B001/FC01/FD01 × 2 sedes |
| **Facturas** | 23 | ACEPTADA, BORRADOR, ENVIADA, RECHAZADA, ANULADA, USD, multi-ítem |
| **Boletas** | 2 | ACEPTADA, BORRADOR |
| **Notas Crédito** | 2 | Descuento, Anulación |
| **Notas Débito** | 2 | Intereses, Gastos administrativos |
| **Correlativos** | 4 | F001→3, B001→2, F002→0, B002→0 |
| **User Preferences** | 5 | moneda, tema, itemType, tcFuente, emisionEnEnvio |
| **Tipo de Cambio** | 7 | SBS + SUNAT (últimos días) |
| **Libros Electrónicos** | 3 | Ventas y compras (2026-02, 2026-03) |
| **Cuotas Crédito** | 🆕 | Cuotas para facturas a crédito |
| **Cargos/Descuentos** | 🆕 | Descuento vol. 5%, Recargo consumo 10% |
| **XML UBL 2.1** | 🆕 10 | Facturas ACEPTADA/ENVIADA con XML completo |

---

## 🛠️ Tech Stack

| Categoría | Tecnología | Versión | Propósito |
|-----------|-----------|---------|-----------|
| **Framework** | Spring Boot | 4.0.5 | Framework principal |
| **Lenguaje** | Java | 21 (LTS) | Runtime |
| **API** | OpenAPI Generator | 7.9.0 | Generación de interfaces y DTOs desde contrato |
| **ORM** | Spring Data JPA + Hibernate | — | Persistencia |
| **Mapping** | MapStruct | 1.6.3 | Conversión DTO ↔ Entity (compile-time) |
| **Boilerplate** | Lombok | 1.18.30 | `@Getter @Setter @Builder @RequiredArgsConstructor` |
| **Template** | Thymeleaf | — | Multi-Page App (14+ vistas + fragments) |
| **Frontend** | Vanilla JS (modular) | ES5+ | 10+ módulos JS, 1,606+ líneas CSS |
| **Mapas** | Leaflet.js | — | Mapa interactivo en Config + GeoInsight |
| **Geocoding** | Nominatim (OSM) | — | Búsqueda de direcciones + reverse geocode |
| **BD (dev)** | H2 Database | — | Base de datos en memoria |
| **BD (prod)** | PostgreSQL | — | Base de datos producción (driver incluido) |
| **Validación** | Hibernate Validator + Jakarta | 3.0.2 | Validación de requests |
| **Docs** | Swagger / OpenAPI 3.0.3 | — | Contrato de API (`ms-bs-etia.yaml`) |
| **Build** | Maven Wrapper | — | Build reproducible sin instalar Maven |
| **Seguridad** | Spring Security + OAuth2 | — | Autenticación/Autorización (FASE B) |
| **JWT** | JJWT | 0.12.6 | JSON Web Tokens (FASE B) |
| **Excel** | Apache POI | 5.2.5 | Exportación de reportes (FASE E/J) |
| **Places** | Google Maps Services | 2.2.0 | Radar de Mercado (FASE D) |
| **Gráficos** | Chart.js | — | Dashboards (futuro) |

---

## 🚀 Quick Start

### Prerrequisitos

- **Java 21+** instalado ([Descargar](https://adoptium.net/))
- **Git** para clonar el repositorio

> No necesitas Maven instalado — el proyecto incluye Maven Wrapper (`mvnw`)

### 1. Clonar el repositorio

```bash
git clone https://github.com/etlions/ms-bs-etia.git
cd ms-bs-etia
```

### 2. Compilar y ejecutar

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

### 3. Abrir la aplicación

```
🖥️ App:          http://localhost:8080/
📋 Menú:         http://localhost:8080/menu
📊 H2 Console:   http://localhost:8080/h2-console
🔌 API REST:     http://localhost:8080/api/v1/
```

### 4. Probar la API con curl

```bash
# Listar empresas
curl http://localhost:8080/api/v1/company?page=0&size=10

# Listar clientes
curl http://localhost:8080/api/v1/client?companyId=cmp-demo-001

# Buscar productos
curl "http://localhost:8080/api/v1/product?companyId=cmp-demo-001&term=consultoría"

# Emitir una factura
curl -X POST http://localhost:8080/api/v1/factura \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": "cmp-demo-001",
    "clientId": "cli-demo-001",
    "serie": "F001",
    "moneda": "PEN",
    "gravadas": 2500.00,
    "igv": 450.00,
    "total": 2950.00,
    "leyenda": "SON DOS MIL NOVECIENTOS CINCUENTA Y 00/100 SOLES",
    "detalle": [{
      "descripcion": "Consultoría TI",
      "unidadMedida": "ZZ",
      "cantidad": 1,
      "valorUnitario": 2500.00,
      "precioUnitario": 2950.00,
      "igv": 450.00,
      "subtotal": 2500.00
    }]
  }'

# Consultar tipo de cambio
curl "http://localhost:8080/api/v1/tipo_cambio?fecha=2026-04-06&moneda=USD&fuente=SBS"
```

### 5. Explorar la base de datos

Consola H2: **http://localhost:8080/h2-console**

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:localdb` |
| User | `sa` |
| Password | *(vacío)* |

---

## 🗺️ Roadmap

```
                                    Estado actual
                                         │
                                         ▼
 ────●────────────────●──────────●───────●─────────────────────────────────────►
     │                │          │       │
     ▼                ▼          ▼       ▼
  ✅ FASE 1        ✅ FASE 2    🔲 FASES A-N           🔲 Production
  (Completada)     (Completada) (Suite v3.0)            Ready

  CRUD + OpenAPI    XML UBL 2.1  00: Auditoría Data     Docker
  Error Handling    12 Catálogos   A: Core               CI/CD
  20 dominios       Wizard 5-step  B: Security + OAuth2  PostgreSQL
  60+ endpoints     Edit mode      C: POS Pro            Firma SUNAT
  14 vistas MPA     Borrador/Emit  D: Radar de Mercado   Envío SOAP
  Notificaciones    295 tests      E: Alpha Campaigns    CDR
  Auto-correlativo                 F: Supply Chain       PDF gen
  Validaciones                     G: Overlord Admin     QR Code
                                   H: Adjuster
                                   I: Deal Flow CRM
                                   J: Data Vault
                                   K: GeoInsight
                                   L: Códigos
                                   M: Frontend Modular
                                   N: Integración Final
```

### ✅ FASE 1 — Plataforma Base Completa (Finalizada Abril 2026)

> De scaffold vacío a **aplicación de facturación electrónica completa conforme a SUNAT** con arquitectura hexagonal homologada.

**Hitos incluidos:**

| Hito | Descripción |
|------|-------------|
| **Base (ex-Fase 0)** | CRUD completo, OpenAPI codegen, MapStruct, JPA, dominios fundacionales |
| **Error Handling (ex-Fase 1)** | `@RestControllerAdvice`, excepciones custom, Thymeleaf wizard, test data |
| **MPA + Dominios (ex-Fase 1.5)** | 14 vistas Thymeleaf, 10 nuevos dominios, frontend modular completo |
| **Homologación arquitectura** | 20 dominios con patrón `openapi/ → service/ → service/impl/ → persistence/` |

**Entregables finales de FASE 1:**

| Feature | Detalle |
|---------|---------|
| **🏪 14 vistas Thymeleaf** | Welcome, Menu, Documents, Facturas, Boletas, Notas C/D, Clients, Products, Employees, Sedes, Tipo Cambio, Resúmenes, Wizard |
| **🖥️ 14 View Controllers** | Cada vista con su propio `@Controller` Spring MVC |
| **📦 18+2 dominios homologados** | Todos con patrón `openapi/service/impl/persistence/mapper` (20 total con catalogo_sunat y tabla_sunat) |
| **🔢 Auto-correlativo** | `CorrelativoService` — F001-1, F001-2, etc. por serie |
| **✅ SunatValidator** | Validaciones de negocio SUNAT pre-emisión |
| **🔔 Notificaciones OpenAPI** | 5 endpoints REST: listar, publicar, marcar leída, marcar todas, limpiar |
| **🚫 Anulación** | Anular documentos rechazados por SUNAT desde notificaciones |
| **👤 Client Modal** | Búsqueda + registro inline de clientes con consulta RENIEC/SUNAT |
| **📦 Product Modal** | Búsqueda live de productos contra API |
| **🔍 Document Preview** | Modal rich preview con ítems, totales, estados, badges |
| **⚙️ Config Overlay** | Panel completo: empresa, preferencias, series, sede, mapa |
| **🗺️ Leaflet Maps** | Mapa interactivo con geocoding para ubicación de sede |
| **💱 Tipo de Cambio** | Consulta automática SBS/SUNAT al cambiar moneda a USD |
| **🎨 Dark/Light theme** | Toggle persistente con CSS variables |
| **❌ Error UX** | Toast con screenshot (html2canvas) + envío por email |
| **📄 JS modular** | 10 archivos JS separados por responsabilidad |
| **🎨 CSS Design System** | 1,606 líneas — componentes, badges, modals, toasts, grids |
| **📊 data.sql** | Seed completo: 5 clientes, 3 productos, 3 empleados, 2 sedes, 8 series, 7 docs, TC, libros |
| **📄 Sample docs** | XMLs y PDFs reales de ejemplo (factura + boleta) |
| **⚡ Multi-doc wizard** | Soporte para Factura, Boleta, NC, ND con mismo wizard |
| **💳 Forma de pago** | CONTADO / CRÉDITO con lógica de fecha de vencimiento |
| **🏪 Sedes + Series** | Cambio de sede → auto-actualización de series |

### ✅ Fase 2 — XML UBL 2.1 SUNAT (Completada)

Generación del XML electrónico conforme al estándar UBL 2.1 que exige SUNAT:

| Feature | Descripción | Estado |
|---------|-------------|--------|
| **Estructura XML UBL 2.1** | Generar `Invoice` conforme al estándar OASIS UBL 2.1 adaptado por SUNAT | ✅ |
| **12 Catálogos SUNAT** | Cat. 01, 02, 03, 04, 05, 06, 07, 13, 16, 51, 52, 53 | ✅ |
| **Builder pattern XML** | `UblInvoiceXmlBuilder` (36 tests) | ✅ |
| **Namespace management** | `cac:`, `cbc:`, `ext:`, `sac:` | ✅ |
| **Emisor/Receptor XML** | `AccountingSupplierParty`, `AccountingCustomerParty` | ✅ |
| **Líneas de detalle** | `InvoiceLine` completo | ✅ |
| **Totales y tributos** | `TaxTotal`, `LegalMonetaryTotal` | ✅ |
| **Leyenda** | `Note` element (Cat. 52) | ✅ |
| **Tipo de cambio XML** | `PaymentExchangeRate` (USD) | ✅ |
| **Forma de pago** | `PaymentTerms` (Contado/Crédito) | ✅ |
| **Cargos/Descuentos** | `AllowanceCharge` (Cat. 53) | ✅ |
| **Almacenamiento XML** | `xml_content` (BD) + `xml_path` (disco) | ✅ |
| **Descarga XML** | `GET /factura/{id}/xml` | ✅ |
| **Modelo enriquecido** | +9 Factura/Boleta, +4 Client, +6 DetalleItem | ✅ |
| **Wizard 5 pasos** | + edit mode + borrador/emit | ✅ |
| **295 tests** | JUnit 5 + Mockito — todos verdes | ✅ |

### 🔲 Suite v3.0 — Fases A-N (Planificadas)

> **15 fases documentadas** en [`docs/fases/`](docs/fases/). Cada fase es un archivo .md independiente con: objetivo, tareas detalladas, esquemas de BD, endpoints OpenAPI, lógica de negocio, escenarios Gherkin, criterios de aceptación y lista de archivos a crear/modificar.

| Fase | Nombre | Módulos | Prioridad | Estado |
|------|--------|---------|-----------|--------|
| **00** | Auditoría de Data | Transversal | 🔴 Crítica | ⬜ |
| **A** | Core (Cimientos) | Core, PuntoVenta base | 🔴 Crítica | ⬜ |
| **B** | Seguridad + OAuth2 | Security, Plans, Roles | 🔴 Crítica | ⬜ |
| **C** | POS Pro + Team Compass | POS, Employee ext. | 🟡 Alta | ⬜ |
| **D** | Radar de Mercado | Google Places, Leads | 🟡 Alta | ⬜ |
| **E** | Alpha Campaigns | Campaigns, ROI, Conversions | 🟡 Alta | ⬜ |
| **F** | Supply Chain | Suppliers, Purchase Orders | 🟡 Alta | ⬜ |
| **G** | Overlord Admin | Dashboard, Tickets, Plan Forge | 🟡 Alta | ⬜ |
| **H** | Adjuster (The Guillotine) | Cancelación parcial/total | 🟡 Alta | ⬜ |
| **I** | Deal Flow CRM | Funnel, Kanban, Activities | 🟢 Media | ⬜ |
| **J** | Data Vault Reportes | Excel export, Métricas | 🟢 Media | ⬜ |
| **K** | GeoInsight | Heatmap, Auto-ubigeo | 🟢 Media-Baja | ⬜ |
| **L** | Códigos Correlativos | Secuencias centralizadas | 🟢 Media | ⬜ |
| **M** | Frontend Modular | JS modular, Auth flow, UI | 🟡 Alta | ⬜ |
| **N** | Integración Final | Compilación, Tests, Data | 🔴 Crítica | ⬜ |

### 🔲 Production Ready (Post Suite v3.0)

| Feature | Descripción | Prioridad |
|---------|-------------|-----------|
| **XMLDSig** | Firma digital XML RSA-SHA256 | 🔴 Crítica |
| **Web Service SUNAT** | Envío SOAP + CDR | 🔴 Crítica |
| **PostgreSQL + Flyway** | Migraciones de BD | 🟡 Alta |
| **PDF generation** | Representación impresa (iText) | 🟡 Alta |
| **QR Code** | QR en PDF conforme SUNAT | 🟢 Media |
| **Docker** | Dockerfile + docker-compose | 🟢 Media |
| **CI/CD** | GitHub Actions | 🟢 Media |
| **Email** | Envío de comprobante por email | 🟢 Media |

---

## 📄 Contrato OpenAPI

```
src/main/resources/swagger/ms-bs-etia.yaml   (~3,300 líneas)
```

Se genera automáticamente durante `mvn compile` usando **OpenAPI Generator 7.9.0**:

| Generado en | Paquete |
|-------------|---------|
| Interfaces API | `com.etlions.openapicodegen.api.v1` |
| DTOs (Request/Response) | `com.etlions.openapicodegen.dto` |

> ⚠️ **No editar** los archivos en `target/generated-sources/openapi/`. Modificar siempre el YAML fuente.

---

## 🏗️ Build

```bash
# Compilar (genera OpenAPI + MapStruct)
.\mvnw.cmd compile

# Empaquetar JAR
.\mvnw.cmd package -DskipTests

# Ejecutar JAR
java -jar target/ms-bs-etia-0.0.1-SNAPSHOT.jar

# Ejecutar tests
.\mvnw.cmd test
```

---

## 📊 Estadísticas del proyecto

| Métrica | Valor actual | Post Suite v3.0 (estimado) |
|---------|-------------|---------------------------|
| Archivos Java (src) | **~140+** | **~350+** |
| Dominios de negocio | **20** | **32+** |
| Endpoints REST (OpenAPI) | **60+** | **140+** |
| View Controllers (MVC) | **14** | **25+** |
| Entidades JPA | **17** | **35+** |
| Repositorios | **17** | **35+** |
| MapStruct Mappers | **14** | **25+** |
| Servicios (interface + impl) | **30+** | **65+** |
| Templates Thymeleaf | **19** | **30+** |
| Archivos JS | **10** | **25+** |
| CSS Design System | **1,606 líneas** | **2,500+** |
| Contrato OpenAPI YAML | **~5,200+ líneas** | **~10,000+** |
| Tests unitarios (JUnit 5) | **295** (0 fallos) | **500+** |
| Catálogos SUNAT | **12** | **12** |
| Fases de ejecución | — | **15** (00 + A-N) |
| Documentos de planificación | — | **20+ archivos .md** |

---

## 📁 Documentación del Proyecto

| Documento | Ubicación | Descripción |
|-----------|-----------|-------------|
| **Especificación Técnica** | [`ESPECIFICACION_TECNICA.md`](ESPECIFICACION_TECNICA.md) | Estándares OpenAPI, endpoints, DTOs, entidades (2,146 líneas) |
| **Plantilla Bootstrap** | [`PLANTILLA_OPENAPI_BOOTSTRAP.yaml`](PLANTILLA_OPENAPI_BOOTSTRAP.yaml) | Template reutilizable para nuevos proyectos (1,500+ líneas) |
| **Arquitectura** | [`docs/ARQUITECTURA_PATRONES_STACK.md`](docs/ARQUITECTURA_PATRONES_STACK.md) | Patrones, stack, convenciones, checklist |
| **Fases de Ejecución** | [`docs/fases/`](docs/fases/) | 15 archivos .md con tareas detalladas |
| **Bitácora** | [`docs/BITACORA_EJECUCION.md`](docs/BITACORA_EJECUCION.md) | Registro de ejecución por fase |
| **Preguntas** | [`docs/PREGUNTAS_DEFINICION.md`](docs/PREGUNTAS_DEFINICION.md) | Decisiones pendientes con defaults |
| **Escenarios Factura** | [`src/main/resources/scenarios/`](src/main/resources/scenarios/) | Gherkin scenarios por tipo de documento |

---

## 🤝 Contribuir

1. Fork del repositorio
2. Crear branch feature: `git checkout -b feature/mi-feature`
3. Commit: `git commit -m 'feat: mi nueva funcionalidad'`
4. Push: `git push origin feature/mi-feature`
5. Crear Pull Request

> 📖 **Antes de implementar:** Leer [`docs/ARQUITECTURA_PATRONES_STACK.md`](docs/ARQUITECTURA_PATRONES_STACK.md) y la fase correspondiente en [`docs/fases/`](docs/fases/).

---

## 📝 Licencia

Este proyecto es propiedad de **ETLIONS**. Todos los derechos reservados.

---

<p align="center">
  <sub>Hecho con ❤️ por el equipo ETLIONS — Lima, Perú 🇵🇪</sub><br/>
  <sub>Última actualización: Abril 2026</sub><br/>
  <sub>🧾 ETIA Suite v3.0 — Business Intelligence Platform</sub>
</p>
