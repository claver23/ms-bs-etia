<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenAPI-3.0.3-85EA2D?style=for-the-badge&logo=openapiinitiative&logoColor=white" />
  <img src="https://img.shields.io/badge/MapStruct-1.6.3-FF6B6B?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Lombok-1.18.30-BC4521?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Thymeleaf-Demo%20UI-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" />
  <img src="https://img.shields.io/badge/H2%20|%20PostgreSQL-Ready-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
</p>

<h1 align="center">🧾 ETIA — Facturación Electrónica Perú</h1>

<p align="center">
  <b>Microservicio backend para emisión de comprobantes electrónicos conforme a SUNAT</b><br/>
  <sub>Facturas · Boletas · Notas de Crédito · Notas de Débito · Demo Interactivo · Error Handling</sub>
</p>

<p align="center">
  <a href="#-arquitectura">Arquitectura</a> •
  <a href="#-dominios">Dominios</a> •
  <a href="#-api-endpoints">API</a> •
  <a href="#-demo-interactivo">Demo</a> •
  <a href="#-error-handling">Errores</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 📐 Arquitectura

El proyecto sigue una **arquitectura hexagonal por dominio**, donde cada módulo de negocio es autónomo con sus propias capas. El contrato OpenAPI genera las interfaces y DTOs, y la aplicación los implementa en capas limpias.

```
┌─────────────────────────────────────────────────────────────────────┐
│                    🖥️ Thymeleaf Demo UI  (/)                        │
│             Dark-mode Wizard · 4 pasos · Emitir Factura             │
│             Pre-populated con test-data del YAML                    │
└────────────────────────────┬────────────────────────────────────────┘
                             │  fetch('/api/v1/factura')
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        🌐  API Gateway                              │
│                     /api/v1/*  (REST JSON)                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│              🛡️ GlobalExceptionHandler (@RestControllerAdvice)      │
│   ResourceNotFoundException → 404 · BusinessException → 422         │
│   ValidationException → 400 · MethodArgumentNotValid → 400          │
│   Exception (fallback) → 500  ·  Wrapper: {isSuccess, status, ...}  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   📜 OpenAPI Contract Layer                         │
│              ms-bs-etia.yaml  →  codegen (interfaces + DTOs)        │
└────────────────────────────┬────────────────────────────────────────┘
                             │  implements
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│               🎮 Controller Layer  (openapi/)                       │
│      @Controller + @RequestMapping("/api/v1")                       │
│      Recibe HTTP → delega al Service → ResponseEntity.ok()          │
└────────────────────────────┬────────────────────────────────────────┘
                             │  calls
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                🧠 Service Layer  (service/ + service/impl/)         │
│      Interface por operación  →  @Service implementación            │
│      Orquesta: Mapper + Repository + Business Logic                 │
└──────────────┬─────────────────────────────────┬────────────────────┘
               │  uses                           │  uses
               ▼                                 ▼
┌──────────────────────────┐   ┌──────────────────────────────────────┐
│  🔄 Mapper (mapper/)     │   │  💾 Persistence (persistence/)       │
│  MapStruct @Mapper       │   │  @Entity + JpaRepository             │
│  DTO ↔ Entity            │   │  H2 (dev) / PostgreSQL (prod)        │
│  Enum ↔ String           │   │                                      │
│  List<DetalleItem> ↔ JSON│   │                                      │
└──────────────────────────┘   └──────────────────────────────────────┘
                                         ▲
                                         │  seeded by
                               ┌─────────┴─────────┐
                               │  📦 DataLoader     │
                               │  CommandLineRunner │
                               │  test-data (YAML)  │
                               └────────────────────┘
```

### 🔁 Flujo de una petición (ejemplo: Registrar Factura)

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
  docFacturaServiceImpl          ← Service orquesta la lógica
        │
        ├──► FacturaMapper.toEntity(request)     ← DTO → Entity (MapStruct)
        │         └── moneda enum → String
        │         └── detalle List → JSON string
        │
        ├──► entity.setEstado("BORRADOR")        ← Business rules
        ├──► entity.setFechaCreacion(now())
        │
        ├──► facturaRepository.save(entity)      ← JPA persist
        │
        ├──► FacturaMapper.toData(saved)         ← Entity → DTO
        │
        └──► AnularFactura200Response            ← Build response wrapper
                  .isSuccess(true)
                  .status("OK")
                  .data(facturaData)
                  .timestamp(now)
        │
        ▼
  ResponseEntity.ok(response)   ← HTTP 200 + JSON body
