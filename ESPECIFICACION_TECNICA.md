# 📘 Especificación Técnica — Estándar de Desarrollo ETLIONS

> Documento de referencia técnica para replicar la arquitectura, patrones, convenciones y stack de este proyecto en nuevos microservicios.

---

## 1. Stack Tecnológico

| Capa | Tecnología | Versión | Notas |
|------|-----------|---------|-------|
| **Runtime** | Java | 21 (LTS) | Usar features de Java 17+ (records, pattern matching, text blocks) |
| **Framework** | Spring Boot | 4.0.5 | Starter parent como BOM |
| **API Contract** | OpenAPI 3.0.3 | — | Contrato YAML fuente de verdad |
| **Code Generation** | OpenAPI Generator Maven Plugin | 7.9.0 | Genera interfaces + DTOs |
| **ORM** | Spring Data JPA + Hibernate | (gestionado por Boot) | Repositorios de interfaz |
| **Mapping** | MapStruct | 1.6.3 | Compile-time, sin reflection |
| **Boilerplate** | Lombok | 1.18.30 | Annotation processor |
| **Binding** | lombok-mapstruct-binding | 0.2.0 | Garantiza que Lombok corre antes que MapStruct |
| **Validación** | Hibernate Validator + Jakarta Validation API | 3.0.2 | JSR 380 / Bean Validation |
| **BD Desarrollo** | H2 Database | (runtime) | En memoria `jdbc:h2:mem:localdb` |
| **BD Producción** | PostgreSQL | (runtime) | Driver incluido, perfil `prod` |
| **Template Engine** | Thymeleaf | (gestionado por Boot) | UI de backoffice embebida |
| **Build** | Maven Wrapper (`mvnw` / `mvnw.cmd`) | — | No requiere Maven global instalado |
| **Testing** | JUnit 5 + Spring Boot Test | (gestionado por Boot) | `spring-boot-starter-test` |

---

## 2. Estructura de Proyecto

### 2.1 Árbol raíz

```
ms-bs-{nombre}/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/{org}/{artifact}/
│   │   │   ├── MsBs{Nombre}Application.java     ← Entry point
│   │   │   ├── app_config/                       ← Configuración y excepciones globales
│   │   │   ├── app_util/                         ← Utilidades transversales
│   │   │   ├── view/                             ← Controllers Thymeleaf (MVC)
│   │   │   ├── {dominio_1}/                      ← Módulo de dominio
│   │   │   ├── {dominio_2}/
│   │   │   └── ...
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── data.sql                          ← Seed data inicial (H2)
│   │       ├── swagger/
│   │       │   └── ms-bs-{nombre}.yaml           ← Contrato OpenAPI
│   │       ├── static/                           ← Assets estáticos (CSS, JS, favicon)
│   │       └── templates/                        ← Plantillas Thymeleaf
│   │           ├── fragments/                    ← Fragmentos reutilizables (head, header, modals)
│   │           └── {vistas}.html
│   └── test/
│       └── java/com/{org}/{artifact}/
│           └── {dominio}/                        ← Tests por dominio
├── {nombre}_storage/                             ← Directorio de almacenamiento en disco (gitignored para binarios)
└── target/
    └── generated-sources/openapi/                ← Código generado (NO editar)
```

### 2.2 Estructura interna de cada módulo de dominio

Cada dominio sigue esta estructura fija de 4 subcarpetas (mínimo):

```
{dominio}/
├── openapi/          ← Controller que implementa la interfaz generada
│   └── {Dominio}OpenApi.java
├── service/          ← Interface del servicio + implementación
│   ├── {Dominio}Service.java          (interface)
│   └── impl/
│       └── {Dominio}ServiceImpl.java  (implementación)
├── mapper/           ← MapStruct mapper (DTO ↔ Entity)
│   └── {Dominio}Mapper.java
└── persistence/      ← Entity JPA + Repository
    ├── {Dominio}Entity.java
    └── {Dominio}Repository.java
```

**Subcarpetas opcionales** (solo si el dominio lo requiere):

```
{dominio}/
├── xml/              ← Builders de XML (UBL, etc.)
├── util/             ← Utilidades específicas del dominio
└── standard/         ← Catálogos o data estándar del dominio
```

---

## 3. Convenciones de Nomenclatura

### 3.1 Paquetes Java

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Base package | `com.{org}.ms_bs_{nombre}` | `com.etlions.ms_bs_etia` |
| Módulo dominio | `{base}.{dominio}` | `com.etlions.ms_bs_etia.doc_factura` |
| Subcapa | `{base}.{dominio}.{capa}` | `com.etlions.ms_bs_etia.doc_factura.service.impl` |
| Config global | `{base}.app_config.config` | `com.etlions.ms_bs_etia.app_config.config` |
| Excepciones global | `{base}.app_config.exception` | `com.etlions.ms_bs_etia.app_config.exception` |
| Utilidades transversales | `{base}.app_util.{subcapa}` | `com.etlions.ms_bs_etia.app_util.validation` |
| Paquete OpenAPI generado | `com.{org}.openapicodegen.api.v1` | `com.etlions.openapicodegen.api.v1` |
| DTOs OpenAPI generados | `com.{org}.openapicodegen.dto` | `com.etlions.openapicodegen.dto` |

> **Nota:** Los paquetes Java usan `_` (underscore) como separador de palabras porque Java no permite `-` en nombres de paquetes. Ejemplo: `doc_factura`, `ms_bs_etia`.

### 3.2 Clases Java

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Entry point | `MsBs{Nombre}Application` | `MsBsEtiaApplication` |
| Controller OpenAPI | `Doc{Dominio}OpenApi` / `{Dominio}OpenApi` | `DocFacturaOpenApi`, `clientOpenApi` |
| Service interface | `Doc{Dominio}Service` / `{dominio}Service` | `DocFacturaService`, `clientService` |
| Service impl | `Doc{Dominio}ServiceImpl` / `{dominio}ServiceImpl` | `DocFacturaServiceImpl` |
| Mapper | `{Dominio}Mapper` | `FacturaMapper`, `BoletaMapper` |
| Entity | `{Dominio}Entity` | `FacturaEntity`, `BoletaEntity` |
| Repository | `{Dominio}Repository` | `FacturaRepository` |
| Exception | `{Tipo}Exception` | `BusinessException`, `ResourceNotFoundException` |
| Properties bean | `{Dominio}Properties` | `SunatProperties` |
| XML Builder | `Ubl{Dominio}XmlBuilder` | `UblInvoiceXmlBuilder` |
| View Controller | `{Vista}ViewController` | `FacturasViewController`, `WelcomeViewController` |

### 3.3 Archivos de recurso

| Tipo | Nombre | Ubicación |
|------|--------|-----------|
| Config principal | `application.yaml` | `src/main/resources/` |
| Config producción | `application-prod.yaml` | `src/main/resources/` |
| Seed data | `data.sql` | `src/main/resources/` |
| Contrato API | `ms-bs-{nombre}.yaml` | `src/main/resources/swagger/` |
| Plantillas HTML | `{dominio}.html` | `src/main/resources/templates/` |
| Fragmentos HTML | `{fragmento}.html` | `src/main/resources/templates/fragments/` |

### 3.4 Naming del artefacto Maven

```xml
<groupId>com.{org}</groupId>
<artifactId>ms-bs-{nombre}</artifactId>
```

- `ms` = microservicio  
- `bs` = business service  
- `{nombre}` = identificador funcional corto  

---

## 4. Patrones de Diseño y Arquitectura

### 4.1 Arquitectura hexagonal por dominio (Feature-based)

Cada módulo de dominio es **autónomo** con sus propias capas. No existen paquetes globales tipo `controllers/`, `services/`, `repositories/`. Cada feature agrupa todo su stack vertical.

```
Request HTTP
    ↓
[OpenAPI Controller]    ← implementa interfaz generada
    ↓
[Service Interface]     ← contrato interno
    ↓
[Service Impl]          ← lógica de negocio + validación + orquestación
    ↓
[Mapper (MapStruct)]    ← DTO → Entity / Entity → DTO
    ↓
[Repository (JPA)]      ← acceso a datos
    ↓
[Entity]                ← modelo persistido
```

### 4.2 Contract-First API (OpenAPI → Codegen)

1. Se define **primero** el contrato YAML en `src/main/resources/swagger/ms-bs-{nombre}.yaml`.
2. El plugin `openapi-generator-maven-plugin` genera:
   - **Interfaces Java** (ej: `FacturaApi`, `BoletaApi`) en `com.{org}.openapicodegen.api.v1`
   - **DTOs Java** (Request/Response) en `com.{org}.openapicodegen.dto`
3. Los controllers **implementan** esas interfaces generadas.
4. **Nunca se editan** los archivos en `target/generated-sources/openapi/`.

**Configuración clave del plugin:**

