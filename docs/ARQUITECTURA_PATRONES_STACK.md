# 🏗️ ARQUITECTURA, PATRONES Y STACK — ETIA Suite

> **Última actualización:** 04/09/2026  
> **Propósito:** Referencia obligatoria antes de implementar cualquier tarea nueva.

---

## 1. Arquitectura General

**Tipo:** Arquitectura Hexagonal por Dominio (Ports & Adapters) dentro de un monolito modular Spring Boot.

```
┌─────────────────────────────────────────────────────┐
│  FRONTEND (Thymeleaf MPA + Vanilla JS modular)      │
│  14+ vistas · 10+ módulos JS · 1 CSS Design System  │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/REST (JSON)
                       ▼
┌─────────────────────────────────────────────────────┐
│  API LAYER (OpenAPI Contract-First)                  │
│  Contrato YAML → codegen → interfaces + DTOs         │
│  Base path: /api/v1/                                 │
└──────────────────────┬──────────────────────────────┘
                       │ implements
                       ▼
┌─────────────────────────────────────────────────────┐
│  CONTROLLER LAYER (openapi/)                         │
│  @Controller implementa interfaz generada            │
│  Recibe HTTP → delega Service → ResponseEntity       │
└──────────────────────┬──────────────────────────────┘
                       │ calls
                       ▼
┌─────────────────────────────────────────────────────┐
│  SERVICE LAYER (service/ + service/impl/)            │
│  Interface + @Service implementación                 │
│  Orquesta: Mapper + Repository + Business Logic      │
└──────────┬──────────────────────┬───────────────────┘
           │                      │
           ▼                      ▼
┌───────────────────┐  ┌─────────────────────────────┐
│  MAPPER (mapper/)  │  │  PERSISTENCE (persistence/) │
│  MapStruct @Mapper │  │  @Entity + JpaRepository    │
│  DTO ↔ Entity      │  │  H2 (dev) / PostgreSQL (prod)│
└───────────────────┘  └─────────────────────────────┘
```

---

## 2. Patrón por dominio (estructura de paquetes)

**TODOS los dominios siguen exactamente este patrón:**

```
com.etlions.ms_bs_etia.{dominio}/
├── openapi/
│   └── {Dominio}OpenApi.java          ← implements interfaz generada por OpenAPI
├── service/
│   ├── {Dominio}Service.java          ← Interface
│   └── impl/
│       └── {Dominio}ServiceImpl.java  ← @Service implementación
├── mapper/
│   └── {Dominio}Mapper.java           ← @Mapper MapStruct
├── persistence/
│   ├── {Dominio}Entity.java           ← @Entity JPA
│   └── {Dominio}Repository.java       ← extends JpaRepository
└── [exception/]                       ← Opcional: excepciones específicas del dominio
    └── {Dominio}Exception.java
```

**Reglas inquebrantables:**
1. El **contrato OpenAPI** define PRIMERO la API (Contract-First)
2. `mvn compile` genera interfaces y DTOs en `target/generated-sources/openapi/`
3. El controller `implements` la interfaz generada (NO crea endpoints manuales)
4. **Nunca** editar archivos en `target/`
5. Los nombres de operaciones son en **español** y **camelCase**: `crearFactura`, `listarClientes`

---

## 3. Stack Tecnológico

### Backend

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Java | 21 (LTS) | Runtime |
| Spring Boot | 4.0.5 | Framework |
| Spring Data JPA + Hibernate | — | ORM/Persistencia |
| Spring Security | — | Autenticación/Autorización (FASE B) |
| OpenAPI Generator | 7.9.0 | Contract-First codegen |
| MapStruct | 1.6.3 | DTO ↔ Entity mapping (compile-time) |
| Lombok | 1.18.30 | Reducción boilerplate |
| H2 Database | — | BD en memoria (desarrollo) |
| PostgreSQL | — | BD producción (futuro) |
| JJWT | 0.12.6 | JWT tokens (FASE B) |
| Apache POI | 5.2.5 | Exportación Excel (FASE E/J) |
| Google Maps Services | 2.2.0 | Google Places API (FASE D) |

### Frontend

| Tecnología | Propósito |
|-----------|-----------|
| Thymeleaf + Layout Dialect | Server-side rendering (MPA) |
| Vanilla JS (ES5+) | Lógica de UI modular |
| Bootstrap 5 (implícito via CSS) | Grid y componentes base |
| Leaflet.js | Mapas interactivos |
| Nominatim (OSM) | Geocoding |
| html2canvas | Screenshots de errores |
| Chart.js (futuro) | Gráficos en dashboards |

### Herramientas de build

| Herramienta | Propósito |
|-------------|-----------|
| Maven Wrapper | Build reproducible |
| OpenAPI Generator Maven Plugin | Genera interfaces + DTOs |
| MapStruct + Lombok Annotation Processors | Procesamiento compile-time |

---

## 4. Convenciones de código

### Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Paquetes | snake_case | `doc_factura`, `punto_venta` |
| Clases | PascalCase | `FacturaEntity`, `CompanyMapper` |
| Métodos Service | español camelCase | `crearFactura()`, `listarClientes()` |
| Endpoints | español snake_case | `/api/v1/factura`, `/api/v1/nota_credito` |
| DTOs | PascalCase + sufijo | `FacturaCreateDto`, `CompanyData` |
| Entidades | PascalCase + Entity | `FacturaEntity`, `ClientEntity` |
| Repos | PascalCase + Repository | `FacturaRepository` |
| Excepciones | PascalCase + Exception | `BusinessException`, `SunatValidationException` |

