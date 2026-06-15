# 🔐 FASE B — SEGURIDAD + OAuth2 + JWT (M6)

> **Módulo:** Security  
> **Paquete:** `com.etlions.ms_bs_etia.security`  
> **Nombre UI:** —  (transversal, no es módulo visible)  
> **Endpoint base:** `/api/v1/auth`  
> **Prioridad:** 🔴 CRÍTICA — Sin seguridad no hay producto  
> **Dependencias:** FASE A (Core)  
> **Puede paralelizarse con:** ❌ NINGUNA — B es prerequisito de C-M. Completar B ANTES de iniciar las demás fases.  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Implementar autenticación híbrida (nativo + Google OAuth2), autorización por roles y planes, aislamiento total por `company_id`, y gestión de sesión con JWT. El usuario admin de ETLIONS nunca debe quedar bloqueado.

---

## 📋 Tareas

### TB-01 — Dependencias Maven

Agregar a `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

---

### TB-02 — Modelo de datos de seguridad

**Tablas nuevas:**

```sql
-- USUARIOS
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),          -- NULL si es OAuth2-only
    nombre VARCHAR(200),
    apellido VARCHAR(200),
    provider VARCHAR(20) DEFAULT 'LOCAL', -- LOCAL, GOOGLE
    provider_id VARCHAR(255),            -- Google sub ID
    avatar_url VARCHAR(500),
    tipo_usuario VARCHAR(20) NOT NULL,   -- ADMIN, NATURAL, EMPRESA
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    ultimo_login TIMESTAMP,
    ultima_ruta VARCHAR(500),            -- Para "continuar donde se quedó"
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

-- PLANES
CREATE TABLE plans (
    id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,  -- FREE, PRO, BUSINESS
    descripcion TEXT,
    precio_mensual DECIMAL(10,2),
    modulos_incluidos TEXT,              -- JSON array de módulos
    max_documentos_mes INTEGER,
    max_empleados INTEGER,
    almacenamiento VARCHAR(50),          -- COMPARTIDO, DEDICADO, PROPIO
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP
);

-- RELACIÓN USUARIO ↔ EMPRESA ↔ ROL
CREATE TABLE user_company_roles (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    company_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,           -- ADMIN_ETIA, OWNER, MANAGER, EMPLOYEE, VIEWER
    plan_id VARCHAR(255),
    modulos_extra TEXT,                  -- JSON array de módulos adicionales
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    fecha_asignacion TIMESTAMP,
    UNIQUE(user_id, company_id)
);
```

**Entidades JPA:**
- `UserEntity` en `security/persistence/`
- `PlanEntity` en `security/persistence/`
- `UserCompanyRoleEntity` en `security/persistence/`

---

### TB-03 — Tipos de usuario y roles

| Tipo Usuario | Descripción | Roles posibles |
|-------------|-------------|----------------|
| `ADMIN` | Administrador ETLIONS | `ADMIN_ETIA` (acceso total + dashboard admin) |
| `NATURAL` | Persona natural (sin RUC) | `OWNER` de su perfil, plan FREE por defecto |
| `EMPRESA` | Empresa con RUC | `OWNER`, `MANAGER`, `EMPLOYEE`, `VIEWER` |

| Plan | Precio sugerido | Módulos incluidos | Almacenamiento |
|------|----------------|-------------------|----------------|
| **FREE** | S/ 0 | Recibos por honorarios, perfil | Compartido |
| **STARTER** | S/ 29.90/mes | FREE + Facturas + Boletas + NC/ND + Clientes + Productos | Compartido |
| **PRO** | S/ 79.90/mes | STARTER + POS Pro + Alpha Campaigns + Radar + Supply Chain + Reportes | Compartido |
| **BUSINESS** | Consultar | PRO + Overlord + Deal Flow + GeoInsight + Soporte dedicado | Dedicado/Propio |

---

### TB-04 — Endpoints de autenticación

**Contrato OpenAPI — tag `auth`:**

```yaml
/auth/register:
  post:
    summary: "Registrar usuario nuevo"
    operationId: registrarUsuario
    tags: [auth]
    requestBody:
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/RegisterDto'
    responses:
      "200": { description: "Usuario registrado" }
      "400": { description: "Email ya existe / datos inválidos" }

