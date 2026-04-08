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
</p>

<h1 align="center">🧾 ETIA — Facturación Electrónica Perú</h1>

<p align="center">
  <b>Microservicio backend + frontend integrado para emisión de comprobantes electrónicos conforme a SUNAT</b><br/>
  <sub>Facturas · Boletas · Notas de Crédito · Notas de Débito · XML UBL 2.1 · Productos · Clientes · Empleados · Sedes · Series · Tipo de Cambio · Libros Electrónicos · Notificaciones en Tiempo Real · Consulta RENIEC/SUNAT</sub>
</p>

<p align="center">
  <a href="#-novedades-abril-2026--fase-2-en-progreso-">FASE 2</a> •
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

## 🔥 Novedades (Abril 2026) — FASE 2 EN PROGRESO 🚧

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

## 📦 Dominios

Cada dominio vive en su propio paquete con **sub-paquetes homologados** (openapi / service / service/impl / mapper / persistence):

```
com.etlions.ms_bs_etia
│
├── 🏢 company/                    Empresas emisoras (CRUD)
│   ├── openapi/                   CompanyOpenApi.java
│   ├── service/impl/              companyServiceImpl.java
│   ├── mapper/                    CompanyMapper.java
│   └── persistence/               CompanyEntity + CompanyRepository
│
├── 👤 client/                     Clientes / adquirientes (CRUD)
│   ├── openapi/                   clientOpenApi.java
│   ├── service/impl/              clienteServiceImpl.java
│   ├── mapper/                    ClientMapper.java
│   └── persistence/               ClientEntity + ClientRepository
│
├── 🧾 doc_factura/                Facturas electrónicas (CRUD + Emisión + Anulación + XML UBL 2.1)
│   ├── openapi/                   DocFacturaOpenApi.java
│   ├── service/impl/              docFacturaServiceImpl.java
│   ├── mapper/                    FacturaMapper.java
│   ├── persistence/               FacturaEntity + FacturaRepository
│   │                              CuotaCreditoEntity + CuotaCreditoRepository
│   │                              AllowanceChargeEntity + AllowanceChargeRepository
│   └── xml/                       UblInvoiceXmlBuilder.java (genera XML UBL 2.1)
│
├── 🎫 doc_boleta/                 Boletas de venta (CRUD + Emisión + Anulación)
│   ├── openapi/                   DocBoletaOpenApi.java
│   ├── service/impl/              docBoletaServiceImpl.java
│   ├── mapper/                    BoletaMapper.java
│   └── persistence/               BoletaEntity + BoletaRepository
│
├── 📉 doc_notacred/               Notas de crédito (CRUD + Emisión)
│   ├── openapi/                   DocNotacredOpenApi.java
│   ├── service/impl/              docNotacredServiceImpl.java
│   ├── mapper/                    NotaCreditoMapper.java
│   └── persistence/               NotaCreditoEntity + NotaCreditoRepository
│
├── 📈 doc_notadeb/                Notas de débito (CRUD + Emisión)
│   ├── openapi/                   DocNotadebOpenApi.java
│   ├── service/impl/              docNotadebServiceImpl.java
│   ├── mapper/                    NotaDebitoMapper.java
│   └── persistence/               NotaDebitoEntity + NotaDebitoRepository
│
├── 📦 product/                    Productos y servicios (CRUD + búsqueda)
│   ├── openapi/                   ProductOpenApi.java
│   ├── service/impl/              productServiceImpl.java
│   ├── mapper/                    ProductMapper.java
│   └── persistence/               ProductEntity + ProductRepository
│
├── 👔 employee/                   Empleados (CRUD)
│   ├── openapi/                   EmployeeOpenApi.java
│   ├── service/impl/              employeeServiceImpl.java
│   ├── mapper/                    EmployeeMapper.java
│   └── persistence/               EmployeeEntity + EmployeeRepository
│
├── 🏪 sede/                       Sedes / sucursales (CRUD + generar series)
│   ├── openapi/                   SedeOpenApi.java
│   ├── service/impl/              sedeServiceImpl.java
│   ├── mapper/                    SedeMapper.java
│   └── persistence/               SedeEntity + SedeRepository
│
├── 🔢 serie_config/               Series por sede (F001, B001, etc.)
│   ├── openapi/                   SerieConfigOpenApi.java
│   ├── service/impl/              serieConfigServiceImpl.java
│   ├── mapper/                    SerieConfigMapper.java
│   └── persistence/               SerieConfigEntity + SerieConfigRepository
│
├── 💱 tipo_cambio/                Tipo de cambio (SBS / SUNAT)
│   ├── openapi/                   TipoCambioOpenApi.java
│   ├── service/impl/              tipoCambioServiceImpl.java
│   ├── mapper/                    TipoCambioMapper.java
│   └── persistence/               TipoCambioEntity + TipoCambioRepository
│
├── 📚 libro_electronico/          Libros electrónicos PLE (ventas, compras)
│   ├── openapi/                   LibroElectronicoOpenApi.java
│   ├── service/impl/              libroElectronicoServiceImpl.java
│   ├── mapper/                    LibroElectronicoMapper.java
│   └── persistence/               LibroElectronicoEntity + LibroElectronicoRepository
│
├── 🗺️ ubigeo/                     Catálogo SUNAT de ubigeos
│   ├── openapi/                   UbigeoOpenApi.java
│   ├── service/impl/              ubigeoServiceImpl.java
│   ├── mapper/                    UbigeoMapper.java
│   └── persistence/               UbigeoEntity + UbigeoRepository
│
├── 📏 unidad_medida/              Catálogo SUNAT de unidades de medida
│   ├── openapi/                   UnidadMedidaOpenApi.java
│   ├── service/impl/              unidadMedidaServiceImpl.java
│   ├── mapper/                    UnidadMedidaMapper.java
│   └── persistence/               UnidadMedidaEntity + UnidadMedidaRepository
│
├── 👤 reniec/                     Consulta RENIEC / SUNAT (DNI ↔ RUC)
│   ├── openapi/                   ReniecOpenApi.java
│   ├── service/                   ReniecService.java
│   └── service/impl/              ReniecServiceImpl.java (mock — producción conectará APIs externas)
│
├── ⚙️ user_preference/            Preferencias de usuario por empresa
│   ├── openapi/                   UserPreferenceOpenApi.java
│   ├── service/impl/              userPreferenceServiceImpl.java
│   ├── mapper/                    UserPreferenceMapper.java
│   └── persistence/               UserPreferenceEntity + UserPreferenceRepository
│
├── 🔔 notification/               Notificaciones in-memory (emisión/anulación)
│   ├── openapi/                   NotificationOpenApi.java (REST controller)
│   ├── service/                   NotificationService.java (interface)
│   ├── service/impl/              NotificationServiceImpl.java
│   └── persistence/               NotificationStore.java + NotificationItem.java
│
├── 📋 catalogo_sunat/             Catálogos estándar SUNAT (12 catálogos)
│   ├── openapi/                   CatalogoSunatOpenApi.java
│   ├── service/impl/              CatalogoSunatServiceImpl.java
│   ├── persistence/               CatalogoSunatEntity + JSON repository
│   └── standard/                  Constantes Cat. 01-53
│
├── 📊 tabla_sunat/                Tablas de referencia SUNAT (JSON)
│   ├── openapi/                   TablaSunatOpenApi.java
│   ├── service/impl/              TablaSunatServiceImpl.java
│   └── persistence/               TablaSunatJsonRepository.java
│
├── 🖥️ view/                       14 View Controllers (Spring MVC + Thymeleaf)
│   ├── WelcomeViewController.java
│   ├── MenuViewController.java
│   ├── DocumentsViewController.java
│   ├── FacturasViewController.java
│   ├── BoletasViewController.java
│   ├── NotasCreditoViewController.java
│   ├── NotasDebitoViewController.java
│   ├── ClientsViewController.java
│   ├── ProductsViewController.java
│   ├── EmployeesViewController.java
│   ├── SedesViewController.java
│   ├── TipoCambioViewController.java
│   ├── ResumenesDiariosViewController.java
│   └── WizardViewController.java
│
├── ⚙️ app_config/                 Configuración global
│   ├── config/                    ThymeleafConfig.java + SunatProperties.java
│   └── exception/                 Excepciones custom
│       ├── GlobalExceptionHandler.java    @RestControllerAdvice (5+ handlers)
│       ├── ResourceNotFoundException.java    404
│       ├── BusinessException.java            422
│       ├── ValidationException.java          400
│       └── SunatValidationException.java     422 (reglas SUNAT)
│
├── 🛠️ app_util/                   Utilidades compartidas
│   ├── service/                   CorrelativoService.java (interface)
│   ├── service/impl/              CorrelativoServiceImpl.java
│   ├── persistence/               CorrelativoEntity.java + CorrelativoRepository.java
│   └── validation/                SunatValidator.java (reglas SUNAT)
│
└── 🚀 MsBsEtiaApplication.java   Entry point
```

