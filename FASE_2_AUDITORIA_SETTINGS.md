# 🔍 AUDITORÍA FASE 2 — Settings, Frontend & Actividades Pendientes

> **Fecha:** 2026-04-08  
> **Autor:** Copilot (a pedido del usuario)  
> **Propósito:** Mapear todo lo relacionado con Settings que no se ejecutó, proponer mejoras, listar cambios frontend verificables, y documentar esta "fase improvisada".

---

## TABLA DE CONTENIDOS

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Auditoría Completa del Panel Settings](#2-auditoría-completa-del-panel-settings)
3. [Lo que SÍ se implementó en Settings (FASE 2)](#3-lo-que-sí-se-implementó)
4. [Lo que NO se implementó / Gaps detectados](#4-gaps-detectados)
5. [Propuestas de mejora para Settings](#5-propuestas-de-mejora)
6. [Auditoría del Logo en el Inicio (Welcome)](#6-auditoría-logo-inicio)
7. [Auditoría de Notificaciones](#7-auditoría-notificaciones)
8. [Mapa de Cambios Frontend de FASE 2 — Dónde Probar](#8-mapa-de-cambios-frontend)
9. [Checklist de Verificación Visual (QA Manual)](#9-checklist-verificación-visual)
10. [Resumen de la "Fase Improvisada"](#10-resumen-fase-improvisada)

---

## 1. RESUMEN EJECUTIVO

La FASE 2 realizó **cambios sustanciales** tanto en backend como en frontend. Sin embargo, al hacer la auditoría se detectan varios **gaps en el panel Settings** y en la integración del logo con la experiencia de usuario. Las notificaciones funcionan a nivel API pero tienen limitaciones de UX. A continuación el desglose completo.

### Veredicto rápido:

| Área | Estado | Completitud |
|------|--------|-------------|
| **Panel Settings — Empresa** | ✅ Implementado | ~85% |
| **Panel Settings — Preferencias** | ✅ Implementado | ~90% |
| **Panel Settings — Series** | ✅ Implementado | ~95% |
| **Panel Settings — Sede** | ✅ Implementado | ~90% |
| **Panel Settings — Tipo de Cambio** | ✅ Implementado | ~95% |
| **Panel Settings — Credenciales SUNAT** | ✅ Implementado | ~80% |
| **Logo en Header/Welcome** | ❌ NO implementado | 0% |
| **Notificaciones — UX completa** | ⚠️ Parcial | ~60% |
| **Notificación persistente "Falta logo"** | ❌ NO implementado | 0% |

---

## 2. AUDITORÍA COMPLETA DEL PANEL SETTINGS

El panel Settings (`config-overlay.html`) tiene **6 tabs**:

### Tab 1: 🏢 Empresa
| Campo / Feature | En HTML | En JS | Funcional | Observación |
|----------------|---------|-------|-----------|-------------|
| Logo preview + click to upload | ✅ | ✅ `handleLogoFileSelect()` | ✅ | Funciona: sube via API, muestra preview |
| Razón Social (readonly) | ✅ | ✅ | ✅ | Se carga de `ETIA.company` |
| RUC (readonly) | ✅ | ✅ | ✅ | — |
| Nombre Comercial (readonly) | ✅ | ✅ | ✅ | — |
| Régimen (disabled select) | ✅ | ✅ | ✅ | Readonly — correcto |
| Dirección Fiscal (readonly) | ✅ | ✅ | ✅ | — |
| Email de Contacto (editable) | ✅ | ✅ `saveCompanyContact()` | ✅ | Guarda via `ApiClient.updateCompany()` |
| Teléfono (editable) | ✅ | ✅ `saveCompanyContact()` | ✅ | Guarda via `ApiClient.updateCompany()` |
| Botón "Guardar Contacto" | ✅ | ✅ | ✅ | — |
| Botón "Cambiar Empresa" | ✅ | — | ⚠️ | **Solo redirige a `/`**, no hace logout real ni limpia sessionStorage |
| Badge "🟢 Activa" | ✅ | — | ⚠️ | **Hardcodeado** — no verifica estado real de `company.estado` |

### Tab 2: ⚙️ Preferencias
| Campo / Feature | En HTML | En JS | Funcional | Observación |
|----------------|---------|-------|-----------|-------------|
| Moneda por defecto | ✅ | ✅ | ✅ | PEN/USD |
| Tipo de ítem por defecto | ✅ | ✅ | ✅ | Servicio/Producto |
| Tema de interfaz (dark/light) | ✅ | ✅ `selectTheme()` | ✅ | Toggle funcional |
| Emisión en Envío | ✅ | ✅ `_updateEmisionEnvioUI()` | ✅ | Checkbox + warning banner |
| Cuotas de Crédito (nro cuotas) | ✅ | ✅ | ✅ | — |
| Cuotas distribución (FIJA_30D/PROPORCIONAL) | ✅ | ✅ | ✅ | — |
| Formato de Documentos | ✅ | ✅ | ✅ | Dropdown + link a `/formato-documentos` |
| Botón "Guardar" | ✅ | ✅ `saveConfig()` | ✅ | Guarda todo a localStorage |

### Tab 3: 📋 Series
| Campo / Feature | En HTML | En JS | Funcional | Observación |
|----------------|---------|-------|-----------|-------------|
| Hero sede activa | ✅ | ✅ `_renderSeriesPanel()` | ✅ | — |
| Tabla de series | ✅ | ✅ | ✅ | Serie, tipo, desc, default, activa |
| Fallback manual | ✅ | ✅ | ✅ | Si no hay sede |
| Botón "Guardar" | ✅ | ✅ | ✅ | — |

### Tab 4: 📍 Sede
| Campo / Feature | En HTML | En JS | Funcional | Observación |
|----------------|---------|-------|-----------|-------------|
| Select ubigeo | ✅ | ✅ | ✅ | Se carga via API |
| Dirección con búsqueda | ✅ | ✅ `searchAddressOnMap()` | ✅ | Nominatim |
| Mapa Leaflet | ✅ | ✅ `initSedeMap()` | ✅ | Click/drag/search |
| Coordenadas (readonly) | ✅ | ✅ | ✅ | — |
| Reverse geocode | — | ✅ `reverseGeocode()` | ✅ | Auto-actualiza dirección |

### Tab 5: 💱 Tipo de Cambio
| Campo / Feature | En HTML | En JS | Funcional | Observación |
|----------------|---------|-------|-----------|-------------|
| Fuente (SBS/SUNAT) | ✅ | ✅ | ✅ | — |
| Link a `/tipo-cambio` | ✅ | — | ✅ | — |

### Tab 6: 🔐 Credenciales SUNAT
| Campo / Feature | En HTML | En JS | Funcional | Observación |
|----------------|---------|-------|-----------|-------------|
| Status indicators (SOL + Cert) | ✅ | ✅ | ✅ | ✅/❌ dinámicos |
| Usuario SOL | ✅ | ✅ | ✅ | — |
| Clave SOL | ✅ | ✅ | ✅ | Enmascarada con `••••••••` |
| Certificado dropzone (drag & drop) | ✅ | ✅ | ✅ | — |
| Upload certificado | ✅ | ✅ `uploadCertificado()` | ✅ | — |
| Contraseña certificado | ✅ | ✅ | ✅ | — |
| Probar Conexión SUNAT | ✅ | ✅ `testSunatConnection()` | ✅ | Llama a backend (mock) |
| Botón "Guardar Credenciales" | ✅ | ✅ `saveCredentials()` | ✅ | — |

---

## 3. LO QUE SÍ SE IMPLEMENTÓ EN SETTINGS (FASE 2)

### ✅ Implementaciones confirmadas — SETTINGS

| # | Feature | FASE 2 Parte | Estado |
|---|---------|-------------|--------|
| 1 | Upload logo (click en panel Empresa) | PARTE 24 (REQ-B) | ✅ Funcional |
| 2 | Preview logo en config overlay | PARTE 24 (REQ-B) | ✅ Funcional |
| 3 | Emisión en Envío toggle + warning | PARTE 9 | ✅ Funcional |
| 4 | Cuotas de crédito (nro + distribución) | Parte anterior | ✅ Funcional |
| 5 | Formato de Documentos selector | PARTE 25 (REQ-F) | ✅ Funcional |
| 6 | Tab Credenciales SUNAT completo | PARTE 22 (REQ-D) | ✅ Funcional |
| 7 | Upload certificado digital (.p12/.pfx) | PARTE 22 (REQ-D) | ✅ Funcional |
| 8 | Probar Conexión SUNAT (mock backend) | PARTE 22 (REQ-D) | ✅ Funcional |
| 9 | Status indicators (SOL ✅/❌, Cert ✅/❌) | PARTE 22 (REQ-D) | ✅ Funcional |
| 10 | Guardar credenciales | PARTE 22 (REQ-D) | ✅ Funcional |
| 11 | Guardar contacto empresa | Pre-existente | ✅ Funcional |
| 12 | Mapa Leaflet en Sede | Pre-existente | ✅ Funcional |

---

## 4. GAPS DETECTADOS — Lo que NO se implementó

### 🔴 GAP-1: Logo de empresa NO aparece en el Header

**Situación actual:**  
- El header (`header.html` línea 8) muestra un emoji estático `🧾` como logo  
- NO se reemplaza por el logo real de la empresa aunque esté configurado  
- El logo solo se ve dentro del panel Settings (tab Empresa)  

**Impacto:** El usuario sube su logo pero nunca lo ve fuera del panel de configuración.  

**Lo esperado (REQ-B):**  
> "Panel Empresa Premium + Logo" incluía logo en preview y en el header  
> La notificación persistente debía mantenerse hasta configurar el logo  

**Archivos involucrados:**  
- `fragments/header.html` → `<span class="g-logo">🧾</span>` (línea 8)
- `static/js/app-state.js` → `ETIA.init()` carga `company.tieneLogo` pero no actualiza header
- `static/css/main.css` → `.g-logo` solo tiene `font-size: 1.6rem`

---

### 🔴 GAP-2: Logo de empresa NO aparece en la pantalla Welcome

**Situación actual:**  
- `welcome.html` línea 9: `<span class="g-logo">🧾</span>` — emoji hardcodeado  
- `welcome.html` línea 35: `<div class="onb-hero-icon">🧾</div>` — hero icon hardcodeado  
- Cuando la empresa ya tiene logo y vuelve al inicio, sigue viendo el emoji  

**Lo esperado:**  
El logo de la empresa debería reemplazar el emoji `🧾` tanto en el mini-header como en el hero del welcome.

---

### 🔴 GAP-3: Notificación persistente "Falta configurar logo" NO implementada

**Situación actual:**  
- En FASE_2.md PARTE 15, REQ-B.4 dice: *"Notificación persistente — SÍ, se mantiene activa hasta que el logo se configure"*  
- En la realidad: **NO existe** ninguna notificación de configuración pendiente  
- `NotificationStore` es in-memory → se pierde al reiniciar  
- `data.sql` no tiene seed de notificaciones  
- NO hay lógica en `ETIA.init()` que verifique si falta logo y genere alerta  

**Lo esperado:**  
Al iniciar la app, si `company.tieneLogo === false`, debería haber un item persistente en el panel de notificaciones tipo:  
> ⚙️ **Configuración pendiente:** Sube el logo de tu empresa para personalizar tus comprobantes.  
> [Ir a Configuración →]

---

### 🟡 GAP-4: Notificaciones "📤 Transmisiones recientes" no muestran datos al arrancar

**Situación actual:**  
- `NotificationStore` es `ConcurrentLinkedDeque` in-memory  
- Al reiniciar la app (o redeployo), la lista queda **vacía**  
- `_loadTransmissionNotifs()` llama a `ApiClient.listNotifications(companyId)` → devuelve `[]` siempre después de restart  
- Las notificaciones solo se generan cuando se emite/anula un documento en la sesión actual  

**Impacto:** El panel de notificaciones siempre muestra "Sin transmisiones recientes" al abrir la app.

---

### 🟡 GAP-5: Badge "🟢 Activa" en tab Empresa es hardcoded

**Situación actual:**  
`config-overlay.html` línea 65: `<span class="cfg-co-badge">🟢 Activa</span>`  
Siempre muestra "Activa" sin importar el estado real de `company.estado`.

---

### 🟡 GAP-6: "Cambiar Empresa" no limpia estado

**Situación actual:**  
El botón "Cambiar Empresa" en la tab Empresa es un `<a href="/">` que solo redirige al welcome.  
No limpia `sessionStorage`, `localStorage` de ETIA, ni hace invalidación de cache.

---

### 🟡 GAP-7: Las preferencias se guardan en localStorage pero NO se sincronizan con el backend

**Situación actual:**  
- `saveConfig()` guarda en `localStorage.setItem('etia_...')`  
- El seed data tiene `user_preferences` con `pref-001` a `pref-008`  
- **PERO** `saveConfig()` en `config-overlay.js` NO llama a `ApiClient.savePreference()`  
- El `loadConfigValues()` NO carga preferencias del backend  
- Si cambias de navegador o limpias cache, pierdes toda la configuración  

**Lo esperado:**  
Idealmente `saveConfig()` debería sincronizar con el backend (write-through):  
`localStorage` ← para lectura rápida, `user_preferences` ← para persistencia real.

---

### 🟢 GAP-8: No hay validación de email en "Guardar Contacto"

**Situación actual:**  
El campo email tiene `type="email"` en HTML, pero `saveCompanyContact()` no valida formato antes de enviar. Si el usuario escribe algo inválido, se envía al backend que puede aceptarlo o rechazarlo silenciosamente.

---

## 5. PROPUESTAS DE MEJORA PARA SETTINGS

### 🚀 PROP-1: Logo dinámico en Header (prioridad alta)

**Descripción:** Reemplazar `<span class="g-logo">🧾</span>` por un `<img>` que muestra el logo real si existe.  
**Archivos:**  
- `fragments/header.html` — agregar `<img id="hdr-logo-img">` con fallback emoji  
- `static/js/app-state.js` — en `_applyCompanyData()`, si `tieneLogo`, setear `src` del img  
- `static/css/main.css` — estilos para el logo img (32×32px, object-fit: contain)  

**Esfuerzo:** 🟢 Bajo (30 min)

---

### 🚀 PROP-2: Logo dinámico en Welcome

**Descripción:** En `welcome.html`, si la empresa ya existe y tiene logo, mostrarlo en vez del emoji `🧾`.  
**Archivos:**  
- `welcome.html` — condicional: si `ETIA.company && ETIA.company.tieneLogo`, usar `<img>`  

**Esfuerzo:** 🟢 Bajo (20 min)

---

### 🚀 PROP-3: Notificación persistente de configuración pendiente

**Descripción:** En `ETIA.init()`, después de cargar la empresa, verificar qué falta configurar y generar notificaciones de tipo "CONFIG" que se muestren en el panel 🔔.  

**Checks propuestos:**  
| Condición | Mensaje |
|-----------|---------|
| `!company.tieneLogo` | ⚙️ Sube el logo de tu empresa |
| `!company.tieneUsuarioSol \|\| !company.tieneClaveSol` | 🔐 Configura tus credenciales SOL |
| `!company.tieneCertificado` | 📜 Sube tu certificado digital |
| `!company.emailContacto` | 📧 Agrega un email de contacto |
| `!company.telefono` | 📱 Agrega un teléfono de contacto |

**Archivos:**  
- `static/js/app-state.js` — nueva función `_checkConfigNotifications(company)`  
- `fragments/header.html` — nueva sección en notif panel: "⚙️ Configuración pendiente"  

**Esfuerzo:** 🟡 Medio (1-2 horas)

---

### 🚀 PROP-4: Sincronizar preferencias con backend

**Descripción:** `saveConfig()` hace write-through a `ApiClient.savePreference()`. `loadConfigValues()` lee preferencias del backend y las sincroniza con localStorage.  

**Archivos:**  
- `config-overlay.js` — `saveConfig()` llama a backend  
- `app-state.js` — `ETIA.init()` carga preferencias y actualiza localStorage  

**Esfuerzo:** 🟡 Medio (1-2 horas)

---

### 🚀 PROP-5: Badge de empresa dinámico

**Descripción:** El badge "🟢 Activa" debería reflejar `company.estado`:  
- ACTIVO → `🟢 Activa`  
- INACTIVO → `🔴 Inactiva`  
- Sin estado → `⚪ Sin verificar`  

**Esfuerzo:** 🟢 Bajo (10 min)

---

### 🚀 PROP-6: "Cambiar Empresa" con limpieza completa

**Descripción:**  
- Confirmación con modal  
- `sessionStorage.clear()` para invalidar cache  
- `localStorage` selectivo (mantener tema, limpiar empresa)  
- Luego redirigir a `/`  

**Esfuerzo:** 🟢 Bajo (20 min)

---

### 🚀 PROP-7: Validación client-side en formularios Settings

**Descripción:**  
- Email: validar formato antes de enviar  
- Teléfono: validar formato peruano (+51...)  
- Usuario SOL: no vacío  
- Toast de error descriptivo  

**Esfuerzo:** 🟢 Bajo (30 min)

---

### 🚀 PROP-8: Notificaciones con seed data (demo)

**Descripción:** El `NotificationStore` in-memory empieza vacío. Agregar un `@PostConstruct` en algún servicio que genere 3-5 notificaciones demo para que la experiencia inicial no esté vacía.  

**Alternativa:** Un `DataInitializer` que publique notificaciones simuladas si es modo demo.  

**Esfuerzo:** 🟢 Bajo (20 min)

---

### 💡 PROP-9: Tab "📊 Resumen de Cuenta" en Settings

**Descripción:** Nuevo tab en Settings que muestre un dashboard mini:  
- Total documentos emitidos (facturas + boletas)  
- Total monto facturado  
- Último documento emitido  
- Estado de la integración SUNAT  

**Esfuerzo:** 🟡 Medio (2-3 horas)

---

### 💡 PROP-10: Tab "🔒 Seguridad" en Settings

**Descripción:** Nuevo tab:  
- Cambiar contraseña de la aplicación (futuro)  
- Historial de sesiones  
- Timeout de inactividad configurable  

**Esfuerzo:** 🔴 Alto (aplazar a fase futura)

---

## 6. AUDITORÍA DEL LOGO EN EL INICIO (WELCOME)

### Estado actual del `welcome.html`:

| Elemento | Ubicación | Valor actual | Logo dinámico | Estado |
|----------|-----------|-------------|---------------|--------|
| Header logo | línea 9 | `🧾` (emoji) | ❌ No | **Sin cambio en FASE 2** |
| Hero icon | línea 35 | `🧾` (emoji) | ❌ No | **Sin cambio en FASE 2** |
| Brand name | línea 11 | `ETIA` (texto) | N/A | OK |
| View 3 icon | línea 169 | `⚡` (emoji) | N/A | OK |

### Estado actual del `header.html` (todas las páginas excepto welcome):

| Elemento | Ubicación | Valor actual | Logo dinámico | Estado |
|----------|-----------|-------------|---------------|--------|
| Header logo | línea 8 | `🧾` (emoji) | ❌ No | **Sin cambio en FASE 2** |
| Brand link | línea 7 | `<a href="/menu">` | N/A | OK |

### Conclusión:
> **El logo de empresa solo es visible dentro del panel Settings** (tab Empresa, tamaño 56×56px).  
> No se propagó al header general, al welcome, ni a ninguna otra vista.  
> Esto contradice el espíritu de REQ-B que proponía un "Panel Empresa Premium" donde el logo fuera protagonista.

---

## 7. AUDITORÍA DE NOTIFICACIONES

### Backend — Completo ✅

| Feature | Estado | Detalle |
|---------|--------|---------|
| `NotificationStore` (in-memory) | ✅ | CRUD completo, thread-safe |
| `NotificationService` | ✅ | publicar, listar, marcar leída, limpiar |
| `NotificationOpenApi` | ✅ | 5 endpoints REST OpenAPI |
| Auto-publicación en emitir | ✅ | `DocFacturaServiceImpl` llama `notificationService.publicar()` |
| Auto-publicación en anular | ✅ | Igual |

### Frontend — Parcial ⚠️

| Feature | Estado | Detalle |
|---------|--------|---------|
| Badge 🔔 con contador | ✅ | `_updateGlobalBadge()` suma rechazados + unread tx |
| Panel notificaciones toggle | ✅ | `toggleNotifPanel()` |
| Sección "Rechazados" | ✅ | `_loadRejectedDocs()` — escanea 4 tipos de doc |
| Sección "Transmisiones" | ✅ | `_loadTransmissionNotifs()` — API |
| Botón "Anular" desde notificación | ✅ | `anularDocumento()` |
| **Marcar como leída individual** | ❌ | `ApiClient.markNotifRead()` existe pero NO se llama en UI |
| **Marcar todas como leídas** | ❌ | `ApiClient.markAllNotifRead()` existe pero NO hay botón en UI |
| **Limpiar notificaciones** | ❌ | `ApiClient.clearNotifications()` existe pero NO hay botón en UI |
| **Notificaciones de config pendiente** | ❌ | No implementado (ver GAP-3) |
| **Notificaciones persistentes al restart** | ❌ | In-memory, se pierde todo |
| **Notificaciones clickeables (navegar a doc)** | ❌ | Las TX notifs NO son links |
| **"Marcar como leída" al click** | ❌ | No implementado |
| **Empty state mejorado** | ⚠️ | Solo texto plano, sin ilustración |

### APIs expuestas pero NO usadas en UI:
```
ApiClient.markAllNotifRead(companyId)  → PUT /notification/read-all
ApiClient.markNotifRead(notifId)       → PUT /notification/{id}/read
ApiClient.clearNotifications(companyId)→ DELETE /notification
```

---

## 8. MAPA DE CAMBIOS FRONTEND DE FASE 2 — Dónde Probar

### 📋 Lista completa de cambios frontend con instrucción de prueba

| # | Archivo modificado | Feature | Cómo probar | URL |
|---|-------------------|---------|-------------|-----|
| 1 | `wizard/factura.html` | Wizard 5 Steps + Serie dropdown | Ir a crear factura, verificar 5 steps, serie como dropdown | `/wizard?type=FACTURA` |
| 2 | `wizard/factura.html` | Step 5 — Vista Previa PDF en iframe | Avanzar al step 5, tab "📄 Vista Previa" debe mostrar PDF render | `/wizard?type=FACTURA` |
| 3 | `wizard/factura.html` | Edit mode (borrador) | Crear borrador, luego editar. Debe cargar datos | `/wizard?type=FACTURA&id=fac-demo-018` |
| 4 | `wizard/factura.html` | Hint emisión en envío | Desactivar "Emisión en Envío" en config, abrir wizard. Debe mostrar warning banner | `/wizard?type=FACTURA` |
| 5 | `wizard/factura.html` | Columna "Afectación" en items | Step 4, agregar producto. Debe haber dropdown Gravado/Exonerado/Inafecto | `/wizard?type=FACTURA` |
| 6 | `wizard/factura.html` | Tipo Operación dropdown | Step 1, debe haber selector Cat. 51 (0101, 0200, 0401) | `/wizard?type=FACTURA` |
| 7 | `wizard/factura.html` | Cargos/Descuentos globales | Step 4, sección inferior. Solo disponible en facturas | `/wizard?type=FACTURA` |
| 8 | `wizard/factura.html` | Cuotas de crédito auto-generadas | Step 4, seleccionar CRÉDITO en forma pago → cuotas se generan | `/wizard?type=FACTURA` |
| 9 | `facturas.html` | Números con padding 8 dígitos | Verificar que se muestra `F001-00000001` y no `F001-1` | `/facturas` |
| 10 | `facturas.html` | Quick Emit corregido | Crear borrador, luego clic "📤 Emitir" → debe emitir el borrador, NO crear nuevo | `/facturas` |
| 11 | `facturas.html` | Links edición borrador | Card BORRADOR debe tener link "✏️ Editar" que lleva al wizard con datos | `/facturas` |
| 12 | `facturas.html` | Filtros persistentes (chips + búsqueda) | Escribir en barra, presionar Enter → chip. Filtro persiste en sesión | `/facturas` |
| 13 | `boletas.html` | Números con padding | Verificar `B001-00000001` | `/boletas` |
| 14 | `boletas.html` | Acciones por estado | BORRADOR: Editar+Validar+Eliminar. VALIDADO: Editar+XML+PDF. CON_ERROR: Corregir | `/boletas` |
| 15 | `boletas.html` | Quick Delete + Quick XML + Quick PDF | Botones funcionales en cada card | `/boletas` |
| 16 | `resumenes-diarios.html` | Lista desde API real | Debe mostrar resúmenes RC-YYYYMMDD-NNN con badges | `/resumenes-diarios` |
| 17 | `resumenes-diarios.html` | Generar Resumen del Día | Botón "Generar" → crea resumen con boletas VALIDADAS | `/resumenes-diarios` |
| 18 | `resumenes-diarios.html` | Consultar Estado | En resumen ENVIADO, botón "Consultar Estado" | `/resumenes-diarios` |
| 19 | `menu.html` | Card "Resúmenes Diarios" | Nueva card en sección Operaciones | `/menu` |
| 20 | `menu.html` | Card "Formato Documentos" | Nueva card en Herramientas con badge "🚧 En Desarrollo" | `/menu` |
| 21 | `formato-documentos.html` | Página formatos | 3 cards (1 activa, 2 "Próximamente"), banner dev, selección guardada | `/formato-documentos` |
| 22 | `config-overlay.html` | Tab Empresa — Logo upload | Abrir Settings → Empresa → click en logo → subir PNG | ⚙️ en header |
| 23 | `config-overlay.html` | Tab Preferencias — Emisión en Envío | Toggle checkbox, verificar warning banner | ⚙️ → Preferencias |
| 24 | `config-overlay.html` | Tab Preferencias — Cuotas | Cambiar nro cuotas y distribución, verificar persist | ⚙️ → Preferencias |
| 25 | `config-overlay.html` | Tab Preferencias — Formato Documentos | Dropdown con link a página de formatos | ⚙️ → Preferencias |
| 26 | `config-overlay.html` | Tab Credenciales — Upload certificado | Drag & drop .p12, upload, verificar indicador ✅ | ⚙️ → Credenciales |
| 27 | `config-overlay.html` | Tab Credenciales — Probar Conexión | Click "Probar Conexión" → resultado mock del backend | ⚙️ → Credenciales |
| 28 | `config-overlay.html` | Tab Credenciales — Guardar | Escribir usuario/clave SOL, guardar, verificar indicadores | ⚙️ → Credenciales |
| 29 | `doc-preview.js` | Export PDF con PdfTemplate unificado | Abrir preview de factura → click "📥 PDF" → impresión | `/facturas` → click card |
| 30 | `doc-preview.js` | PDF con logo | Si hay logo subido, el PDF debe mostrarlo en header | `/facturas` → click card → PDF |
| 31 | `doc-preview.js` | Cargos/Descuentos en PDF | Factura con cargos → PDF debe incluir tabla de cargos | Preview de `fac-demo-025` |
| 32 | `doc-preview.js` | Números formateados en preview | Serie-Número con padding en header, anular, eliminar, PDF | `/facturas` → click card |
| 33 | `app-state.js` | `ETIA.clients[]` + `findClientById()` | Wizard nuevo: campos cliente VACÍOS (no pre-fill demo) | `/wizard?type=FACTURA` |
| 34 | `app-state.js` | `ETIA.formatNumero()` | Usado en notificaciones, listas, preview → verificar padding | Todo el sistema |
| 35 | `app-state.js` | `ETIA.getLogoBase64()` + cache | PDF/preview debe cargar logo una vez y cachear | Imprimir PDF |
| 36 | `pdf-template.js` | Template "standard" funcional | PDF generado tiene header empresa + recuadro + tabla items | Cualquier PDF |
| 37 | `pdf-template.js` | PdfTemplate extensible | Solo template "standard" activo, otros próximamente | Config → Formato |
| 38 | `wizard.js` | BOLETA modo: botón "Guardar Boleta" | Crear boleta → botón principal es "💾 Guardar", no "Emitir" | `/wizard?type=BOLETA` |
| 39 | `wizard.js` | BOLETA edit mode | Editar boleta borrador → carga datos → "💾 Guardar Cambios" | `/wizard?type=BOLETA&id=...` |
| 40 | `wizard.js` | Redirect a `/boletas` en resetAll | Después de guardar boleta, vuelve a lista de boletas | Después de guardar |

---

## 9. CHECKLIST DE VERIFICACIÓN VISUAL (QA Manual)

### 🏠 Welcome (`/`)
- [ ] Hero muestra correctamente con partículas animadas
- [ ] Botón "INICIAR DEMO" lleva a View 2
- [ ] View 2: 9 cards de módulos disponibles + 4 próximamente
- [ ] View 3: Formulario carga datos de empresa desde API
- [ ] ❌ **Logo empresa:** No se muestra (GAP-1, GAP-2)

### 📋 Menú (`/menu`)
- [ ] Header: nombre empresa, sede selector, 💱 badge, 🔔 badge, ⚙️ botón
- [ ] Card "Documentos" funcional
- [ ] Card "Resúmenes Diarios" funcional (NUEVO en FASE 2)
- [ ] Card "Formato Documentos" con badge "🚧 En Desarrollo" (NUEVO en FASE 2)
- [ ] Widget Tipo de Cambio se carga y muestra tasas
- [ ] ❌ **Logo empresa en header:** No se muestra (GAP-1)

### 🧾 Facturas (`/facturas`)
- [ ] Números con padding 8 dígitos
- [ ] Filtros con chips persistentes
- [ ] BORRADOR: link "Editar" funcional
- [ ] BORRADOR: botón "Emitir" emite el borrador (no crea nueva)
- [ ] Preview: PDF con PdfTemplate + logo (si existe)
- [ ] Preview: Cargos/Descuentos + Cuotas visibles

### 🎫 Boletas (`/boletas`)
- [ ] Números con padding
- [ ] BORRADOR: Editar + Validar + Eliminar
- [ ] VALIDADO: "📨 Lista para Resumen Diario"
- [ ] CON_ERROR: banner de errores + Re-validar
- [ ] ENVIADA/ACEPTADA: XML + PDF funcionales

### 📑 Resúmenes Diarios (`/resumenes-diarios`)
- [ ] Lista de resúmenes desde API
- [ ] Botón "Generar Resumen del Día"
- [ ] Botón "Consultar Estado" en ENVIADO
- [ ] Badges de color por estado

### 📐 Formato Documentos (`/formato-documentos`)
- [ ] 3 cards con mini-previews
- [ ] Solo "Estándar" seleccionable
- [ ] "Compacto" y "Detallado" deshabilitados con "🚧 Próximamente"
- [ ] Selección se guarda en localStorage

### ⚙️ Settings (overlay)
- [ ] Tab Empresa: logo preview, datos readonly, email/tel editables
- [ ] Tab Preferencias: moneda, item type, tema, emisión envío, cuotas, formato docs
- [ ] Tab Series: tabla dinámica desde sede activa
- [ ] Tab Sede: mapa Leaflet funcional, búsqueda, reverse geocode
- [ ] Tab Tipo Cambio: selector SBS/SUNAT
- [ ] Tab Credenciales: upload cert, usuario/clave SOL, probar conexión, indicadores

### 🔔 Notificaciones
- [ ] Badge 🔔 muestra contador correcto
- [ ] Panel: sección "Transmisiones" (vacía al inicio — GAP-4)
- [ ] Panel: sección "Rechazados" (muestra RECHAZADOS si existen)
- [ ] Botón "Anular" desde notificación funcional
- [ ] ❌ **"Marcar todas leídas":** No hay botón (GAP)
- [ ] ❌ **"Limpiar notificaciones":** No hay botón (GAP)
- [ ] ❌ **Notificaciones de config pendiente:** No existen (GAP-3)

---

## 10. RESUMEN DE LA "FASE IMPROVISADA"

### ¿Qué fue la FASE 2?

La FASE 2 (documentada en `FASE_2.md`) fue una serie de iteraciones intensivas que cubrieron:

```
FASE 2 Timeline:
├── PARTE 1-4:   Análisis gap XML UBL 2.1 vs modelo actual
├── PARTE 5-8:   Ejecución: OpenAPI + Entities + Mappers + Services + data.sql + Tests
├── PARTE 9:     Feature: Emisión en Envío + Borrador (Factura)
├── PARTE 10:    Garantía inmutabilidad XML descargado
├── PARTE 11:    Wizard 5-Step Refactor + Edit Mode
├── PARTE 12:    Fix: Preview, cliente en edición, padding números
├── PARTE 13:    Fix: PDF completo, quickEmit, test fix, seed data
├── PARTE 14:    Regularización endpoint XML (OpenAPI-first)
├── PARTE 15:    Plan Fase 2.5 (REQ-A a REQ-G) — Q&A con usuario
├── PARTE 22:    REQ-D: Settings + Credenciales SUNAT ✅
├── PARTE 23:    REQ-E: UX Fixes REQ-1–8 ✅ (verificación — ya estaban)
├── PARTE 24:    REQ-B: Panel Empresa Premium + Logo ✅
├── PARTE 25:    REQ-F: Formato Documentos ✅
├── PARTE 26:    REQ-C: PDF Preview con Logo ✅
├── PARTE 27:    REQ-A: Boleta + Resumen Diario ✅
└── UNIT TESTS:  61 tests nuevos (DocBoleta: 44, ResumenDiario: 17)
```

### Lo que se logró ✅

| # | Logro | Tests |
|---|-------|-------|
| 1 | Modelo de datos UBL 2.1 completo (12 catálogos SUNAT) | ✅ |
| 2 | Generador XML UBL 2.1 (factura + boleta) | 36 tests |
| 3 | Endpoint XML OpenAPI-first | ✅ |
| 4 | Wizard 5-step con edit mode | ✅ |
| 5 | Flujo boleta completo (BORRADOR → VALIDADO → Resumen Diario → ACEPTADA) | 44+17 tests |
| 6 | Panel Credenciales SUNAT | ✅ |
| 7 | Upload certificado digital | ✅ |
| 8 | Probar Conexión SUNAT (mock) | ✅ |
| 9 | Upload logo empresa | ✅ |
| 10 | PDF con PdfTemplate extensible + logo embebido | ✅ |
| 11 | Formato Documentos (estructura) | ✅ |
| 12 | Resúmenes Diarios (UI + API) | ✅ |
| 13 | Padding números SUNAT (8 dígitos) | ✅ |
| 14 | Filtros persistentes en facturas | ✅ |
| 15 | 278 tests totales passing | ✅ |

### Lo que quedó pendiente ❌

| # | Pendiente | Prioridad | Esfuerzo |
|---|-----------|-----------|----------|
| 1 | Logo en header (todas las páginas) | 🔴 Alta | 🟢 30 min |
| 2 | Logo en welcome | 🔴 Alta | 🟢 20 min |
| 3 | Notificación persistente "Configuración pendiente" | 🟡 Media | 🟡 1-2 horas |
| 4 | Seed data de notificaciones | 🟡 Media | 🟢 20 min |
| 5 | Botones "Marcar todas leídas" / "Limpiar" en panel notif | 🟡 Media | 🟢 30 min |
| 6 | Click en notificación → navegar al documento | 🟡 Media | 🟡 1 hora |
| 7 | Sincronización preferencias localStorage ↔ backend | 🟡 Media | 🟡 1-2 horas |
| 8 | Badge empresa dinámico | 🟢 Baja | 🟢 10 min |
| 9 | "Cambiar Empresa" con limpieza estado | 🟢 Baja | 🟢 20 min |
| 10 | Validación client-side en forms Settings | 🟢 Baja | 🟢 30 min |

### Esfuerzo total pendientes: ~5-8 horas

---

## APÉNDICE A — Archivos frontend modificados en FASE 2 (referencia rápida)

### JavaScript (13 archivos)
| Archivo | Cambios principales |
|---------|-------------------|
| `api-client.js` | +uploadLogo, +getLogoUrl, +uploadCertificado, +probarConexion, +boleta CRUD, +resumen diario |
| `app-state.js` | +`clients[]`, +`findClientById`, +`formatNumero`, +`getLogoBase64`, +`invalidateLogoCache`, +`documentFormat` |
| `config-overlay.js` | +tab credenciales, +upload cert/logo, +probar conexión, +formato docs, +cuotas |
| `wizard.js` | +5 steps, +edit mode `_loadDraftForEdit()`, +BOLETA mode, +`_loadPdfPreview()`, +`printPdfPreview()` |
| `items.js` | +afectación IGV dropdown, +auto-recálculo tributario, +cargos/descuentos, +cuotas auto |
| `doc-preview.js` | +`exportPdf()` refactorizado con PdfTemplate, +padding números, +cargos/descuentos PDF |
| `pdf-template.js` | **NUEVO** — Motor de templates extensible + template "standard" |
| `navigation.js` | Sin cambios significativos en FASE 2 |
| `error-handler.js` | Sin cambios en FASE 2 |
| `client-modal.js` | Sin cambios en FASE 2 |
| `product-modal.js` | Sin cambios en FASE 2 |
| `json-viewer.js` | Sin cambios en FASE 2 |
| `numero-letras.js` | Sin cambios en FASE 2 |

### HTML Templates (14 archivos)
| Archivo | Cambios principales |
|---------|-------------------|
| `wizard/factura.html` | 5 steps, serie dropdown, afectación col, +pdf-template.js, edit banner |
| `facturas.html` | Padding números, filtros chips, quickEmit corregido, +pdf-template.js |
| `boletas.html` | Acciones por estado, padding, quickDelete/XML/PDF, +pdf-template.js |
| `resumenes-diarios.html` | Reescrito con APIs reales |
| `menu.html` | +card Resúmenes Diarios, +card Formato Docs |
| `formato-documentos.html` | **NUEVO** — Página de formatos con 3 cards |
| `notas-credito.html` | +pdf-template.js |
| `notas-debito.html` | +pdf-template.js |
| `fragments/config-overlay.html` | +tab credenciales, +upload cert/logo, +formato docs, +cuotas |
| `fragments/header.html` | Sin cambios en FASE 2 (⚠️ logo no actualizado) |
| `fragments/head.html` | Sin cambios significativos |
| `fragments/doc-preview.html` | Sin cambios significativos |
| `fragments/modals.html` | Sin cambios significativos |
| `welcome.html` | Sin cambios en FASE 2 (⚠️ logo no actualizado) |

### CSS (1 archivo)
| Archivo | Cambios |
|---------|---------|
| `main.css` | +`.mc-chip-dev` para badge "En Desarrollo" |

### Backend nuevos archivos en FASE 2
| Archivo | Descripción |
|---------|-------------|
| `LogoStorageService.java` | Almacenamiento logo empresa |
| `CertificadoStorageService.java` | Almacenamiento certificado digital |
| `FormatoDocumentosViewController.java` | Controller para `/formato-documentos` |
| `ResumenDiarioEntity.java` + Repository + Service + OpenApi | Resumen Diario completo |
| `SunatSoapClient.java` + `MockSunatSoapClient.java` | Interface + mock para envío SUNAT |
| `DocumentValidatorService.java` | Validación interna de documentos |

---

## APÉNDICE B — Orden sugerido para implementar pendientes

```
PRIORIDAD ALTA (impacto visual inmediato):
  1. PROP-1: Logo dinámico en Header           (30 min)
  2. PROP-2: Logo dinámico en Welcome          (20 min)
  3. PROP-3: Notificaciones de config pendiente (1-2h)

PRIORIDAD MEDIA (mejora de UX):
  4. PROP-8: Seed notificaciones demo          (20 min)
  5. Botones "Marcar leídas" / "Limpiar"       (30 min)
  6. Click notificación → navegar              (1h)
  7. PROP-4: Sincronizar preferencias backend   (1-2h)

PRIORIDAD BAJA (pulido):
  8. PROP-5: Badge empresa dinámico            (10 min)
  9. PROP-6: Cambiar Empresa con limpieza      (20 min)
  10. PROP-7: Validación client-side           (30 min)
```

**Tiempo total estimado: ~6-8 horas**

---

> **Este documento fue PLANIFICACIÓN inicial. Todos los gaps han sido implementados.**  
> Cambios finales: seed notificaciones demo, TX notificaciones clickeables con navegación,  
> mark-as-read individual al click, empty states mejorados con iconos.