/auth/login:
  post:
    summary: "Login nativo (email + password)"
    operationId: loginNativo
    tags: [auth]
    requestBody:
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/LoginDto'
    responses:
      "200":
        description: "Login exitoso"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AuthResponseDto'

/auth/oauth2/google:
  post:
    summary: "Login con Google OAuth2"
    operationId: loginGoogle
    tags: [auth]

/auth/refresh:
  post:
    summary: "Renovar JWT token"
    operationId: refreshToken
    tags: [auth]

/auth/me:
  get:
    summary: "Obtener usuario actual + empresa + plan"
    operationId: obtenerUsuarioActual
    tags: [auth]
```

**Schemas:**

```yaml
RegisterDto:
  type: object
  required: [email, password, tipoUsuario]
  properties:
    email: { type: string, format: email, example: "usuario@email.com" }
    password: { type: string, minLength: 8, example: "MiPassword123!" }
    nombre: { type: string, example: "Álvaro" }
    apellido: { type: string, example: "Claver Herbozo" }
    tipoUsuario: { type: string, enum: [NATURAL, EMPRESA], example: "EMPRESA" }
    ruc: { type: string, description: "Solo si tipoUsuario=EMPRESA", example: "20601234567" }
    planId: { type: string, example: "plan-free" }

LoginDto:
  type: object
  required: [email, password]
  properties:
    email: { type: string, example: "claveralvaro@gmail.com" }
    password: { type: string, example: "Etia2025!" }

AuthResponseDto:
  type: object
  properties:
    accessToken: { type: string }
    refreshToken: { type: string }
    expiresIn: { type: integer, example: 3600 }
    user:
      $ref: '#/components/schemas/UserData'
    company:
      $ref: '#/components/schemas/CompanyData'
    plan:
      $ref: '#/components/schemas/PlanData'
    modulosActivos: { type: array, items: { type: string }, example: ["facturacion","pos","campaigns"] }
```

---

### TB-05 — JWT Service + Security Filter

**Paquete:** `com.etlions.ms_bs_etia.security`

```
security/
├── config/
│   ├── SecurityConfig.java              ← @Configuration Spring Security
│   └── JwtProperties.java              ← secret, expiration, etc.
├── jwt/
│   ├── JwtService.java                 ← Generar / validar JWT
│   ├── JwtAuthenticationFilter.java    ← OncePerRequestFilter
│   └── CompanyIsolationFilter.java     ← Inyecta companyId + valida ownership
├── oauth2/
│   ├── GoogleOAuth2Service.java        ← Verificar token de Google
│   └── OAuth2UserMapper.java           ← Mapear Google user → UserEntity
├── openapi/
│   └── AuthOpenApi.java                ← Controller implementa AuthApi
├── service/
│   ├── AuthService.java                ← Interface
│   └── impl/AuthServiceImpl.java       ← Register, login, refresh
├── mapper/
│   └── UserMapper.java
├── persistence/
│   ├── UserEntity.java
│   ├── UserRepository.java
│   ├── PlanEntity.java
│   ├── PlanRepository.java
│   ├── UserCompanyRoleEntity.java
│   └── UserCompanyRoleRepository.java
└── exception/
    ├── AuthenticationException.java     ← 401
    └── AuthorizationException.java      ← 403
```

---

### TB-06 — CompanyIsolationFilter (CRUCIAL)

```java
@Component
@Order(2) // Después de JwtAuthenticationFilter
public class CompanyIsolationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(request, response, chain) {
        // 1. Extraer companyId del JWT claims
        String tokenCompanyId = SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal().getCompanyId();
        
        // 2. Extraer companyId del request (query param, path, body)
        String requestCompanyId = extractCompanyIdFromRequest(request);
        
        // 3. VALIDAR: token.companyId == request.companyId
        if (requestCompanyId != null && !requestCompanyId.equals(tokenCompanyId)) {
            response.sendError(403, "Acceso denegado: no perteneces a esta empresa");
            return;
        }
        
