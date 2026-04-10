# 🔧 FASE A — CORE (M0) — Impedimento Técnico Base

> **Módulo:** Core / Base  
> **Paquete:** `com.etlions.ms_bs_etia` (transversal)  
> **Prioridad:** 🔴 CRÍTICA — Ejecutar PRIMERO, es requisito de todas las fases  
> **Dependencias:** FASE 00 completada  
> **Puede paralelizarse con:** Nada (es prerrequisito)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Establecer los cimientos técnicos que todas las fases posteriores necesitan: campo `website_url` en Company, estandarización de fechas `dd/MM/yyyy` (estándar peruano), extensión del módulo Employee con áreas, y creación de la entidad PointOfSale con su estado.

---

## 📋 Tareas

### TA-01 — Agregar `website_url` a Company

**Patrón:** OpenAPI → Codegen → Entity → Mapper → Service → data.sql

1. **Contrato OpenAPI** (`ms-bs-etia.yaml`):
   - Agregar `websiteUrl` a `CompanyCreateDto`, `CompanyUpdateDto`, `CompanyData`
   - Tipo: `string`, format: `uri`, ejemplo: `"https://www.etlions.com"`
   - Opcional (no en `required`)

2. **Entity** (`CompanyEntity.java`):
   ```java
   @Column(name = "website_url", length = 500)
   private String websiteUrl;
   ```

3. **Mapper** (`CompanyMapper.java`): Agregar mapping si no es automático por nombre.

4. **data.sql**: Agregar `website_url` a la empresa demo:
   ```sql
   UPDATE companies SET website_url = 'https://www.etlions.com' WHERE id = 'cmp-demo-001';
   ```

5. **Frontend**: Mostrar campo en Config Overlay (empresa) y en el wizard paso 2 (Emisor).

**Validaciones:** URL válida si se proporciona (regex o `@URL` en validación interna).

---

### TA-02 — Estandarizar formato de fechas dd/MM/yyyy (solo UI)

**Backend (Jackson):**

1. **NO crear `JacksonDateConfig.java`.** El backend mantiene ISO 8601 (`yyyy-MM-dd`) en todas las respuestas JSON. Esto:
   - No rompe los 295 tests existentes
   - Mantiene el contrato OpenAPI estándar
   - Facilita integración con cualquier frontend futuro
   - Es la práctica estándar de APIs REST

2. **Nota:** Las fechas en JSON serán `"2026-04-09"` (ISO). El formateo `dd/MM/yyyy` es responsabilidad exclusiva del frontend.

**Frontend (JavaScript):**

3. **Crear `date-utils.js`** en `static/js/core/`:
   ```javascript
   ETIA.formatDate = function(isoDate) {
       if (!isoDate) return '';
       const d = new Date(isoDate);
       return String(d.getDate()).padStart(2,'0') + '/' + 
              String(d.getMonth()+1).padStart(2,'0') + '/' + d.getFullYear();
   };
   
   ETIA.formatDateTime = function(isoDateTime) {
       if (!isoDateTime) return '';
       const d = new Date(isoDateTime);
       return ETIA.formatDate(isoDateTime) + ' ' +
              String(d.getHours()).padStart(2,'0') + ':' +
              String(d.getMinutes()).padStart(2,'0');
   };
   ```

4. **Buscar y reemplazar** todas las instancias de formateo de fecha en las 14+ vistas HTML y archivos JS. Usar `ETIA.formatDate()` en todos los renders de fecha.

**Validaciones:**
- [ ] Verificar que los PDFs generados usan dd/MM/yyyy (la lógica de PDF formatea internamente)
- [ ] Verificar que el wizard muestra fechas en dd/MM/yyyy via `ETIA.formatDate()`
- [ ] Verificar que las listas de documentos usan dd/MM/yyyy via `ETIA.formatDate()`
- [ ] Verificar que el XML UBL sigue usando ISO 8601 (yyyy-MM-dd) — sin cambios
- [ ] Verificar que las respuestas JSON mantienen ISO 8601 — sin cambios