```xml
<configOptions>
    <interfaceOnly>true</interfaceOnly>          <!-- Solo genera interfaces, no controllers -->
    <delegatePattern>true</delegatePattern>       <!-- Patrón delegado -->
    <useJakartaEe>true</useJakartaEe>             <!-- Jakarta EE (Spring Boot 3+) -->
    <useSpringBoot3>true</useSpringBoot3>
    <useLombok>true</useLombok>                   <!-- DTOs con Lombok -->
    <dateLibrary>legacy</dateLibrary>             <!-- java.util.Date para compatibilidad -->
    <useTags>true</useTags>                       <!-- Agrupa por tags del YAML -->
    <generateBuilders>true</generateBuilders>
    <additionalModelTypeAnnotations>
        @lombok.Getter
        @lombok.Setter
    </additionalModelTypeAnnotations>
</configOptions>
<additionalProperties>
    <openApiNullable>false</openApiNullable>       <!-- Sin Optional wrapper -->
</additionalProperties>
```

### 4.3 Patrón Controller: Delegación pura

El controller OpenAPI **no tiene lógica**. Solo delega al service y retorna `ResponseEntity`:

```java
@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocFacturaOpenApi implements FacturaApi {

    private final DocFacturaService docFacturaService;

    @Override
    public ResponseEntity<FacturaResponseDto> emitirFactura(FacturaEmitirDto request) {
        return ResponseEntity.ok(docFacturaService.emitirFactura(request));
    }
}
```

- **Anotación:** `@Controller` (no `@RestController` — la interfaz generada ya tiene `@ResponseBody`).
- **Base path:** `@RequestMapping("/api/v1")` — siempre versionado.
- **Inyección:** `@RequiredArgsConstructor` (Lombok) + campos `final`.

### 4.4 Patrón Service: Interface + Impl

```java
// Interface — en service/
public interface DocFacturaService {
    FacturaResponseDto emitirFactura(FacturaEmitirDto request);
    FacturaResponseDto obtenerFactura(String facturaId);
    // ...
}

// Implementación — en service/impl/
@Service
@RequiredArgsConstructor
public class DocFacturaServiceImpl implements DocFacturaService {
    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;
    // ...
}
```

- La **interfaz** vive en `{dominio}/service/`.
- La **implementación** vive en `{dominio}/service/impl/`.
- Se usa `@Transactional` en métodos que persisten datos.

### 4.5 Patrón Mapper: MapStruct compile-time

```java
@Mapper(componentModel = "spring")
public interface FacturaMapper {

    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    FacturaEntity toEntity(FacturaEmitirDto request);

    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    FacturaData toData(FacturaEntity entity);

    @Named("detalleToJson")
    default String detalleToJson(List<DetalleItem> detalle) { /* Jackson serialize */ }

    @Named("jsonToDetalle")
    default List<DetalleItem> jsonToDetalle(String json) { /* Jackson deserialize */ }
}
```

- `componentModel = "spring"` → se inyecta como bean.
- Conversiones complejas (enum ↔ String, List ↔ JSON) se resuelven con `@Named` + métodos `default`.
- El binding Lombok ↔ MapStruct se asegura con `lombok-mapstruct-binding` en `annotationProcessorPaths`.

### 4.6 Patrón Entity: JPA + Lombok

```java
@Entity
@Table(name = "facturas")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class FacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id")
    private String companyId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalleJson;          // List<> serializado a JSON string

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
```

- **ID:** `String` con `GenerationType.UUID`.
- **Columnas:** se usa `@Column(name = "snake_case")`.
- **Listas anidadas:** se serializan a JSON `String` en la entity (`detalleJson`) y se deserializan en el mapper.
- **Annotations Lombok obligatorias:** `@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder`.

### 4.7 Patrón Repository: JPA Interface

```java
@Repository
public interface FacturaRepository extends JpaRepository<FacturaEntity, String> {
    List<FacturaEntity> findByCompanyId(String companyId);
}
```

- Extiende `JpaRepository<Entity, IdType>`.
- Se usa anotación `@Repository`.
- Consultas derivadas por convención de Spring Data (`findBy...`).

### 4.8 Patrón Response Wrapper

Todas las respuestas de la API usan un wrapper unificado definido en el contrato OpenAPI:

**Respuesta exitosa (individual):**
```json
{
  "isSuccess": true,
  "status": "OK",
  "message": "Operación exitosa",
  "timestamp": "2026-04-04T10:30:00Z",
  "data": { ... }
}
```

**Respuesta exitosa (paginada):**
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

**Respuesta de error:**
```json
{
  "code": "ERR-400",
  "message": "Error de validación en los campos enviados",
  "timestamp": "2026-04-04T10:30:00Z",
  "errors": ["campo1: no puede ser nulo", "campo2: formato inválido"]
}
```

---

## 5. Manejo de Excepciones

### 5.1 Jerarquía de excepciones custom

```
RuntimeException
├── BusinessException              → HTTP 422 (Unprocessable Entity)
├── ResourceNotFoundException      → HTTP 404 (Not Found)
├── ValidationException            → HTTP 400 (Bad Request) + lista de errores
└── SunatValidationException       → HTTP 400 (Bad Request) + lista de errores (código ERR-SUNAT)
```

### 5.2 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(...) { /* ERR-404 */ }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(...) { /* ERR-422 */ }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(...) { /* ERR-400 */ }

    @ExceptionHandler(SunatValidationException.class)
    public ResponseEntity<ErrorResponse> handleSunatValidation(...) { /* ERR-SUNAT */ }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(...) { /* ERR-400 */ }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(...) { /* ERR-500 */ }
}
```

- Ubicación: `app_config/exception/`.
- El `ErrorResponse` DTO se genera desde el contrato OpenAPI.
- Todas las excepciones custom van en `app_config/exception/`.

### 5.3 Códigos de error

| Código | HTTP | Cuándo |
|--------|------|--------|
| `ERR-400` | 400 | Validación de campos (Jakarta / manual) |
| `ERR-SUNAT` | 400 | Validación específica de reglas de negocio externas |
| `ERR-404` | 404 | Recurso no encontrado |
| `ERR-422` | 422 | Regla de negocio violada |
| `ERR-500` | 500 | Error interno no controlado |

---

## 6. Configuración

### 6.1 `application.yaml` — Estructura base

```yaml
spring:
  application:
    name: ms-bs-{nombre}
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
      mode: always           # Ejecuta data.sql al iniciar
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

# ── Storage local ──
{nombre}:
  storage:
    base-path: ${STORAGE_PATH:./{nombre}_storage}
```

### 6.2 Properties tipadas con `@ConfigurationProperties`

```java
@Component
@ConfigurationProperties(prefix = "{namespace}")
@Getter @Setter
public class {Dominio}Properties {
    private String modo = "beta";
    private Map<String, String> endpoints;

    @Getter @Setter
    public static class SubConfig {
        private String valor = "default";
    }
}
```

- Se ubican en `app_config/config/`.
- Permiten tipado seguro de propiedades del YAML.
- Se inyectan como beans de Spring.

---

## 7. `pom.xml` — Estructura mínima

### 7.1 Parent

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>
```

### 7.2 Properties

```xml
<properties>
    <java.version>21</java.version>
    <mapstruct.version>1.6.3</mapstruct.version>
</properties>
```

### 7.3 Dependencies mínimas

```xml
<!-- ═══ CORE ═══ -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>spring-boot-starter-validation</dependency>
<dependency>spring-boot-starter-thymeleaf</dependency>   <!-- si hay UI -->

<!-- ═══ BD ═══ -->
<dependency>h2 (runtime)</dependency>
<dependency>postgresql (runtime)</dependency>

<!-- ═══ MAPPING / BOILERPLATE ═══ -->
<dependency>lombok (optional)</dependency>
<dependency>mapstruct</dependency>

<!-- ═══ OPENAPI CODEGEN ═══ -->
<dependency>swagger-annotations (2.2.8)</dependency>
<dependency>jackson-databind-nullable (0.2.6)</dependency>

<!-- ═══ JAKARTA (Spring Boot 3+) ═══ -->
<dependency>jakarta.validation-api (3.0.2)</dependency>
<dependency>jakarta.annotation-api (2.1.1)</dependency>
<dependency>hibernate-validator</dependency>

<!-- ═══ TEST ═══ -->
<dependency>spring-boot-starter-test (test)</dependency>
```

### 7.4 Plugins mínimos

**1. Spring Boot Maven Plugin:**
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>org.projectlombok:lombok</exclude>
        </excludes>
    </configuration>
</plugin>
```

**2. OpenAPI Generator Plugin:**
```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.9.0</version>
    <!-- Ver sección 4.2 para configuración completa -->
</plugin>
```

**3. Maven Compiler Plugin (Lombok + MapStruct):**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>21</release>
        <annotationProcessorPaths>
            <path>lombok (1.18.30)</path>
            <path>mapstruct-processor (${mapstruct.version})</path>
            <path>lombok-mapstruct-binding (0.2.0)</path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

> **Orden crítico en `annotationProcessorPaths`:**  
> 1. Lombok → 2. MapStruct processor → 3. lombok-mapstruct-binding  
> Esto garantiza que Lombok genere getters/setters antes de que MapStruct los necesite.

---

## 8. Capa de Utilidades Transversales (`app_util`)

Estructura del paquete para servicios compartidos entre dominios:

```
app_util/
├── validation/                    ← Validadores y formateadores
│   ├── SunatValidator.java        ← Validaciones hard-fail (lanza excepción)
│   ├── SunatFormatter.java        ← Formateo de datos (montos, fechas)
│   ├── DocumentValidatorService.java  ← Validación soft-fail (acumula errores/advertencias)
│   └── DocumentValidationResult.java  ← Record/VO con resultado de validación
├── service/                       ← Servicios transversales
│   ├── CorrelativoService.java    ← Generación de correlativos secuenciales
│   └── XmlStorageService.java     ← Persistencia de archivos en disco
├── persistence/                   ← Entidades transversales
│   ├── CorrelativoEntity.java
│   └── CorrelativoRepository.java
└── sunat/                         ← Integración con servicios externos
    ├── SunatSoapClient.java       ← Interface del cliente SOAP
    ├── MockSunatSoapClient.java   ← Mock para desarrollo/beta
    ├── SendResult.java            ← VO resultado de envío
    └── StatusResult.java          ← VO resultado de consulta
