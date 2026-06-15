# 🔐 SPRINT 3 — SEGURIDAD BÁSICA (JWT + Company Isolation)

> **Objetivo:** Que cada empresa solo vea su data  
> **Entregable:** Login funcional, JWT en cada request, aislamiento por empresa  
> **Dependencias Maven nuevas:** 4  
> **Archivos nuevos:** ~12  
> **Archivos modificados:** ~5

---

## 📦 DEPENDENCIAS MAVEN

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JJWT (JSON Web Token) -->
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

## 📋 PASOS EN ORDEN

### Paso 3.1 — Migración V3: tabla users

**Archivo:** `src/main/resources/db/migration/V3__add_users_table.sql`  
**Acción:** CREAR

```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(255),
    company_id VARCHAR(255),
    rol VARCHAR(50) DEFAULT 'USER',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Usuario demo (password: Etia2025! hasheado con BCrypt, cost=10)
INSERT INTO users (id, email, password_hash, nombre, company_id, rol, activo, fecha_creacion)
VALUES ('usr-admin-001', 'claveralvaro@gmail.com', 
        '$2a$10$EyHeiBYMOXLV4Y2XtwXbrOtZ8L8d//1cgbysiKvaiNB.IRoF9IiJa', 'Alvaro Claver', 
        'cmp-demo-001', 'ADMIN', TRUE, CURRENT_TIMESTAMP);
```

**Nota:** Hash BCrypt verificado — `$2a$10$` es intercambiable con `$2b$10$` en Spring Security.

---

### Paso 3.2 — UserEntity

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/persistence/UserEntity.java`  
**Acción:** CREAR

**Campos:** id, email, passwordHash, nombre, companyId, rol (ADMIN/USER), activo, fechaCreacion  
**Tabla:** `users`

---

### Paso 3.3 — UserRepository

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/persistence/UserRepository.java`  
**Acción:** CREAR

```java
Optional<UserEntity> findByEmail(String email);
boolean existsByEmail(String email);
```

---

### Paso 3.4 — JwtService

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/service/JwtService.java`  
**Acción:** CREAR

**Responsabilidad:** Generar y validar JWT

```
Métodos:
- generateToken(UserEntity user) → String
  Claims: sub=email, companyId, rol, iat, exp
  
- validateToken(String token) → boolean

- extractEmail(String token) → String

- extractCompanyId(String token) → String

- extractRol(String token) → String
```

**Configuración (de application.yaml):**
- `etia.jwt.secret` — clave secreta (mín 256 bits)
- `etia.jwt.expiration-hours` — TTL (default 24h)

---

### Paso 3.5 — JwtAuthFilter

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/filter/JwtAuthFilter.java`  
**Acción:** CREAR

**Extiende:** `OncePerRequestFilter`

```
Flujo:
1. Leer header "Authorization"
2. Si empieza con "Bearer " → extraer token
3. Validar con JwtService
4. Si válido → crear UsernamePasswordAuthenticationToken
   - Principal: email
   - Authorities: rol
   - Details: companyId (en un Map o custom object)
5. Setear en SecurityContextHolder
6. Si no válido o no existe → continuar sin autenticar (Spring Security rechazará)
```

---

### Paso 3.6 — SecurityConfig

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/config/SecurityConfig.java`  
**Acción:** CREAR

```
@Configuration @EnableWebSecurity
SecurityFilterChain:

