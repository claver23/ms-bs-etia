# 🎨 FASE M — Frontend Modularización + UI/UX Premium (M12)

> **Módulo:** Frontend transversal  
> **Prioridad:** 🟡 Alta (paralela a cada fase de backend)  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA), cada fase genera sus vistas  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Modularizar el frontend, estandarizar componentes, implementar los nuevos módulos en el menú, homologar modales y acciones de documentos, mejorar el favicon, y mantener la estética "Wall Street Premium" en todas las vistas nuevas.

---

## 📋 Tareas

### TM-01 — Modularización JS

**Estructura propuesta:**
```
static/js/
├── core/
│   ├── api-client.js         ← (existente) + agregar auth headers
│   ├── app-state.js          ← (existente) + plan/módulos/última ruta
│   ├── date-utils.js         ← 🆕 Formato dd/MM/yyyy
│   ├── error-handler.js      ← (existente)
│   └── navigation.js         ← (existente) + verificar módulo vs plan
├── components/
│   ├── client-modal.js       ← (existente)
│   ├── product-modal.js      ← (existente)
│   ├── config-overlay.js     ← (existente)
│   ├── doc-preview.js        ← (existente) + homologar acciones
│   ├── slider-component.js   ← 🆕 The Guillotine slider reusable
│   ├── export-button.js      ← 🆕 Botón exportar Excel genérico
│   └── kanban-board.js       ← 🆕 Board para CRM Deal Flow
├── modules/
│   ├── wizard.js             ← (existente) + campo campaignId
│   ├── items.js              ← (existente)
│   ├── pos-view.js           ← 🆕 Punto de venta
│   ├── radar-view.js         ← 🆕 Radar de mercado
│   ├── campaign-view.js      ← 🆕 Alpha Campaigns
│   ├── purchase-view.js      ← 🆕 Supply Chain
│   ├── crm-view.js           ← 🆕 Deal Flow
│   ├── reporting-view.js     ← 🆕 Data Vault
│   ├── geo-view.js           ← 🆕 GeoInsight
│   └── admin-view.js         ← 🆕 Overlord
└── utils/
    ├── json-viewer.js        ← (existente)
    ├── numero-letras.js      ← (existente)
    └── pdf-template.js       ← (existente)
```

---

### TM-02 — Menú actualizado

```
ETIA Suite
├── 🏠 Dashboard
├── ── FACTURACIÓN ──────────
├── 📄 Documentos
├── 🧾 Facturas
├── 🎫 Boletas
├── 📉 Notas de Crédito
├── 📈 Notas de Débito
├── 📧 Resúmenes Diarios
├── ── OPERACIONES ──────────
├── 🏪 POS Pro
├── 🚚 Supply Chain (Compras)
├── 👤 Clientes
├── 📦 Productos
├── ── INTELIGENCIA ─────────
├── 🔍 Radar de Mercado
├── 📈 Alpha Campaigns
├── 🤝 Deal Flow (CRM)
├── 🗺️ GeoInsight
├── 📊 Data Vault (Reportes)
├── ── CONFIGURACIÓN ────────
├── 👔 Team Compass (Empleados)
├── 🏢 Sedes
├── 💱 Tipo de Cambio
├── 🔢 Códigos
├── ⚙️ Configuración
└── 👑 Overlord (solo admin) [🔒]
```

**Cada módulo en el menú verifica si está incluido en el plan del usuario. Si no → muestra candado 🔒 y tooltip "Disponible en plan PRO".**

---

### TM-03 — Homologación de modales y acciones de documentos

**Acciones estándar para documentos en lista y modal:**

| Acción | Estado permitido | Comportamiento |
|--------|-----------------|----------------|
| 👁️ Ver | Todos | Abre modal preview |
| ✅ Validar | BORRADOR, CON_ERROR | POST /{tipo}/{id}/validar |
| 📤 Emitir | BORRADOR, VALIDADO, RECHAZADA | PUT /{tipo}/{id}/emitir |
| ⚔️ Anular | EMITIDA, ACEPTADA | Abre The Guillotine |
| 🗑️ Eliminar | BORRADOR, CON_ERROR | DELETE /{tipo}?id={id} |
| ✏️ Editar | BORRADOR, CON_ERROR, VALIDADO | /wizard?type={tipo}&id={id} |
| 📥 XML | EMITIDA+ (con XML) | GET /{tipo}/{id}/xml |

**Estas acciones deben ser IDÉNTICAS en:**
- Modal de preview (`doc-preview.js`)
- Lista de documentos (tabla)
- Vista del wizard (paso 5)

**Colores hover por estado (CSS):**
```css
tr[data-estado="BORRADOR"]:hover { border-left: 3px solid #3498db; }
tr[data-estado="VALIDADO"]:hover { border-left: 3px solid #9b59b6; }
tr[data-estado="ENVIADA"]:hover { border-left: 3px solid #f39c12; }
tr[data-estado="ACEPTADA"]:hover { border-left: 3px solid #27ae60; }
tr[data-estado="RECHAZADA"]:hover { border-left: 3px solid #e74c3c; }
tr[data-estado="ANULADA"]:hover { border-left: 3px solid #7f8c8d; }
tr[data-estado="CON_ERROR"]:hover { border-left: 3px solid #e67e22; }
```

---