```

### 8.1 Patrón de validación dual

Se manejan **dos niveles** de validación:

| Nivel | Clase | Comportamiento | Cuándo usar |
|-------|-------|----------------|-------------|
| **Hard-fail** | `SunatValidator` (static) | Lanza `SunatValidationException` inmediatamente | En el Service al recibir un request que debe fallar rápido |
| **Soft-fail** | `DocumentValidatorService` (bean) | Acumula errores + advertencias, retorna `DocumentValidationResult` | En validación pre-emisión, donde el usuario necesita ver **todos** los problemas |

### 8.2 Almacenamiento en disco

Estructura de archivos generados:

```
{storage_base}/{ruc}/{serie}/{numero_con_padding}/{ruc}-{tipoDoc}-{serie}-{numero}.xml
```

Ejemplo:
```
etia_storage/20517863948/F003/00001716/20517863948-01-F003-00001716.xml
```

- Configurado vía `application.yaml` con variable de entorno fallback.
- El service devuelve **rutas relativas** que se persisten en la entity.

---

## 9. Capa de Vista (Thymeleaf)

### 9.1 View Controllers

```java
@Controller
public class FacturasViewController {
    @GetMapping("/facturas")
    public String facturas() {
        return "facturas";    // → templates/facturas.html
    }
}
```

- Los view controllers viven en `view/` (paquete plano, sin subcarpetas).
- Solo retornan el nombre de la template.
- Son clases ligeras — la lógica de datos está en la API REST.

### 9.2 Templates y fragmentos

```
templates/
├── fragments/
│   ├── head.html              ← <head> compartido (CSS, meta)
│   ├── header.html            ← Navbar
│   ├── modals.html            ← Modales reutilizables
│   ├── config-overlay.html    ← Overlay de configuración
│   └── doc-preview.html       ← Preview de documentos
├── welcome.html
├── facturas.html
├── boletas.html
└── ...
```

- Fragmentos con `th:fragment` y se incluyen con `th:replace`.
- Auto-configuración de Spring Boot (no se definen beans de TemplateResolver manualmente).

---

## 10. Archivos mínimos requeridos para un nuevo proyecto

### Checklist de bootstrapping

```
□ pom.xml                                    (con parent, deps, plugins)
□ mvnw + mvnw.cmd + .mvn/                    (Maven Wrapper)
□ src/main/java/.../MsBs{Nombre}Application.java
□ src/main/java/.../app_config/config/        (al menos ThymeleafConfig si hay UI)
□ src/main/java/.../app_config/exception/
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   └── ValidationException.java
□ src/main/java/.../view/
│   └── WelcomeViewController.java            (si hay UI)
□ src/main/resources/application.yaml
□ src/main/resources/data.sql                 (puede estar vacío)
□ src/main/resources/swagger/ms-bs-{nombre}.yaml
□ src/main/resources/static/favicon.svg
□ src/main/resources/templates/welcome.html   (si hay UI)
□ src/main/resources/templates/fragments/
│   ├── head.html
│   └── header.html
□ src/test/java/.../MsBs{Nombre}ApplicationTests.java
```

### Para agregar un nuevo dominio, crear:

```
□ {dominio}/openapi/{Dominio}OpenApi.java
□ {dominio}/service/{Dominio}Service.java
□ {dominio}/service/impl/{Dominio}ServiceImpl.java
□ {dominio}/mapper/{Dominio}Mapper.java
□ {dominio}/persistence/{Dominio}Entity.java
□ {dominio}/persistence/{Dominio}Repository.java
□ Actualizar swagger/ms-bs-{nombre}.yaml con los nuevos endpoints
□ (Opcional) view/{Dominio}ViewController.java + templates/{dominio}.html
```

---

## 11. Ciclo de Build y Generación

### 11.1 Comandos esenciales

```bash
# Compilar (genera OpenAPI + MapStruct)
.\mvnw.cmd compile

# Ejecutar en desarrollo
.\mvnw.cmd spring-boot:run

# Ejecutar tests
.\mvnw.cmd test

# Empaquetar JAR (sin tests)
.\mvnw.cmd package -DskipTests

# Ejecutar JAR
java -jar target/ms-bs-{nombre}-0.0.1-SNAPSHOT.jar
```

### 11.2 Pipeline de compilación

```
mvn compile
    ↓
[1] openapi-generator-maven-plugin
    → Lee swagger/ms-bs-{nombre}.yaml
    → Genera interfaces en target/generated-sources/openapi/
    → Genera DTOs en target/generated-sources/openapi/
    ↓
[2] maven-compiler-plugin
    → Procesa annotations (Lombok → MapStruct → binding)
    → Compila fuentes + fuentes generadas
```

---

## 12. Prácticas y Reglas

### 12.1 Generales

- **YAML > properties**: siempre `application.yaml`, nunca `.properties`.
- **No editar generated-sources**: toda modificación en el contrato YAML.
- **Un dominio = un paquete autónomo**: nunca mezclar entidades de dominios diferentes.
- **DTOs del contrato OpenAPI**: no crear DTOs manuales para la API — usar los generados.
- **Sin constructores manuales en entities/DTOs**: Lombok los genera (`@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`).

### 12.2 Inyección de dependencias

- **Siempre inyección por constructor** (`@RequiredArgsConstructor` + campos `final`).
- **Nunca** `@Autowired` en campo.
- Se permite `@Value` solo en constructores de servicios de infraestructura (ej: `XmlStorageService`).

### 12.3 Base de datos

- **Desarrollo:** H2 en memoria con `ddl-auto: update` + `data.sql` para seed.
- **Producción:** PostgreSQL con `ddl-auto: validate` (schema manejado por migraciones).
- **ID strategy:** `GenerationType.UUID` con tipo `String`.
- **Columnas:** nombradas en `snake_case` vía `@Column(name = "...")`.
- **Tablas:** nombradas en plural `snake_case` (ej: `facturas`, `notas_credito`).
- **Listas embebidas:** serializadas como JSON `TEXT` (no tablas join para detalle de documentos).

### 12.4 API REST

- **Prefijo base:** `/api/v1` — siempre versionado.
- **Todos los endpoints devuelven wrapper** con `isSuccess`, `status`, `message`, `timestamp`, `data`.
- **Paginación en listas** con `page`, `size`, y metadata `pagination`.
- **Content-Type:** `application/json` (excepto descargas de archivos: `application/xml`, `application/octet-stream`).

### 12.5 Testing

- Tests ubicados en `src/test/java` replicando la estructura de dominio.
- Clases de test: `{Clase}Test.java` (ej: `DocFacturaServiceImplTest.java`).
- Framework: JUnit 5 + `@SpringBootTest` para tests de integración.
- Mocks con Mockito (incluido en `spring-boot-starter-test`).

### 12.6 Logging

- Usar `@Slf4j` (Lombok) en clases que necesiten logging.
- Logs con emojis para categorización visual: `📄`, `📂`, `❌`, `⚠️`, `✅`.
- No loguear datos sensibles (contraseñas, certificados).

---

## 13. Módulos de dominio existentes como referencia

| Módulo | Paquete | Capas | Descripción |
|--------|---------|-------|-------------|
| `doc_factura` | openapi, service, mapper, persistence, xml | Controller + Service + Mapper + Entity + Repo + XML Builder |
| `doc_boleta` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `doc_notacred` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `doc_notadeb` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `company` | openapi, service, mapper, persistence, util | Controller + Service + Mapper + Entity + Repo + Util |
| `client` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `employee` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `product` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `sede` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `serie_config` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `resumen_diario` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `catalogo_sunat` | openapi, persistence, service, standard | Catálogos estáticos SUNAT |
| `libro_electronico` | openapi, service, mapper, persistence | Controller + Service + Mapper + Entity + Repo |
| `notification` | openapi, persistence, service | Sistema de notificaciones internas |
| `reniec` | — | Integración con servicio RENIEC |
| `tabla_sunat` | — | Tablas auxiliares SUNAT |
| `tipo_cambio` | — | Tipo de cambio de moneda |
| `ubigeo` | — | Ubigeos nacionales |
| `unidad_medida` | — | Catálogo de unidades de medida |
| `user_preference` | — | Preferencias del usuario |

---

## 14. Resumen de decisiones técnicas clave

| Decisión | Elección | Razón |
|----------|----------|-------|
| Contract-first vs Code-first | **Contract-first** (OpenAPI YAML) | Consistencia API, generación automática, documentación siempre actualizada |
| Estructura de paquetes | **Feature-based** (por dominio) | Módulos autónomos, fácil de navegar, escala mejor |
| Mapper | **MapStruct** (compile-time) | Sin overhead de reflection, errores en compilación |
| Boilerplate | **Lombok** | Reduce código repetitivo sin magia en runtime |
| ID | **UUID String** | Distribuido-friendly, sin colisiones |
| Listas anidadas en BD | **JSON Text** | Evita tablas intermedias para datos que siempre se leen/escriben juntos |
| Validación | **Dual (hard + soft)** | Hard-fail para requests, soft-fail para validación exhaustiva |
| Error handling | **@RestControllerAdvice centralizado** | Un solo punto de control, respuestas consistentes |
| UI Backoffice | **Thymeleaf server-side** | Rápido de desarrollar, sin framework SPA adicional |
| Storage de archivos | **Filesystem local** | Simple, configurable por entorno |

---

*Documento generado como especificación técnica estándar ETLIONS. Última actualización: 2026-04-09*

---

# 📐 PARTE II — Estándar OpenAPI: Endpoints, Schemas y DTOs

> Hoja de estandarización para definir nuevos endpoints en el contrato OpenAPI 3.0.3.  
> Todo lo que sea negocio está variabilizado con `{placeholder}`. Esta guía es **100% técnica y reutilizable** en cualquier proyecto.

---

## 15. Estructura base del contrato OpenAPI

### 15.1 Cabecera obligatoria

```yaml
openapi: 3.0.3
info:
  title: "{Nombre del Proyecto} — {Descripción corta}"
  version: "{major}.{minor}.{patch}"
  description: "{Descripción técnica del servicio}"