```

### 🖥️ Flujo de la Demo UI (Thymeleaf)

```
GET /                          ← DemoController (Spring MVC)
    │
    ├──► CompanyRepository.findAll()     ← Obtiene empresa demo
    ├──► ClientRepository.findByCompanyId()  ← Obtiene cliente demo
    ├──► TestDataProperties.getProducts()    ← Productos de prueba (YAML)
    │
    └──► Model → "index" template
              │
              ▼
         🖥️ Wizard Dark-mode UI
              │
              ├── Paso 1: 🏢 Datos del Emisor (read-only, precargados)
              ├── Paso 2: 👤 Datos del Cliente (editable, precargados)
              ├── Paso 3: 📦 Detalle de ítems (editable, auto-cálculo IGV)
              └── Paso 4: ✅ Revisión y Emisión
                          │
                          └──► fetch('POST /api/v1/factura') → JSON response modal
```

---

## 📦 Dominios

Cada dominio vive en su propio paquete con **4 sub-paquetes independientes**:

```
com.etlions.ms_bs_etia
│
├── 🏢 company/                    Empresas emisoras
│   ├── openapi/                   CompanyOpenApi.java
│   ├── service/                   companyService.java
│   │   └── impl/                  companyServiceImpl.java
│   ├── mapper/                    CompanyMapper.java
│   └── persistence/               CompanyEntity.java + CompanyRepository.java
│
├── 👤 client/                     Clientes (adquirientes)
│   ├── openapi/                   clientOpenApi.java
│   ├── service/                   clientService.java
│   │   └── impl/                  clienteServiceImpl.java
│   ├── mapper/                    ClientMapper.java
│   └── persistence/               ClientEntity.java + ClientRepository.java
│
├── 🧾 doc_factura/                Facturas electrónicas
│   ├── openapi/                   DocFacturaOpenApi.java
│   ├── service/                   docFacturaService.java
│   │   └── impl/                  docFacturaServiceImpl.java
│   ├── mapper/                    FacturaMapper.java
│   └── persistence/               FacturaEntity.java + FacturaRepository.java
│
├── 🎫 doc_boleta/                 Boletas de venta
│   ├── openapi/                   DocBoletaOpenApi.java
│   ├── service/                   docBoletaService.java
│   │   └── impl/                  docBoletaServiceImpl.java
│   ├── mapper/                    BoletaMapper.java
│   └── persistence/               BoletaEntity.java + BoletaRepository.java
│
├── 📉 doc_notacred/               Notas de crédito
│   ├── openapi/                   DocNotacredOpenApi.java
│   ├── service/                   docNotacredService.java
│   │   └── impl/                  docNotacredServiceImpl.java
│   ├── mapper/                    NotaCreditoMapper.java
│   └── persistence/               NotaCreditoEntity.java + NotaCreditoRepository.java
│
├── 📈 doc_notadeb/                Notas de débito
│   ├── openapi/                   DocNotadebOpenApi.java
│   ├── service/                   docNotadebService.java
│   │   └── impl/                  docNotadebServiceImpl.java
│   ├── mapper/                    NotaDebitoMapper.java
│   └── persistence/               NotaDebitoEntity.java + NotaDebitoRepository.java
│
├── ⚙️ app_config/                 Configuración y utilidades
│   ├── GlobalExceptionHandler.java    ← @RestControllerAdvice
│   ├── DemoController.java            ← Thymeleaf MVC controller
│   ├── DataLoader.java                ← CommandLineRunner (seed H2)
│   ├── TestDataProperties.java        ← @ConfigurationProperties
│   └── exception/
│       ├── ResourceNotFoundException.java
│       ├── BusinessException.java
│       └── ValidationException.java
│
├── 🛠️ app_util/                   Utilidades compartidas
└── 🚀 MsBsEtiaApplication.java   Entry point
```

### 📊 Modelo de datos

```
┌──────────────────┐       ┌──────────────────┐
│    companies     │       │     clients      │
├──────────────────┤       ├──────────────────┤
│ id          (PK) │◄──┐   │ id          (PK) │
│ ruc         (UQ) │   │   │ company_id  (FK) │──┐
│ razon_social     │   │   │ tipo_documento   │  │
│ nombre_comercial │   │   │ numero_documento │  │
│ codigo_ubigeo    │   │   │ denominacion     │  │
│ direccion        │   │   │ direccion        │  │
│ urbanizacion     │   │   │ email            │  │
│ departamento     │   │   │ telefono         │  │
│ provincia        │   │   │ fecha_creacion   │  │
│ distrito         │   │   └──────────────────┘  │
│ usuario_sol      │   │                          │
│ clave_sol        │   │                          │
│ certificado_path │   │                          │
│ certificado_pwd  │   │                          │
│ email_contacto   │   │                          │
│ telefono         │   │                          │
│ regimen          │   │                          │
│ estado           │   │                          │
│ fecha_creacion   │   │                          │
│ fecha_actualizac │   │                          │
└──────────────────┘   │                          │
                       │                          │