### 📊 Modelo de datos

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│    companies     │       │     clients      │       │    employees     │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id          (PK) │◄──┐   │ id          (PK) │       │ id          (PK) │
│ ruc         (UQ) │   │   │ company_id  (FK) │──┐    │ company_id  (FK) │
│ razon_social     │   │   │ tipo_documento   │  │    │ nombre           │
│ nombre_comercial │   │   │ numero_documento │  │    │ cargo            │
│ codigo_ubigeo    │   │   │ denominacion     │  │    │ dni              │
│ direccion        │   │   │ direccion        │  │    │ codigo           │
│ departamento     │   │   │ departamento  🆕 │  │    │ estado           │
│ provincia        │   │   │ provincia     🆕 │  │    └──────────────────┘
│ distrito         │   │   │ distrito      🆕 │  │
│ usuario_sol      │   │   │ codigo_pais   🆕 │  │    ┌──────────────────┐
│ clave_sol        │   │   │ email            │  │    │    products      │
│ certificado_*    │   │   │ telefono         │  │    ├──────────────────┤
│ email_contacto   │   │   └──────────────────┘  │    │ id          (PK) │
│ telefono         │   │                          │    │ company_id  (FK) │
│ regimen          │   │                          │    │ codigo_producto  │
│ emision_en_envio │   │                          │    │ descripcion      │
│ estado           │   │                          │    │ unidad_medida    │
└──────────────────┘   │                          │    │ valor_unitario   │
                       │                          │    │ precio_unitario  │