servers:
  - url: http://localhost:{port}/api
```

**Reglas:**
- Siempre `openapi: 3.0.3` (compatibilidad con OpenAPI Generator 7.9.0).
- `servers[0].url` termina en `/api` (sin `/v1` — ese se define en `@RequestMapping`).
- Versionado semántico: `major` para breaking changes, `minor` para nuevos endpoints, `patch` para correcciones.

### 15.2 Tags — Declaración obligatoria

Cada dominio tiene **un tag**. Se declaran todos arriba del bloque `paths`:

```yaml
tags:
  - name: {dominio}
    description: "{Descripción corta del dominio}"
```

**Convenciones de naming de tags:**
| Tipo de dominio | Ejemplo de tag | Ejemplo de nombre |
|-----------------|----------------|-------------------|
| Entidad maestra (singular) | `{entidad}` | `company`, `client`, `product` |
| Documento electrónico | `{tipo_documento}` | `factura`, `boleta`, `nota_credito` |
| Catálogo/referencia | `{catalogo}` | `ubigeo`, `unidad_medida`, `catalogo_sunat` |
| Servicio transversal | `{servicio}` | `notification`, `tipo_cambio`, `reniec` |

### 15.3 Archivo y ubicación

```
src/main/resources/swagger/ms-bs-{nombre}.yaml
```

Un solo archivo YAML por microservicio. No se fragmenta.

---

## 16. Taxonomía de tipos de endpoint

Todo endpoint del sistema pertenece a **uno de estos 9 tipos estándar**. Al crear uno nuevo, se debe identificar primero su tipo y seguir la plantilla correspondiente.

| # | Tipo | Verbo HTTP | Path pattern | Response |
|---|------|-----------|-------------|----------|
| 1 | **Crear recurso** | `POST` | `/{dominio}` | `{Dominio}ResponseDto` |
| 2 | **Listar con paginación** | `GET` | `/{dominio}` | `{Dominio}ListResponseDto` |
| 3 | **Obtener por ID** | `GET` | `/{dominio}/{id}` | `{Dominio}ResponseDto` |
| 4 | **Actualizar recurso** | `PUT` | `/{dominio}` ó `/{dominio}/{id}/actualizar` | `{Dominio}ResponseDto` |
| 5 | **Eliminar recurso** | `DELETE` | `/{dominio}` | `SuccessResponse` |
| 6 | **Acción sobre recurso** | `PUT` ó `POST` | `/{dominio}/{id}/{accion}` | `{Dominio}ResponseDto` ó DTO específico |
| 7 | **Descarga de archivo** | `GET` | `/{dominio}/{id}/{tipo_archivo}` | `binary` (application/xml, octet-stream) |
| 8 | **Upload de archivo** | `POST` | `/{dominio}/{sub_recurso}` | `{Dominio}ResponseDto` |
| 9 | **Catálogo estático** | `GET` | `/{catalogo}` | `array` ó `{Catalogo}ResponseDto` |

---

## 17. Plantillas estándar por tipo de endpoint

### 17.1 TIPO 1 — Crear recurso (`POST /{dominio}`)

```yaml
/{dominio}:
  post:
    summary: "Crear {recurso_singular}"
    operationId: crear{Dominio}                          # camelCase, verbo + sustantivo
    tags:
      - {dominio}
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/{Dominio}CreateDto'
          example:                                        # ← EJEMPLO OBLIGATORIO
            companyId: "cmp_20601234567"
            nombre: "Valor de ejemplo"
            descripcion: "Descripción de ejemplo"
    responses:
      "200":
        description: "{Recurso} creado exitosamente"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{prefijo}_{uuid_corto}"
                nombre: "Valor de ejemplo"
                estado: "ACTIVO"
                fechaCreacion: "2026-04-09T10:30:00Z"
      "400":
        description: "Error de validación"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
            example:
              code: "ERR-400"
              message: "Error de validación en los campos enviados"
              timestamp: "2026-04-09T10:30:00Z"
              errors:
                - "nombre: no puede ser vacío"
                - "companyId: es requerido"
      "500":
        description: "Error interno"
```

**Reglas:**
- `operationId`: `crear{Dominio}`, `registrar{Dominio}`, ó `emitir{Dominio}` (nunca `add`, `new`, `insert`).
- `requestBody`: siempre `required: true`.
- Response `200`: wrapper con `data` → `{Dominio}Data`.
- Response `400`: siempre incluir ejemplo de `ErrorResponse` con campo `errors[]`.
- Response `500`: solo `description`, sin body de ejemplo.

---

### 17.2 TIPO 2 — Listar con paginación (`GET /{dominio}`)

```yaml
/{dominio}:
  get:
    summary: "Listar {recursos_plural}"
    operationId: listar{DominioPlural}                   # listarFacturas, listarClientes
    tags:
      - {dominio}
    parameters:
      # ── FILTRO OBLIGATORIO: Scope por empresa ──
      - name: companyId
        in: query
        required: true                                    # ← Siempre requerido
        schema:
          type: string
        description: "ID de la empresa"
        example: "cmp_20601234567"

      # ── FILTROS OPCIONALES (varían según dominio) ──
      - name: {filtro_1}
        in: query
        schema:
          type: string
          enum: [{VALOR_1}, {VALOR_2}]                   # ← Solo si es valor cerrado
        description: "Filtrar por {campo}"
        example: "{VALOR_1}"

      - name: desdeFecha                                  # ← Solo en dominios temporales
        in: query
        schema:
          type: string
          format: date
        example: "2026-04-01"

      - name: hastaFecha
        in: query
        schema:
          type: string
          format: date
        example: "2026-04-30"

      # ── PAGINACIÓN ESTÁNDAR (siempre al final) ──
      - name: page
        in: query
        schema:
          type: integer
          default: 0
        example: 0
      - name: size
        in: query
        schema:
          type: integer
          default: 10
        example: 10
    responses:
      "200":
        description: "Lista de {recursos}"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ListResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                content:
                  - id: "{prefijo}_001"
                    nombre: "Ejemplo 1"
                    estado: "ACTIVO"
                  - id: "{prefijo}_002"
                    nombre: "Ejemplo 2"
                    estado: "ACTIVO"
                pagination:
                  totalElements: 50
                  totalPages: 5
                  currentPage: 0
                  pageSize: 10
      "401":
        description: "Unauthorized"
```

**Reglas:**
- **`companyId` siempre requerido** como primer parámetro (scope de empresa multi-tenant).
- Parámetros de paginación `page`/`size` siempre al final, con defaults `0` y `10`.
- Filtros de fecha usan `format: date` y prefijo `desdeFecha`/`hastaFecha`.
- Filtros de estado usan `enum` (nunca string libre).
- Orden de parámetros: `companyId` → filtros de negocio → filtros de fecha → `page`/`size`.

---

### 17.3 TIPO 3 — Obtener por ID (`GET /{dominio}/{id}`)

```yaml
/{dominio}/{{dominio}Id}:
  get:
    summary: "Obtener {recurso} por ID"
    operationId: obtener{Dominio}
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: path                                          # ← path, no query
        required: true
        schema:
          type: string
        description: "ID del {recurso}"
        example: "{prefijo}_123456"
    responses:
      "200":
        description: "{Recurso} encontrado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{prefijo}_123456"
                companyId: "cmp_20601234567"
                nombre: "Ejemplo"
                estado: "ACTIVO"
                fechaCreacion: "2026-04-09T10:30:00Z"
      "404":
        description: "{Recurso} no encontrado"
