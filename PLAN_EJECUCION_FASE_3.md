# 🚀 PLAN DE EJECUCIÓN — FASE 3: BUGS + MEJORAS UX + FEATURES

> **Fecha:** 2026-04-08  
> **Estado actual:** 295 tests ✅ · BUILD SUCCESS  
> **Regla:** Cada paso termina con `mvn clean compile` + `mvn test` → 0 failures  
> **Nota:** PASOs 1-9 COMPLETADOS. PASO 10 pendiente (auditoría).  
> **Base:** Continuación de PLAN_EJECUCION_FASE_2_5.md (completado)

---

## TABLA DE CONTENIDOS

### FASE A — Bugs Críticos de Datos (Prioridad Alta) ✅
1. [✅ PASO 1 — BUG: Logo en Welcome no persiste / flujo incorrecto](#paso-1)
2. [✅ PASO 2 — BUG: Validación inconsistente de cuotas (backend vs frontend)](#paso-2)

### FASE B — Bugs de UX (Prioridad Media) ✅
3. [✅ PASO 3 — BUG: Rediseño header del Wizard (estilo stock-ticker)](#paso-3)
4. [✅ PASO 4 — BUG: Botón "Validar" faltante en modal preview para ciertos estados](#paso-4)
5. [✅ PASO 5 — BUG: Error duplicado toast + modal en Wizard](#paso-5)

### FASE C — Features Nuevos ✅
6. [✅ PASO 6 — FEATURE: Modo lectura (read-only) en Wizard](#paso-6)
7. [✅ PASO 7 — FEATURE: Duplicar documento](#paso-7)
8. [✅ PASO 8 — FEATURE: Configuración de duración de toast + modo de error en Settings](#paso-8)
9. [✅ PASO 9 — FEATURE: Sección de Notificaciones en Settings](#paso-9)

### FASE D — Auditoría ✅
10. [✅ PASO 10 — Auditoría backend ↔ frontend data flow](#paso-10)

---

## DEPENDENCIAS Y ORDEN

```
FASE A (Bugs Datos) — Prioridad ALTA, ejecutar primero
  PASO 1 ── Logo Welcome (independiente)
  PASO 2 ── Validación cuotas (independiente de PASO 1)

FASE B (Bugs UX) — Prioridad MEDIA
  PASO 3 ── Wizard header (independiente)
  PASO 4 ── Validar en preview modal (independiente)
  PASO 5 ── Error duplicado (independiente, pero prepara PASO 8)

FASE C (Features) — Prioridad NORMAL
  PASO 6 ── Modo lectura wizard (depende de PASO 3 por el header)
  PASO 7 ── Duplicar documento (depende de PASO 6 por el wizard flow)
  PASO 8 ── Config toast/errores (depende de PASO 5)
  PASO 9 ── Notificaciones settings (independiente)

FASE D (Auditoría) — En paralelo con FASE B
  PASO 10 ── Auditoría data flow (se ejecuta investigando mientras se hacen otros pasos)
```

**Ruta crítica:** 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10

---

## ✅ PASO 1 — BUG: Logo en Welcome no persiste / flujo incorrecto {#paso-1}
> **COMPLETADO** — 2026-04-08 · 290 tests ✅ · BUILD SUCCESS

### Diagnóstico

**Problema reportado:** 
- Se carga logo en Welcome "Configura tu Empresa" → al continuar no aparece en la app
- Luego, al ponerlo desde Settings, sí persiste (incluso con hard reset)
- Al volver a `/` se ve el logo en la barra y en la sección hero

**Causa raíz identificada:**

1. **welcome.html L228** — El botón "🚀 Comenzar a Facturar" es un simple `<a th:href="@{/menu}">` sin ningún `onclick` que guarde los datos del formulario. El upload del logo SÍ se envía al backend (línea 650 `ApiClient.uploadLogo()`), pero los campos del formulario (RUC, SOL, email, teléfono) **no se guardan** — simplemente navega directamente a `/menu`.

2. **welcome.html tiene su propio header** (líneas 7-21 con inline header, NO usa el fragment `header.html`). Los IDs `hdr-logo-img` / `hdr-logo-emoji` están duplicados. Cuando `_doOnbLogoUpload()` actualiza el logo, actualiza los elementos del header inline de welcome, que es innecesario — esta es una página de setup.

3. **Logo en hero section** — Las líneas 597-607 y 663-671 de welcome.html actualizan el logo en el hero y en el header inline de welcome. Esto no tiene sentido: welcome es una página de configuración, NO la app. El logo solo debe verse una vez que el usuario ENTRE a la app (`/menu`, `/facturas`, etc.).

4. **`app-state.js` L82-105** — El cache de company en sessionStorage no se invalida tras upload de logo en welcome, por lo que al navegar a `/menu`, puede cargar datos stale sin `tieneLogo: true`.

### Archivos a modificar

#### 1.1 `src/main/resources/templates/welcome.html` — Botón "Comenzar a Facturar"

**Cambio:** Reemplazar link directo por función que guarde los datos del formulario:

```html
<!-- ANTES (L228): -->
<a th:href="@{/menu}" class="btn btn-accent btn-block" style="margin-top:1.5rem;">
    🚀 Comenzar a Facturar
</a>

<!-- DESPUÉS: -->
<button class="btn btn-accent btn-block" style="margin-top:1.5rem;" onclick="onbSaveAndGo()">
    🚀 Comenzar a Facturar
</button>
```

Agregar función `onbSaveAndGo()` en el script:
```javascript
function onbSaveAndGo() {
    var payload = {};
    var el;
    el = document.getElementById('onb-usersol'); if (el && el.value.trim()) payload.usuarioSol = el.value.trim();
    el = document.getElementById('onb-clavesol'); if (el && el.value.trim()) payload.claveSol = el.value.trim();
    el = document.getElementById('onb-email'); if (el && el.value.trim()) payload.emailContacto = el.value.trim();
    el = document.getElementById('onb-phone'); if (el && el.value.trim()) payload.telefono = el.value.trim();
    
    if (ETIA.companyId && Object.keys(payload).length > 0) {
        ApiClient.updateCompany(ETIA.companyId, payload).then(function() {
            sessionStorage.removeItem('etia_company'); // Invalidar cache
            window.location.href = '/menu';
        }).catch(function() {
            window.location.href = '/menu'; // Navegar igual en caso de error
        });
    } else {
        window.location.href = '/menu';
    }
}
```

#### 1.2 `src/main/resources/templates/welcome.html` — Remover logo de hero y header inline

**Cambio:** Eliminar la lógica de display de logo en el hero (líneas 597-607) y en `_doOnbLogoUpload` (líneas 663-671). El logo en welcome solo debe mostrarse en el preview del widget de upload.

- Eliminar bloque L597-607 (`/* Dynamic logo in header + hero */`)
- En `_doOnbLogoUpload`: eliminar líneas que actualizan `hdr-logo-img`, `hdr-logo-emoji`, `onb-hero-logo-img`, `onb-hero-logo-emoji`

#### 1.3 `src/main/resources/static/js/app-state.js` — Invalidar cache tras upload

**Cambio:** En la función que cachea company, asegurar que `ETIA.invalidateLogoCache()` también invalide el cache de company:

```javascript
ETIA.invalidateLogoCache = function() {
    _logoBase64Cache = null;
    sessionStorage.removeItem('etia_company'); // forzar re-fetch
};
```

#### 1.4 Verificar estructura de carpetas de logos

**Verificar:** Que `LogoStorageService.guardarLogo()` cree correctamente:
```
etia_storage/company_logos/{ruc}/logo.{ext}
```

Revisar que el nombre del archivo se construye como `logo` + extensión (NO basado en el RUC como sugiere el reporte: "ruc_logo.formato"). El nombre actual es `logo.png`, `logo.jpg`, o `logo.svg` — esto es correcto según el `LogoStorageService` L68-69.

### Gherkin

```gherkin
Feature: Logo en Welcome persiste correctamente

  Scenario: Logo subido en Welcome está visible al entrar al menú
    Given el usuario está en la vista 3 "Configura tu Empresa"
    And la empresa demo tiene RUC "20601234567"
    When sube un archivo logo.png válido de 200x200px
    And clickea "🚀 Comenzar a Facturar"
    Then los datos del formulario se guardan via API
    And se invalida el cache de sessionStorage
    And al cargar /menu el header muestra el logo subido
    And un hard refresh en /menu mantiene el logo visible

  Scenario: Welcome NO muestra logo en hero ni header
    Given el usuario está en la vista 1 del onboarding
    Then el hero muestra el emoji 🧾 (no un logo de empresa)
    And la barra superior muestra "ETIA" sin logo de empresa

  Scenario: Logo subido en Settings persiste correctamente
    Given el usuario está en /menu con Settings abierto
    When sube un logo desde el panel Empresa
    Then el logo aparece inmediatamente en el header
    And al hacer hard refresh el logo sigue visible
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290 tests, 0 failures
```

---

## ✅ PASO 2 — BUG: Validación inconsistente de cuotas (backend vs frontend) {#paso-2}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Diagnóstico

**Problema reportado:** 
- F001-00000017 se valida como OK desde el modal de la lista
- Pero al cargarlo en el wizard, el detalle lanza: "La suma de las cuotas debe ser igual al total del comprobante. ✗ Suma: 10344.30 ≠ Total: 9923.80"

**Causa raíz identificada:**

1. **La validación backend (`DocumentValidatorService`) NO valida cuotas vs total.** El método `validarFactura()` verifica campos obligatorios, series, moneda, detalle, y totales de montos — pero NO verifica que `∑ cuotas == total` para forma de pago CRÉDITO.

2. **La validación frontend (`items.js` `validateCuotasSum()`) SÍ valida cuotas vs total.** Al cargar el draft en el wizard via `_loadDraftForEdit`, se recalcula el total con `getCurrentTotal()` que puede dar un valor distinto si los cargos/descuentos globales no se cargan correctamente.

3. **Discrepancia de totales (10344.30 vs 9923.80):** Sugiere que el total guardado en la entidad incluye cargos/descuentos que el wizard no carga correctamente al editar. O que `getCurrentTotal()` recalcula sin considerar cargos/descuentos que sí fueron parte del total original.

### Archivos a modificar

#### 2.1 `src/main/java/com/etlions/ms_bs_etia/app_util/validation/SunatValidator.java`

**Agregar método:**
```java
public static void validarSumaCuotas(BigDecimal sumaCuotas, BigDecimal totalComprobante, 
                                       String formaPago, List<String> errores) {
    if (!"CREDITO".equalsIgnoreCase(formaPago)) return;
    if (sumaCuotas == null || totalComprobante == null) return;
    if (sumaCuotas.compareTo(totalComprobante) != 0) {
        errores.add("La suma de las cuotas (" + sumaCuotas + ") debe ser igual al total del comprobante (" 
                     + totalComprobante + ")");
    }
}
```

#### 2.2 `src/main/java/com/etlions/ms_bs_etia/doc_factura/service/impl/DocFacturaServiceImpl.java`

**En el método de validación (o donde se invoca `DocumentValidatorService`):**
- Cargar cuotas de la entidad: `cuotaCreditoRepository.findByFacturaIdOrderByNumeroCuota(facturaId)`
- Sumar montos: `cuotas.stream().map(CuotaCreditoEntity::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add)`
- Llamar `SunatValidator.validarSumaCuotas(sumaCuotas, entity.getTotal(), entity.getFormaPago(), errores)`

#### 2.3 `src/main/resources/static/js/wizard.js` — `_loadDraftForEdit`

**Auditar:** Verificar que al cargar un draft para edición:
1. Los cargos/descuentos globales se cargan correctamente en el array `cargosDescuentos`
2. `getCurrentTotal()` los incluye en el cálculo
3. Las cuotas se cargan con sus montos originales

**Posible fix:** Si `getCurrentTotal()` no coincide con el total guardado, usar el total de la entidad (`data.total`) como referencia para la comparación de cuotas, NO el total recalculado.

#### 2.4 Auditoría del documento F001-00000017

**Investigar en data.sql o via API:**
- ¿Cuál es el total guardado en la entidad?
- ¿Cuáles son las cuotas guardadas y su suma?
- ¿Hay cargos/descuentos globales que expliquen la diferencia?

### Tests a agregar

#### 2.5 `src/test/java/com/etlions/ms_bs_etia/app_util/validation/SunatValidatorTest.java`

```java
@Nested
@DisplayName("validarSumaCuotas")
class ValidarSumaCuotas {
    
    @Test
    @DisplayName("CREDITO con cuotas que suman total → sin error")
    void creditoCuotasIgualesTotal() {
        var errores = new ArrayList<String>();
        SunatValidator.validarSumaCuotas(
            new BigDecimal("1000.00"), new BigDecimal("1000.00"), "CREDITO", errores);
        assertTrue(errores.isEmpty());
    }
    
    @Test
    @DisplayName("CREDITO con cuotas que NO suman total → error")
    void creditoCuotasDistintasTotal() {
        var errores = new ArrayList<String>();
        SunatValidator.validarSumaCuotas(
            new BigDecimal("800.00"), new BigDecimal("1000.00"), "CREDITO", errores);
        assertEquals(1, errores.size());
        assertTrue(errores.get(0).contains("suma de las cuotas"));
    }
    
    @Test
    @DisplayName("CONTADO ignora validación de cuotas")
    void contadoIgnoraCuotas() {
        var errores = new ArrayList<String>();
        SunatValidator.validarSumaCuotas(
            new BigDecimal("500.00"), new BigDecimal("1000.00"), "CONTADO", errores);
        assertTrue(errores.isEmpty());
    }
}
```

### Gherkin

```gherkin
Feature: Validación consistente de cuotas backend y frontend

  Scenario: Validar factura CRÉDITO con cuotas que suman total
    Given factura F001-00000018 con formaPago "CREDITO" y total 1000.00
    And tiene 2 cuotas: 500.00 y 500.00
    When se invoca validarFactura
    Then el resultado es valid: true

  Scenario: Validar factura CRÉDITO con cuotas que NO suman total
    Given factura F001-00000019 con formaPago "CREDITO" y total 1000.00
    And tiene 2 cuotas: 600.00 y 500.00
    When se invoca validarFactura
    Then el resultado es valid: false
    And errores contiene "La suma de las cuotas"

  Scenario: Wizard carga draft con cargos/descuentos correctamente
    Given factura F001-00000017 con total 9923.80 y cargos/descuentos
    When se abre en /wizard?type=FACTURA&id={id}
    Then getCurrentTotal() retorna 9923.80
    And las cuotas suman 9923.80 (no 10344.30)
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290 + 3 new tests, 0 failures
```

---

## ✅ PASO 3 — BUG: Rediseño header del Wizard (estilo stock-ticker) {#paso-3}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Problema
El banner actual `wiz-editing-banner` (L12-16 de factura.html) es minúsculo:
```html
<div class="warn-banner" style="font-size:0.82rem;">
    ✏️ <strong>Editando</strong> <span class="badge badge-blue">F001-00000017</span>
</div>
```
No transmite la importancia del documento. Necesita ser prominente, estilo "bolsa de valores".

### Archivos a modificar

#### 3.1 `src/main/resources/templates/wizard/factura.html` (L12-16)

**Reemplazar** el `wiz-editing-banner` por un componente prominente:

```html
<!-- Wizard Document Header — Stock Ticker Style -->
<div id="wiz-doc-header" style="display:none;margin-bottom:1.2rem;">
    <div class="wiz-ticker">
        <div class="wiz-ticker-left">
            <span class="wiz-ticker-serie" id="wiz-ticker-serie">F001</span>
            <span class="wiz-ticker-sep">-</span>
            <span class="wiz-ticker-numero" id="wiz-ticker-numero">00000000</span>
        </div>
        <div class="wiz-ticker-right">
            <span class="wiz-ticker-badge" id="wiz-ticker-badge">NUEVO</span>
            <span class="wiz-ticker-mode" id="wiz-ticker-mode">✨ Creación</span>
        </div>
    </div>
</div>
```

#### 3.2 `src/main/resources/static/css/main.css` — Estilos stock-ticker

```css
/* ─── Wizard Stock Ticker Header ─── */
.wiz-ticker {
    display: flex; justify-content: space-between; align-items: center;
    background: var(--bg-secondary); border: 1px solid var(--border);
    border-radius: var(--radius); padding: 1.2rem 2rem;
    position: relative; overflow: hidden;
}
.wiz-ticker::before {
    content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
    background: linear-gradient(90deg, var(--accent), var(--info));
}
.wiz-ticker-left {
    display: flex; align-items: baseline; gap: 0.1rem;
    font-family: var(--font-mono);
}
.wiz-ticker-serie {
    font-size: 1.8rem; font-weight: 800; color: var(--text-muted);
    letter-spacing: 2px;
}
.wiz-ticker-sep {
    font-size: 1.8rem; color: var(--text-muted); margin: 0 0.15rem;
}
.wiz-ticker-numero {
    font-size: 2.4rem; font-weight: 900; color: var(--accent);
    letter-spacing: 1px;
}
.wiz-ticker-right {
    display: flex; align-items: center; gap: 0.8rem;
}
.wiz-ticker-badge {
    font-size: 0.9rem; font-weight: 700; padding: 0.4rem 1.2rem;
    border-radius: 20px; text-transform: uppercase; letter-spacing: 1px;
    font-family: var(--font-mono);
}
.wiz-ticker-mode {
    font-size: 0.82rem; color: var(--text-secondary);
}
/* Badge colors por estado */
.wiz-ticker-badge.tk-nuevo { background: var(--accent-glow); color: var(--accent); border: 1px solid var(--accent); }
.wiz-ticker-badge.tk-borrador { background: rgba(139,148,158,0.1); color: var(--text-secondary); border: 1px solid var(--border); }
.wiz-ticker-badge.tk-validado { background: rgba(88,166,255,0.1); color: var(--info); border: 1px solid var(--info); }
.wiz-ticker-badge.tk-con-error { background: rgba(248,81,73,0.1); color: var(--danger); border: 1px solid var(--danger); }
.wiz-ticker-badge.tk-enviada { background: rgba(88,166,255,0.1); color: var(--info); border: 1px solid var(--info); }
.wiz-ticker-badge.tk-aceptada { background: rgba(0,255,136,0.1); color: var(--accent); border: 1px solid var(--accent); }
.wiz-ticker-badge.tk-rechazada { background: rgba(248,81,73,0.1); color: var(--danger); border: 1px solid var(--danger); }
```

#### 3.3 `src/main/resources/static/js/wizard.js` — Actualizar `_loadDraftForEdit` y `initWizard`

**En `initWizard()`** (modo creación):
```javascript
var tickerEl = document.getElementById('wiz-doc-header');
if (tickerEl) {
    tickerEl.style.display = 'block';
    document.getElementById('wiz-ticker-serie').textContent = selectedSerie || 'F001';
    document.getElementById('wiz-ticker-numero').textContent = '(Nuevo)';
    var badge = document.getElementById('wiz-ticker-badge');
    badge.textContent = 'NUEVO'; badge.className = 'wiz-ticker-badge tk-nuevo';
    document.getElementById('wiz-ticker-mode').textContent = '✨ Creación';
}
```

**En `_loadDraftForEdit()`** (modo edición):
```javascript
var tickerEl = document.getElementById('wiz-doc-header');
if (tickerEl) {
    tickerEl.style.display = 'block';
    document.getElementById('wiz-ticker-serie').textContent = data.serie || '';
    document.getElementById('wiz-ticker-numero').textContent = 
        String(data.numero || '').padStart(8, '0');
    var badge = document.getElementById('wiz-ticker-badge');
    var estadoMap = { 'BORRADOR':'tk-borrador', 'VALIDADO':'tk-validado', 
                      'CON_ERROR':'tk-con-error', 'ENVIADA':'tk-enviada',
                      'ACEPTADA':'tk-aceptada', 'RECHAZADA':'tk-rechazada' };
    badge.textContent = data.estado || 'BORRADOR';
    badge.className = 'wiz-ticker-badge ' + (estadoMap[data.estado] || 'tk-borrador');
    document.getElementById('wiz-ticker-mode').textContent = '✏️ Edición';
}
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 4 — BUG: Botón "Validar" faltante en modal preview para ciertos estados {#paso-4}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Problema
En `doc-preview.js`:
- Estado `RECHAZADA` (facturas, L379-383): solo muestra "Corregir y Re-emitir" pero NO el botón Validar
- Estado `VALIDADO` (facturas, L360-365): NO muestra botón Validar (el usuario podría querer re-validar)
- Boletas: estado `RECHAZADA` no tiene case en el switch

### Archivos a modificar

#### 4.1 `src/main/resources/static/js/doc-preview.js` — Facturas switch

**Agregar `validateBtn` en RECHAZADA:**
```javascript
case 'RECHAZADA':
    editBtn.style.display = 'inline-flex';
    editBtn.textContent = '🔧 Corregir y Re-emitir';
    editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId);
    if (validateBtn) { validateBtn.style.display = 'inline-flex'; validateBtn.textContent = '🔍 Re-validar'; }
    break;
```

**Agregar `validateBtn` en VALIDADO:**
```javascript
case 'VALIDADO':
    editBtn.style.display = 'inline-flex';
    editBtn.textContent = '✏️ Editar';
    editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId);
    if (validateBtn) { validateBtn.style.display = 'inline-flex'; validateBtn.textContent = '🔍 Re-validar'; }
    emitBtn.style.display = 'inline-flex';
    if (deleteBtn) deleteBtn.style.display = 'inline-flex';
    break;
```

#### 4.2 `src/main/resources/static/js/doc-preview.js` — Boletas switch

**Agregar case RECHAZADA para boletas:**
```javascript
case 'RECHAZADA':
    editBtn.style.display = 'inline-flex';
    editBtn.textContent = '🔧 Corregir';
    editBtn.href = '/wizard?type=BOLETA&id=' + encodeURIComponent(_currentDocId);
    if (validateBtn) { validateBtn.style.display = 'inline-flex'; validateBtn.textContent = '🔍 Re-validar'; }
    break;
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 5 — BUG: Error duplicado toast + modal en Wizard {#paso-5}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Problema
Cuando hay un error HTTP (ej: intentar actualizar factura ENVIADA):
1. **Toast** (vía `error-handler.js`): muestra error detallado con body, URL, captura de pantalla → MUY ÚTIL
2. **Modal** (vía `wizard.js` `_showResultModal`): muestra solo "❌ Error de conexión" con botón "🔄 Nuevo Documento" → INÚTIL y confuso

Ambos aparecen simultáneamente. El modal bloquea la interacción y ofrece "Nuevo Documento" que es absurdo como respuesta a un error.

### Archivos a modificar

#### 5.1 `src/main/resources/static/js/wizard.js` — `_showResultModal`

**Cambio:** Cuando `isOk === false`, NO mostrar el modal. Solo re-habilitar botones y dejar que el toast maneje la comunicación:

```javascript
function _showResultModal(isOk, emitir, dataOrErr) {
    if (!isOk || (isOk && dataOrErr && dataOrErr.isSuccess === false)) {
        // Error: NO mostrar modal, solo re-habilitar botones
        // El toast del error-handler.js ya muestra el error detallado
        _resetButtons(
            document.querySelector('.spinner'),
            document.getElementById('btn-emitir'),
            document.getElementById('btn-guardar')
        );
        return;
    }
    // Solo mostrar modal para ÉXITO
    var card = document.getElementById('modal-card');
    var title = document.getElementById('modal-title');
    card.className = 'modal-card success';
    if (editMode && emitir) {
        title.innerHTML = '✅ <span style="color:var(--accent);">Borrador emitido exitosamente</span>';
    } else if (emitir) {
        title.innerHTML = '✅ <span style="color:var(--accent);">Comprobante emitido exitosamente</span>';
    } else {
        title.innerHTML = '💾 <span style="color:var(--accent);">Borrador guardado exitosamente</span>';
    }
    if (ETIA.refreshNotifications) ETIA.refreshNotifications();
    document.getElementById('modal-body').textContent = JSON.stringify(dataOrErr, null, 2);
    document.getElementById('modal').classList.add('show');
}
```

#### 5.2 `src/main/resources/static/js/error-handler.js` — Duración configurable (preparación PASO 8)

**Cambio:** Leer duración desde `ETIA.config`:

```javascript
/* Auto-dismiss */
var duration = (ETIA.config && ETIA.config.toastDurationError) 
    ? ETIA.config.toastDurationError * 1000 
    : 12000;
setTimeout(function() { ... }, duration);
```

Mismo para success toast:
```javascript
var duration = (ETIA.config && ETIA.config.toastDurationSuccess) 
    ? ETIA.config.toastDurationSuccess * 1000 
    : 4000;
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 6 — FEATURE: Modo lectura (read-only) en Wizard {#paso-6}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Objetivo
Permitir ver un documento en el wizard con todos los controles bloqueados, sin opción de editar/emitir/guardar.

### Archivos a modificar

#### 6.1 `src/main/resources/templates/wizard/factura.html` (L437-453)

**Cambio:** Leer parámetro `mode` de la URL:
```javascript
var docType = params.get('type') || 'FACTURA';
var editId  = params.get('id') || null;
var viewMode = params.get('mode') === 'view';
// ...
initWizard(docType, editId, viewMode);
```

#### 6.2 `src/main/resources/static/js/wizard.js` — `initWizard` y nueva función `_applyReadOnlyMode`

**Cambio en `initWizard(docType, editId, viewMode)`:**
```javascript
if (editId) {
    _loadDraftForEdit(editId).then(function() {
        if (viewMode) _applyReadOnlyMode();
    });
} 
```

**Nueva función `_applyReadOnlyMode()`:**
```javascript
function _applyReadOnlyMode() {
    // Disable all inputs, selects, textareas, buttons (excepto navegación)
    document.querySelectorAll('.wiz-panel input, .wiz-panel select, .wiz-panel textarea')
        .forEach(function(el) { el.disabled = true; el.style.opacity = '0.7'; });
    
    // Ocultar botones de acción
    ['btn-emitir', 'btn-guardar', 'btn-add-item', 'btn-add-cuota', 'btn-add-cargo']
        .forEach(function(id) { var el = document.getElementById(id); if (el) el.style.display = 'none'; });
    
    // Ocultar botones de eliminar items
    document.querySelectorAll('.item-remove, .cuota-remove, .ac-remove')
        .forEach(function(el) { el.style.display = 'none'; });
    
    // Actualizar ticker
    var mode = document.getElementById('wiz-ticker-mode');
    if (mode) mode.textContent = '👁️ Modo Lectura';
}
```

#### 6.3 `src/main/resources/static/js/doc-preview.js` — Agregar botón "Ver en Wizard"

**Para estados terminales** (ACEPTADA, ANULADA, ENVIADA):
```javascript
case 'ACEPTADA':
    annulBtn.style.display = 'inline-flex';
    // Nuevo: botón ver
    if (editBtn) {
        editBtn.style.display = 'inline-flex';
        editBtn.textContent = '👁️ Ver detalle';
        editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId) + '&mode=view';
    }
    break;
```

### Gherkin

```gherkin
Feature: Modo lectura en Wizard

  Scenario: Ver documento en modo lectura
    Given existe factura F001-00000017 en estado ACEPTADA
    When navego a /wizard?type=FACTURA&id={id}&mode=view
    Then todos los inputs y selects están deshabilitados
    And no se muestran botones "Emitir" ni "Guardar"
    And el ticker muestra "👁️ Modo Lectura"
    And se pueden navegar todos los steps del wizard

  Scenario: No se puede modificar datos en modo lectura
    Given estoy viendo factura F001-00000017 en modo lectura
    When intento cambiar el campo "Moneda"
    Then el select está deshabilitado y no permite cambio
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 7 — FEATURE: Duplicar documento {#paso-7}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Objetivo
Crear un clon de un documento existente como nuevo BORRADOR, permitiendo seleccionar qué datos copiar.

### Archivos a modificar

#### 7.1 `src/main/resources/static/js/doc-preview.js` — Agregar botón "Duplicar"

**En la sección de exports** (junto a JSON/XML/PDF), agregar botón visible para TODOS los estados:
```javascript
var dupBtn = document.getElementById('dp-duplicate-btn');
if (dupBtn) dupBtn.style.display = 'inline-flex';
```

#### 7.2 `src/main/resources/templates/fragments/doc-preview.html` — Agregar botón y dropdown

**Agregar en la zona de botones de export:**
```html
<button class="dp-action dp-action-dup" id="dp-duplicate-btn" style="display:none;" 
        onclick="DocPreview.showDuplicateOptions()">📄 Duplicar</button>
```

**Agregar dropdown/mini-modal de opciones:**
```html
<div id="dp-dup-options" style="display:none;...">
    <p style="font-weight:600;margin-bottom:0.5rem;">📄 Duplicar — ¿Qué copiar?</p>
    <label><input type="checkbox" id="dp-dup-cliente" checked/> 👤 Cliente</label>
    <label><input type="checkbox" id="dp-dup-detalle" checked/> 📋 Detalle (ítems)</label>
    <label><input type="checkbox" id="dp-dup-cuotas"/> 💳 Cuotas</label>
    <label><input type="checkbox" id="dp-dup-cargos"/> 📊 Cargos/Descuentos</label>
    <button class="btn btn-accent" onclick="DocPreview.executeDuplicate()">✨ Crear Clon</button>
</div>
```

#### 7.3 `src/main/resources/static/js/doc-preview.js` — Funciones de duplicación

```javascript
DocPreview.showDuplicateOptions = function() {
    document.getElementById('dp-dup-options').style.display = 'block';
};

DocPreview.executeDuplicate = function() {
    var fields = [];
    if (document.getElementById('dp-dup-cliente').checked) fields.push('cliente');
    if (document.getElementById('dp-dup-detalle').checked) fields.push('detalle');
    if (document.getElementById('dp-dup-cuotas').checked) fields.push('cuotas');
    if (document.getElementById('dp-dup-cargos').checked) fields.push('cargos');
    
    var type = _currentDocType === 'factura' ? 'FACTURA' : 'BOLETA';
    window.location.href = '/wizard?type=' + type 
        + '&duplicate=' + encodeURIComponent(_currentDocId) 
        + '&fields=' + fields.join(',');
};
```

#### 7.4 `src/main/resources/static/js/wizard.js` — Leer parámetro `duplicate`

**En `initWizard()`:**
```javascript
var duplicateId = params.get('duplicate');
var duplicateFields = (params.get('fields') || '').split(',');

if (duplicateId) {
    _loadDocumentForDuplicate(duplicateId, duplicateFields);
}
```

**Nueva función `_loadDocumentForDuplicate(docId, fields)`:**
- Cargar documento fuente via API
- Poblar solo los campos seleccionados:
  - `cliente`: tipoDoc, numDoc, denominación, dirección
  - `detalle`: copiar array de items (sin IDs)
  - `cuotas`: copiar cuotas (sin IDs, ajustar fechas)
  - `cargos`: copiar cargos/descuentos (sin IDs)
- NO copiar: serie, número, fecha, estado, ID
- Setear serie por defecto, estado visual "NUEVO"
- Actualizar ticker: "📄 Clonado de F001-00000017"

### Gherkin

```gherkin
Feature: Duplicar documento

  Scenario: Duplicar factura con cliente y detalle
    Given factura F001-00000017 con cliente "EMPRESA SAC" y 3 ítems
    When abro el modal preview y clickeo "📄 Duplicar"
    And selecciono "Cliente" y "Detalle"
    And clickeo "Crear Clon"
    Then se abre el wizard en modo creación
    And el cliente es "EMPRESA SAC"
    And el detalle tiene los mismos 3 ítems
    And serie/número son los predeterminados (nuevo)
    And el estado es "NUEVO"

  Scenario: Duplicar factura solo con detalle
    Given factura F001-00000017 con 3 ítems
    When duplico seleccionando solo "Detalle"
    Then los campos de cliente están vacíos
    And el detalle tiene los mismos 3 ítems

  Scenario: Duplicar factura con todo
    Given factura con cliente, detalle, cuotas y cargos
    When duplico seleccionando todos los checkboxes
    Then todos los datos se copian excepto serie/número/fecha/estado
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 8 — FEATURE: Configuración de duración de toast + modo de error {#paso-8}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Objetivo
Permitir al usuario configurar la duración de los toasts y elegir el modo de display de errores.

### Archivos a modificar

#### 8.1 `src/main/resources/templates/fragments/config-overlay.html` — Panel Preferencias

**Agregar al panel `cfg-panel-preferencias`** (después de tema y moneda):

```html
<!-- ─── Notificaciones de error ─── -->
<div style="margin-top:1.5rem;border-top:1px solid var(--border);padding-top:1.5rem;">
    <h3 style="font-size:0.85rem;font-weight:700;color:var(--accent);margin-bottom:1rem;">
        🔔 Notificaciones y Errores
    </h3>
    
    <div class="cfg-field">
        <label class="cfg-field-label">Duración toast de error (segundos)</label>
        <input type="number" id="cfg-toast-error-duration" min="3" max="60" value="12" step="1"/>
        <span class="cfg-field-hint">Cuánto tiempo se muestra el toast de error. 0 = no auto-cerrar.</span>
    </div>
    
    <div class="cfg-field">
        <label class="cfg-field-label">Duración toast de éxito (segundos)</label>
        <input type="number" id="cfg-toast-success-duration" min="1" max="30" value="4" step="1"/>
    </div>
    
    <div class="cfg-field">
        <label class="cfg-field-label">Modo de notificación de errores</label>
        <select id="cfg-error-display-mode">
            <option value="toast">🔔 Solo Toast (auto-desaparece)</option>
            <option value="banner">📌 Solo Banner persistente</option>
            <option value="both">🔔📌 Ambos (Toast + Banner)</option>
        </select>
        <span class="cfg-field-hint">El banner persistente queda visible hasta que el usuario lo cierre.</span>
    </div>
</div>
```

#### 8.2 `src/main/resources/static/js/app-state.js` — Config defaults

**Agregar a `ETIA.config`:**
```javascript
ETIA.config = ETIA.config || {};
ETIA.config.toastDurationError = parseInt(localStorage.getItem('etia_toast_error_dur') || '12');
ETIA.config.toastDurationSuccess = parseInt(localStorage.getItem('etia_toast_success_dur') || '4');
ETIA.config.errorDisplayMode = localStorage.getItem('etia_error_mode') || 'toast';
```

#### 8.3 `src/main/resources/static/js/config-overlay.js` — Save/Load de las nuevas preferencias

**En la función que carga datos al abrir settings:**
```javascript
document.getElementById('cfg-toast-error-duration').value = ETIA.config.toastDurationError || 12;
document.getElementById('cfg-toast-success-duration').value = ETIA.config.toastDurationSuccess || 4;
document.getElementById('cfg-error-display-mode').value = ETIA.config.errorDisplayMode || 'toast';
```

**En la función de guardar:**
```javascript
var errDur = parseInt(document.getElementById('cfg-toast-error-duration').value) || 12;
var sucDur = parseInt(document.getElementById('cfg-toast-success-duration').value) || 4;
var errMode = document.getElementById('cfg-error-display-mode').value || 'toast';
localStorage.setItem('etia_toast_error_dur', errDur);
localStorage.setItem('etia_toast_success_dur', sucDur);
localStorage.setItem('etia_error_mode', errMode);
ETIA.config.toastDurationError = errDur;
ETIA.config.toastDurationSuccess = sucDur;
ETIA.config.errorDisplayMode = errMode;
```

#### 8.4 `src/main/resources/static/js/error-handler.js` — Implementar modos

**Cambio en `ETIA.showError`:**
```javascript
var mode = (ETIA.config && ETIA.config.errorDisplayMode) || 'toast';
if (mode === 'toast' || mode === 'both') {
    // Mostrar toast (código actual)
}
if (mode === 'banner' || mode === 'both') {
    // Crear banner persistente inline
    _showPersistentBanner(detail);
}
```

**Nueva función `_showPersistentBanner(detail)`:**
Crea un banner fijo en la parte superior del contenido principal que NO desaparece automáticamente, con botón ✕ para cerrar.

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 9 — FEATURE: Sección de Notificaciones en Settings {#paso-9}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Objetivo
Agregar un tab "🔔 Notificaciones" al panel de Settings con configuración de notificaciones por email/SMS (placeholder para integración futura).

### Archivos a modificar

#### 9.1 `src/main/resources/templates/fragments/config-overlay.html`

**Agregar tab en sidebar (después de Credenciales SUNAT):**
```html
<div class="cfg-sidebar-item" data-tab="notificaciones" onclick="switchConfigTab('notificaciones')">
    <span class="cfg-icon">🔔</span>
    <span class="cfg-label">Notificaciones</span>
    <span class="cfg-arrow">›</span>
</div>
```

**Agregar panel:**
```html
<!-- ══ PANEL: Notificaciones ══ -->
<div class="cfg-panel" id="cfg-panel-notificaciones">
    <div style="background:var(--accent-glow);border:1px solid var(--accent);border-radius:var(--radius-sm);padding:1rem;margin-bottom:1.5rem;">
        <p style="font-size:0.82rem;color:var(--accent);margin:0;">
            📧 <strong>Próximamente:</strong> Recibirás alertas cuando SUNAT responda tus comprobantes.
        </p>
    </div>
    
    <div class="cfg-field">
        <label class="cfg-field-label">📧 Notificaciones por email</label>
        <div class="cfg-theme-toggle" style="margin-top:0.4rem;">
            <div class="cfg-theme-option" id="cfg-notif-email-on" onclick="toggleNotifEmail(true)">
                <span class="cfg-th-icon">✅</span>
                <span class="cfg-th-label">Activado</span>
            </div>
            <div class="cfg-theme-option active" id="cfg-notif-email-off" onclick="toggleNotifEmail(false)">
                <span class="cfg-th-icon">⛔</span>
                <span class="cfg-th-label">Desactivado</span>
            </div>
        </div>
    </div>
    
    <div class="cfg-field">
        <label class="cfg-field-label">Email para notificaciones</label>
        <input type="email" id="cfg-notif-email" placeholder="facturacion@empresa.com"/>
        <span class="cfg-field-hint">Se enviará a este correo cuando SUNAT responda tus comprobantes</span>
    </div>
    
    <div class="cfg-field">
        <label class="cfg-field-label">📱 Notificaciones por SMS</label>
        <div class="cfg-theme-toggle" style="margin-top:0.4rem;">
            <div class="cfg-theme-option" id="cfg-notif-sms-on" onclick="toggleNotifSms(true)">
                <span class="cfg-th-icon">✅</span>
                <span class="cfg-th-label">Activado</span>
            </div>
            <div class="cfg-theme-option active" id="cfg-notif-sms-off" onclick="toggleNotifSms(false)">
                <span class="cfg-th-icon">⛔</span>
                <span class="cfg-th-label">Desactivado</span>
            </div>
        </div>
    </div>
    
    <div class="cfg-field">
        <label class="cfg-field-label">Teléfono para SMS</label>
        <input type="tel" id="cfg-notif-phone" placeholder="+51 999 888 777"/>
    </div>

    <h3 style="font-size:0.8rem;font-weight:700;color:var(--accent);margin:1.5rem 0 0.8rem;border-top:1px solid var(--border);padding-top:1rem;">
        📋 Tipos de notificación
    </h3>
    
    <div class="cfg-field">
        <label><input type="checkbox" id="cfg-notif-aceptada" checked/> ✅ Comprobante aceptado por SUNAT</label>
    </div>
    <div class="cfg-field">
        <label><input type="checkbox" id="cfg-notif-rechazada" checked/> ❌ Comprobante rechazado por SUNAT</label>
    </div>
    <div class="cfg-field">
        <label><input type="checkbox" id="cfg-notif-resumen" checked/> 📊 Resumen diario procesado</label>
    </div>
    <div class="cfg-field">
        <label><input type="checkbox" id="cfg-notif-cert-expiry"/> 📜 Certificado próximo a vencer</label>
    </div>

    <div class="cfg-save-bar">
        <button class="btn btn-accent" onclick="saveNotifPreferences()">💾 Guardar preferencias</button>
    </div>
</div>
```

#### 9.2 `src/main/resources/static/js/config-overlay.js`

**Agregar funciones:**
```javascript
function toggleNotifEmail(on) { ... }
function toggleNotifSms(on) { ... }
function saveNotifPreferences() { ... } // localStorage por ahora
function loadNotifPreferences() { ... }
```

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → 290+ tests, 0 failures
```

---

## ✅ PASO 10 — Auditoría backend ↔ frontend data flow {#paso-10}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Objetivo
Verificar que todos los campos del API están correctamente mapeados entre swagger → backend DTOs → frontend JS.

### Resultados de la auditoría

| # | Verificación | Estado | Hallazgo |
|---|-------------|--------|----------|
| 1 | `CompanyData.logoFormat` llega al frontend | ⚠️ NO USADO | `logoFormat` existe en Entity/DTO pero NO se usa en ningún JS. No es bug — el frontend obtiene el logo via endpoint `/company/logo` que retorna la imagen directamente con content-type correcto. |
| 2 | `FacturaData.cuotas` se incluye en GET response | ✅ OK | `buildFacturaData()` enriquece con cuotas via `enrichFacturaData()`. Se usa en listar (L380) y obtener individual (L402). |
| 3 | `FacturaData.cargosDescuentos` se incluye en GET | ✅ OK | Mismo `enrichFacturaData()` enriquece con cargos/descuentos. Incluidos en GET list y GET individual. |
| 4 | `detalle` vs `detalleJson` consistencia | ✅ OK | FacturaMapper L41 mapea `detalleJson` (entity) → `detalle` (DTO). Frontend usa `doc.detalle \|\| doc.detalleJson` como fallback defensivo — correcto. |
| 5 | `totalCargos` / `totalDescuentos` en response | ✅ OK | Campos existen en Entity (L80), Swagger, y DTO. MapStruct los mapea automáticamente (no están en ignore list). |
| 6 | `exoneradas` / `inafectas` en response | ✅ OK | Campos existen en Entity (L69, L71), Swagger, y DTO. MapStruct los mapea automáticamente. |
| 7 | `LogoStorageService` estructura de carpetas | ✅ OK | L68-72: guarda como `etia_storage/company_logos/{ruc}/logo.{ext}`. Ejemplo: `company_logos/20601234567/logo.jpg`. |
| 8 | `config-overlay.js` lee `logoFormat` | ⚠️ NO USADO | No se usa `logoFormat`. El file input tiene `accept` hardcodeado en HTML. No es bug — el accept correcto ya filtra PNG/JPG/SVG. |

### Discrepancias encontradas
**Ninguna discrepancia crítica.** Los 2 puntos marcados ⚠️ son campos disponibles pero no consumidos en frontend — no causan bugs. El campo `logoFormat` podría usarse en el futuro para optimizar la carga de logos, pero actualmente el endpoint `/company/logo` resuelve esto correctamente.

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → todos los tests pasan
```

---

## ✅ PASO 10.1 — MEJORA: Selector de tipo de imagen + validación + render óptimo de logo {#paso-10-1}
> **COMPLETADO** — 2026-04-08 · 295 tests ✅ · BUILD SUCCESS

### Problema
1. El upload de logo acepta cualquier formato (PNG/JPG/SVG) sin que el usuario elija primero → no hay control.
2. No se valida que el contenido real del archivo coincida con la extensión.
3. El `logoFormat` guardado en backend NO se usa en frontend para optimizar el render (SVG debería renderizarse como vector, rasters como bitmap con `object-fit`).

### Solución

#### A) Selector de tipo ANTES del upload
- Agregar `<select>` con opciones: PNG, JPG, SVG
- El `accept` del `<input type="file">` se ajusta dinámicamente según la selección
- Al seleccionar tipo, se actualiza la validación permitida

#### B) Validación de contenido
- Verificar que la extensión del archivo coincida con el tipo seleccionado
- Para PNG: validar magic bytes `89 50 4E 47`
- Para JPG: validar magic bytes `FF D8 FF`
- Para SVG: verificar que contenga `<svg`

#### C) Render optimizado según `logoFormat`
- Leer `company.logoFormat` del response
- SVG → `<img>` con `width:100%;height:100%` sin `object-fit` (escala vectorial)
- PNG/JPG → `<img>` con `object-fit:contain` (raster)

### Archivos a modificar
1. `config-overlay.html` — Agregar selector de tipo antes del logo wrap
2. `config-overlay.js` — Leer tipo seleccionado, validar magic bytes, ajustar accept
3. `app-state.js` — Render del logo en header según `logoFormat`
4. `welcome.html` — Mismo selector de tipo en onboarding

### Verificación
```
mvn clean compile → BUILD SUCCESS
mvn test → todos los tests pasan
```

---

## RESUMEN DE ARCHIVOS

### Archivos a CREAR

| # | Archivo | Paso |
|---|---------|------|
| — | Ningún archivo nuevo necesario | — |

### Archivos a MODIFICAR

| # | Archivo | Pasos |
|---|---------|-------|
| 1 | `templates/welcome.html` | PASO 1 |
| 2 | `static/js/app-state.js` | PASO 1, PASO 8 |
| 3 | `app_util/validation/SunatValidator.java` | PASO 2 |
| 4 | `doc_factura/service/impl/DocFacturaServiceImpl.java` | PASO 2 |
| 5 | `static/js/wizard.js` | PASO 2, 3, 5, 6, 7 |
| 6 | `templates/wizard/factura.html` | PASO 3, 6 |
| 7 | `static/css/main.css` | PASO 3 |
| 8 | `static/js/doc-preview.js` | PASO 4, 6, 7 |
| 9 | `static/js/error-handler.js` | PASO 5, 8 |
| 10 | `templates/fragments/config-overlay.html` | PASO 8, 9 |
| 11 | `static/js/config-overlay.js` | PASO 8, 9 |
| 12 | `templates/fragments/doc-preview.html` | PASO 7 |
| 13 | `test/.../SunatValidatorTest.java` | PASO 2 |

### Tests a crear/modificar

| # | Test | Paso |
|---|------|------|
| 1 | `SunatValidatorTest.validarSumaCuotas` (3 tests) | PASO 2 |
| 2 | Integración: validación con cuotas en DocFacturaServiceImplTest | PASO 2 |

### Esfuerzo estimado por paso

| Paso | Descripción | Esfuerzo | Riesgo |
|------|------------|----------|--------|
| PASO 1 | Logo Welcome fix | 🟢 45 min | Bajo |
| PASO 2 | Validación cuotas backend | 🟡 2-3h | Medio (lógica de negocio) |
| PASO 3 | Wizard stock-ticker header | 🟢 1h | Bajo |
| PASO 4 | Validar btn en modal | 🟢 20 min | Bajo |
| PASO 5 | Error duplicado fix | 🟢 45 min | Bajo |
| PASO 6 | Modo lectura wizard | 🟡 1.5-2h | Medio |
| PASO 7 | Duplicar documento | 🟡 2-3h | Medio (flujo complejo) |
| PASO 8 | Config toast + errores | 🟡 1.5-2h | Medio |
| PASO 9 | Notificaciones settings | 🟢 1h | Bajo |
| PASO 10 | Auditoría data flow | 🟡 2-3h | Medio (investigación) |

**Total estimado: ~13-18 horas**

---

> **✅ FASE 3 COMPLETADA** — 2026-04-08  
> Build: 295 tests ✅ · 0 failures · BUILD SUCCESS  
> Todos los 10 PASOs completados exitosamente.  
> Auditoría (PASO 10): 0 discrepancias críticas encontradas.