┌──────────────────┐   │   ┌──────────────────┐   │    │ igv              │
│    facturas      │   │   │     boletas      │   │    │ estado           │
├──────────────────┤   │   ├──────────────────┤   │    └──────────────────┘
│ id          (PK) │   │   │ id          (PK) │   │
│ company_id  (FK) │───┘   │ company_id  (FK) │───┤
│ client_id   (FK) │───────│ client_id   (FK) │───┘
│ serie            │       │ serie            │        ┌──────────────────┐
│ numero           │       │ numero           │        │      sedes       │
│ fecha_emision    │       │ fecha_emision    │        ├──────────────────┤
│ hora_emision  🆕 │       │ hora_emision  🆕 │        │ id          (PK) │
│ tipo_operacion🆕 │       │ tipo_operacion🆕 │        │ company_id  (FK) │
│ moneda           │       │ moneda           │        │ codigo           │
│ gravadas         │       │ gravadas         │        │ nombre           │
│ exoneradas    🆕 │       │ exoneradas    🆕 │        │ direccion        │
│ inafectas     🆕 │       │ inafectas     🆕 │        │ codigo_ubigeo    │
│ gratuitas     🆕 │       │ gratuitas     🆕 │        │ departamento     │
│ igv              │       │ igv              │        │ provincia        │
│ total_cargos  🆕 │       │ total_cargos  🆕 │        │ distrito         │
│ total_desc    🆕 │       │ total_desc    🆕 │        │ es_default       │
│ subtotal      🆕 │       │ subtotal      🆕 │        └────────┬─────────┘
│ total_con_imp 🆕 │       │ total_con_imp 🆕 │                 │
│ total            │       │ total            │        ┌────────▼─────────┐
│ estado           │       │ estado           │        │  series_config   │
│ leyenda          │       │ leyenda          │        ├──────────────────┤
│ xml_content   🆕 │       │ xml_content   🆕 │        │ id          (PK) │
│ xml_path      🆕 │       │ xml_path      🆕 │        │ company_id  (FK) │
│ detalle_json     │       │ detalle_json     │        │ sede_id     (FK) │
└────────┬─────────┘       └──────────────────┘        │ tipo_documento   │
         │                                             │ serie            │
         │ referenciada por                            │ es_default       │
         ▼                                             │ activa           │