---

### TA-03 — Extender módulo Employee con campo `area`

**Patrón:** OpenAPI → Codegen → Entity → Mapper

1. **Contrato OpenAPI** (`ms-bs-etia.yaml`):
   - Agregar a `EmployeeCreateDto`, `EmployeeUpdateDto`, `EmployeeData`:
     - `area`: string, ejemplo: `"Ventas"`, descripción: `"Área de la empresa"`
     - `sedeId`: string, nullable, ejemplo: `"sede-001"`, descripción: `"Sede asignada"`

2. **Entity** (`EmployeeEntity.java`):
   ```java
   @Column(name = "area", length = 100)
   private String area;
   
   @Column(name = "sede_id")
   private String sedeId;
   ```

3. **data.sql**: Actualizar empleados demo con áreas:
   ```sql
   -- Emp1: Contador → área Contabilidad, sede principal
   -- Emp2: Asistente → área Administración, sede principal
   -- Emp3: Cajero → área Ventas, sede sucursal
   ```

4. **Vista `employees.html`**: Agregar columna "Área" en la tabla.

---

### TA-04 — Crear entidad PointOfSale y endpoint de asignación

**Patrón completo:** OpenAPI → Controller → Service → Mapper → Entity → Repository

1. **Contrato OpenAPI** — Agregar tag `punto_venta` y endpoints:

   ```yaml
   # ==================== PUNTO DE VENTA ====================
   /punto_venta:
     post:
       summary: "Crear punto de venta"
       operationId: crearPuntoVenta
       tags: [punto_venta]
       # ... (seguir plantilla TIPO 1)
     get:
       summary: "Listar puntos de venta"
       operationId: listarPuntosVenta
       tags: [punto_venta]
       # ... (seguir plantilla TIPO 2)

   /punto_venta/{puntoVentaId}:
     get:
       summary: "Obtener punto de venta por ID"
       operationId: obtenerPuntoVenta
       tags: [punto_venta]

   /punto_venta/{puntoVentaId}/asignar-empleado:
     post:
       summary: "Asignar empleado al punto de venta"
       operationId: asignarEmpleadoPuntoVenta
       tags: [punto_venta]
       parameters:
         - name: puntoVentaId
           in: path
           required: true
           schema:
             type: string
       requestBody:
         required: true
         content:
           application/json:
             schema:
               $ref: '#/components/schemas/AsignarEmpleadoPosDto'
       responses:
         "200":
           description: "Empleado asignado"
   ```

2. **Schemas OpenAPI:**

   ```yaml
   PuntoVentaCreateDto:
     type: object
     required: [companyId, sedeId, nombre]
     properties:
       companyId: { type: string, example: "cmp_20601234567" }
       sedeId: { type: string, example: "sede-001" }
       nombre: { type: string, example: "Caja 1 - Principal" }
       descripcion: { type: string, example: "Punto de venta principal" }

   PuntoVentaData:
     type: object
     properties:
       id: { type: string, example: "pos_001" }
       companyId: { type: string }
       sedeId: { type: string }
       nombre: { type: string }
       status:
         type: string
         enum: [EN_CONSTRUCCION, ACTIVO, INACTIVO]
         default: EN_CONSTRUCCION
       empleadoAsignado: { $ref: '#/components/schemas/EmployeeData' }
       fechaCreacion: { type: string, format: date-time }

   AsignarEmpleadoPosDto:
     type: object
     required: [employeeId]
     properties:
       employeeId: { type: string, example: "emp_001" }
   ```

3. **Paquete Java:** `com.etlions.ms_bs_etia.punto_venta/`
   ```
   punto_venta/
   ├── openapi/PuntoVentaOpenApi.java
   ├── service/PuntoVentaService.java
   ├── service/impl/PuntoVentaServiceImpl.java
   ├── mapper/PuntoVentaMapper.java
   └── persistence/
       ├── PuntoVentaEntity.java
       └── PuntoVentaRepository.java
   ```

