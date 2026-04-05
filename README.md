<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenAPI-3.0.3-85EA2D?style=for-the-badge&logo=openapiinitiative&logoColor=white" />
  <img src="https://img.shields.io/badge/MapStruct-1.6.3-FF6B6B?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Lombok-1.18.30-BC4521?style=for-the-badge" />
  <img src="https://img.shields.io/badge/H2%20|%20PostgreSQL-Ready-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
</p>

<h1 align="center">🧾 ETIA — Facturación Electrónica Perú</h1>

<p align="center">
  <b>Microservicio backend para emisión de comprobantes electrónicos conforme a SUNAT</b><br/>
  <sub>Facturas · Boletas · Notas de Crédito · Notas de Débito · Libros Electrónicos</sub>
</p>

<p align="center">
  <a href="#-arquitectura">Arquitectura</a> •
  <a href="#-dominios">Dominios</a> •
  <a href="#-api-endpoints">API</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 📐 Arquitectura

El proyecto sigue una **arquitectura hexagonal por dominio**, donde cada módulo de negocio es autónomo con sus propias capas. El contrato OpenAPI genera las interfaces y DTOs, y la aplicación los implementa en capas limpias.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        🌐  API Gateway                              │
│                     /api/v1/*  (REST JSON)                          │
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
```

### 🔁 Flujo de una petición (ejemplo: Registrar Factura)

```
POST /api/v1/factura
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
├── ⚙️ config/                     Configuración de la aplicación
├── 🛠️ util/                       Utilidades compartidas
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

> **Total: 24 endpoints** distribuidos en 6 dominios

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

## 🛠️ Tech Stack

| Categoría | Tecnología | Versión | Propósito |
|-----------|-----------|---------|-----------|
| **Framework** | Spring Boot | 4.0.5 | Framework principal |
| **Lenguaje** | Java | 21 (LTS) | Runtime |
| **API** | OpenAPI Generator | 7.9.0 | Generación de interfaces y DTOs desde contrato |
| **ORM** | Spring Data JPA + Hibernate | — | Persistencia |
| **Mapping** | MapStruct | 1.6.3 | Conversión DTO ↔ Entity (compile-time) |
| **Boilerplate** | Lombok | 1.18.30 | `@Getter @Setter @Builder @RequiredArgsConstructor` |
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

### 3. Verificar que funciona

```bash
# La aplicación arranca en http://localhost:8080

# Registrar una empresa
curl -X POST http://localhost:8080/api/v1/company \
  -H "Content-Type: application/json" \
  -d '{
    "ruc": "20601234567",
    "razonSocial": "ETLIONS S.A.C.",
    "nombreComercial": "ETLIONS",
    "direccion": "Av. Javier Prado 1234",
    "departamento": "LIMA",
    "provincia": "LIMA",
    "distrito": "SAN ISIDRO",
    "codigoUbigeo": "150131",
    "emailContacto": "admin@etlions.com",
    "telefono": "51999888777",
    "regimen": "general"
  }'
```

### 4. Explorar la base de datos

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
Estado actual                                         Producción
    │                                                      │
    ▼                                                      ▼
────●──────────●──────────●──────────●──────────●──────────●────►
    │          │          │          │          │          │
    ▼          ▼          ▼          ▼          ▼          ▼

 ✅ Fase 0   🔲 Fase 1   🔲 Fase 2   🔲 Fase 3   🔲 Fase 4   🔲 Fase 5
  CRUD +      Error       XML UBL     Firma +     Resumen     Production
  OpenAPI     Handling    2.1 SUNAT   Envío       Diario +    Ready
  Mappers     Validación  Catálogos   SUNAT       Com. Baja
  JPA         Negocio     JAXB        CDR         Boletas
```

| Fase | Estado | Descripción | Prioridad |
|------|--------|-------------|-----------|
| **0. Base** | ✅ Completado | CRUD completo, OpenAPI codegen, MapStruct, JPA, 24 endpoints | — |
| **1. Errores + Validación** | 🔲 Pendiente | `@RestControllerAdvice`, excepciones custom, validaciones SUNAT | 🔴 Alta |
| **2. XML UBL 2.1** | 🔲 Pendiente | Generación XML SUNAT, catálogos, enriquecimiento de entidades | 🔴 Alta |
| **3. Firma + Envío** | 🔲 Pendiente | XMLDSig, certificado .p12, Web Service SUNAT, CDR | 🔴 Alta |
| **4. Resumen + Baja** | 🔲 Pendiente | Resumen diario (boletas), comunicación de baja, tickets | 🟡 Media |
| **5. Producción** | 🔲 Pendiente | PostgreSQL + Flyway, JWT Security, tests, PDF + QR | 🟡 Media |
| **6. Post-MVP** | 🔲 Futuro | Email automático, libros electrónicos PLE, dashboard | 🟢 Baja |

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
| Archivos Java (src) | 37 |
| Dominios de negocio | 6 |
| Endpoints REST | 24 |
| Entidades JPA | 6 |
| Repositorios | 6 |
| MapStruct Mappers | 6 |
| Servicios (interface + impl) | 12 |
| Controllers | 6 |
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