┌──────────────────┐       ┌──────────────────┐        └──────────────────┘
│  notas_credito   │       │  notas_debito    │
├──────────────────┤       ├──────────────────┤        ┌──────────────────┐
│ id          (PK) │       │ id          (PK) │        │ cuotas_credito🆕 │
│ company_id  (FK) │       │ company_id  (FK) │        ├──────────────────┤
│ factura_ref (FK) │       │ factura_ref (FK) │        │ id          (PK) │
│ motivo           │       │ motivo           │        │ factura_id  (FK) │
│ monto_descuento  │       │ monto_incremento │        │ numero_cuota     │
│ tipo_documento   │       │ descripcion      │        │ monto            │
│ descripcion      │       │ estado           │        │ fecha_venc       │
│ estado           │       │ detalle_json     │        │ moneda           │
│ detalle_json     │       └──────────────────┘        └──────────────────┘
└──────────────────┘
                                                       ┌──────────────────────┐
                                                       │ allowance_charges 🆕 │
┌──────────────────┐       ┌──────────────────┐        ├──────────────────────┤
│   tipo_cambio    │       │  user_preference │        │ id            (PK)   │
├──────────────────┤       ├──────────────────┤        │ factura_id    (FK)   │
│ id          (PK) │       │ id          (PK) │        │ tipo (CARGO/DESC)    │
│ fecha            │       │ company_id       │        │ codigo_cat53         │
│ moneda_origen    │       │ clave            │        │ descripcion          │
│ moneda_destino   │       │ valor            │        │ factor               │
│ tasa_compra      │       └──────────────────┘        │ base_imponible       │
│ tasa_venta       │                                   │ monto                │
│ fuente           │       ┌──────────────────┐        └──────────────────────┘
└──────────────────┘       │  correlativos    │
                           ├──────────────────┤        ┌──────────────────────┐
┌──────────────────┐       │ id          (PK) │        │ libros_electronicos  │
│ unidades_medida  │       │ company_id       │        ├──────────────────────┤
├──────────────────┤       │ serie            │        │ id            (PK)   │
│ id          (PK) │       │ ultimo_numero    │        │ company_id           │
│ codigo           │       └──────────────────┘        │ tipo                 │
│ descripcion      │                                   │ periodo              │
└──────────────────┘       ┌──────────────────┐        │ formato              │
                           │     ubigeos      │        │ estado               │
                           ├──────────────────┤        │ total_registros      │
                           │ id          (PK) │        └──────────────────────┘
                           │ codigo           │
                           │ departamento     │
                           │ provincia        │
                           │ distrito         │
                           └──────────────────┘
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
│ 📄 Doc   │   │ 🏢 Emisor│   │ 👤 Client│   │ 📦 Items │   │ 🚀 Emitir   │
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
| **Template** | Thymeleaf | — | Multi-Page App (14 vistas + fragments) |
| **Frontend** | Vanilla JS (modular) | ES5+ | 10 módulos JS, 1,606 líneas CSS |
| **Mapas** | Leaflet.js | — | Mapa interactivo en Config (sede location) |
| **Geocoding** | Nominatim (OSM) | — | Búsqueda de direcciones + reverse geocode |
| **BD (dev)** | H2 Database | — | Base de datos en memoria |
| **BD (prod)** | PostgreSQL | — | Base de datos producción (driver incluido) |
| **Validación** | Hibernate Validator + Jakarta | 3.0.2 | Validación de requests |
| **Docs** | Swagger / OpenAPI 3.0.3 | — | Contrato de API (`ms-bs-etia.yaml`) |
| **Build** | Maven Wrapper | — | Build reproducible sin instalar Maven |

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

## 🗄️ Configuración

### `application.yaml` (Desarrollo)

```yaml
spring:
  application:
    name: ms-bs-etia
  datasource:
    url: jdbc:h2:mem:localdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    defer-datasource-initialization: true
  sql:
    init:
      mode: always                 # Ejecuta data.sql al arrancar
  h2:
    console:
      enabled: true
      path: /h2-console
  thymeleaf:
    cache: false
  web:
    resources:
      cache:
        cachecontrol:
          no-cache: true
          no-store: true

# 🆕 Configuración SUNAT (type-safe)
sunat:
  certificado:
    base-path: ${SUNAT_CERT_PATH:./certs}    # Ruta base para certificados .pfx/.p12
  sol:
    default-user: ${SUNAT_SOL_USER:MODDATOS}
    default-password: ${SUNAT_SOL_PASSWORD:MODDATOS}
```

### Producción (futuro `application-prod.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/etia_db
    driver-class-name: org.postgresql.Driver
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
  sql:
    init:
      mode: never
  h2:
    console:
      enabled: false
```

---

## 🗺️ Roadmap

```
                                                        Estado actual
                                                             │
                                                             ▼