┌──────────────────┐   │   ┌──────────────────┐   │
│    facturas      │   │   │     boletas      │   │
├──────────────────┤   │   ├──────────────────┤   │
│ id          (PK) │   │   │ id          (PK) │   │
│ company_id  (FK) │───┘   │ company_id  (FK) │───┤
│ client_id   (FK) │───────│ client_id   (FK) │───┘
│ serie            │       │ serie            │
│ numero           │       │ numero           │
│ fecha_emision    │       │ fecha_emision    │
│ moneda           │       │ moneda           │
│ gravadas         │       │ gravadas         │
│ igv              │       │ igv              │
│ total            │       │ total            │
│ estado           │       │ estado           │
│ hash_cdr         │       │ leyenda          │
│ cod_resp_sunat   │       │ detalle_json     │
│ desc_resp_sunat  │       │ fecha_creacion   │
│ leyenda          │       └──────────────────┘
│ detalle_json     │
│ fecha_creacion   │
└──────────────────┘
         │
         │ referenciada por
         ▼
┌──────────────────┐       ┌──────────────────┐
│  notas_credito   │       │  notas_debito    │
├──────────────────┤       ├──────────────────┤
│ id          (PK) │       │ id          (PK) │
│ company_id  (FK) │       │ company_id  (FK) │
│ factura_ref (FK) │       │ factura_ref (FK) │
│ motivo           │       │ motivo           │
│ monto_descuento  │       │ monto_incremento │
│ tipo_documento   │       │ descripcion      │
│ descripcion      │       │ estado           │
│ estado           │       │ detalle_json     │
│ detalle_json     │       │ fecha_creacion   │
│ fecha_creacion   │       └──────────────────┘
└──────────────────┘
```

---

## 🔌 API Endpoints

Todos los endpoints están bajo el prefijo **`/api/v1`** y son generados desde el contrato OpenAPI.

### 🏢 Company — Empresas emisoras

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/company` | Registrar nueva empresa emisora |
| `GET` | `/api/v1/company` | Listar empresas (paginado, filtro por RUC/estado) |
| `PUT` | `/api/v1/company` | Actualizar datos de empresa |
| `DELETE` | `/api/v1/company` | Eliminar empresa (soft/hard delete) |

### 👤 Client — Clientes (adquirientes)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/client` | Registrar cliente |
| `GET` | `/api/v1/client` | Listar clientes por empresa |
| `PUT` | `/api/v1/client` | Actualizar cliente |
| `DELETE` | `/api/v1/client` | Eliminar cliente |

### 🧾 Factura — Facturas electrónicas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/factura` | Emitir factura electrónica |
| `GET` | `/api/v1/factura` | Listar facturas (filtros: fecha, serie, estado) |
| `PUT` | `/api/v1/factura` | Anular factura |
| `DELETE` | `/api/v1/factura` | Eliminar factura (solo borradores) |

### 🎫 Boleta — Boletas de venta

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/boleta` | Emitir boleta de venta |
| `GET` | `/api/v1/boleta` | Listar boletas emitidas |
| `PUT` | `/api/v1/boleta` | Anular boleta |
| `DELETE` | `/api/v1/boleta` | Eliminar borrador de boleta |

### 📉 Nota de Crédito

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/nota_credito` | Emitir nota de crédito |
| `GET` | `/api/v1/nota_credito` | Listar notas de crédito |
| `PUT` | `/api/v1/nota_credito` | Actualizar borrador |
| `DELETE` | `/api/v1/nota_credito` | Eliminar borrador |