4. **Entity:**
   ```java
   @Entity
   @Table(name = "puntos_venta")
   @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
   public class PuntoVentaEntity {
       @Id @GeneratedValue(strategy = GenerationType.UUID)
       private String id;
       @Column(name = "company_id") private String companyId;
       @Column(name = "sede_id") private String sedeId;
       @Column(name = "nombre") private String nombre;
       @Column(name = "descripcion") private String descripcion;
       @Column(name = "status") private String status; // EN_CONSTRUCCION, ACTIVO, INACTIVO
       @Column(name = "employee_id") private String employeeId;
       @Column(name = "fecha_creacion") private LocalDateTime fechaCreacion;
   }
   ```

5. **Lógica de negocio:**
   - Al asignar empleado → status cambia a `EN_CONSTRUCCION`
   - Validar que el empleado pertenece a la misma empresa
   - Validar que el empleado no está ya asignado a otro POS activo
   - Para activar: `PUT /punto_venta/{id}` con status=ACTIVO (solo si tiene empleado)

6. **data.sql:**
   ```sql
   INSERT INTO puntos_venta (id, company_id, sede_id, nombre, status, fecha_creacion)
   VALUES ('pos-demo-001', 'cmp-demo-001', 'sede-001', 'Caja 1 - Principal', 'ACTIVO', CURRENT_TIMESTAMP);
   ```

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/core_dates_and_website.feature`

```gherkin
Feature: Core - Website URL y Fechas

  Scenario: Agregar website_url a empresa existente
    Given una empresa "ETLIONS S.A.C." sin website_url
    When actualizo la empresa con websiteUrl "https://www.etlions.com"
    Then la empresa tiene websiteUrl "https://www.etlions.com"

  Scenario: Rechazar URL inválida en website_url
    Given una empresa existente
    When actualizo la empresa con websiteUrl "no-es-una-url"
    Then recibo error 400 "Formato de URL inválido"

  Scenario: website_url es opcional
    Given una empresa sin website_url
    When actualizo otros campos sin enviar websiteUrl
    Then la empresa se actualiza correctamente con websiteUrl null

  Scenario: Formato de fecha ISO 8601 en respuesta JSON (sin cambios)
    Given una factura creada el "2026-04-09"
    When obtengo la factura por ID via JSON API
    Then la fechaEmision en JSON es "2026-04-09" (ISO 8601)

  Scenario: Frontend muestra fecha en dd/MM/yyyy
    Given una factura con fechaEmision "2026-04-09" en JSON
    When el frontend renderiza la fecha con ETIA.formatDate()
    Then se muestra "09/04/2026"

  Scenario: XML UBL mantiene formato ISO 8601
    Given una factura creada el "2026-04-09"
    When genero el XML UBL
    Then la fecha en el XML es "2026-04-09" (ISO 8601)

  Scenario: Crear punto de venta
    Given una sede "sede-001" de la empresa "cmp-demo-001"
    When creo un punto de venta "Caja 1" en esa sede
    Then el punto de venta se crea con status "EN_CONSTRUCCION"

  Scenario: Crear punto de venta sin sede rechazado
    Given una empresa "cmp-demo-001"
    When creo un punto de venta sin sedeId
    Then recibo error 400 "La sede es obligatoria"

  Scenario: Asignar empleado a punto de venta
    Given un punto de venta "pos-001" sin empleado
    And un empleado "emp-001" de la misma empresa
    When asigno el empleado al punto de venta
    Then el punto de venta tiene employeeId "emp-001"
    And el status es "EN_CONSTRUCCION"

  Scenario: No asignar empleado de otra empresa
    Given un punto de venta de empresa "cmp-001"
    And un empleado de empresa "cmp-002"
    When intento asignar el empleado al punto de venta
    Then recibo error 400 "El empleado no pertenece a esta empresa"

  Scenario: Activar POS sin empleado asignado rechazado
    Given un punto de venta sin empleado asignado
    When intento cambiar status a ACTIVO
    Then recibo error 400 "El POS requiere un empleado asignado para activarse"

  Scenario: No eliminar POS con ventas asociadas
    Given un punto de venta con ventas registradas
    When intento eliminar el POS
    Then recibo error 400 "No se puede eliminar un POS con ventas asociadas"

  Scenario: Empleado con campo area
    Given un empleado sin area asignada
    When actualizo el empleado con area "Ventas"
    Then el empleado tiene area "Ventas"

  Scenario: Validar existencia de sede al asignar a PuntoVenta
    Given un sedeId "sede-inexistente" que no existe
    When creo un punto de venta con ese sedeId
    Then recibo error 400 "La sede especificada no existe"