────●──────────────────────────────●──────────────────────●──────────●──────────●────►
    │                              │                      │          │          │
    ▼                              ▼                      ▼          ▼          ▼

 ✅ FASE 1 (Completada)         🟡 FASE 2 (~90%)       🔲 Fase 3   🔲 Fase 4   🔲 Fase 5
   CRUD + OpenAPI + MapStruct     XML UBL 2.1 ✅         Firma +     Resumen     Production
   Error Handling + MPA           12 Catálogos ✅        Envío       Diario +    Ready
   20 dominios homologados        Modelo datos ✅        SUNAT       Com. Baja
   60+ endpoints + 14 vistas      Wizard 5-step ✅       CDR         Boletas
   Notificaciones · Validaciones  Edit mode ✅
   Auto-correlativo · Series      Borrador/Emit ✅
   Arquitectura hexagonal         184 tests ✅
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

### 🟡 Fase 2 — XML UBL 2.1 SUNAT (~90% completada)

Generación del XML electrónico conforme al estándar UBL 2.1 que exige SUNAT:

| Feature | Descripción | Estado |
|---------|-------------|--------|
| **Estructura XML UBL 2.1** | Generar `Invoice` conforme al estándar OASIS UBL 2.1 adaptado por SUNAT | ✅ Completado |
| **Catálogos SUNAT** | 12 catálogos: 01, 02, 03, 04, 05, 06, 07, 13, 16, 51, 52, 53 | ✅ Completado |
| **Builder pattern XML** | `UblInvoiceXmlBuilder` ensambla XML desde entidades (36 tests) | ✅ Completado |
| **Namespace management** | `cac:`, `cbc:`, `ext:`, `sac:` correctamente manejados | ✅ Completado |
| **Datos del emisor en XML** | `AccountingSupplierParty` con RUC, razón social, dirección, ubigeo | ✅ Completado |
| **Datos del receptor en XML** | `AccountingCustomerParty` con tipo/número doc, denominación, dirección | ✅ Completado |
| **Líneas de detalle** | `InvoiceLine` con cantidad, UM, precios, IGV, subtotal, afectación | ✅ Completado |
| **Totales y tributos** | `TaxTotal`, `LegalMonetaryTotal` con gravadas, exoneradas, IGV, total | ✅ Completado |
| **Leyenda (monto en letras)** | `Note` element con monto textual (Cat. 52) | ✅ Completado |
| **Tipo de cambio en XML** | `PaymentExchangeRate` para docs en USD | ✅ Completado |
| **Forma de pago** | `PaymentTerms` — Contado / Crédito con cuotas | ✅ Completado |
| **Cargos/Descuentos** | `AllowanceCharge` con Cat. 53 (recargo consumo, descuento vol., etc.) | ✅ Completado |
| **Almacenamiento XML** | Campos `xml_content` (BD) + `xml_path` (disco) — inmutable | ✅ Completado |
| **Descarga XML** | `GET /factura/{id}/xml` — endpoint OpenAPI-first | ✅ Completado |
| **Modelo enriquecido** | +9 campos Factura/Boleta, +4 Client, +6 DetalleItem, +2 entidades | ✅ Completado |
| **Wizard 5 pasos** | Documento → Emisor → Cliente → Detalle → Emitir (+ edit mode) | ✅ Completado |
| **Emisión/Borrador** | Toggle `emisionEnEnvio` + Quick Emit + re-emisión RECHAZADA→ENVIADA | ✅ Completado |
| **SunatProperties** | Config type-safe: `sunat.certificado.base-path`, `sunat.sol.*` | ✅ Completado |
| **184 tests** | JUnit 5 + Mockito — todos verdes | ✅ Completado |
| **Vinculación Sede → XML** | `AddressTypeCode` del establecimiento emisor (0001=principal) | ❌ Pendiente |
| **Notas: doc referencia** | `BillingReference` → `InvoiceDocumentReference` para NC/ND | ❌ Pendiente |
| **Validación XML vs XSD** | Validar contra XSD antes de firmar | ❌ Pendiente |

### 🔲 Fase 3 — Firma Digital + Envío SUNAT