### 📈 Nota de Débito

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/nota_debito` | Emitir nota de débito |
| `GET` | `/api/v1/nota_debito` | Listar notas de débito |
| `PUT` | `/api/v1/nota_debito` | Actualizar borrador |
| `DELETE` | `/api/v1/nota_debito` | Eliminar borrador |

### 🖥️ Demo UI

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/` | Wizard interactivo Thymeleaf — Emitir Factura Demo |
| `GET` | `/h2-console` | Consola H2 (explorar base de datos) |

> **Total: 24 endpoints REST + 1 demo UI + consola H2**

### 📋 Formato de respuesta estándar

Todas las respuestas siguen un wrapper unificado:

```json
{
  "isSuccess": true,
  "status": "OK",
  "message": "Operación exitosa",
  "timestamp": "2026-04-04T10:30:00Z",
  "data": { ... }
}
```

Las **respuestas de error** (manejadas por `GlobalExceptionHandler`) siguen el mismo formato:

```json
{
  "isSuccess": false,
  "status": "NOT_FOUND",
  "message": "Empresa no encontrado con ID: abc-123",
  "timestamp": "2026-04-04T10:30:00Z"
}
```

```json
{
  "isSuccess": false,
  "status": "VALIDATION_ERROR",
  "message": "Error de validación en los campos enviados",
  "timestamp": "2026-04-04T10:30:00Z",
  "data": [
    "ruc: no puede estar vacío",
    "razonSocial: no puede estar vacío"
  ]
}
```

Las listas incluyen metadata de paginación:

```json
{
  "isSuccess": true,
  "status": "OK",
  "message": "Operación exitosa",
  "timestamp": "2026-04-04T10:30:00Z",
  "data": {
    "content": [ ... ],
    "pagination": {
      "currentPage": 0,
      "pageSize": 10,
      "totalElements": 50,
      "totalPages": 5
    }
  }
}
```

---

## 🖥️ Demo Interactivo

La aplicación incluye un **wizard interactivo** construido con **Thymeleaf** que permite emitir facturas de prueba directamente desde el navegador.

### Acceso

```
http://localhost:8080/
```

### Características

