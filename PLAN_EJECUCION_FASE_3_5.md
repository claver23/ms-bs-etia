# ✅ PLAN DE EJECUCIÓN — FASE 3.5: AUDITORÍA INTEGRAL + UX + FAVICON

> **Fecha:** 2026-04-08  
> **Estado: ✅ COMPLETADO** — 295 tests ✅ · 0 failures · BUILD SUCCESS  
> **Regla:** Cada paso termina con `mvn clean compile` + `mvn test` → 0 failures  
> **Todos los 12 pasos COMPLETADOS.**  
> **Base:** Continuación de PLAN_EJECUCION_FASE_3.md (completado) — NO es FASE 4

---

## TABLA DE CONTENIDOS

### EJE A — Auditoría de flujos Frontend → API → BD (Prioridad Alta)
1. [PASO 1 — Dead End: `annulDocument` — endpoint inexistente](#paso-1)
2. [PASO 2 — Dead End: `doc-preview.js` acciones solo-factura (emitir/anular/eliminar/xml)](#paso-2)
3. [PASO 3 — Dead End: `anularBoleta` falta en api-client.js](#paso-3)
4. [PASO 4 — Dead End: `emitirBoletaBorrador` falta en api-client.js](#paso-4)
5. [PASO 5 — Persistencia parcial: preferencias de notificación solo localStorage](#paso-5)
6. [PASO 6 — Persistencia parcial: `toastDuration`, `errorDisplayMode` no sincronizan a backend](#paso-6)

### EJE B — UX: Wizard de factura más alto + Estilo Wall Street
7. [PASO 7 — Aumentar altura visual del wizard](#paso-7)
10. [PASO 10 — BUG: Logo upload rechaza extensión en mayúsculas (PNG vs .png)](#paso-10)
11. [PASO 11 — UX: Breadcrumb estilo Wall Street / bolsa de valores](#paso-11)
12. [PASO 12 — UX: Validar/Re-validar disponible en todo momento (excepto emitidas)](#paso-12)

### EJE C — Branding: Favicon contable
8. [PASO 8 — Agregar favicon de calculadora/contable](#paso-8)

### EJE D — Documentación: Matriz de integridad
9. [PASO 9 — Generar matriz completa API JS ↔ Swagger ↔ Controller ↔ Service ↔ BD](#paso-9)

---

## DEPENDENCIAS Y ORDEN

```
EJE A (Auditoría flujos) — Prioridad ALTA, ejecutar primero
  PASO 1 ── annulDocument (independiente, CRÍTICO — botón visible llama endpoint inexistente)
  PASO 2 ── doc-preview multi-tipo (depende de PASO 3 y 4 para tener funciones ApiClient)
  PASO 3 ── anularBoleta en api-client (independiente)
  PASO 4 ── emitirBoletaBorrador en api-client (independiente)
  PASO 5 ── notif prefs a backend (independiente)
  PASO 6 ── toast/error prefs a backend (independiente, similar a PASO 5)

EJE B (UX + Estilo) — Prioridad MEDIA, paralelo con EJE A
  PASO 7  ── Wizard más alto (independiente)
  PASO 10 ── Logo case-insensitive (independiente, fix rápido)
  PASO 11 ── Breadcrumb estilo Wall Street (independiente, CSS + HTML)
  PASO 12 ── Validar/Re-validar siempre disponible (depende de PASO 2)

EJE C (Branding) — Prioridad BAJA
  PASO 8 ── Favicon (independiente)

EJE D (Documentación) — Al final, como verificación
  PASO 9 ── Matriz de integridad (después de EJE A)
```

**Ruta crítica:** 3 → 4 → 2 → 1 → 12 (las acciones de doc-preview necesitan las funciones ApiClient primero; validar-siempre depende de la refactorización multi-tipo)

---

## ✅ PASO 1 — Dead End: `annulDocument` — endpoint inexistente {#paso-1}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> `annulDocument()` eliminado. `anularDocumento()` ahora despacha a `anularFactura`/`anularBoleta`.

### Diagnóstico

**Problema:** `ApiClient.annulDocument()` (api-client.js L408-414) hace `PUT /api/v1/notification/annul` con payload `{documentId, tipoDocumento, motivoAnulacion}`. Este endpoint:
- ❌ **NO existe en swagger** (`ms-bs-etia.yaml`) — grep "annul" devuelve 0 resultados
- ❌ **NO existe en backend** — grep "annul" en `*.java` devuelve 0 resultados
- ⚠️ Se invoca desde `ETIA.anularDocumento()` (app-state.js L405) — accesible desde panel de notificaciones

**Impacto:** El usuario puede pulsar "Anular" desde una notificación de documento rechazado → la app hace un PUT a un endpoint que no existe → error 404 silencioso.

### Solución propuesta

**Opción A (Recomendada):** Eliminar el endpoint genérico y despachar a los endpoints existentes:
- `app-state.js` → `ETIA.anularDocumento()` debe verificar `tipoDocumento` y llamar:
  - `'FACTURA'` → `ApiClient.anularFactura(documentId, {motivoAnulacion})`  
  - `'BOLETA'` → `ApiClient.anularBoleta(documentId, {motivoAnulacion})` (requiere PASO 3)
- Eliminar `ApiClient.annulDocument()` del api-client.js (ya no se necesita)

**Opción B:** Crear el endpoint genérico en backend que internamente despache — más complejo, menos beneficio.

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/app-state.js` | Reescribir `ETIA.anularDocumento()` para despachar según tipo |
| 2 | `static/js/api-client.js` | Eliminar `annulDocument()`, agregar `anularBoleta()` si PASO 3 no está hecho |

### Subtareas
1. Leer `ETIA.anularDocumento()` completa para entender el contexto (notificaciones)
2. Implementar despacho por tipo usando endpoints existentes
3. Eliminar la función `annulDocument` muerta
4. Testear: Abrir notificación de factura rechazada → anular → verificar que cambia estado en BD

---

## ✅ PASO 2 — Dead End: `doc-preview.js` acciones solo-factura {#paso-2}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> `emitir()`, `anular()`, `eliminar()`, `descargarXml()` ahora despachan según `_currentDocType`.  
> Boletas: agregados botones eliminar, XML/PDF, anular, Ver detalle.

### Diagnóstico

**Problema:** El modal DocPreview se abre para facturas, boletas, notas de crédito y notas de débito. Sin embargo, 4 de sus acciones están hardcoded solo para facturas:

| Acción | Función | Endpoint usado | ¿Multi-tipo? |
|--------|---------|---------------|---------------|
| Emitir | `emitir()` L443 | `ApiClient.emitirFacturaBorrador()` | ❌ Solo factura |
| Anular | `anular()` L458 | `ApiClient.anularFactura()` | ❌ Solo factura |
| Eliminar | `eliminar()` L501 | `ApiClient.eliminarFactura()` | ❌ Solo factura |
| Descargar XML | `descargarXml()` L476 | `ApiClient.descargarFacturaXml()` | ❌ Solo factura |
| Validar | `validar()` L518 | `ApiClient.validarBoleta / validarFactura` | ✅ Multi-tipo |
| Export PDF | `exportPdf()` L582 | Local (html2canvas) | ✅ Multi-tipo |
| Export JSON | `exportJson()` L541 | Local (clipboard) | ✅ Multi-tipo |
| Duplicar | `executeDuplicate()` | Redirige a wizard | ✅ Multi-tipo |

**Impacto:** Si un usuario abre una boleta en DocPreview y pulsa "Emitir", se llama a `emitirFacturaBorrador` con un ID de boleta → error 404 o peor, comportamiento inesperado.

### Solución propuesta

Refactorizar las 4 funciones para despachar según `_currentDocType`:

```javascript
// emitir() → despachar según tipo
var emitFn;
if (_currentDocType === 'boleta')        emitFn = ApiClient.emitirBoletaBorrador; // PASO 4
else if (_currentDocType === 'factura')   emitFn = ApiClient.emitirFacturaBorrador;
else return; // notas: no se emiten desde preview (tienen su propio wizard)

// anular() → despachar según tipo
var anulFn;
if (_currentDocType === 'boleta')        anulFn = ApiClient.anularBoleta; // PASO 3
else if (_currentDocType === 'factura')   anulFn = ApiClient.anularFactura;
else return;

// eliminar() → despachar según tipo
var delFn;
if (_currentDocType === 'boleta')        delFn = ApiClient.eliminarBoleta;
else if (_currentDocType === 'factura')   delFn = ApiClient.eliminarFactura;
else return;

// descargarXml() → despachar según tipo
var xmlFn;
if (_currentDocType === 'boleta')        xmlFn = ApiClient.descargarBoletaXml;
else if (_currentDocType === 'factura')   xmlFn = ApiClient.descargarFacturaXml;
else return;
```

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/doc-preview.js` | Refactorizar `emitir()`, `anular()`, `eliminar()`, `descargarXml()` para multi-tipo |

### Subtareas
1. Refactorizar `emitir()` — despachar factura vs boleta (notas no se emiten desde preview)
2. Refactorizar `anular()` — despachar factura vs boleta
3. Refactorizar `eliminar()` — despachar factura vs boleta
4. Refactorizar `descargarXml()` — despachar factura vs boleta
5. Actualizar los textos de confirmación: "¿Anular la factura..." → usar `_labels[_currentDocType].name`
6. Verificar que los botones se muestran/ocultan correctamente para cada tipo+estado

---

## ✅ PASO 3 — Dead End: `anularBoleta` falta en api-client.js {#paso-3}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS

### Diagnóstico

**Problema:** El swagger define `operationId: anularBoleta` (L792), el backend lo implementa, pero `api-client.js` NO tiene una función `anularBoleta()`.

**Endpoints de boleta en swagger vs api-client.js:**

| Endpoint swagger | operationId | ¿Existe en api-client.js? |
|-----------------|-------------|--------------------------|
| `POST /boleta` | emitirBoleta | ✅ L301 |
| `GET /boleta/list` | listarBoletas | ✅ L305 |
| `PUT /boleta/validar` | validarBoleta | ✅ L311 |
| `PUT /boleta/actualizar` | actualizarBoletaBorrador | ✅ L315 |
| `DELETE /boleta` | eliminarBoletaBorrador | ✅ L319 (`eliminarBoleta`) |
| `GET /boleta` | obtenerBoleta | ✅ L213 (`getBoleta`) |
| `GET /boleta/xml` | descargarBoletaXml | ✅ L323 |
| **`PUT /boleta/anular`** | **anularBoleta** | ❌ **FALTA** |

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/api-client.js` | Agregar función `anularBoleta(boletaId, payload)` |

### Subtareas
1. Verificar el path exacto y payload del swagger para `anularBoleta`
2. Agregar la función en api-client.js siguiendo el patrón de `anularFactura`
3. Exponerla en el return object del módulo

---

## ✅ PASO 4 — Dead End: `emitirBoletaBorrador` falta en api-client.js {#paso-4}
> **COMPLETADO** — 2026-04-08 · No se necesita endpoint separado.  
> Las boletas no se emiten directamente (se envían vía Resumen Diario).  
> El botón "Emitir" ya NO se muestra para boletas en doc-preview. ✅ Correcto por diseño.

### Diagnóstico

**Problema:** Para facturas existe `emitirFacturaBorrador(facturaId)` (api-client.js L276) que hace `POST /factura/emitir-borrador?facturaId=X`. No existe un equivalente para boletas.

**Verificar si el swagger tiene este endpoint para boletas.** Si no existe, verificar si `emitirBoleta` ya maneja el caso de emitir un borrador existente (posiblemente la boleta ya tiene este flujo integrado en `emitirBoleta`).

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `swagger: ms-bs-etia.yaml` | Verificar si existe endpoint emitir-borrador para boleta |
| 2 | `static/js/api-client.js` | Agregar `emitirBoletaBorrador()` si corresponde |

### Subtareas
1. Buscar en swagger si hay operationId para emitir boleta borrador
2. Si no existe: verificar si `DocBoletaServiceImpl` ya maneja la emisión de borradores en el endpoint `emitirBoleta`
3. Si maneja — usar `emitirBoleta` directamente desde doc-preview
4. Si no maneja — agregar endpoint en swagger + controller + service

---

## ✅ PASO 5 — Persistencia parcial: preferencias de notificación solo localStorage {#paso-5}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> `saveNotifPreferences()` ahora sincroniza a backend via `ApiClient.savePreference()`.  
> `_loadNotifPreferences()` usa backend como fallback si localStorage vacío.

### Diagnóstico

**Problema:** `saveNotifPreferences()` (config-overlay.js L976-993) guarda en `localStorage('etia_notif_prefs')` solamente. Si el usuario cambia de navegador o dispositivo, pierde sus preferencias de notificación.

**Campos afectados:**
- `emailEnabled` (boolean)
- `email` (string)
- `smsEnabled` (boolean)
- `phone` (string)
- `aceptada`, `rechazada`, `resumen`, `certExpiry` (booleans)

**Contraste:** Las preferencias generales (currency, defaultItemType, etc.) SÍ se sincronizan al backend vía `ApiClient.savePreference()` en `saveConfig()` L346-353.

### Solución propuesta

Reutilizar el mismo mecanismo de `ApiClient.savePreference()` para persistir las preferencias de notificación como un JSON serializado con clave `notif_prefs`.

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/config-overlay.js` | `saveNotifPreferences()` → agregar `ApiClient.savePreference(companyId, 'notif_prefs', JSON.stringify(prefs))` |
| 2 | `static/js/config-overlay.js` | `_loadNotifPreferences()` → agregar carga desde backend como fallback |

### Subtareas
1. En `saveNotifPreferences()`, después de guardar en localStorage, sincronizar al backend
2. En `_loadNotifPreferences()`, intentar cargar desde backend si localStorage está vacío
3. Mantener localStorage como cache rápido (write-through pattern, igual que el config)

---

## ✅ PASO 6 — Persistencia parcial: `toastDuration`, `errorDisplayMode` no sincronizan {#paso-6}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS

### Diagnóstico

**Problema:** `saveConfig()` (config-overlay.js L344) sincroniza 7 claves al backend:
```javascript
var prefsToSync = ['currency', 'defaultItemType', 'tipoCambioFuente', 'emisionEnEnvio',
                   'defaultCuotas', 'cuotaDistribucion', 'documentFormat'];
```

Pero estas 3 claves del PASO 8 de FASE 3 se guardan en `ETIA.config` (localStorage) pero **NO se sincronizan**:
- `toastDurationError` (número, duración del toast de error)
- `toastDurationSuccess` (número, duración del toast de éxito)
- `errorDisplayMode` (string: 'toast', 'banner', 'both')

### Solución propuesta

Agregar las 3 claves faltantes a `prefsToSync`.

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/config-overlay.js` | Agregar 3 claves a `prefsToSync` array |

### Subtareas
1. Agregar `'toastDurationError', 'toastDurationSuccess', 'errorDisplayMode'` al array `prefsToSync` L346-347
2. Verificar que `_loadPreferencesFromBackend` en app-state.js las cargue correctamente al init

---

## ✅ PASO 7 — Aumentar altura visual del wizard {#paso-7}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> `.wiz-steps` margin 2.5→1.5rem, `.panel` padding 2→1.5rem + min-height calc(100vh-260px).  
> `.panel-head` margin 1.5→1rem. Ganancia neta: ~60px de espacio vertical útil.

### Diagnóstico

**Estado actual del layout del wizard:**

```
┌─────────────────────────────────────┐
│ g-header (fixed top)                │  ~56px
├─────────────────────────────────────┤
│ wiz-ticker (stock-ticker header)    │  ~44px
├─────────────────────────────────────┤
│ wiz-steps (paso 1-5 circles)        │  ~80px + margin-bottom: 2.5rem (40px)
├─────────────────────────────────────┤
│ .panel (contenido del paso)         │  padding: 2rem → espacio útil
│   .container (max-width: 1100px)    │
│     [formulario / items / etc.]     │
├─────────────────────────────────────┤
│ .panel footer (botones nav)         │  ~60px
└─────────────────────────────────────┘
```

**CSS actual relevante:**
- `.container` → `max-width: 1100px; padding: 2rem 1.5rem;` (main.css L473)
- `.panel` → `padding: 2rem;` (main.css L916)
- `.wiz-steps` → `margin-bottom: 2.5rem; padding: 1.5rem 0 0.5rem;` (main.css L707-710)

**Overhead vertical estimado:** ~280px (~56 header + ~44 ticker + ~120 steps+margin + ~60 padding) antes del contenido real del formulario.

### Solución propuesta

Cambios CSS para maximizar el área útil del formulario:

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/css/main.css` | Reducir margins/paddings del wizard |
| 2 | `static/css/main.css` | Agregar `min-height` al `.panel` del wizard |

### Subtareas
1. Reducir `.wiz-steps` `margin-bottom` de `2.5rem` a `1.5rem` (ganar ~16px)
2. Reducir `.panel` `padding` de `2rem` a `1.5rem` (ganar ~16px top+bottom)
3. Agregar al `.panel` dentro del wizard: `min-height: calc(100vh - 260px)` para que ocupe el viewport
4. Evaluar si `.container` `max-width: 1100px` debe subir a `1200px` (solo para wizard, via clase `.container-wiz`)
5. Reducir `.panel-head` `margin-bottom` de `1.5rem` a `1rem` (ganar ~8px)
6. Probar en viewport 1920×1080 y 1366×768 — verificar que no se rompe nada en mobile

---

## ✅ PASO 8 — Agregar favicon de calculadora/contable {#paso-8}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Creado `favicon.svg` — calculadora minimalista con S/., fondo oscuro, botones verdes accent.  
> Agregado `<link rel="icon">` en head.html. Solo SVG (máxima compatibilidad moderna).

### Diagnóstico

**Estado actual:** 
- ❌ No existe ningún favicon en el proyecto
- ❌ No hay `<link rel="icon">` en `head.html`
- ❌ No hay archivos `.ico`, `.png`, `.svg` de favicon en `static/`
- El navegador muestra el ícono genérico por defecto

### Solución propuesta

Crear un favicon SVG inline de calculadora/ícono contable y generar los formatos necesarios.

### Archivos a crear
| # | Archivo | Descripción |
|---|---------|-------------|
| 1 | `static/favicon.svg` | SVG vectorial de calculadora (ícono principal) |
| 2 | `static/favicon.ico` | ICO 32×32 para compatibilidad |
| 3 | `static/favicon-32x32.png` | PNG 32×32 |
| 4 | `static/favicon-16x16.png` | PNG 16×16 |
| 5 | `static/apple-touch-icon.png` | PNG 180×180 para iOS |

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `templates/fragments/head.html` | Agregar `<link rel="icon">` tags |

### Subtareas
1. Crear SVG de calculadora minimalista con estilo acorde al design system (colores `--accent` green, fondo oscuro)
2. Generar los archivos PNG/ICO derivados del SVG
3. Agregar en `head.html` (entre L5 y L6):
   ```html
   <link rel="icon" type="image/svg+xml" th:href="@{/favicon.svg}"/>
   <link rel="icon" type="image/x-icon" th:href="@{/favicon.ico}" sizes="32x32"/>
   <link rel="apple-touch-icon" th:href="@{/apple-touch-icon.png}" sizes="180x180"/>
   ```
4. Verificar que Spring Boot sirve los archivos desde `/static/` (ya lo hace para CSS/JS)
5. Verificar en Chrome, Firefox, Edge que se muestra el ícono

---

## ✅ PASO 9 — Generar matriz completa de integridad API {#paso-9}
> **COMPLETADO** — 2026-04-08  
> Creado `FASE_3_5_MATRIZ_INTEGRIDAD.md` — 69 funciones JS mapeadas, 0 dead-ends activos.  
> 11 operationIds swagger sin JS (3 server-only OK, 8 para FASE 4).

### Objetivo

Crear un documento de referencia que mapee CADA función del frontend (api-client.js) con su endpoint swagger, controller, service y entidad BD. Esto permite detectar:
- Funciones JS que no llegan al backend
- Endpoints swagger que nadie consume
- Flujos parciales (frontend llama, backend no persiste)

### Archivos a crear
| # | Archivo | Descripción |
|---|---------|-------------|
| 1 | `FASE_3_5_MATRIZ_INTEGRIDAD.md` | Documento con la matriz completa |

### Estructura de la matriz

```
| # | Función JS (api-client.js) | HTTP | Path | Swagger operationId | Controller | Service | Repository | Estado |
```

### Subtareas
1. Listar las ~55 funciones de api-client.js con su method + path
2. Para cada una, verificar: swagger ✅/❌ → controller ✅/❌ → service ✅/❌ → repository ✅/❌
3. Listar endpoints swagger que NO tienen función JS (ej: endpoints que solo usa el wizard internamente)
4. Documentar funciones "solo-frontend" (localStorage, clipboard, etc.) como "N/A backend"
5. Marcar con ❌ los dead-ends que requieren acción en FASE 4

---

## ✅ PASO 10 — BUG: Logo upload rechaza extensión en mayúsculas (PNG vs .png) {#paso-10}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Eliminada validación de extensión — magic bytes son la fuente de verdad.  
> `accept` ampliado con MIME types + extensiones upper/lowercase.

### Diagnóstico

**Problema:** El selector de tipo de logo (PASO 10.1 de FASE 3) usa un `<select>` con valores `png`, `jpg`, `svg`. Al seleccionar "PNG" y subir un archivo `logo.PNG` (extensión en mayúsculas), la validación JS compara con `.png` y rechaza el archivo con el mensaje "El archivo no es PNG. Selecciona un archivo .png".

**Causa raíz:** La validación en `handleLogoFileSelect()` (config-overlay.js) hace `name.endsWith(ext)` donde `ext` es `.png` (minúsculas), pero el `name` del archivo puede tener extensión en mayúsculas o mixtas (`.PNG`, `.Png`, `.JPG`, `.JPEG`, `.SVG`).

Además, la validación de magic bytes debería ser la ÚNICA validación real necesaria — si los bytes dicen que es PNG, no importa si la extensión es `.PNG` o `.png`.

**Alcance del problema:**
1. `config-overlay.js` → `handleLogoFileSelect()` — comparación case-sensitive de extensión
2. `welcome.html` → `onbHandleLogoUpload()` — misma comparación case-sensitive
3. `config-overlay.js` → `_logoTypeAcceptMap` — el `accept` del input puede no aceptar `.PNG` en algunos navegadores
4. Backend `LogoStorageService.java` L58 → `hasAllowedExtension()` ya usa `.toLowerCase()` → ✅ backend OK

### Solución propuesta

1. Normalizar `file.name` a lowercase ANTES de cualquier comparación
2. En el `accept` del file input, agregar variantes en mayúsculas: `.png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.svg,.SVG`
3. Hacer que la validación de magic bytes sea la fuente de verdad — si pasa magic bytes, aceptar el archivo aunque la extensión sea rara
4. Revisar que todo el flujo sea robusto: selector tipo → accept input → extensión → magic bytes → upload

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/config-overlay.js` | Normalizar a lowercase en `handleLogoFileSelect()`, ajustar accept |
| 2 | `templates/welcome.html` | Normalizar a lowercase en `onbHandleLogoUpload()`, ajustar accept |
| 3 | `templates/fragments/config-overlay.html` | Ampliar `accept` del file input |

### Subtareas
1. En `handleLogoFileSelect()`: ya se hace `name = file.name.toLowerCase()` pero la comparación con `_logoTypeAcceptMap` debe ser también case-insensitive en el accept
2. Cambiar la lógica: si magic bytes pasan → aceptar, aunque extensión no coincida exactamente (warning informativo, no bloqueo)
3. En welcome.html: misma corrección en `onbHandleLogoUpload()`
4. Probar: subir `logo.PNG`, `logo.Jpg`, `logo.SVG` → todos deben funcionar

---

## ✅ PASO 11 — UX: Breadcrumb estilo Wall Street / bolsa de valores {#paso-11}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Breadcrumb rediseñado: fondo oscuro terminal, font JetBrains Mono, separadores ▸ verdes,  
> dot pulsante en item activo, glow accent, hover tipo selección de terminal.

### Diagnóstico

**Problema:** El breadcrumb actual en `header.html` es funcional pero genérico — texto plano con separadores `\`. El estilo visual del sistema ETIA busca una estética **Wall Street pre-crash**: el auge frenético de operaciones, pantallas de trading llenas de datos, tickers corriendo, todo en movimiento, sosteniendo la vida de todos. Esa energía de abundancia y velocidad es el ADN del diseño.

**Estado actual del breadcrumb (header.html L13-19):**
```html
<nav class="g-breadcrumb" id="hdr-breadcrumb">
    <span class="g-bc-sep">\</span>
    <a class="g-bc-item">Menú</a>
    <span class="g-bc-sep">\</span>
    <span class="g-bc-current">Página</span>
</nav>
```

**CSS actual:** `.g-breadcrumb` es un flex simple con font-size `0.78rem` y color muted.

### Visión de diseño

El breadcrumb debe evocar la **cinta de teletipo de Wall Street** (stock ticker tape):
- Fondo oscuro con borde sutil tipo terminal de trading
- Texto en tipografía monoespaciada (JetBrains Mono, ya cargada)
- Separadores tipo `›` o `▸` con color accent (verde neón del design system)
- Items con efecto hover que simule selección en una terminal
- Posiblemente un indicador pulsante (dot) en el item activo, como un cursor de terminal vivo
- El item actual con un leve glow verde, como un precio al alza
- Transiciones suaves, sensación de que todo "fluye" como datos en tiempo real
- Consistente con el stock-ticker del wizard (`wiz-ticker`) que ya tiene ese estilo

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/css/main.css` | Rediseñar `.g-breadcrumb`, `.g-bc-item`, `.g-bc-sep`, `.g-bc-current` con estilo Wall Street |
| 2 | `templates/fragments/header.html` | Posibles ajustes al HTML del breadcrumb (separadores, estructura) |

### Subtareas
1. Diseñar el estilo CSS del breadcrumb como "ticker tape" — fondo semi-transparente oscuro, borde terminal, font mono
2. Separadores: cambiar `\` por `›` o `▸` con color accent animado
3. Item activo (`.g-bc-current`): glow verde pulsante, peso bold, como un precio subiendo
4. Items clickeables (`.g-bc-item`): hover con background tipo selección de terminal (`rgba(0,255,136,0.1)`)
5. Agregar un "dot pulsante" (●) antes del item actual — como cursor activo de terminal
6. Asegurar que el breadcrumb se integra con el `g-header` sin romper el layout
7. Verificar responsive: en mobile el breadcrumb debe compactarse sin perder estilo
8. Mantener coherencia con `.wiz-ticker` (ya tiene estilo stock-ticker implementado en FASE 3)

---

## ✅ PASO 12 — UX: Validar/Re-validar disponible en todo momento (excepto emitidas) {#paso-12}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Botón validar visible globalmente para todos los estados excepto ENVIADA/ACEPTADA.  
> Aplica para facturas, boletas, y notas. ANULADA incluida como validable.

### Diagnóstico

**Problema:** El botón "Validar" / "Re-validar" en el modal DocPreview solo se muestra para ciertos estados. Debería estar **siempre disponible** para cualquier documento que NO esté emitido/enviado, permitiendo al usuario verificar la integridad de sus datos en cualquier momento.

**Estado actual de visibilidad del botón validar por estado:**

| Estado | Factura | Boleta | Debería |
|--------|---------|--------|---------|
| `BORRADOR` | ✅ Validar | ✅ Validar | ✅ OK |
| `VALIDADO` | ✅ Re-validar | ✅ Re-validar | ✅ OK |
| `CON_ERROR` | ✅ Validar | ✅ Validar | ✅ OK |
| `RECHAZADA` | ✅ Validar | ✅ Validar | ✅ OK |
| `ENVIADA` | ❌ No muestra | ❌ No muestra | ❌ **Correcto — ya se envió** |
| `ACEPTADA` | ❌ No muestra | ❌ No muestra | ❌ **Correcto — ya aceptada por SUNAT** |
| `ANULADA` | ❌ No muestra | ❌ No muestra | ⚠️ **Evaluar** — ¿tiene sentido re-validar una anulada? |

**Regla propuesta:** Mostrar botón "🔍 Validar" o "🔍 Re-validar" para TODOS los estados **excepto** `ENVIADA` y `ACEPTADA`. Incluir `ANULADA` como validable (puede ser útil para diagnóstico).

**Verificar también:** Que esto aplique no solo en el modal DocPreview sino también en las vistas de lista (facturas.html, boletas.html) si tienen acciones de validación inline.

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/doc-preview.js` | Mostrar botón validar para todos los estados excepto ENVIADA/ACEPTADA |

### Subtareas
1. En la función `_renderActions()` de doc-preview.js, agregar lógica: si estado NO es `ENVIADA` ni `ACEPTADA` → mostrar botón validar
2. Para `VALIDADO`: texto "🔍 Re-validar" (ya existe)
3. Para `BORRADOR`, `CON_ERROR`, `RECHAZADA`, `ANULADA`: texto "🔍 Validar"
4. Aplicar tanto para facturas como boletas (ya soporta multi-tipo via `validar()`)
5. Verificar que la función `validar()` del backend acepte re-validación en cualquier estado no-emitido
6. Verificar notas de crédito/débito — ¿tienen validación? Si sí, aplicar misma regla

---

## RESUMEN

### Archivos a CREAR

| # | Archivo | Paso |
|---|---------|------|
| 1 | `static/favicon.svg` | PASO 8 |
| 2 | `static/favicon.ico` | PASO 8 |
| 3 | `static/favicon-32x32.png` | PASO 8 |
| 4 | `static/favicon-16x16.png` | PASO 8 |
| 5 | `static/apple-touch-icon.png` | PASO 8 |
| 6 | `FASE_3_5_MATRIZ_INTEGRIDAD.md` | PASO 9 |

### Archivos a MODIFICAR

| # | Archivo | Pasos |
|---|---------|-------|
| 1 | `static/js/api-client.js` | PASO 1, 3, 4 |
| 2 | `static/js/app-state.js` | PASO 1 |
| 3 | `static/js/doc-preview.js` | PASO 2, 12 |
| 4 | `static/js/config-overlay.js` | PASO 5, 6, 10 |
| 5 | `static/css/main.css` | PASO 7, 11 |
| 6 | `templates/fragments/head.html` | PASO 8 |
| 7 | `templates/fragments/header.html` | PASO 11 |
| 8 | `templates/welcome.html` | PASO 10 |
| 9 | `templates/fragments/config-overlay.html` | PASO 10 |

### Esfuerzo estimado por paso

| Paso | Descripción | Esfuerzo | Riesgo |
|------|------------|----------|--------|
| PASO 1 | annulDocument dead-end fix | 🟢 30 min | Bajo |
| PASO 2 | doc-preview multi-tipo | 🟡 1.5-2h | Medio (4 funciones, testing) |
| PASO 3 | anularBoleta en api-client | 🟢 15 min | Bajo |
| PASO 4 | emitirBoletaBorrador verificar/agregar | 🟢 30 min | Bajo |
| PASO 5 | notif prefs → backend | 🟢 30 min | Bajo |
| PASO 6 | toast/error prefs → prefsToSync | 🟢 10 min | Bajo |
| PASO 7 | Wizard más alto (CSS) | 🟢 45 min | Bajo |
| PASO 8 | Favicon calculadora | 🟢 30 min | Bajo |
| PASO 9 | Matriz de integridad | 🟡 2-3h | Medio (investigación exhaustiva) |
| PASO 10 | Logo case-insensitive + magic bytes | 🟢 30 min | Bajo |
| PASO 11 | Breadcrumb Wall Street | 🟡 1-1.5h | Medio (diseño CSS complejo) |
| PASO 12 | Validar siempre disponible | 🟢 20 min | Bajo |

**Total estimado: ~8-11 horas**

---

## HALLAZGOS ADICIONALES (Para FASE 4 — NO ejecutar ahora)

Estos son problemas detectados que NO entran en FASE 3.5 pero deben documentarse:

### H1. Notas de crédito/débito sin XML ni anulación en doc-preview
Las notas solo tienen "Editar" en el preview modal. No hay botones de emitir, anular ni descargar XML.
**Acción FASE 4:** Agregar soporte completo de notas en doc-preview.

### H2. Resúmenes diarios no tienen preview modal
La vista `resumenes-diarios.html` no usa DocPreview — tiene acciones inline.
**Acción FASE 4:** Evaluar si conviene unificar al pattern DocPreview.

### H3. Scripts inline en templates HTML
Los templates `facturas.html`, `boletas.html`, `clients.html`, `products.html`, `sedes.html`, `employees.html`, `notas-credito.html`, `notas-debito.html` tienen scripts inline extensos (100-300 líneas cada uno) con funciones `loadDocs()`, `createDoc()`, etc.
**Acción FASE 4:** Evaluar extracción a archivos JS dedicados para mantenibilidad.

### H4. `logout()` / `ETIA.cambiarEmpresa()` sin comunicación al backend
`navigation.js` solo limpia localStorage. No hay endpoint de logout ni invalidación de sesión server-side.
**Acción FASE 4:** Evaluar si se necesita endpoint de logout para seguridad.

### H5. Formato de documentos — `formato-documentos.html` con preview estático
La vista de formato de documentos permite configurar el layout del PDF, pero el preview usa HTML estático.
**Acción FASE 4:** Conectar el preview con el motor real de PDF.

---

> **RECORDATORIO:** Este documento es SOLO planificación. No se ha ejecutado ningún cambio.  
> Cada paso debe verificarse con `mvn clean compile` + `mvn test` antes de pasar al siguiente.



