| Feature | Descripción | Prioridad |
|---------|-------------|-----------|
| **XMLDSig (firma digital)** | Firmar XML con certificado .p12/.pfx (RSA-SHA256, enveloped) | 🔴 Crítica |
| **Gestión de certificados** | Upload, almacenamiento y lectura del certificado digital por empresa | 🔴 Crítica |
| **Web Service SUNAT** | Envío SOAP a `https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService` | 🔴 Crítica |
| **Procesamiento CDR** | Parsear ZIP de respuesta → extraer CDR XML → estado ACEPTADA/RECHAZADA/OBSERVADA | 🔴 Crítica |
| **Actualización de estado** | Actualizar entity con hash CDR, código y descripción de respuesta SUNAT | 🟡 Alta |
| **Reintentos** | Cola de reintento para envíos fallidos con backoff exponencial | 🟡 Alta |
| **Modo beta/producción** | Switch entre endpoints SUNAT beta y producción | 🟡 Alta |

### 🔲 Fase 4 — Resumen Diario + Comunicación de Baja

| Feature | Descripción | Prioridad |
|---------|-------------|-----------|
| **Resumen Diario** | `SummaryDocuments` para boletas (envío diferido a SUNAT) | 🟡 Alta |
| **Comunicación de Baja** | `VoidedDocuments` para anular comprobantes ante SUNAT | 🟡 Alta |
| **Ticket de consulta** | Consultar estado de resúmenes/bajas con `getStatus` SOAP | 🟡 Alta |
| **Generación automática** | Job programado para generar resúmenes diarios de boletas | 🟢 Media |

### 🔲 Fase 5 — Production Ready

| Feature | Descripción | Prioridad |
|---------|-------------|-----------|
| **PostgreSQL + Flyway** | Migraciones versionadas de BD | 🟡 Alta |
| **JWT Security** | Spring Security + JWT para autenticación | 🟡 Alta |
| **Tests unitarios** | JUnit 5 + Mockito para servicios y mappers | 🟡 Alta |
| **Tests de integración** | @SpringBootTest con H2 | 🟡 Alta |
| **PDF generation** | Generar representación impresa del comprobante (Jasper o iText) | 🟡 Alta |
| **QR Code** | QR en PDF conforme a SUNAT (serie-numero\|IGV\|total\|fecha\|RUC) | 🟢 Media |
| **Docker** | Dockerfile + docker-compose (app + PostgreSQL) | 🟢 Media |
| **CI/CD** | GitHub Actions para build + test + deploy | 🟢 Media |

### 🔲 Fase 6 — Post-MVP

| Feature | Descripción | Prioridad |
|---------|-------------|-----------|
| **Email automático** | Envío de comprobante por email al cliente | 🟢 Media |
| **Libros Electrónicos PLE** | Registro de Ventas (14.1) y Compras (8.1) | 🟢 Media |
| **Dashboard** | Estadísticas: ventas por mes, docs emitidos, IGV, etc. | 🟢 Baja |
| **Multi-tenant** | Soporte para múltiples empresas con aislamiento | 🟢 Baja |

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

## 📊 Estadísticas del proyecto (FASE 2 — En Progreso)

| Métrica | Valor |
|---------|-------|
| Archivos Java (src) | **~140+** |
| Dominios de negocio | **20** (todos homologados) |
| Endpoints REST (OpenAPI) | **60+** (incluye notificaciones + XML UBL) |
| View Controllers (MVC) | **14** |
| Entidades JPA | **17** (+CuotaCreditoEntity, +AllowanceChargeEntity) |
| Repositorios | **17** |
| MapStruct Mappers | **14** |
| Servicios (interface + impl) | **30+** |
| XML Builder | **1** (UblInvoiceXmlBuilder — 36 tests) |
| Templates Thymeleaf | **19** (14 vistas + 5 fragments) |
| Archivos JS | **10** |
| CSS Design System | **1,606 líneas** |
| Contrato OpenAPI YAML | **~3,500+ líneas** |
| Tests unitarios (JUnit 5) | **184** (0 fallos) |
| Catálogos SUNAT | **12** de 36 (Cat. 01-53) |
| data.sql (seed) | **300+ líneas** (80+ registros, 23 facturas) |
| Sample docs | **4** (XMLs + PDFs reales) |

---

## 🤝 Contribuir

1. Fork del repositorio
2. Crear branch feature: `git checkout -b feature/mi-feature`
3. Commit: `git commit -m 'feat: mi nueva funcionalidad'`
4. Push: `git push origin feature/mi-feature`
5. Crear Pull Request

---

## 📝 Licencia

Este proyecto es propiedad de **ETLIONS**. Todos los derechos reservados.

---

<p align="center">
  <sub>Hecho con ❤️ por el equipo ETLIONS — Lima, Perú 🇵🇪</sub><br/>
  <sub>Última actualización: Abril 2026</sub>
</p>