```

---

## ✅ Criterios de Aceptación

- [ ] `website_url` visible en Company CRUD y wizard
- [ ] Fechas en formato `dd/MM/yyyy` en TODA la capa frontend (vistas HTML + JS)
- [ ] JSON API mantiene ISO 8601 (`yyyy-MM-dd`) — sin cambios en backend
- [ ] XML UBL mantiene `yyyy-MM-dd` (ISO 8601)
- [ ] Employee tiene campo `area` y `sedeId`
- [ ] PuntoVenta CRUD completo con endpoint de asignación
- [ ] Validación de existencia de sede en PuntoVenta y Employee (misma empresa)
- [ ] POS no se puede activar sin empleado asignado
- [ ] POS con ventas no se puede eliminar
- [ ] Compilación limpia: `.\mvnw.cmd compile`
- [ ] Tests pasan: `.\mvnw.cmd test` (295 tests, sin afectación por fechas)
- [ ] data.sql actualizado con datos demo

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 Agregar PuntoVenta + campos Company/Employee |
| `src/main/java/.../company/persistence/CompanyEntity.java` | 🔧 +websiteUrl |
| `src/main/java/.../employee/persistence/EmployeeEntity.java` | 🔧 +area, +sedeId |
| `src/main/java/.../punto_venta/**` | 🆕 Paquete completo (6 archivos) |
| `src/main/resources/data.sql` | 🔧 +website_url, +areas, +puntos_venta |
| `src/main/resources/static/js/core/date-utils.js` | 🆕 Crear (ETIA.formatDate + ETIA.formatDateTime) |
| `src/main/resources/templates/employees.html` | 🔧 +columna Área |
| `src/test/resources/features/core_dates_and_website.feature` | 🆕 Crear |

---

## ⚠️ Notas de implementación

- **Jackson DateFormat:** NO customizar. Backend mantiene ISO 8601 en JSON. Solo el frontend formatea a `dd/MM/yyyy`.
- **No romper contrato ISO en OpenAPI:** El contrato usa `format: date` (ISO). Las respuestas JSON envían `yyyy-MM-dd`.
- **XML UBL permanece en ISO 8601:** Los XML para SUNAT SIEMPRE usan `yyyy-MM-dd`. Sin cambios.
- **PDFs:** La lógica de generación de PDF formatea internamente a `dd/MM/yyyy`. No depende de Jackson.
- **Backward compatibility:** Los 295 tests NO se afectan porque el backend NO cambia el formato de fechas.
- **PuntoVenta es prerequisito de FASE C (POS Pro):** Sin esta base, no hay donde construir el flujo de venta.
- **Validar existencia de sede:** Al crear PuntoVenta o asignar sedeId a Employee, validar que la sede existe en la BD y pertenece a la misma empresa.
- **Sede es entidad preexistente:** El paquete `sede/` ya existe en el codebase. No se crea tabla nueva.

---

*Fase A — Los cimientos. Sin esto, nada se sostiene.*