| Característica | Detalle |
|---------------|---------|
| **Tema** | 🌙 Dark mode (#0d1117 fondo, #00ff88 acentos verde neón) |
| **Wizard 4 pasos** | Emisor → Cliente → Detalle → Revisión & Emisión |
| **Datos precargados** | Empresa, cliente y productos de prueba desde `application.yaml` |
| **Cálculo automático** | IGV 18% se recalcula en tiempo real al editar cantidades/precios |
| **API real** | El botón "Emitir" hace `POST /api/v1/factura` contra la API real |
| **Response modal** | Muestra el JSON de respuesta completo (éxito o error) |
| **Responsive** | Adaptable a desktop, tablet y mobile |

### Flujo visual del Wizard

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────────┐
│  PASO 1 │───►│  PASO 2 │───►│  PASO 3 │───►│   PASO 4    │
│ 🏢 Emisor│    │ 👤 Client│    │ 📦 Items │    │ ✅ Emitir   │
│ (readonly)│    │(editable)│    │(editable)│    │  (review)   │
└─────────┘    └─────────┘    └─────────┘    └──────┬──────┘
                                                     │
                                              fetch POST
                                              /api/v1/factura
                                                     │
                                                     ▼
                                              ┌─────────────┐
                                              │ 📋 Response  │
                                              │   JSON modal │
                                              └─────────────┘
```

---

## 🛡️ Error Handling

El sistema incluye un **Global Exception Handler** (`@RestControllerAdvice`) que captura todas las excepciones y las transforma en respuestas JSON consistentes.

### Excepciones custom

| Excepción | HTTP Status | Uso |
|-----------|-------------|-----|
| `ResourceNotFoundException` | 404 Not Found | Entidad no existe en BD |
| `BusinessException` | 422 Unprocessable Entity | Regla de negocio violada |
| `ValidationException` | 400 Bad Request | Validaciones custom con lista de errores |
| `MethodArgumentNotValidException` | 400 Bad Request | Falla de `@Valid` automática (Jakarta) |
| `Exception` (fallback) | 500 Internal Server Error | Cualquier error no esperado |

### Diagrama de manejo de errores

```
    Request entrante
          │
          ▼
   ┌──────────────┐      ┌─────────────────────────────┐
   │  Controller   │─────►│ GlobalExceptionHandler       │
   │  (openapi/)   │ throw│ @RestControllerAdvice        │
   └──────────────┘      │                               │
                          │  ResourceNotFound → 404       │
                          │  Business        → 422        │
                          │  Validation      → 400        │
                          │  @Valid fail     → 400        │
                          │  Exception       → 500        │
                          │                               │
                          │  → { isSuccess: false,        │
                          │       status: "...",          │
                          │       message: "...",         │
                          │       timestamp: "...",       │
                          │       data: [...] }           │
                          └─────────────────────────────┘
```

---

## 📦 Test Data & DataLoader

La aplicación carga **datos de prueba automáticamente** al iniciar usando `CommandLineRunner`, leyendo la configuración del nodo `test-data` en `application.yaml`.

### Datos precargados

| Entidad | Datos de prueba |
|---------|----------------|
| **Empresa** | ETLIONS S.A.C. · RUC 20601234567 · San Isidro, Lima · Ubigeo 150131 |
| **Cliente** | INVERSIONES TECH PERU S.A.C. · RUC 20505050501 · Miraflores |
| **Producto 1** | Consultoría en TI · S/ 2,500.00 + IGV |
| **Producto 2** | Licencia Software ERP × 2 · S/ 3,600.00 + IGV |
| **Producto 3** | Soporte Técnico Premium × 6 meses · S/ 2,100.00 + IGV |
| **Ubigeos** | Lima, San Isidro, Miraflores, San Borja, Surco, Arequipa, Trujillo |

### Estructura en application.yaml

```yaml
test-data:                         # ← Nodo al mismo nivel que 'spring'
  company:
    ruc: "20601234567"
    razon-social: "ETLIONS S.A.C."
    # ... más campos
  client:
    tipo-documento: "6"            # 6 = RUC
    numero-documento: "20505050501"
    # ... más campos
  products:
    - descripcion: "Servicio de Consultoría en TI"
      cantidad: 1
      valor-unitario: 2500.00
      # ... más campos
  ubigeos:
    - codigo: "150131"
      departamento: "LIMA"
      # ... más ubigeos
```

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
| **Template** | Thymeleaf | — | Demo UI interactivo (wizard facturación) |
| **BD (dev)** | H2 Database | — | Base de datos en memoria para desarrollo |
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

### 3. Abrir el Demo Interactivo

```
🖥️ Demo UI:     http://localhost:8080/
📊 H2 Console:  http://localhost:8080/h2-console
🔌 API REST:    http://localhost:8080/api/v1/
```

Al iniciar, verás en consola:

```
🏗️ Cargando datos de prueba...
✅ Empresa demo creada: ETLIONS S.A.C. (ID: xxx-xxx)
✅ Cliente demo creado: INVERSIONES TECH PERU S.A.C. (ID: yyy-yyy)
🚀 Datos de prueba cargados. Demo disponible en http://localhost:8080
```

### 4. Probar la API con curl

```bash
# Listar empresas (ya tendrá la demo precargada)
curl http://localhost:8080/api/v1/company?page=0&size=10

# Emitir una factura
curl -X POST http://localhost:8080/api/v1/factura \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": "<ID_EMPRESA>",
    "clientId": "<ID_CLIENTE>",
    "serie": "F001",
    "numero": 1,
    "moneda": "PEN",
    "gravadas": 8200.00,
    "igv": 1476.00,
    "total": 9676.00,
    "leyenda": "SON NUEVE MIL SEISCIENTOS SETENTA Y SEIS CON 00/100 SOLES",
    "detalle": [
      {
        "descripcion": "Consultoría TI",
        "cantidad": 1,
        "valorUnitario": 2500.00,
        "precioUnitario": 2950.00,
        "igv": 450.00,
        "subtotal": 2500.00
      }
    ]
  }'