```

**Reglas:**
- El ID va en `path` (no en `query`).
- `operationId`: siempre `obtener{Dominio}` (nunca `get`, `find`, `fetch`).
- Response obligatoria: `200` + `404`.
- Sin parámetros `companyId` — el recurso se identifica solo por su UUID.

---

### 17.4 TIPO 4 — Actualizar recurso (`PUT`)

**Variante A — ID en query (maestros simples):**

```yaml
/{dominio}:
  put:
    summary: "Actualizar {recurso}"
    operationId: actualizar{Dominio}
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: query                                         # ← query para maestros simples
        required: true
        schema:
          type: string
        example: "{prefijo}_123456"
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/{Dominio}UpdateDto'
          example:
            nombre: "Nuevo nombre"
            direccion: "Calle Nueva 456"
    responses:
      "200":
        description: "{Recurso} actualizado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{prefijo}_123456"
                nombre: "Nuevo nombre"
                estado: "ACTIVO"
      "400":
        description: "No se puede actualizar (estado inválido)"
      "404":
        description: "{Recurso} no encontrado"
```

**Variante B — ID en path + sub-ruta (documentos):**

```yaml
/{dominio}/{{dominio}Id}/actualizar:
  put:
    summary: "Actualizar {recurso} en estado BORRADOR"
    operationId: actualizar{Dominio}Borrador
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: path                                          # ← path para sub-rutas
        required: true
        schema:
          type: string
        example: "{prefijo}_123456"
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/{Dominio}EmitirDto'   # ← Reutiliza EmitirDto
          example:
            companyId: "cmp_20601234567"
            clientId: "cli_123456"
            total: 1180.00
    responses:
      "200":
        description: "Borrador actualizado exitosamente"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
      "400":
        description: "El {recurso} no está en estado editable"
      "404":
        description: "{Recurso} no encontrado"
```

**Reglas:**
- Maestros simples → ID en `query`, path `/{dominio}`.
- Documentos con lifecycle → ID en `path`, sub-ruta `/{dominio}/{id}/actualizar`.
- `UpdateDto` tiene **todos los campos opcionales** (partial update).
- Para documentos, se reutiliza `EmitirDto` (full replace de datos mutables).

---

### 17.5 TIPO 5 — Eliminar recurso (`DELETE /{dominio}`)

```yaml
/{dominio}:
  delete:
    summary: "Eliminar {recurso}"
    operationId: eliminar{Dominio}
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: query
        required: true
        schema:
          type: string
        example: "{prefijo}_123456"
      - name: softDelete                                  # ← Opcional, solo si el dominio lo soporta
        in: query
        schema:
          type: boolean
          default: true
        description: "true = desactiva (soft), false = elimina físicamente"
        example: true
    responses:
      "200":
        description: "{Recurso} eliminado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SuccessResponse'
            example:
              isSuccess: true
              status: "OK"
              message: "{Recurso} eliminado exitosamente"
              timestamp: "2026-04-09T10:30:00Z"
      "400":
        description: "No se puede eliminar (restricción de estado)"
      "404":
        description: "{Recurso} no encontrado"
```

**Reglas:**
- **Siempre retorna `SuccessResponse`** (no `{Dominio}ResponseDto` vacío).
- El parámetro `softDelete` se incluye solo en dominios que soportan ambos modos.
- Para documentos electrónicos: solo se permite si `estado = BORRADOR`.

---

### 17.6 TIPO 6 — Acción sobre recurso (`POST|PUT /{dominio}/{id}/{accion}`)

**Sub-tipo 6A — Validar (POST):**

```yaml
/{dominio}/{{dominio}Id}/validar:
  post:
    summary: "Validar {recurso} internamente (sin emitir)"
    description: |
      Ejecuta todas las reglas de validación sobre un {recurso} existente
      y actualiza su estado a VALIDADO o CON_ERROR. No genera archivos ni transmite.
      Puede ejecutarse sobre recursos en estado BORRADOR, CON_ERROR o VALIDADO.
    operationId: validar{Dominio}
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: path
        required: true
        schema:
          type: string
        description: "ID del {recurso} a validar"
        example: "{prefijo}_123456"
    responses:
      "200":
        description: "Resultado de la validación"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/DocumentValidationResultDto'
            example:
              valid: false
              errores:
                - "Serie inválida: debe empezar con {prefijo_serie}"
                - "Total no coincide con la suma de líneas"
              advertencias:
                - "Tipo de cambio no informado para moneda USD"
                - "Leyenda no proporcionada — se generará automáticamente"
      "404":
        description: "{Recurso} no encontrado"
```

**Sub-tipo 6B — Emitir/Promover estado (PUT):**

```yaml
/{dominio}/{{dominio}Id}/emitir:
  put:
    summary: "Emitir {recurso} en estado BORRADOR"
    description: |
      Toma un {recurso} existente con estado BORRADOR o VALIDADO, genera el archivo
      de salida, lo persiste en disco y BD, y publica la notificación de emisión.
      Solo recursos con estado permitido pueden ser promovidos.
    operationId: emitir{Dominio}Borrador
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: path
        required: true
        schema:
          type: string
        description: "ID del {recurso} borrador a emitir"
        example: "{prefijo}_123456"
    responses:
      "200":
        description: "{Recurso} emitido exitosamente"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "{Recurso} emitido exitosamente"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{prefijo}_123456"
                estado: "ENVIADA"
      "400":
        description: "El {recurso} no está en estado BORRADOR o VALIDADO"
      "404":
        description: "{Recurso} no encontrado"
```

**Sub-tipo 6C — Anular (PUT en path raíz):**

```yaml
/{dominio}:
  put:
    summary: "Anular {recurso}"
    operationId: anular{Dominio}
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: query
        required: true
        schema:
          type: string
        example: "{prefijo}_123456"
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Anular{Dominio}Dto'
          example:
            motivoAnulacion: "Error en datos del documento"
    responses:
      "200":
        description: "{Recurso} anulado exitosamente"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "{Recurso} anulado"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{prefijo}_123456"
                estado: "ANULADA"
      "400":
        description: "No se puede anular"
      "404":
        description: "{Recurso} no encontrado"
```

**Reglas de acciones:**
- `validar` → `POST` (no modifica el recurso visible, solo estado interno).
- `emitir` → `PUT` (transición de estado).
- `anular` → `PUT` en path raíz con ID en query (transición de estado).
- `consultar-estado` → `POST` (consulta externa + posible transición).
- Verbo en español para `operationId`: `validar`, `emitir`, `anular`, `consultar`.

---

### 17.7 TIPO 7 — Descarga de archivo (`GET /{dominio}/{id}/{tipo}`)

```yaml
/{dominio}/{{dominio}Id}/{tipo_archivo}:
  get:
    summary: "Descargar {tipo_archivo} del {recurso}"
    description: |
      Retorna el archivo generado para el {recurso} indicado.
      El nombre del archivo sigue el formato: {formato_nombre}.
      Si el {recurso} aún no tiene archivo generado, retorna 204.
    operationId: descargar{Dominio}{TipoArchivo}         # descargarFacturaXml
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: path
        required: true
        schema:
          type: string
        description: "ID del {recurso}"
        example: "{prefijo}_123456"
    responses:
      "200":
        description: "Archivo del {recurso}"
        headers:
          Content-Disposition:
            schema:
              type: string
            description: 'attachment; filename="{nombre_archivo}"'
            example: 'attachment; filename="20601234567-01-F001-00000001.xml"'
        content:
          application/xml:                                # ← ó application/octet-stream
            schema:
              type: string
              format: binary
      "204":
        description: "El {recurso} existe pero aún no tiene archivo generado"
      "404":
        description: "{Recurso} no encontrado"
```

**Reglas:**
- Response `200`: `Content-Disposition: attachment` + content-type real (`application/xml`, `image/png`, etc.).
- Response `204`: body vacío — el recurso existe pero no tiene archivo.
- Response `404`: el recurso no existe.
- `operationId`: `descargar{Dominio}{Formato}` (ej: `descargarFacturaXml`, `obtenerLogo`).

---

### 17.8 TIPO 8 — Upload de archivo (`POST multipart`)

```yaml
/{dominio}/{sub_recurso}:
  post:
    summary: "Subir {tipo_archivo} del {recurso}"
    operationId: subir{TipoArchivo}
    tags:
      - {dominio}
    parameters:
      - name: {dominio}Id
        in: query
        required: true
        schema:
          type: string
        description: "ID del {recurso}"
        example: "{prefijo}_123456"
    requestBody:
      required: true
      content:
        multipart/form-data:
          schema:
            type: object
            required:
              - archivo
            properties:
              archivo:
                type: string
                format: binary
                description: "Archivo ({extensiones_permitidas}, max {tamaño}MB)"
    responses:
      "200":
        description: "Archivo subido exitosamente"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Dominio}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Archivo subido exitosamente"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{prefijo}_123456"
      "400":
        description: "Archivo inválido ({restricciones})"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
            example:
              code: "ERR-400"
              message: "Archivo inválido"
              timestamp: "2026-04-09T10:30:00Z"
              errors:
                - "Solo se permiten archivos {extensiones_permitidas}"
      "404":
        description: "{Recurso} no encontrado"