### IDs

| Dominio | Prefijo demo | Ejemplo |
|---------|-------------|---------|
| Company | `cmp-` | `cmp-demo-001` |
| Client | `cli-` | `cli-demo-001` |
| Factura | `fac-` | `fac-demo-001` |
| Boleta | `bol-` | `bol-demo-001` |
| Product | `prd-` | `prd-demo-001` |
| Employee | `emp-` | `emp-demo-001` |
| Sede | `sede-` | `sede-001` |
| POS | `pos-` | `pos-demo-001` |
| Campaign | `camp-` | `camp-demo-001` |
| Deal | `deal-` | `deal-demo-001` |
| User | `usr-` | `usr-admin-001` |

### Formato de respuesta

```json
// Éxito individual
{ "isSuccess": true, "status": "OK", "message": "...", "timestamp": "...", "data": { ... } }

// Éxito paginado
{ "isSuccess": true, "data": { "content": [...], "pagination": { "currentPage": 0, "pageSize": 10, "totalElements": 50, "totalPages": 5 } } }

// Error
{ "isSuccess": false, "status": "NOT_FOUND", "message": "...", "timestamp": "..." }
```

---

## 5. Flujo de datos estándar (para implementar una feature nueva)

```
1. OpenAPI YAML (contrato)
   └── Definir paths + schemas + operationId
   
2. mvn compile (genera interfaces + DTOs)
   └── target/generated-sources/openapi/
   
3. Controller (openapi/{Dominio}OpenApi.java)
   └── implements interfaz generada
   └── delega al Service
   
4. Service Interface (service/{Dominio}Service.java)
   └── Define contrato de negocio
   
5. Service Impl (service/impl/{Dominio}ServiceImpl.java)
   └── @Service con lógica de negocio
   └── Usa Mapper + Repository
   
6. Mapper (mapper/{Dominio}Mapper.java)
   └── @Mapper(componentModel = "spring")
   └── DTO ↔ Entity conversiones
   
7. Entity (persistence/{Dominio}Entity.java)
   └── @Entity + @Table + campos
   
8. Repository (persistence/{Dominio}Repository.java)
   └── extends JpaRepository<Entity, String>
   
9. data.sql (datos de prueba)
   └── INSERT INTO ... VALUES ...
   
10. Test (test/.../service/impl/{Dominio}ServiceImplTest.java)
    └── @ExtendWith(MockitoExtension.class)
    └── Mockear repo y mapper
```

---

## 6. Excepciones y manejo de errores

```
GlobalExceptionHandler (@RestControllerAdvice)
├── ResourceNotFoundException     → 404
├── BusinessException             → 422
├── ValidationException           → 400
├── SunatValidationException      → 422
├── MethodArgumentNotValid        → 400
├── AuthenticationException       → 401 (FASE B)
├── AuthorizationException        → 403 (FASE B)
└── Exception (fallback)          → 500
```

**Cada paquete nuevo puede crear sus propias excepciones** que extienden de las base (`BusinessException`, etc.).

---

## 7. Seguridad (post FASE B)

```
Request → JwtAuthenticationFilter → CompanyIsolationFilter → Controller
                                          ↓
                                   Valida: token.companyId == request.companyId
                                   ADMIN_ETIA bypasea esta validación
```

**Claims del JWT:**
- `userId`, `email`, `companyId`, `role`, `plan`, `modulosActivos[]`

---

## 8. Frontend — Integración

```
Thymeleaf renderiza HTML con datos del servidor
    ↓
JS modular (api-client.js) hace fetch con Bearer token
    ↓
Backend responde JSON
    ↓
JS actualiza DOM (no framework reactivo — vanilla)
```

**Regla:** El frontend SOLO orquesta llamadas al API. NO tiene lógica de negocio.

---

## 9. Testing

| Tipo | Herramienta | Ubicación |
|------|------------|-----------|
| Unit | JUnit 5 + Mockito | `src/test/java/.../service/impl/` |
| Integration | @SpringBootTest + H2 | `src/test/java/...` |
| Escenarios | Gherkin (.feature) | `src/test/resources/features/` |
| Escenarios docs | Markdown | `src/main/resources/scenarios/` |

---

## 10. Checklist rápido para agregar un módulo nuevo

```
□ 1. Definir endpoints en ms-bs-etia.yaml (tag + paths + schemas)
□ 2. mvn compile (verificar codegen)
□ 3. Crear paquete: {dominio}/openapi/ + service/ + service/impl/ + mapper/ + persistence/
□ 4. Implementar controller que implements interfaz generada
□ 5. Implementar service con lógica de negocio
□ 6. Crear mapper MapStruct
□ 7. Crear entity + repository
□ 8. Agregar datos a data.sql
□ 9. Crear vista Thymeleaf + view controller
□ 10. Crear módulo JS
□ 11. Agregar al menú (verificar plan)
□ 12. Crear escenarios en scenarios/
□ 13. Crear tests unitarios
□ 14. mvn compile && mvn test
□ 15. Actualizar bitácora
```

---

*Este documento es ley. Toda implementación que lo contradiga será revertida.*