```

### 5. Explorar la base de datos

Accede a la consola H2 en: **http://localhost:8080/h2-console**

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
  h2:
    console:
      enabled: true
      path: /h2-console
  thymeleaf:
    cache: false

test-data:
  company: { ... }     # Datos de empresa demo
  client: { ... }      # Datos de cliente demo
  products: [ ... ]    # Productos de prueba
  ubigeos: [ ... ]     # Tabla de ubigeos
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
────●──────────●──────────●──────────●──────────●──────────●────►
    │          │          │          │          │          │
    ▼          ▼          ▼          ▼          ▼          ▼

 ✅ Fase 0   ✅ Fase 1   🔲 Fase 2   🔲 Fase 3   🔲 Fase 4   🔲 Fase 5
  CRUD +      Error       XML UBL     Firma +     Resumen     Production
  OpenAPI     Handling    2.1 SUNAT   Envío       Diario +    Ready
  Mappers     Demo UI     Catálogos   SUNAT       Com. Baja
  JPA         Test Data   JAXB        CDR         Boletas
              Thymeleaf
```

| Fase | Estado | Descripción | Prioridad |
|------|--------|-------------|-----------|
| **0. Base** | ✅ Completado | CRUD completo, OpenAPI codegen, MapStruct, JPA, 24 endpoints | — |
| **1. Error Handling + Demo** | ✅ Completado | `@RestControllerAdvice`, excepciones custom, Thymeleaf wizard, test data, DataLoader | — |
| **2. XML UBL 2.1** | 🔲 Pendiente | Generación XML SUNAT, catálogos, enriquecimiento de entidades | 🔴 Alta |
| **3. Firma + Envío** | 🔲 Pendiente | XMLDSig, certificado .p12, Web Service SUNAT, CDR | 🔴 Alta |
| **4. Resumen + Baja** | 🔲 Pendiente | Resumen diario (boletas), comunicación de baja, tickets | 🟡 Media |
| **5. Producción** | 🔲 Pendiente | PostgreSQL + Flyway, JWT Security, tests, PDF + QR | 🟡 Media |
| **6. Post-MVP** | 🔲 Futuro | Email automático, libros electrónicos PLE, dashboard | 🟢 Baja |

### ✅ Detalle — Fase 1 (Completada)

| Componente | Archivo | Descripción |
|-----------|---------|-------------|
| **GlobalExceptionHandler** | `app_config/GlobalExceptionHandler.java` | Manejo centralizado con 5 handlers |
| **ResourceNotFoundException** | `app_config/exception/ResourceNotFoundException.java` | 404 — recurso no encontrado |
| **BusinessException** | `app_config/exception/BusinessException.java` | 422 — regla de negocio |
| **ValidationException** | `app_config/exception/ValidationException.java` | 400 — validación con lista de errores |
| **TestDataProperties** | `app_config/TestDataProperties.java` | `@ConfigurationProperties` para test-data |
| **DataLoader** | `app_config/DataLoader.java` | `CommandLineRunner` que semilla H2 |
| **DemoController** | `app_config/DemoController.java` | `@Controller` Spring MVC para Thymeleaf |
| **Demo Template** | `templates/index.html` | Wizard dark-mode 4 pasos |
| **Test Data** | `application.yaml` → `test-data:` | Empresa + cliente + productos + ubigeos |

---

## 📄 Contrato OpenAPI

El contrato de la API está en:

```
src/main/resources/swagger/ms-bs-etia.yaml
```

Se genera automáticamente durante `mvn compile` usando **OpenAPI Generator 7.9.0** con los siguientes paquetes:

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

| Métrica | Valor |
|---------|-------|
| Archivos Java (src) | 42+ |
| Dominios de negocio | 6 |
| Endpoints REST | 24 |
| Entidades JPA | 6 |
| Repositorios | 6 |
| MapStruct Mappers | 6 |
| Servicios (interface + impl) | 12 |
| Controllers (API) | 6 |
| Controllers (MVC) | 1 |
| Excepciones custom | 3 |
| Template Thymeleaf | 1 |
| Datos de prueba (YAML) | Empresa + Cliente + 3 Productos + 7 Ubigeos |
| Líneas de contrato YAML | ~1,769 |

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
  <sub>Hecho con ❤️ por el equipo ETLIONS — Lima, Perú 🇵🇪</sub>
</p>