```

**Reglas:**
- Content-Type: `multipart/form-data`.
- Nombre del campo: siempre `archivo` (estándar unificado).
- Documentar extensiones y tamaño máximo en `description`.
- Retorna el `{Dominio}ResponseDto` actualizado (no `SuccessResponse` vacío).

---

### 17.9 TIPO 9 — Catálogo estático (`GET /{catalogo}`)

```yaml
/{catalogo}:
  get:
    summary: "Listar {catálogo}"
    operationId: listar{Catalogo}
    tags:
      - {catalogo}
    parameters: []                                        # ← Sin parámetros (o filtro básico)
    responses:
      "200":
        description: "Lista de {catálogo}"
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/{Catalogo}Data'
            example:
              - id: "001"
                codigo: "01"
                descripcion: "Valor ejemplo 1"
              - id: "002"
                codigo: "02"
                descripcion: "Valor ejemplo 2"
```

**Variante — Catálogo con búsqueda:**

```yaml
/{catalogo}:
  get:
    summary: "Obtener {catálogo} por nombre"
    operationId: obtener{Catalogo}
    tags:
      - {catalogo}
    parameters:
      - name: nombre{Catalogo}
        in: query
        required: true
        schema:
          type: string
        description: "Nombre exacto del {catálogo}"
        example: "Nombre ejemplo"
    responses:
      "200":
        description: "Datos del {catálogo}"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Catalogo}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                nombre: "Nombre ejemplo"
                registros: 25
                data:
                  - codigo: "01"
                    descripcion: "Valor 1"
      "404":
        description: "{Catálogo} no encontrado"
```

**Reglas:**
- Sin paginación (datos estáticos de tamaño controlado).
- Puede retornar `array` directamente (sin wrapper) o con wrapper si tiene metadata.
- Sin `companyId` — son datos globales del sistema.

---

## 18. Estándar de Schemas (DTOs)

### 18.1 Taxonomía de DTOs

Cada dominio tiene **exactamente** estos schemas:

| Schema | Propósito | Campos | Usado en |
|--------|-----------|--------|----------|
| `{Dominio}CreateDto` | Crear recurso (request) | Campos requeridos + opcionales con `default` | `POST /{dominio}` |
| `{Dominio}UpdateDto` | Actualizar recurso (request) | Todos los campos opcionales (partial update) | `PUT /{dominio}` |
| `{Dominio}Data` | Modelo de respuesta (core) | Todos los campos del recurso + calculados | Embebido en ResponseDto y ListResponseDto |
| `{Dominio}ResponseDto` | Respuesta individual (response) | `SuccessResponse` + `data: {Dominio}Data` | `POST`, `GET /{id}`, `PUT` |
| `{Dominio}ListResponseDto` | Respuesta paginada (response) | `SuccessResponse` + `data.content[]` + `data.pagination` | `GET /{dominio}` |

**Opcionales (solo si el dominio lo requiere):**

| Schema | Propósito | Cuándo |
|--------|-----------|--------|
| `{Dominio}EmitirDto` | Crear + emitir documento | Dominios con lifecycle (documentos electrónicos) |
| `Anular{Dominio}Dto` | Body para anulación | Dominios que requieren motivo de anulación |

### 18.2 Plantilla: `{Dominio}CreateDto`

```yaml
{Dominio}CreateDto:
  type: object
  required:                                                # ← Lista explícita de campos requeridos
    - companyId
    - {campo_requerido_1}
    - {campo_requerido_2}
  properties:
    companyId:
      type: string
      minLength: 1
      description: "ID de la empresa propietaria"
      example: "cmp_20601234567"
    {campo_requerido_1}:
      type: string
      minLength: 1
      maxLength: 300
      description: "{Descripción del campo}"
      example: "{Valor ejemplo}"
    {campo_requerido_2}:
      type: number
      minimum: 0
      description: "{Descripción}"
      example: 1000.00
    {campo_opcional_con_default}:
      type: string
      enum: [{VALOR_1}, {VALOR_2}]
      default: "{VALOR_1}"                                 # ← Default explícito
      description: "{Descripción}"
      example: "{VALOR_1}"
    {campo_opcional_libre}:
      type: string
      description: "{Descripción}"
      example: "{Valor ejemplo}"
    {campo_fecha}:
      type: string
      format: date                                         # ← YYYY-MM-DD (sin hora)
      example: "2026-04-09"
    {campo_timestamp}:
      type: string
      format: date-time                                    # ← ISO 8601 con timezone
      example: "2026-04-09T10:30:00Z"
    {campo_array}:
      type: array
      minItems: 1                                          # ← Si es requerido
      items:
        $ref: '#/components/schemas/{SubItem}'
    {campo_booleano_con_descripcion}:
      type: boolean
      default: true
      description: |
        Si true (default), {comportamiento A}.
        Si false, {comportamiento B}.
      example: true
```

**Reglas de CreateDto:**
- **`required`**: lista explícita arriba del `properties`.
- **`companyId`**: siempre presente y requerido (multi-tenant).
- **`minLength: 1`**: en strings requeridos para prevenir strings vacíos.
- **`minimum: 0`**: en numbers monetarios.
- **`default`**: en campos con valor por defecto.
- **`example`**: en TODOS los campos sin excepción.
- **`description`**: en todos los campos no obvios.
- **`enum`**: solo para valores cerrados conocidos.

### 18.3 Plantilla: `{Dominio}UpdateDto`

```yaml
{Dominio}UpdateDto:
  type: object
  properties:                                              # ← Sin "required" — todo es opcional
    {campo_1}:
      type: string
      example: "Nuevo valor"
    {campo_2}:
      type: string
      example: "Otro valor"
    {campo_enum}:
      type: string
      enum: [{VALOR_1}, {VALOR_2}]
    {campo_numero}:
      type: number
    {campo_array}:
      type: array
      items:
        $ref: '#/components/schemas/{SubItem}'
```

**Reglas de UpdateDto:**
- **Sin `required`** — todos los campos son opcionales.
- **Sin `companyId`** — no se puede cambiar la empresa propietaria.
- **Sin `id`** — viene por parámetro path/query.
- **Sin `fechaCreacion`** — inmutable.
- **Sin `estado`** — controlado por máquina de estados.
- `example` obligatorio solo en campos con estructura no obvia.

### 18.4 Plantilla: `{Dominio}Data`

```yaml
{Dominio}Data:
  type: object
  properties:
    # ── Campos de identidad (siempre primero) ──
    id:
      type: string
      example: "{prefijo}_123456"
    companyId:
      type: string
      example: "cmp_20601234567"

    # ── Campos de negocio (orden lógico) ──
    {campo_1}:
      type: string
      example: "{valor}"
    {campo_2}:
      type: number
      example: 1000.00

    # ── Campos de estado (control de lifecycle) ──
    estado:
      type: string
      enum: [{ESTADO_1}, {ESTADO_2}, {ESTADO_N}]
      example: "{ESTADO_DEFAULT}"
    erroresValidacion:
      type: array
      nullable: true
      items:
        type: string
      example: []

    # ── Campos de integración externa (nullable) ──
    hashCdr:
      type: string
      nullable: true
    codigoRespuestaExterno:
      type: string
      nullable: true
    descripcionRespuestaExterno:
      type: string
      nullable: true

    # ── Campos embebidos (arrays/objetos) ──
    {campo_array}:
      type: array
      items:
        $ref: '#/components/schemas/{SubItem}'

    # ── Campos de auditoría (siempre al final) ──
    fechaCreacion:
      type: string
      format: date-time
      example: "2026-04-09T10:30:00Z"
    fechaActualizacion:
      type: string
      format: date-time
      example: "2026-04-09T12:00:00Z"
```

**Reglas de Data:**
- **Orden de campos**: identidad → negocio → estado → integración → embebidos → auditoría.
- **`id`**: siempre tipo `string` (UUID).
- **`estado`**: siempre `enum` con todos los estados posibles del lifecycle.
- **`nullable: true`**: solo en campos que pueden ser `null` semánticamente.
- **`example`**: obligatorio en todos los campos.
- **Nunca incluir campos sensibles**: passwords, certificados, claves SOL.
- **Campos calculados por backend**: documentar con `description` (ej: "Calculado por el sistema").

### 18.5 Plantilla: `{Dominio}ResponseDto` (Wrapper individual)

```yaml
{Dominio}ResponseDto:
  allOf:
    - $ref: '#/components/schemas/SuccessResponse'
    - type: object
      properties:
        data:
          $ref: '#/components/schemas/{Dominio}Data'
```

### 18.6 Plantilla: `{Dominio}ListResponseDto` (Wrapper paginado)

```yaml
{Dominio}ListResponseDto:
  allOf:
    - $ref: '#/components/schemas/SuccessResponse'
    - type: object
      properties:
        data:
          type: object
          properties:
            content:
              type: array
              items:
                $ref: '#/components/schemas/{Dominio}Data'
            pagination:
              $ref: '#/components/schemas/PaginationMeta'