### TM-04 — Slider component reusable

```javascript
// slider-component.js
ETIA.Slider = function(container, options) {
    // options: { min, max, step, value, onChange, gradient: ['#27ae60','#f39c12','#e74c3c'] }
    // Renderiza un slider con gradiente y tooltip flotante
    // Estilo: premium oscuro, tipo "control de volumen"
};
```

Usado en:
- The Guillotine (% de cancelación)
- Radar (radio de búsqueda en km)
- Campañas (presupuesto estimado por radio)

---

### TM-05 — Favicon rediseño

**Concepto:** Contabilidad electrónica en esteroides. No fuego artificial, no wifi.
- **Opción A:** Letra "E" estilizada con gráfico de barras ascendente dentro
- **Opción B:** Icono de rayo ⚡ + signo $ integrado (velocidad + dinero)
- **Opción C:** Libro abierto con circuito electrónico (contabilidad + tech)

**Implementar como SVG inline** para que se vea bien a cualquier tamaño:
```html
<!-- Favicon SVG optimizado -->
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
```

---

### TM-06 — Auth flow en frontend

```javascript
// api-client.js actualizado
ETIA.api = {
    _getHeaders: function() {
        const token = localStorage.getItem('etia_token');
        return {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': 'Bearer ' + token } : {})
        };
    },
    // Todos los fetch usan _getHeaders()
    // Si 401 → redirect a /login
    // Si 403 → mostrar "Módulo no incluido en tu plan"
};
```

---

### TM-07 — Last route (continuar donde se quedó)

```javascript
// En cada navegación:
localStorage.setItem('etia_last_route', window.location.pathname + window.location.search);

// Al login exitoso:
const lastRoute = localStorage.getItem('etia_last_route');
window.location.href = lastRoute || '/menu';
```

---

## ✅ Criterios de Aceptación

- [ ] Menú muestra todos los módulos con candado si no están en el plan
- [ ] Acciones de documentos homologadas entre modal, lista y wizard
- [ ] Colores de estado en hover
- [ ] Slider component reusable
- [ ] Favicon actualizado (contabilidad + tech)
- [ ] Auth headers en todas las llamadas API
- [ ] Last route funcional
- [ ] CSS compatible con dark/light mode
- [ ] 401 → redirect a /login
- [ ] 403 → toast "Módulo no incluido en tu plan"

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/frontend_modular.feature`

```gherkin
Feature: Frontend Modular — UI/UX

  Scenario: Módulo bloqueado muestra candado
    Given un usuario con plan FREE
    When accedo al menú
    Then el módulo "POS Pro" muestra ícono 🔒
    And tooltip "Disponible en plan PRO"

  Scenario: Click en módulo bloqueado muestra upgrade
    Given un usuario con plan STARTER
    When hago click en "Alpha Campaigns" (bloqueado)
    Then veo modal "Este módulo requiere plan PRO. ¿Deseas mejorar tu plan?"

  Scenario: Dark mode activado por defecto
    Given un usuario nuevo sin preferencias
    When accede a la aplicación
    Then el tema es dark mode

  Scenario: Toggle dark/light mode
    Given un usuario en dark mode
    When cambia a light mode
    Then todas las vistas cambian a tema claro
    And la preferencia se guarda en localStorage

  Scenario: Auth 401 redirige a login
    Given un token expirado en localStorage
    When cualquier fetch devuelve 401
    Then el usuario es redirigido a /login
    And se limpia localStorage

  Scenario: Auth 403 muestra toast
    Given un usuario con plan FREE
    When intenta acceder a un endpoint de módulo PRO
    Then se muestra toast "Módulo no incluido en tu plan"
    And no se redirige

  Scenario: Last route se restaura al login
    Given un usuario estaba en /facturas
    And su sesión expiró
    When hace login nuevamente
    Then es redirigido a /facturas (no a /menu)

  Scenario: Acciones de documento homologadas
    Given una factura en estado ACEPTADA
    When veo la factura en lista, modal y wizard
    Then las acciones disponibles son las mismas: Ver, Anular, XML
    And el botón Anular abre The Guillotine

  Scenario: Slider component reusable
    Given el slider component cargado
    When lo uso en The Guillotine con min=0 max=100
    Then muestra gradiente verde→amarillo→rojo
    And tooltip flotante con valor en tiempo real

  Scenario: Favicon SVG se muestra correctamente
    Given el favicon.svg cargado
    When abro la app en cualquier navegador
    Then el favicon muestra la "E" estilizada con barras ascendentes
```

---

## ⚠️ Notas de implementación

- **Decisión M1 (PREGUNTAS_DEFINICION):** Mantener Vanilla JS. No migrar a Vue/React.
- **Decisión M2 (PREGUNTAS_DEFINICION):** Dark mode por defecto. Toggle disponible.
- **Decisión M3 (PREGUNTAS_DEFINICION):** Favicon SVG diseñado internamente. Letra "E" con gráfico de barras ascendente.
- **Formato fecha en vistas:** Usar `ETIA.formatDate()` de `date-utils.js` para mostrar `dd/MM/yyyy`.
- **Kanban Board:** Implementar con HTML5 DragEvent API (usado en FASE I).

---

*Fase M — Frontend. La UI es la cara del producto. Si la cara es fea, nadie se queda.*




