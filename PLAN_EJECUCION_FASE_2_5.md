# 🚀 PLAN DE EJECUCIÓN — FASE 2.5 MEJORAS UX + LOGO + VALIDACIÓN

> **Fecha:** 2026-04-08  
> **Estado actual:** 290 tests ✅ · BUILD SUCCESS · ✅ TODOS LOS PASOS COMPLETADOS  
> **Regla:** Cada paso termina con `mvn clean compile` + `mvn test` → 0 failures  
> **Nota:** ✅ Plan ejecutado completamente el 2026-04-08.

---

## TABLA DE CONTENIDOS

1. [PASO 1 — Bug Fix: Errores de validación muestran `[object Object]`](#paso-1)
2. [PASO 2 — Settings panel más espacioso (CSS/layout)](#paso-2)
3. [PASO 3 — Backend: Soporte SVG + campo `logoFormat` + validación dimensiones](#paso-3)
4. [PASO 4 — Backend: Utilitario `LogoUtil` (byte array, base64, resize, preview)](#paso-4)
5. [PASO 5 — Frontend: SVG + validación dimensiones + resize client-side en Settings](#paso-5)
6. [PASO 6 — Frontend: Logo upload en Welcome "Configura tu Empresa"](#paso-6)
7. [PASO 7 — Header: Breadcrumb con logo de empresa](#paso-7)
8. [Dependencias y orden](#dependencias)
9. [Resumen de archivos](#resumen-archivos)

---

## ✅ PASO 1 — Bug Fix: Errores de validación muestran `[object Object]` {#paso-1}
> **COMPLETADO** — 2026-04-08 · 278 tests ✅ · BUILD SUCCESS

### Problema

La API de validación retorna:
```json
{"valid": false, "errores": ["Moneda USD requiere tipo de cambio > 0"], "advertencias": []}
```

Pero el frontend muestra `[object Object]` en 2 lugares:
1. **Modal de preview** (`doc-preview.js` L127): `JSON.parse(doc.erroresValidacion)` — cuando `erroresValidacion` ya es un objeto (no string), `JSON.parse()` falla y el `catch` hace `errores = [doc.erroresValidacion]`, produciendo `[object Object]`.
2. **Toast de error** (`boletas.html` L256 y `facturas.html` L294): Solo muestra "⚠ Validación encontró 1 error(es)" sin listar los errores reales.

Además, la sección de acciones del toast (`etia-toast-actions` con botones 📸 y 📧) no es visible. Esto se debe a que `showError()` se llama sin `body` → no se renderiza el `<pre class="etia-toast-detail">`, y las `etia-toast-actions` están ahí pero visualmente se pierden porque no hay contenido de detalle que le dé contexto al usuario.

### Archivos a modificar

#### 1.1 `src/main/resources/static/js/doc-preview.js` (L125-128)

**Cambio:** Refactorizar la extracción de errores para manejar 3 formatos posibles:

```javascript
// ANTES (L127):
try { errores = JSON.parse(doc.erroresValidacion); } catch(e) { errores = [doc.erroresValidacion]; }

// DESPUÉS:
var raw = doc.erroresValidacion;
if (typeof raw === 'string') {
    try {
        var parsed = JSON.parse(raw);
        // Puede ser array directo ["err1"] o objeto {valid:false, errores:["err1"]}
        errores = Array.isArray(parsed) ? parsed : (parsed.errores || [String(raw)]);
    } catch(e) {
        errores = [raw];
    }
} else if (raw && typeof raw === 'object') {
    // Ya es objeto — extraer array errores
    errores = Array.isArray(raw) ? raw : (raw.errores || [JSON.stringify(raw)]);
} else if (raw) {
    errores = [String(raw)];
}
```

#### 1.2 `src/main/resources/templates/boletas.html` — función `quickValidar` (L248-264)

**Cambio:** Pasar los mensajes de error reales al toast via el parámetro `body`:

```javascript
// ANTES (L256):
if (ETIA.showError) ETIA.showError({ message: '⚠ Validación encontró ' + errCount + ' error(es). Revisa el documento.' });

// DESPUÉS:
if (ETIA.showError) {
    var errList = (result && result.errores) ? result.errores : [];
    ETIA.showError({
        message: '⚠ Validación encontró ' + errList.length + ' error(es). Revisa el documento.',
        body: errList.join('\n')
    });
}
```

#### 1.3 `src/main/resources/templates/facturas.html` — función `quickValidar` (L286-302)

**Cambio:** Idéntico al de boletas:

```javascript
// ANTES (L294):
if (ETIA.showError) ETIA.showError({ message: '⚠ Validación encontró ' + errCount + ' error(es). Revisa el documento.' });

// DESPUÉS:
if (ETIA.showError) {
    var errList = (result && result.errores) ? result.errores : [];
    ETIA.showError({
        message: '⚠ Validación encontró ' + errList.length + ' error(es). Revisa el documento.',
        body: errList.join('\n')
    });
}
```

#### 1.4 `src/main/resources/templates/boletas.html` — bloque `errorInfo` (L179-188)

**Verificar:** Este bloque YA parsea correctamente (`typeof === 'string' ? JSON.parse : directo`). Sin embargo, si `erroresValidacion` es un objeto `{valid, errores}` en vez de un array, `Array.isArray()` retornará false. Agregar:

```javascript
// DESPUÉS de L181:
if (!Array.isArray(errArr) && errArr && errArr.errores) errArr = errArr.errores;
```

#### 1.5 `src/main/resources/templates/facturas.html` — mismo patrón si existe bloque `errorInfo`

**Verificar:** facturas.html NO tiene bloque `errorInfo` inline (las facturas CON_ERROR no existían en el scope original). Si se agrega en el futuro, aplicar misma lógica.

### Verificación

```
mvn clean compile → BUILD SUCCESS
mvn test → 278 tests, 0 failures
```

**Test manual:**
- Crear boleta con moneda USD sin tipo de cambio → Validar → El modal debe mostrar "Moneda USD requiere tipo de cambio > 0"
- El toast debe mostrar el mensaje de error en la sección `<pre class="etia-toast-detail">`
- Los botones 📸 y 📧 deben ser visibles

---

## ✅ PASO 2 — Settings panel más espacioso (CSS/layout) {#paso-2}
> **COMPLETADO** — 2026-04-08 · 278 tests ✅ · BUILD SUCCESS

### Objetivo

Hacer que el panel de configuración se sienta significativamente más grande y respire. Sin cambios funcionales, SOLO CSS/layout.

### Archivos a modificar

#### 2.1 `src/main/resources/static/css/main.css` — Sección CONFIG OVERLAY (L999+)

**Cambios concretos:**

| Selector | Propiedad actual | Nuevo valor | Motivo |
|----------|-----------------|-------------|--------|
| `.cfg-card` | `width: 720px` | `width: 960px` | +33% ancho |
| `.cfg-card` | `max-height: 75dvh` | `max-height: 90dvh` | Más alto, menos scroll |
| `.cfg-sidebar` | `width: 200px; min-width: 200px` | `width: 240px; min-width: 240px` | Sidebar más amplio |
| `.cfg-sidebar` | `padding: 1.5rem 0` | `padding: 2rem 0` | Más respiro vertical |
| `.cfg-sidebar-item` | `padding: 0.75rem 1.2rem` | `padding: 0.9rem 1.5rem` | Items más espaciados |
| `.cfg-sidebar-item` | `font-size: 0.85rem` | `font-size: 0.88rem` | Ligeramente más legible |
| `.cfg-sidebar-item` | `gap: 0.6rem` | `gap: 0.8rem` | Más espacio icono↔texto |
| `.cfg-content` | `padding: 2rem` | `padding: 2.5rem 3rem` | Más padding horizontal |
| `.cfg-field` (agregar) | — | `margin-bottom: 1.5rem` | Campos más separados |
| `.cfg-row` (agregar) | — | `gap: 1.5rem` | Filas con más espacio |
| `.cfg-company-card` (agregar) | — | `padding: 1.5rem; gap: 1.2rem` | Card empresa más holgada |
| `.cfg-save-bar` (agregar) | — | `padding-top: 1.5rem; margin-top: 2rem` | Barra de guardado separada |
| `.cfg-content-header` (ajustar) | — | `margin-bottom: 1.8rem` | Título con más margen |
| `.cfg-field input, .cfg-field select` (ajustar) | — | `padding: 0.65rem 1rem` | Controles más cómodos |

#### 2.2 `src/main/resources/static/css/main.css` — Media query responsive (L2004)

**Ajustar breakpoint mobile:**
```css
/* Antes: */
.cfg-card { flex-direction: column; }
.cfg-sidebar { width: 100%; min-width: 100%; }
/* Después (misma lógica, verificar que funciona con las nuevas dimensiones) */
```

### Verificación

```
mvn clean compile → BUILD SUCCESS
mvn test → 278 tests, 0 failures
```

**Test manual:**
- Abrir Settings → verificar que se ve más grande y espacioso
- Recorrer los 6 tabs → verificar que todo se ve bien, sin overflow roto
- Responsive en viewport < 768px → sidebar horizontal funcional

---

## ✅ PASO 3 — Backend: Soporte SVG + campo `logoFormat` {#paso-3}
> **COMPLETADO** — 2026-04-08 · 278 tests ✅ · BUILD SUCCESS

### Objetivo

Extender el backend para aceptar SVG, y registrar el formato del logo en la entidad Company.

### Archivos a modificar

#### 3.1 `src/main/resources/swagger/ms-bs-etia.yaml`

**Cambios:**
- Endpoint `POST /company/logo`: Actualizar description para incluir SVG en formatos aceptados
- Schema `CompanyData`: Agregar campo `logoFormat` (string, example: `"png"`)
- Endpoint `GET /company/logo`: Agregar `image/svg+xml` como media type alternativo en response 200

#### 3.2 `src/main/java/com/etlions/ms_bs_etia/company/service/LogoStorageService.java`

**Cambios:**
```java
// ANTES:
private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");
private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");

// DESPUÉS:
private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".svg");
private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/svg+xml");
```

- `getContentType()` (L112-127): Agregar case para `.svg` → `"image/svg+xml"`
- Nuevo método `getLogoFormat(String ruc)`: Retorna extensión sin punto (`"png"`, `"jpg"`, `"svg"`)
- Actualizar mensaje de error: `"Solo se permiten archivos .png, .jpg, .jpeg o .svg"`

#### 3.3 CompanyEntity/CompanyMapper (si aplica)

**Evaluar:** Si `logoFormat` debe persistirse como columna nueva en `CompanyEntity`:
- Agregar `@Column(name = "logo_format") private String logoFormat;` a CompanyEntity
- H2 auto-update creará la columna
- CompanyMapper: mapear `logoFormat` a `CompanyData`
- CompanyServiceImpl: en `subirLogo()`, después de guardar archivo, actualizar `entity.setLogoFormat(extension)` y `save()`

#### 3.4 `src/main/resources/data.sql`

- UPDATE la empresa demo para tener `logo_format = 'png'` si tiene logo existente

### Verificación

```
mvn clean compile → BUILD SUCCESS (incluyendo regeneración codegen de OpenAPI)
mvn test → 278+ tests, 0 failures
```

---

## ✅ PASO 4 — Backend: Utilitario `LogoUtil` {#paso-4}
> **COMPLETADO** — 2026-04-08 · 290 tests ✅ · BUILD SUCCESS

### Objetivo

Crear una clase utilitaria para manipulación de logo: bytes → base64, validación dimensiones, resize.

### Archivos a crear

#### 4.1 `src/main/java/com/etlions/ms_bs_etia/company/util/LogoUtil.java`

```java
@Component
public class LogoUtil {

    private final LogoStorageService logoStorageService;
    
    // Constantes de dimensiones
    public static final int MIN_WIDTH = 50;
    public static final int MIN_HEIGHT = 50;
    public static final int MAX_WIDTH = 2000;
    public static final int MAX_HEIGHT = 2000;
    public static final int RECOMMENDED_WIDTH = 400;
    public static final int RECOMMENDED_HEIGHT = 400;

    /**
     * Obtiene bytes del logo de una empresa.
     */
    public Optional<byte[]> getLogoBytes(String ruc) { ... }

    /**
     * Convierte logo a data URL base64 para embeber en PDF.
     * Retorna "data:image/{format};base64,..." o empty si no hay logo.
     */
    public Optional<String> getLogoBase64DataUrl(String ruc) { ... }

    /**
     * Valida dimensiones de una imagen PNG/JPG.
     * SVG siempre pasa (es vectorial).
     * Retorna LogoDimensionResult con width, height, y flags (tooSmall, tooLarge, isValid).
     */
    public LogoDimensionResult validateDimensions(byte[] imageBytes, String format) { ... }

    /**
     * Redimensiona imagen a dimensiones objetivo.
     * Mantiene aspect ratio. Solo aplica a PNG/JPG (no SVG).
     */
    public byte[] resizeImage(byte[] imageBytes, int targetWidth, int targetHeight, String format) { ... }
    
    // Record para resultado de validación
    public record LogoDimensionResult(
        int width, int height,
        boolean tooSmall, boolean tooLarge, boolean isVector,
        String message
    ) {}
}
```

#### 4.2 `src/test/java/com/etlions/ms_bs_etia/company/util/LogoUtilTest.java`

Tests unitarios:
- `validateDimensions_imagenValida_retornaOk`
- `validateDimensions_imagenMuyPequena_retornaTooSmall`
- `validateDimensions_imagenMuyGrande_retornaTooLarge`
- `validateDimensions_svg_siempreValida`
- `resizeImage_reduceTamano_correctamente`
- `getLogoBase64DataUrl_conLogo_retornaDataUrl`
- `getLogoBase64DataUrl_sinLogo_retornaEmpty`

### Verificación

```
mvn clean compile → BUILD SUCCESS
mvn test → 278 + N new tests, 0 failures
```

---

## ✅ PASO 5 — Frontend: SVG + validación dimensiones + resize client-side {#paso-5}
> **COMPLETADO** — 2026-04-08 · 290 tests ✅ · BUILD SUCCESS

### Objetivo

Extender la UI de upload de logo en Settings para aceptar SVG, validar dimensiones, y ofrecer resize automático.

### Archivos a modificar

#### 5.1 `src/main/resources/templates/fragments/config-overlay.html` (L59)

**Cambio:** Actualizar atributo `accept`:
```html
<!-- ANTES: -->
<input type="file" id="cfg-logo-file-input" accept=".png,.jpg,.jpeg" .../>

<!-- DESPUÉS: -->
<input type="file" id="cfg-logo-file-input" accept=".png,.jpg,.jpeg,.svg" .../>
```

#### 5.2 `src/main/resources/static/js/config-overlay.js` — función `handleLogoFileSelect` (L373-424)

**Cambios:**

1. Actualizar validación de extensión:
```javascript
// ANTES (L377):
if (!name.endsWith('.png') && !name.endsWith('.jpg') && !name.endsWith('.jpeg')) {
    showConfigToast('⚠️ Solo se permiten archivos .png, .jpg o .jpeg');

// DESPUÉS:
if (!name.endsWith('.png') && !name.endsWith('.jpg') && !name.endsWith('.jpeg') && !name.endsWith('.svg')) {
    showConfigToast('⚠️ Solo se permiten archivos .png, .jpg, .jpeg o .svg');
```

2. Agregar validación de dimensiones ANTES del upload:
```javascript
// Para PNG/JPG: cargar en Image(), leer naturalWidth/Height
// Para SVG: saltar validación (vectorial)
function _validateLogoDimensions(file) {
    return new Promise(function(resolve) {
        if (file.name.toLowerCase().endsWith('.svg')) {
            resolve({ valid: true, isVector: true });
            return;
        }
        var img = new Image();
        img.onload = function() {
            var w = img.naturalWidth, h = img.naturalHeight;
            if (w < 50 || h < 50) {
                resolve({ valid: false, tooSmall: true, width: w, height: h });
            } else if (w > 2000 || h > 2000) {
                resolve({ valid: true, tooLarge: true, width: w, height: h });
            } else {
                resolve({ valid: true, width: w, height: h });
            }
            URL.revokeObjectURL(img.src);
        };
        img.onerror = function() { resolve({ valid: true }); };
        img.src = URL.createObjectURL(file);
    });
}
```

3. Agregar resize automático via `<canvas>`:
```javascript
function _resizeLogoFile(file, maxW, maxH) {
    return new Promise(function(resolve) {
        var img = new Image();
        img.onload = function() {
            var ratio = Math.min(maxW / img.naturalWidth, maxH / img.naturalHeight);
            var canvas = document.createElement('canvas');
            canvas.width = Math.round(img.naturalWidth * ratio);
            canvas.height = Math.round(img.naturalHeight * ratio);
            var ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            canvas.toBlob(function(blob) {
                var resized = new File([blob], file.name, { type: file.type });
                resolve(resized);
            }, file.type);
            URL.revokeObjectURL(img.src);
        };
        img.src = URL.createObjectURL(file);
    });
}
```

4. Flujo: Validar dimensiones → si `tooSmall` mostrar warning y abortar → si `tooLarge` ofrecer resize con confirm → si OK, resize y upload → si no, upload directo.

### Verificación

```
mvn clean compile → BUILD SUCCESS
mvn test → 278+ tests, 0 failures
```

**Test manual:**
- Subir PNG 50x50 → debe aceptar sin warning
- Subir PNG 30x30 → debe mostrar warning "demasiado pequeña"
- Subir PNG 4000x3000 → debe ofrecer "¿Desea redimensionar a 2000x1500?"
- Subir SVG → debe aceptar sin validación de dimensiones
- Subir archivo .bmp → debe rechazar

---

## ✅ PASO 6 — Frontend: Logo upload en Welcome "Configura tu Empresa" {#paso-6}
> **COMPLETADO** — 2026-04-08 · 290 tests ✅ · BUILD SUCCESS

### Objetivo

Agregar la opción de subir logo durante el onboarding (View 3 de welcome.html).

### Archivos a modificar

#### 6.1 `src/main/resources/templates/welcome.html` — View 3 (L172-234)

**Agregar widget de logo** entre el formulario y el botón "🚀 Comenzar a Facturar":

```html
<!-- Logo upload widget (antes del botón Comenzar) -->
<div class="f-group" style="margin-top:1.2rem;">
    <label>🖼️ Logo de tu Empresa <span style="font-size:.72rem;color:var(--text-muted);">(Opcional)</span></label>
    <div id="onb-logo-upload-wrap" style="...estilos inline dashed border...">
        <img id="onb-logo-preview" style="display:none;width:80px;height:80px;object-fit:contain;" alt="Logo preview"/>
        <span id="onb-logo-placeholder" style="font-size:2rem;">📷</span>
        <p style="font-size:.72rem;color:var(--text-muted);">PNG, JPG o SVG · Máx 10MB</p>
        <input type="file" id="onb-logo-file-input" accept=".png,.jpg,.jpeg,.svg" style="display:none;"/>
    </div>
    <div id="onb-logo-result" style="display:none;font-size:.78rem;margin-top:.3rem;"></div>
</div>
```

#### 6.2 `src/main/resources/templates/welcome.html` — Script (L562+)

**Agregar lógica de upload:**

Reutilizar la misma lógica de validación de dimensiones y resize del PASO 5. Al subir exitosamente:
1. Actualizar preview en el widget
2. Actualizar logo en header (`hdr-logo-img`)
3. Actualizar logo en hero (`onb-hero-logo-img`)
4. Invalidar cache: `ETIA.invalidateLogoCache()`

**Alternativa más limpia:** Crear un archivo compartido `logo-upload.js` con las funciones `_validateLogoDimensions()` y `_resizeLogoFile()`, incluido en ambas páginas. Esto evita duplicación.

#### 6.3 (Opcional) Crear `src/main/resources/static/js/logo-upload.js`

Módulo compartido `ETIA.LogoUpload`:
- `ETIA.LogoUpload.validateFile(file)` → Promise con resultado de validación
- `ETIA.LogoUpload.resizeIfNeeded(file, maxW, maxH)` → Promise con file (resize o original)
- `ETIA.LogoUpload.showPreview(file, imgElement)` → Muestra preview en un `<img>`
- `ETIA.LogoUpload.upload(companyId, file)` → Llama `ApiClient.uploadLogo()` + manejo de errores

Incluir en:
- `config-overlay.html` (via `<script th:src="@{/js/logo-upload.js}">`)
- `welcome.html` (inline o via script tag)

### Verificación

```
mvn clean compile → BUILD SUCCESS
mvn test → 278+ tests, 0 failures
```

**Test manual:**
- En Welcome View 3: subir logo → aparece preview → click "Comenzar" → logo visible en header del menú
- Subir SVG en Welcome → funciona
- Imagen grande → ofrece resize

---

## ✅ PASO 7 — Header: Breadcrumb con logo de empresa {#paso-7}
> **COMPLETADO** — 2026-04-08 · 290 tests ✅ · BUILD SUCCESS

### Objetivo

Reemplazar el branding ETIA del header por:
- Logo de empresa (clickeable → menú)
- Breadcrumb de navegación: `LOGO \ Documentos \ Facturas`

### Archivos a modificar

#### 7.1 `src/main/resources/templates/fragments/header.html`

**Cambio estructural del bloque LEFT (L5-20):**

```html
<!-- ANTES: -->
<div class="g-header-left">
    <a th:href="@{/menu}" class="g-brand-link">
        <span class="g-logo" id="hdr-logo">
            <img id="hdr-logo-img" class="g-logo-img" style="display:none;" alt="Logo"/>
            <span id="hdr-logo-emoji">🧾</span>
        </span>
        <div class="g-brand">
            <h1>ETIA</h1>
            <sub>Facturación Electrónica · v0.1.0</sub>
        </div>
    </a>
    <a th:if="${backUrl != null}" th:href="@{__${backUrl}__}" class="g-back">
        ← <span th:text="${backLabel} ?: 'Atrás'">Atrás</span>
    </a>
</div>

<!-- DESPUÉS: -->
<div class="g-header-left">
    <a th:href="@{/menu}" class="g-brand-link" title="Ir al menú">
        <span class="g-logo" id="hdr-logo">
            <img id="hdr-logo-img" class="g-logo-img" style="display:none;" alt="Logo"/>
            <span id="hdr-logo-emoji">🧾</span>
        </span>
    </a>
    <nav class="g-breadcrumb" id="hdr-breadcrumb" th:if="${breadcrumb != null}">
        <span class="g-bc-sep">\</span>
        <!-- Intermediate crumbs (backUrl is the parent) -->
        <a th:if="${backUrl != null}" th:href="@{__${backUrl}__}" class="g-bc-item" 
           th:text="${backLabel} ?: 'Menú'">Menú</a>
        <span th:if="${backUrl != null}" class="g-bc-sep">\</span>
        <!-- Current page -->
        <span class="g-bc-current" th:text="${breadcrumb}">Página</span>
    </nav>
    <!-- Fallback for menu (no breadcrumb) -->
    <span th:if="${breadcrumb == null}" class="g-bc-brand">ETIA <sub style="font-size:.55rem;color:var(--text-muted);">v0.1.0</sub></span>
</div>
```

**Cambio en la firma del fragment (L2):**
```html
<!-- ANTES: -->
<header th:fragment="header(backUrl, backLabel)" class="g-header" ...>

<!-- DESPUÉS: -->
<header th:fragment="header(backUrl, backLabel, breadcrumb)" class="g-header" ...>
```

#### 7.2 Actualización de TODAS las páginas que usan el fragment (16 archivos)

Cada página debe pasar el nuevo parámetro `breadcrumb`:

| Página | backUrl | backLabel | breadcrumb (NUEVO) |
|--------|---------|-----------|-------------------|
| `menu.html` | null | null | null (muestra "ETIA v0.1.0") |
| `documents.html` | /menu | Menú | 'Documentos' |
| `facturas.html` | /documents | Documentos | 'Facturas' |
| `boletas.html` | /documents | Documentos | 'Boletas' |
| `notas-credito.html` | /documents | Documentos | 'Notas de Crédito' |
| `notas-debito.html` | /documents | Documentos | 'Notas de Débito' |
| `resumenes-diarios.html` | /menu | Menú | 'Resúmenes Diarios' |
| `clients.html` | /menu | Menú | 'Clientes' |
| `products.html` | /menu | Menú | 'Productos' |
| `employees.html` | /menu | Menú | 'Empleados' |
| `sedes.html` | /menu | Menú | 'Sedes' |
| `tipo-cambio.html` | /menu | Menú | 'Tipo de Cambio' |
| `catalogo-sunat.html` | /menu | Menú | 'Catálogos SUNAT' |
| `tablas-sunat.html` | /menu | Menú | 'Tablas SUNAT' |
| `formato-documentos.html` | /menu | Menú | 'Formato Documentos' |
| `wizard/factura.html` | /documents | Documentos | 'Nuevo Comprobante' |

**Ejemplo para `facturas.html` (L6):**
```html
<!-- ANTES: -->
<header th:replace="~{fragments/header :: header(backUrl='/documents', backLabel='Documentos')}"></header>

<!-- DESPUÉS: -->
<header th:replace="~{fragments/header :: header(backUrl='/documents', backLabel='Documentos', breadcrumb='Facturas')}"></header>
```

#### 7.3 `src/main/resources/static/css/main.css` — Nuevos estilos

**Agregar estilos de breadcrumb:**

```css
/* ─── Breadcrumb Navigation ─── */
.g-breadcrumb {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.82rem;
    font-family: var(--font-sans);
    margin-left: 0.2rem;
}
.g-bc-sep {
    color: var(--text-muted);
    font-size: 0.78rem;
    opacity: 0.5;
    user-select: none;
}
.g-bc-item {
    color: var(--text-secondary);
    text-decoration: none;
    transition: color 0.2s;
}
.g-bc-item:hover {
    color: var(--accent);
}
.g-bc-current {
    color: var(--text-primary);
    font-weight: 600;
}
.g-bc-brand {
    font-size: 0.88rem;
    font-weight: 700;
    color: var(--text-primary);
    margin-left: 0.4rem;
}
```

**Ajustar `.g-brand-link` (L90 aprox):**
- Remover el bloque `.g-brand h1` y `.g-brand sub` ya que no se usarán
- O mantener como fallback y ocultar vía `.g-brand { display: none }` cuando hay breadcrumb

**Ajustar `.g-logo-img`:** Quizás aumentar a 36x36 o 40x40 para que el logo tenga más presencia como elemento principal.

#### 7.4 `src/main/resources/templates/welcome.html`

**Esta página usa header inline (no fragment).** Mantener el diseño actual simplificado ya que es una landing page sin breadcrumb. El logo ya se muestra dinámicamente.

#### 7.5 `src/main/resources/static/js/app-state.js` — `_applyCompanyData()` (L163-180)

**Verificar:** La función ya setea `hdr-logo-img` src. No debería requerir cambios si los IDs se mantienen. Verificar que el logo se muestra correctamente con la nueva estructura del header.

### Verificación

```
mvn clean compile → BUILD SUCCESS
mvn test → 278+ tests, 0 failures
```

**Test manual:**
- Menú: Logo + "ETIA v0.1.0" (sin breadcrumb ya que es la raíz)
- Facturas: `Logo \ Documentos \ Facturas` donde "Documentos" es link clickeable
- Wizard: `Logo \ Documentos \ Nuevo Comprobante`
- Click en logo → navega al menú desde cualquier página
- Sin logo → emoji 🧾 como fallback, todo funciona igual

---

## DEPENDENCIAS Y ORDEN {#dependencias}

```
PASO 1 ──── Bug fix validación (independiente, prioridad alta)
  │
PASO 2 ──── CSS Settings (independiente, sin dependencias)
  │
PASO 3 ──── Backend SVG + logoFormat
  │
  └─→ PASO 4 ── LogoUtil (depende de PASO 3)
       │
       └─→ PASO 5 ── Frontend SVG + resize en Settings (depende de PASO 3)
            │
            └─→ PASO 6 ── Logo en Welcome (depende de PASO 5 para reutilizar lógica)
  │
PASO 7 ──── Header breadcrumb (independiente de 3-6, pero conviene hacerlo al final
             para que el logo ya funcione con SVG cuando se integra al breadcrumb)
```

**Ruta crítica:** 1 → 2 → 3 → 4 → 5 → 6 → 7

**Pasos paralelizables (si se desea):**
- PASO 1 y PASO 2 son totalmente independientes entre sí
- PASO 7 es independiente de PASO 4/5/6 pero conviene dejarlo último

---

## RESUMEN DE ARCHIVOS {#resumen-archivos}

### Archivos a CREAR (nuevos)

| # | Archivo | Paso |
|---|---------|------|
| 1 | `src/main/java/.../company/util/LogoUtil.java` | PASO 4 |
| 2 | `src/test/java/.../company/util/LogoUtilTest.java` | PASO 4 |
| 3 | `src/main/resources/static/js/logo-upload.js` (opcional) | PASO 6 |

### Archivos a MODIFICAR

| # | Archivo | Pasos |
|---|---------|-------|
| 1 | `static/js/doc-preview.js` | PASO 1 |
| 2 | `templates/boletas.html` | PASO 1 |
| 3 | `templates/facturas.html` | PASO 1 |
| 4 | `static/css/main.css` | PASO 2, PASO 7 |
| 5 | `swagger/ms-bs-etia.yaml` | PASO 3 |
| 6 | `company/service/LogoStorageService.java` | PASO 3 |
| 7 | `company/entity/CompanyEntity.java` | PASO 3 (si se agrega logoFormat) |
| 8 | `company/service/CompanyServiceImpl.java` | PASO 3 |
| 9 | `templates/fragments/config-overlay.html` | PASO 5 |
| 10 | `static/js/config-overlay.js` | PASO 5 |
| 11 | `templates/welcome.html` | PASO 6 |
| 12 | `templates/fragments/header.html` | PASO 7 |
| 13 | **16 páginas** con header fragment | PASO 7 |
| 14 | `static/js/app-state.js` | PASO 7 (verificación) |

### Esfuerzo estimado por paso

| Paso | Esfuerzo | Riesgo |
|------|----------|--------|
| PASO 1 — Bug fix validación | 🟢 30 min | Bajo |
| PASO 2 — CSS Settings | 🟢 45 min | Bajo |
| PASO 3 — Backend SVG + format | 🟡 1-2h | Medio (codegen) |
| PASO 4 — LogoUtil | 🟡 1-2h | Medio (ImageIO, tests) |
| PASO 5 — Frontend SVG + resize | 🟡 1-2h | Medio |
| PASO 6 — Logo en Welcome | 🟢 45 min | Bajo |
| PASO 7 — Header breadcrumb | 🟡 2-3h | Medio (16 archivos) |

**Total estimado: ~8-11 horas**

---

> **RECORDATORIO:** Este documento es SOLO planificación. No se ha ejecutado ningún cambio. Cada paso debe verificarse con `mvn clean compile` + `mvn test` antes de pasar al siguiente.