```

### 18.7 Schemas transversales reutilizables (NUNCA redefinir)

Estos schemas se definen **una vez** y se reusan en todo el contrato:

```yaml
# ── WRAPPER ESTÁNDAR ──
SuccessResponse:
  type: object
  required: [isSuccess, status, message, timestamp]
  properties:
    isSuccess:
      type: boolean
      example: true
    status:
      type: string
      example: "OK"
    message:
      type: string
      example: "Operación exitosa"
    timestamp:
      type: string
      format: date-time
      example: "2026-04-09T10:30:00Z"

ErrorResponse:
  type: object
  properties:
    code:
      type: string
      example: "ERR-400"
    message:
      type: string
      example: "Error en la solicitud"
    timestamp:
      type: string
      format: date-time
      example: "2026-04-09T10:30:00Z"
    errors:
      type: array
      items:
        type: string
      example: ["Campo requerido faltante", "Formato inválido"]

PaginationMeta:
  type: object
  properties:
    totalElements:
      type: integer
      format: int64
      example: 50
    totalPages:
      type: integer
      example: 5
    currentPage:
      type: integer
      example: 0
    pageSize:
      type: integer
      example: 10

DocumentValidationResultDto:
  type: object
  description: "Resultado de validación interna de un recurso"
  properties:
    valid:
      type: boolean
      description: "true si pasó todas las validaciones"
      example: true
    errores:
      type: array
      description: "Errores que bloquean la operación"
      items:
        type: string
      example: []
    advertencias:
      type: array
      description: "Advertencias que no bloquean pero recomiendan corrección"
      items:
        type: string
      example: []
```

---

## 19. Estándar de Entidades JPA

### 19.1 Plantilla de entity para un dominio maestro

```java
@Entity
@Table(name = "{dominio_plural}")                          // ← snake_case, plural
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class {Dominio}Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id")
    private String companyId;

    // ── Campos de negocio ──
    @Column(name = "{campo_snake}", length = 300)
    private String {campoJava};

    @Column(name = "{campo_numerico}", precision = 12, scale = 2)
    private BigDecimal {campoNumerico};

    @Column(name = "{campo_fecha}")
    private LocalDate {campoFecha};

    @Column(name = "{campo_enum}", length = 50)
    private String {campoEnum};                             // ← String, no Java enum

    // ── Campos embebidos como JSON ──
    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalleJson;                             // ← List<> serializado a JSON

    // ── Campos de estado ──
    @Column(name = "estado", length = 30)
    private String estado;

    @Lob
    @Column(name = "errores_validacion", columnDefinition = "TEXT")
    private String erroresValidacion;                       // ← JSON string de errores

    // ── Campos de integración externa (nullable) ──
    @Column(name = "hash_cdr")
    private String hashCdr;

    @Column(name = "codigo_respuesta_externo")
    private String codigoRespuestaExterno;

    // ── Campos de archivo ──
    @Lob
    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;

    @Column(name = "xml_path")
    private String xmlPath;

    // ── Auditoría ──
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
```

**Reglas de Entity:**
| Regla | Detalle |
|-------|---------|
| Tabla | `snake_case`, plural: `facturas`, `notas_credito`, `clientes` |
| ID | `String` + `GenerationType.UUID` — siempre |
| Columnas | `@Column(name = "snake_case")` — siempre explícito |
| Enums | Persistidos como `String` (no `@Enumerated`) para flexibilidad |
| Listas | Serializadas como JSON `TEXT` (`detalleJson`, no tabla join) |
| Lombok | `@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder` — siempre los 5 |
| BigDecimal | Para montos monetarios (nunca `double`/`float`) |
| LocalDate | Para fechas sin hora |
| LocalDateTime | Para timestamps de auditoría |
| Sin `@PrePersist` | La fecha de creación se setea en el ServiceImpl |

### 19.2 Mapping Entity ↔ DTO — Convención de nombres

| Entity field | DTO field | Conversión |
|-------------|-----------|------------|
| `detalleJson` (String) | `detalle` (List<DetalleItem>) | JSON serialize/deserialize en Mapper |
| `cuotasJson` (String) | `cuotas` (List<CuotaCredito>) | JSON serialize/deserialize en Mapper |
| `erroresValidacion` (String) | `erroresValidacion` (List<String>) | JSON serialize/deserialize en Mapper |
| `estado` (String) | `estado` (String/enum) | Directo |
| `fechaEmision` (LocalDate) | `fechaEmision` (Date) | MapStruct auto-convert |
| `xmlContent` (String) | NO se mapea al DTO de lista | Excluido por performance |

---

## 20. Estándar de `operationId` (Nomenclatura)

El `operationId` del contrato genera el nombre del método en la interfaz Java. Seguir estas convenciones:

### 20.1 Tabla de operationIds estándar

| Acción | operationId | Ejemplo generado en Java |
|--------|-------------|--------------------------|
| Crear | `crear{Dominio}` / `registrar{Dominio}` | `crearProducto()`, `registrarEmpresa()` |
| Emitir (crear + procesar) | `emitir{Dominio}` | `emitirFactura()` |
| Listar (paginado) | `listar{DominioPlural}` | `listarFacturas()`, `listarClientes` |
| Buscar (con filtro principal) | `buscar{DominioPlural}` | `buscarProductos()` |
| Obtener por ID | `obtener{Dominio}` | `obtenerFactura()` |
| Actualizar | `actualizar{Dominio}` | `actualizarCliente()` |
| Actualizar borrador | `actualizar{Dominio}Borrador` | `actualizarFacturaBorrador()` |
| Eliminar | `eliminar{Dominio}` | `eliminarProducto()` |
| Eliminar borrador | `eliminar{Dominio}Borrador` | `eliminarBoletaBorrador()` |
| Anular | `anular{Dominio}` | `anularFactura()` |
| Validar | `validar{Dominio}` | `validarFactura()` |
| Emitir borrador | `emitir{Dominio}Borrador` | `emitirFacturaBorrador()` |
| Descargar archivo | `descargar{Dominio}{Formato}` | `descargarFacturaXml()` |
| Subir archivo | `subir{TipoArchivo}` | `subirCertificado()`, `subirLogo()` |
| Consultar estado | `consultarEstado{Dominio}` | `consultarEstadoResumen()` |

**Reglas:**
- Siempre en **español** (el contrato y el proyecto están en español).
- Siempre **camelCase**.
- Verbo primero: `crear`, `listar`, `obtener`, `actualizar`, `eliminar`, `anular`, `validar`, `emitir`, `descargar`, `subir`, `consultar`.
- Nunca mezclar idiomas: no `getFactura`, no `deleteCliente`.

---

## 21. Estándar de ejemplos en el contrato

### 21.1 Regla general

**Todo campo de todo schema DEBE tener `example`**. No hay excepciones. Esto garantiza que:
1. La documentación Swagger UI es navegable.
2. Los consumidores tienen valores de referencia.
3. El codegen puede generar tests automáticos.

### 21.2 Convenciones de valores de ejemplo

| Tipo de campo | Convención de ejemplo |
|---------------|----------------------|
| ID de recurso | `"{prefijo}_{uuid_corto}"` → `"fac_123456"`, `"cli_001"`, `"cmp_20601234567"` |
| companyId | `"cmp_20601234567"` |
| RUC | `"20601234567"` |
| DNI | `"45678901"` |
| Serie | `"F001"`, `"B001"`, `"FC01"`, `"FD01"` |
| Número (correlativo) | `1` |
| Monto monetario | Valor realista: `1000.00`, `180.00`, `1180.00` |
| Porcentaje | `18.00` (IGV estándar) |
| Moneda | `"PEN"` (default) |
| Fecha | `"2026-04-09"` |
| Timestamp | `"2026-04-09T10:30:00Z"` |
| Email | `"contacto@empresa.com"` |
| Teléfono | `"5112345678"` |
| Estado entidad | `"ACTIVO"` |
| Estado documento | `"BORRADOR"` o el más frecuente |
| Dirección | `"Av. Principal 123, San Isidro"` |
| Ubigeo | `"150101"` (Lima) |
| Boolean | `true` (el valor por defecto) |
| Enum | El primer valor del enum |
| Array vacío | `[]` |
| Leyenda montos | `"SON MIL CIENTO OCHENTA Y 00/100 SOLES"` |

### 21.3 Prefijos estándar de ID

| Dominio | Prefijo | Ejemplo |
|---------|---------|---------|
| Company | `cmp_` | `"cmp_20601234567"` |
| Client | `cli_` | `"cli_123456"` |
| Factura | `fac_` | `"fac_123456"` |
| Boleta | `bol_` | `"bol_123456"` |
| Nota Crédito | `nc_` | `"nc_123456"` |
| Nota Débito | `nd_` | `"nd_123456"` |
| Product | `prd_` | `"prd_123456"` |
| Employee | `emp_` | `"emp_123456"` |
| Sede | `sede-` | `"sede-001"` |
| Libro Electrónico | `lib_` | `"lib_123456"` |
| Resumen Diario | `rd-` | `"rd-001"` |
| Notification | `notif-` | `"notif-001"` |
| Tipo Cambio | `tc-` | `"tc-001"` |

> **Nota:** Los prefijos de ID son convenciones de documentación. En la implementación real, los UUIDs son generados por JPA sin prefijo. Los prefijos ayudan a identificar rápidamente el tipo de recurso en logs, debug y documentación.

### 21.4 Ejemplo de response completo con wrapper

```yaml
# ── Respuesta individual exitosa ──
example:
  isSuccess: true
  status: "OK"
  message: "Operación exitosa"
  timestamp: "2026-04-09T10:30:00Z"
  data:
    id: "{prefijo}_123456"
    companyId: "cmp_20601234567"
    # ... campos del dominio ...
    estado: "ACTIVO"
    fechaCreacion: "2026-04-09T10:30:00Z"