        // 4. ADMIN_ETIA puede acceder a cualquier empresa
        if (hasRole("ADMIN_ETIA")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 5. Inyectar companyId en request attribute para uso en services
        request.setAttribute("currentCompanyId", tokenCompanyId);
        chain.doFilter(request, response);
    }
}
```

**Regla de oro:** Si `token.companyId ≠ request.companyId` → **403 PROHIBIDO**. Sin excepciones. Excepto `ADMIN_ETIA`.

---

### TB-07 — Selección de plan obligatoria

**Flujo de registro:**
1. Usuario se registra (email + password o Google)
2. Se le muestra pantalla de selección de plan (FREE / STARTER / PRO / BUSINESS)
3. **Sin plan seleccionado → NO puede usar el sistema** (redirect a `/seleccionar-plan`)
4. Plan FREE se asigna inmediatamente
5. BUSINESS requiere agendar cita (formulario → genera incidencia en admin)

**Frontend:**
- `localStorage.setItem('etia_plan', planId)` + cookie httpOnly con JWT
- `localStorage.setItem('etia_last_route', window.location.pathname)` → al volver, redirect a última ruta
- Cada vista verifica módulos del plan antes de renderizar

---

### TB-08 — Data de prueba — Usuario Admin ETLIONS

```sql
-- Usuario Admin ETLIONS
INSERT INTO users (id, email, username, password_hash, nombre, apellido, provider, tipo_usuario, estado, fecha_creacion)
VALUES ('usr-admin-001', 'claveralvaro@gmail.com', 'claver-herbozo',
    '$2a$10$...hash_de_Etia2025!...', 'Álvaro', 'Claver Herbozo',
    'LOCAL', 'ADMIN', 'ACTIVO', CURRENT_TIMESTAMP);

-- Plan datos
INSERT INTO plans (id, nombre, descripcion, precio_mensual, modulos_incluidos, max_documentos_mes, almacenamiento, estado)
VALUES
('plan-free', 'FREE', 'Recibos por honorarios básico', 0.00, '["recibos"]', 10, 'COMPARTIDO', 'ACTIVO'),
('plan-starter', 'STARTER', 'Facturación electrónica completa', 29.90, '["facturacion","clientes","productos","empleados","sedes"]', 100, 'COMPARTIDO', 'ACTIVO'),
('plan-pro', 'PRO', 'Suite completa con POS y Campañas', 79.90, '["facturacion","clientes","productos","empleados","sedes","pos","campaigns","radar","purchase","reporting"]', 500, 'COMPARTIDO', 'ACTIVO'),
('plan-business', 'BUSINESS', 'Solución enterprise personalizada', 199.90, '["ALL"]', -1, 'DEDICADO', 'ACTIVO');