Rutas PÚBLICAS (sin auth):
  - POST /api/v1/auth/login
  - POST /api/v1/auth/register
  - GET  /login
  - GET  /register
  - GET  /
  - GET  /css/**
  - GET  /js/**
  - GET  /favicon.*
  - GET  /h2-console/**  (solo dev)

Rutas PROTEGIDAS (todo lo demás):
  - /api/v1/** → JWT obligatorio
  - /menu, /facturas, /boletas, etc. → autenticado

Configuración:
  - sessionManagement: STATELESS
  - csrf: disabled (para API REST + MVP)
  - JwtAuthFilter antes de UsernamePasswordAuthenticationFilter
  - formLogin().loginPage("/login") → redirige vistas no autenticadas
  - PasswordEncoder: BCryptPasswordEncoder
  - frameOptions: sameOrigin (para H2 console)
```

---

### Paso 3.7 — AuthController

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/openapi/AuthController.java`  
**Acción:** CREAR

**Endpoints:**

```
POST /api/v1/auth/login
  Body: { email, password }
  Response: { isSuccess, data: { token, companyId, nombre, email, rol } }
  Lógica: Buscar user por email → verificar BCrypt → generar JWT

POST /api/v1/auth/register
  Body: { email, password, nombre }
  Response: { isSuccess, data: { token, companyId, userId } }
  Lógica: Crear user → asignar company (ver Sprint 4 onboarding)
  Para MVP: registro crea user SIN company (se asocia en onboarding)

GET /api/v1/auth/me
  Headers: Authorization: Bearer {token}
  Response: { isSuccess, data: { userId, email, nombre, companyId, rol } }
  Lógica: Extraer datos del JWT
```

---

### Paso 3.8 — CompanyIsolationInterceptor

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/filter/CompanyIsolationInterceptor.java`  
**Acción:** CREAR

**Registrado como:** `HandlerInterceptor` en `WebMvcConfigurer`  
**Aplica a:** `/api/v1/**` (excepto `/api/v1/auth/**`)

```
Flujo:
1. Obtener companyId del SecurityContext (del JWT, seteado por JwtAuthFilter)
2. Obtener companyId del request:
   - Query param: ?companyId=xxx
   - Path variable: revisar patrón de URL
3. Si request tiene companyId y != JWT companyId → 403 Forbidden
4. Si request NO tiene companyId → continuar (el service validará internamente)
```

**Excepción:** Rol ADMIN_ETIA bypasea validación (futuro — no para MVP)

---

### Paso 3.9 — Vistas login y register

**Archivos nuevos:**
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`

**Login.html:**
- Formulario: email + password + botón "Ingresar"
- JS: `ApiClient.login(email, pass)` → guarda token en localStorage → redirige a `/menu`
- Link: "¿No tienes cuenta? Regístrate"
- Estilo: CSS existente del design system

**Register.html:**
- Formulario: nombre + email + password + confirmar password
- JS: `ApiClient.register(nombre, email, pass)` → guarda token → redirige a onboarding (o `/menu`)
- Link: "¿Ya tienes cuenta? Inicia sesión"

**View Controller:**
- `src/main/java/com/etlions/ms_bs_etia/view/AuthViewController.java` — GET /login, GET /register

---

### Paso 3.10 — Modificar api-client.js

**Archivo:** `src/main/resources/static/js/api-client.js`  
**Acción:** MODIFICAR

**Cambios en los 4 helpers (líneas 23-50):**

```javascript
// Agregar función helper para obtener headers con auth
function _headers(contentType) {
    var h = {};
    if (contentType) h['Content-Type'] = contentType;
    var token = localStorage.getItem('etia_token');
    if (token) h['Authorization'] = 'Bearer ' + token;
    return h;
}

// Modificar _get
function _get(path) {
    var url = BASE + path;
    return fetch(url, { headers: _headers() })
        .then(function(r) { return _handleResponse(r, url); });
}

// Modificar _post 
function _post(path, body) {
    var url = BASE + path;
    return fetch(url, {
        method: 'POST',
        headers: _headers('application/json'),
        body: JSON.stringify(body)
    }).then(function(r) { return _handleResponse(r, url); });
}

// Igual para _put y _delete
```

**Agregar funciones de auth:**
```javascript
function login(email, password) { ... }   // POST /auth/login → guardar token
function register(data) { ... }            // POST /auth/register → guardar token
function logout() { ... }                  // Limpiar token → redirigir /login
function isAuthenticated() { ... }         // Verificar si hay token válido
function getCompanyIdFromToken() { ... }   // Decodificar JWT → companyId
```

**Modificar _handleResponse (línea 12-21):**
```javascript
// Si recibe 401 → redirigir a /login
if (r.status === 401) {
    localStorage.removeItem('etia_token');
    window.location.href = '/login';
    throw new Error('Sesión expirada');
}
```

---

### Paso 3.11 — Modificar app-state.js

**Archivo:** `src/main/resources/static/js/app-state.js`  
**Acción:** MODIFICAR

**En `ETIA.init()` :**
```javascript
// ANTES de cargar company:
if (!ApiClient.isAuthenticated()) {
    window.location.href = '/login';
    return;
}

// Obtener companyId del token (no de la primera company de la API)
ETIA.companyId = ApiClient.getCompanyIdFromToken();
```

**Agregar `ETIA.logout()`:**
```javascript
ETIA.logout = function() {
    ApiClient.logout();
};
```

---

### Paso 3.12 — Configurar JWT en application.yaml

**Archivo:** `src/main/resources/application.yaml`  
**Acción:** MODIFICAR — agregar bloque:

```yaml
etia:
  # ...storage existente...
  jwt:
    secret: ${JWT_SECRET:clave-secreta-dev-minimo-32-caracteres-aqui}
    expiration-hours: ${JWT_EXPIRATION:24}
```

---

### Paso 3.13 — Configurar tests

**Problema:** Al agregar Spring Security, los tests existentes podrían fallar con 401/403.

**Análisis de impacto real (7 test classes, ~295 métodos):**

| Test Class | Framework | ¿Impactado? | Acción |
|-----------|-----------|-------------|--------|
| `MsBsEtiaApplicationTests` | `@SpringBootTest` | **SÍ** — necesita JWT config | Agregar `application-test.yaml` con `etia.jwt.secret` |
| `DocFacturaServiceImplTest` | `@ExtendWith(MockitoExtension)` | NO | Ninguna |
| `DocBoletaServiceImplTest` | `@ExtendWith(MockitoExtension)` | NO | Ninguna |
| `ResumenDiarioServiceImplTest` | `@ExtendWith(MockitoExtension)` | NO | Ninguna |
| `LogoUtilTest` | `@ExtendWith(MockitoExtension)` | NO | Ninguna |
| `UblInvoiceXmlBuilderTest` | JUnit puro | NO | Ninguna |
| `SunatValidatorTest` | JUnit puro | NO | Ninguna |

**Resultado: 0 tests necesitan `@WithMockUser`. Solo 1 clase necesita `application-test.yaml`.**

**Solución:**

**Archivo:** `src/test/resources/application-test.yaml` (o modificar existente)
```yaml
etia:
  jwt:
    secret: test-secret-key-for-unit-tests-minimum-32-chars-here
    expiration-hours: 24
```

**Para tests de servicio (con @ExtendWith(MockitoExtension.class)):** No necesitan cambios — no usan Spring Security.

**Para `MsBsEtiaApplicationTests` (@SpringBootTest):** Solo necesita que el JWT secret esté en `application-test.yaml`. NO necesita `@WithMockUser` ya que no ejecuta requests HTTP.

**Archivo nuevo:** `src/test/java/com/etlions/ms_bs_etia/auth/TestSecurityConfig.java`
- Provee un `SecurityFilterChain` permisivo para el perfil test

---

## ⚠️ RIESGOS Y MITIGACIONES

| Riesgo | Probabilidad | Mitigación |
|--------|-------------|------------|
| Tests existentes fallan con 401 | **Baja** | De 7 test classes, 6 usan Mockito puro (0 impacto). Solo `MsBsEtiaApplicationTests` necesita `application-test.yaml` con JWT secret. **0 tests necesitan @WithMockUser** |
| MPA + JWT es antipatrón | Media | Las vistas HTML se cargan públicamente, el contenido real se obtiene por API (con JWT). El redirect a /login se hace con formLogin de Spring Security |
| CompanyIsolation en body params | Media | En MVP, solo validar query params y path variables. Body validation lo hacen los services |
| Token en localStorage vulnerable a XSS | Baja para MVP | Aceptable para MVP. En v2.0 migrar a HttpOnly cookie |

---

## ✅ CRITERIOS DE DONE

- [ ] Login funcional desde frontend (email + password → JWT → /menu)
- [ ] Register funcional (crea usuario en BD)
- [ ] API rechaza requests sin token → 401
- [ ] API rechaza requests con companyId diferente al JWT → 403
- [ ] Vistas redirigen a /login si no autenticado
- [ ] Token contiene: email, companyId, rol, exp
- [ ] Logout limpia token y redirige
- [ ] 295+ tests siguen pasando (adaptados)