# ── Respuesta paginada exitosa ──
example:
  isSuccess: true
  status: "OK"
  message: "Operación exitosa"
  timestamp: "2026-04-09T10:30:00Z"
  data:
    content:
      - id: "{prefijo}_001"
        nombre: "Ejemplo 1"
        estado: "ACTIVO"
      - id: "{prefijo}_002"
        nombre: "Ejemplo 2"
        estado: "ACTIVO"
    pagination:
      totalElements: 50
      totalPages: 5
      currentPage: 0
      pageSize: 10

# ── Respuesta de error ──
example:
  code: "ERR-400"
  message: "Error de validación en los campos enviados"
  timestamp: "2026-04-09T10:30:00Z"
  errors:
    - "campo1: no puede ser nulo"
    - "campo2: formato inválido"

# ── Respuesta de validación ──
example:
  valid: false
  errores:
    - "Serie inválida: debe empezar con F para facturas"
    - "Total (1180.00) no coincide con gravadas (1000.00) + IGV (100.00)"
  advertencias:
    - "Tipo de cambio no informado para moneda USD"
    - "Leyenda no proporcionada — se generará automáticamente"
```

---

## 22. Checklist para agregar un nuevo endpoint

### 22.1 Paso a paso

```
□ 1. Identificar el TIPO de endpoint (sección 16)
□ 2. Copiar la plantilla YAML correspondiente (sección 17)
□ 3. Reemplazar todos los {placeholders} con valores del dominio
□ 4. Agregar `example` a TODOS los campos de TODOS los schemas
□ 5. Verificar que existen los schemas referenciados en components:
│     □ {Dominio}CreateDto (si POST)
│     □ {Dominio}UpdateDto (si PUT)
│     □ {Dominio}Data
│     □ {Dominio}ResponseDto (wrapper individual)
│     □ {Dominio}ListResponseDto (wrapper paginado, si tiene GET lista)
□ 6. Verificar que {Dominio}ResponseDto usa `allOf` con SuccessResponse
□ 7. Verificar que {Dominio}ListResponseDto tiene `content[]` + `pagination`
□ 8. Agregar examples en los responses del endpoint (200, 400, 404)
□ 9. Verificar operationId en español y camelCase (sección 20)
□ 10. Verificar que el tag existe en la sección `tags:` global
□ 11. Compilar: mvnw compile  (verifica que OpenAPI Generator procesa sin errores)
□ 12. Crear el controller Java que implementa la interfaz generada
□ 13. Crear los archivos del dominio (sección 10 de PARTE I)
```

### 22.2 Ejemplo completo: Agregar dominio `{recurso}` desde cero

**1. Agregar tag:**
```yaml
tags:
  # ...existing tags...
  - name: {recurso}
    description: "{Descripción del dominio}"
```

**2. Agregar paths (CRUD completo):**
```yaml
# ==================== {RECURSO} ====================
/{recurso}:
  post:
    summary: "Crear {recurso}"
    operationId: crear{Recurso}
    tags: [{recurso}]
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/{Recurso}CreateDto'
          example:
            companyId: "cmp_20601234567"
            nombre: "Ejemplo"
    responses:
      "200":
        description: "{Recurso} creado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Recurso}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{pre}_001"
                companyId: "cmp_20601234567"
                nombre: "Ejemplo"
                estado: "ACTIVO"
                fechaCreacion: "2026-04-09T10:30:00Z"
      "400":
        description: "Error de validación"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
            example:
              code: "ERR-400"
              message: "Error de validación"
              timestamp: "2026-04-09T10:30:00Z"
              errors: ["nombre: es requerido"]
      "500":
        description: "Error interno"

  get:
    summary: "Listar {recursos}"
    operationId: listar{Recursos}
    tags: [{recurso}]
    parameters:
      - name: companyId
        in: query
        required: true
        schema:
          type: string
        example: "cmp_20601234567"
      - name: estado
        in: query
        schema:
          type: string
          enum: [ACTIVO, INACTIVO]
        example: "ACTIVO"
      - name: page
        in: query
        schema:
          type: integer
          default: 0
        example: 0
      - name: size
        in: query
        schema:
          type: integer
          default: 10
        example: 10
    responses:
      "200":
        description: "Lista de {recursos}"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Recurso}ListResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                content:
                  - id: "{pre}_001"
                    nombre: "Ejemplo 1"
                    estado: "ACTIVO"
                  - id: "{pre}_002"
                    nombre: "Ejemplo 2"
                    estado: "ACTIVO"
                pagination:
                  totalElements: 2
                  totalPages: 1
                  currentPage: 0
                  pageSize: 10
      "401":
        description: "Unauthorized"

  put:
    summary: "Actualizar {recurso}"
    operationId: actualizar{Recurso}
    tags: [{recurso}]
    parameters:
      - name: {recurso}Id
        in: query
        required: true
        schema:
          type: string
        example: "{pre}_001"
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/{Recurso}UpdateDto'
          example:
            nombre: "Nuevo nombre"
    responses:
      "200":
        description: "{Recurso} actualizado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Recurso}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{pre}_001"
                nombre: "Nuevo nombre"
                estado: "ACTIVO"
      "404":
        description: "{Recurso} no encontrado"

  delete:
    summary: "Eliminar {recurso}"
    operationId: eliminar{Recurso}
    tags: [{recurso}]
    parameters:
      - name: {recurso}Id
        in: query
        required: true
        schema:
          type: string
        example: "{pre}_001"
    responses:
      "200":
        description: "{Recurso} eliminado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SuccessResponse'
            example:
              isSuccess: true
              status: "OK"
              message: "{Recurso} eliminado exitosamente"
              timestamp: "2026-04-09T10:30:00Z"
      "404":
        description: "{Recurso} no encontrado"

/{recurso}/{{recurso}Id}:
  get:
    summary: "Obtener {recurso} por ID"
    operationId: obtener{Recurso}
    tags: [{recurso}]
    parameters:
      - name: {recurso}Id
        in: path
        required: true
        schema:
          type: string
        example: "{pre}_001"
    responses:
      "200":
        description: "{Recurso} encontrado"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/{Recurso}ResponseDto'
            example:
              isSuccess: true
              status: "OK"
              message: "Operación exitosa"
              timestamp: "2026-04-09T10:30:00Z"
              data:
                id: "{pre}_001"
                companyId: "cmp_20601234567"
                nombre: "Ejemplo"
                estado: "ACTIVO"
                fechaCreacion: "2026-04-09T10:30:00Z"
      "404":
        description: "{Recurso} no encontrado"
```

**3. Agregar schemas:**
```yaml
components:
  schemas:
    # ── Request DTOs ──
    {Recurso}CreateDto:
      type: object
      required: [companyId, nombre]
      properties:
        companyId:
          type: string
          minLength: 1
          example: "cmp_20601234567"
        nombre:
          type: string
          minLength: 1
          maxLength: 300
          example: "Ejemplo"
        descripcion:
          type: string
          example: "Descripción opcional"

    {Recurso}UpdateDto:
      type: object
      properties:
        nombre:
          type: string
          example: "Nuevo nombre"
        descripcion:
          type: string
          example: "Nueva descripción"

    # ── Core Data ──
    {Recurso}Data:
      type: object
      properties:
        id:
          type: string
          example: "{pre}_001"
        companyId:
          type: string
          example: "cmp_20601234567"
        nombre:
          type: string
          example: "Ejemplo"
        descripcion:
          type: string
          example: "Descripción"
        estado:
          type: string
          enum: [ACTIVO, INACTIVO]
          example: "ACTIVO"
        fechaCreacion:
          type: string
          format: date-time
          example: "2026-04-09T10:30:00Z"

    # ── Response Wrappers ──
    {Recurso}ResponseDto:
      allOf:
        - $ref: '#/components/schemas/SuccessResponse'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/{Recurso}Data'

    {Recurso}ListResponseDto:
      allOf:
        - $ref: '#/components/schemas/SuccessResponse'
        - type: object
          properties:
            data:
              type: object
              properties:
                content:
                  type: array
                  items:
                    $ref: '#/components/schemas/{Recurso}Data'
                pagination:
                  $ref: '#/components/schemas/PaginationMeta'
```

**4. Crear archivos Java:**
```
□ {recurso}/openapi/{Recurso}OpenApi.java
□ {recurso}/service/{Recurso}Service.java
□ {recurso}/service/impl/{Recurso}ServiceImpl.java
□ {recurso}/mapper/{Recurso}Mapper.java
□ {recurso}/persistence/{Recurso}Entity.java
□ {recurso}/persistence/{Recurso}Repository.java
```

**5. Compilar y verificar:**
```bash
.\mvnw.cmd compile    # OpenAPI Generator + MapStruct + Lombok
.\mvnw.cmd test       # Verificar que no se rompen tests existentes
```

---