-- Rol Admin → Empresa ETLIONS
INSERT INTO user_company_roles (id, user_id, company_id, role, plan_id, estado, fecha_asignacion)
VALUES ('ucr-001', 'usr-admin-001', 'cmp-demo-001', 'ADMIN_ETIA', 'plan-business', 'ACTIVO', CURRENT_TIMESTAMP);
```

---

### TB-09 — Vista Login/Register

**Thymeleaf nuevas vistas:**
- `templates/login.html` — Formulario email+password + botón "Continuar con Google"
- `templates/register.html` — Registro con selección de tipo (Natural/Empresa)
- `templates/seleccionar-plan.html` — Cards con planes y precios
- View controllers: `LoginViewController.java`, `RegisterViewController.java`, `PlanViewController.java`

**Rutas públicas (sin auth):** `/`, `/login`, `/register`, `/seleccionar-plan`, `/h2-console`  
**Rutas protegidas:** Todo lo demás (`/menu`, `/facturas`, `/api/v1/**`)

---

### TB-10 — Adaptar tests existentes al nuevo Security layer

**Problema:** Al agregar Spring Security, los 295 tests actuales fallarán porque todos los endpoints estarán protegidos.

**Solución:**

1. **Crear `TestSecurityConfig.java`** en `src/test/java/.../config/`:
   ```java
   @TestConfiguration
   @Profile("test")
   public class TestSecurityConfig {
       @Bean
       @Order(Ordered.HIGHEST_PRECEDENCE)
       public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
           http.csrf(c -> c.disable())
               .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
           return http.build();
       }
   }
   ```

2. **Agregar `@ActiveProfiles("test")`** a todas las test classes existentes.

3. **Crear `TestJwtHelper.java`** para tests que SÍ necesitan probar auth:
   ```java
   public class TestJwtHelper {
       public static String generateTestToken(String userId, String companyId, String role) {
           // Genera JWT válido para tests de integración
       }
   }
   ```

4. **Verificar:** `.\mvnw.cmd test` debe pasar 295+ tests tras agregar Security.

---

### TB-11 — Variables de entorno requeridas

**Todas las env vars que esta fase necesita:**

```bash
# JWT
JWT_SECRET=${JWT_SECRET:etia-dev-secret-key-min-256-bits-aaaaaa}
JWT_EXPIRATION_MS=${JWT_EXPIRATION_MS:3600000}
JWT_REFRESH_EXPIRATION_MS=${JWT_REFRESH_EXPIRATION_MS:604800000}

# Google OAuth2
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID:pendiente}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET:pendiente}

# Admin default
ADMIN_DEFAULT_PASSWORD=${ADMIN_DEFAULT_PASSWORD:Etia2025!}
```

**En `application.yaml`:**
```yaml
jwt:
  secret: ${JWT_SECRET:etia-dev-secret-key-min-256-bits-aaaaaa}
  expiration-ms: ${JWT_EXPIRATION_MS:3600000}
  refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}

spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:pendiente}
            client-secret: ${GOOGLE_CLIENT_SECRET:pendiente}
            scope: openid, profile, email
```

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/auth_and_company_isolation.feature`

```gherkin
Feature: Autenticación y aislamiento por empresa

  # === AUTENTICACIÓN NATIVA ===

  Scenario: Login nativo exitoso
    Given un usuario registrado "claveralvaro@gmail.com" con password "Etia2025!"
    When hago POST /auth/login con esas credenciales
    Then recibo accessToken y refreshToken
    And el token contiene companyId "cmp-demo-001"

  Scenario: Login con credenciales incorrectas
    When hago POST /auth/login con email "claveralvaro@gmail.com" password "WRONG"
    Then recibo error 401 "Credenciales inválidas"

  Scenario: Registro de usuario empresa
    When hago POST /auth/register con tipoUsuario "EMPRESA" y ruc "20999888777"
    Then se crea el usuario y la empresa automáticamente
    And debo seleccionar un plan antes de continuar

  Scenario: Registro con email ya existente
    Given un usuario registrado con email "claveralvaro@gmail.com"
    When intento registrarme de nuevo con ese email
    Then recibo error 400 "El email ya está registrado"

  Scenario: Registro con email OAuth2 existente intenta registro nativo
    Given un usuario registrado vía Google con email "user@gmail.com"
    When intento registrarme nativamente con ese email
    Then recibo error 400 "Este email ya está vinculado a una cuenta Google. Use 'Continuar con Google'."

  Scenario: Password no cumple requisitos
    When registro con password "abc"
    Then recibo error 400 "La contraseña debe tener mínimo 8 caracteres, 1 mayúscula, 1 número y 1 carácter especial"

  # === JWT TOKENS ===

  Scenario: Access token expirado intenta acceder
    Given un access token expirado
    When hago GET /api/v1/factura
    Then recibo error 401 "Token expirado"

  Scenario: Refresh token renueva access token
    Given un refresh token válido
    When hago POST /auth/refresh con el refresh token
    Then recibo un nuevo accessToken
    And el refreshToken anterior sigue siendo válido

  Scenario: Refresh token expirado
    Given un refresh token expirado (más de 7 días)
    When intento renovar
    Then recibo error 401 "Sesión expirada. Inicie sesión nuevamente."

  # === COMPANY ISOLATION ===

  Scenario: Aislamiento por empresa — acceso propio
    Given estoy autenticado como usuario de empresa "cmp-001"
    When hago GET /api/v1/factura?companyId=cmp-001
    Then recibo las facturas de mi empresa

  Scenario: Aislamiento por empresa — acceso DENEGADO a otra empresa
    Given estoy autenticado como usuario de empresa "cmp-001"
    When hago GET /api/v1/factura?companyId=cmp-002
    Then recibo error 403 "Acceso denegado: no perteneces a esta empresa"

  Scenario: Admin ETIA accede a cualquier empresa
    Given estoy autenticado como "claveralvaro@gmail.com" (ADMIN_ETIA)
    When hago GET /api/v1/factura?companyId=cmp-002
    Then recibo las facturas (acceso permitido para admin)

  # === PLANES Y MÓDULOS ===

  Scenario: Sin plan seleccionado no puede operar
    Given un usuario registrado sin plan
    When intento acceder a /menu
    Then soy redirigido a /seleccionar-plan

  Scenario: Módulo no incluido en plan
    Given un usuario con plan FREE
    When intento acceder a /facturas
    Then recibo error 403 "Módulo no incluido en tu plan"

  Scenario: Plan FREE solo perfil personal sin empresa
    Given un usuario con plan FREE
    When intento crear una empresa
    Then recibo error 403 "Para crear empresa necesitas plan STARTER o superior"

  # === GOOGLE OAUTH2 ===

  Scenario: Login con Google OAuth2 exitoso
    Given un token válido de Google para "user@gmail.com"
    When hago POST /auth/oauth2/google con ese token
    Then si el usuario no existe se crea automáticamente
    And recibo accessToken y refreshToken

  # === SEGURIDAD ===

  Scenario: Tests existentes siguen pasando con Security
    Given Spring Security habilitado
    When ejecuto .\mvnw.cmd test con @ActiveProfiles("test")
    Then los 295+ tests existentes pasan
```

---

## ✅ Criterios de Aceptación

- [ ] Login nativo funciona con `claveralvaro@gmail.com` / `Etia2025!`
- [ ] Google OAuth2 funciona (con client_id configurado)
- [ ] JWT contiene: userId, companyId, role, plan, modulosActivos
- [ ] CompanyIsolationFilter bloquea acceso cross-company
- [ ] Admin ETIA puede acceder a todo
- [ ] Sin plan → redirect a selección
- [ ] Última ruta guardada y restaurada al volver
- [ ] Compilación limpia + tests existentes pasan
- [ ] Rutas públicas accesibles sin auth

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `pom.xml` | 🔧 +spring-security, +oauth2-client, +jjwt |
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag auth, +endpoints, +schemas |
| `src/main/java/.../security/**` | 🆕 Paquete completo (~15 archivos) |
| `src/main/java/.../view/LoginViewController.java` | 🆕 Crear |
| `src/main/java/.../view/RegisterViewController.java` | 🆕 Crear |
| `src/main/java/.../view/PlanViewController.java` | 🆕 Crear |
| `src/main/resources/templates/login.html` | 🆕 Crear |
| `src/main/resources/templates/register.html` | 🆕 Crear |
| `src/main/resources/templates/seleccionar-plan.html` | 🆕 Crear |
| `src/main/resources/application.yaml` | 🔧 +jwt config, +oauth2 config |
| `src/main/resources/data.sql` | 🔧 +users, +plans, +user_company_roles |
| `src/test/resources/features/auth_and_company_isolation.feature` | 🆕 Crear |

---

## ⚠️ Notas críticas

- **ADMIN nunca se bloquea:** Siempre debe existir un usuario admin hardcodeado en data.sql que NO depende de OAuth para acceder.
- **Backward compatibility:** Los endpoints existentes siguen funcionando con el JWT. El `companyId` que antes era query param ahora se valida contra el JWT.
- **Frontend:** Agregar header `Authorization: Bearer {token}` a TODAS las llamadas fetch en `api-client.js`.
- **H2 Console:** Mantenerla accesible sin auth en dev (`/h2-console` en `SecurityConfig.permitAll()`).
- **Multi-empresa NO en v1:** Un usuario pertenece a UNA empresa. `user_company_roles` tiene `UNIQUE(user_id, company_id)` pero no se soporta switch entre empresas. JWT contiene `companyId` fijo. Multi-empresa es futuro.
- **Password reset NO en v1:** No hay endpoint `/auth/forgot-password`. Flujo alternativo: contactar admin por ticket soporte (FASE G). Se implementará en fase futura.
- **Account lockout NO en v1:** No hay bloqueo por intentos fallidos. Riesgo aceptado para MVP.
- **Empresa SUSPENDIDA:** El `CompanyIsolationFilter` verifica claim `companyStatus` del JWT. Si es `SUSPENDIDO` → 403. El claim se actualiza al renovar el refresh token. Al suspender empresa (FASE G), los tokens existentes siguen válidos hasta que expiren (1 hora máximo).
---

*Fase B — La muralla. Sin seguridad, esto es una invitación abierta al caos.*




