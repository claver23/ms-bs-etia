# 🚚 FASE F — Supply Chain — Compras (M4)

> **Módulo:** Supply Chain  
> **Paquete:** `com.etlions.ms_bs_etia.purchase`  
> **Endpoint base:** `/api/v1/purchase`  
> **Prioridad:** 🟡 Alta  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA), FASE D (Radar — para proveedores)  
> **Puede paralelizarse con:** FASE C, E, G, H (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Módulo de compras: registrar órdenes de compra, facturas de compra recibidas, asociar proveedores (del Radar o manuales), vincular compras a campañas. Complementa el ciclo contable (ventas ya existen, falta compras).

---

## 📋 Tareas

### TF-01 — Modelo de datos

```sql
CREATE TABLE suppliers (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    ruc VARCHAR(11),
    razon_social VARCHAR(500),
    nombre_comercial VARCHAR(500),
    direccion VARCHAR(500),
    ubigeo_code VARCHAR(10),
    email VARCHAR(200),
    telefono VARCHAR(50),
    website_url VARCHAR(500),
    origin VARCHAR(30) DEFAULT 'MANUAL',    -- MANUAL, RADAR
    radar_lead_id VARCHAR(255),             -- FK a potential_business_contacts
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP
);

CREATE TABLE purchase_orders (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    codigo VARCHAR(50),                     -- OC-20260409-0001
    supplier_id VARCHAR(255) NOT NULL,
    campaign_id VARCHAR(255),               -- Si viene de campaña de compra
    status VARCHAR(30) DEFAULT 'BORRADOR',  -- BORRADOR, ENVIADA, CONFIRMADA, RECIBIDA, CANCELADA
    moneda VARCHAR(3) DEFAULT 'PEN',
    subtotal DECIMAL(12,2),
    igv DECIMAL(12,2),
    total DECIMAL(12,2),
    notas TEXT,
    detalle_json TEXT,                      -- JSON items de compra
    fecha_emision DATE,
    fecha_entrega_estimada DATE,
    fecha_creacion TIMESTAMP
);

CREATE TABLE purchase_invoices (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    purchase_order_id VARCHAR(255),         -- Puede no tener OC
    supplier_id VARCHAR(255) NOT NULL,
    campaign_id VARCHAR(255),
    serie VARCHAR(10),
    numero INTEGER,
    moneda VARCHAR(3) DEFAULT 'PEN',
    subtotal DECIMAL(12,2),
    igv DECIMAL(12,2),
    total DECIMAL(12,2),
    detalle_json TEXT,
    fecha_emision DATE,
    fecha_recepcion DATE,
    estado VARCHAR(30) DEFAULT 'REGISTRADA',  -- REGISTRADA, CONTABILIZADA, ANULADA
    fecha_creacion TIMESTAMP
);
```

---

### TF-02 — Endpoints REST

```yaml
# ==================== SUPPLY CHAIN ====================
/purchase/suppliers:
  post: { operationId: crearProveedor }
  get:  { operationId: listarProveedores, params: [companyId, ubigeo, estado] }

/purchase/suppliers/{supplierId}:
  get: { operationId: obtenerProveedor }

/purchase/suppliers/from-radar:
  post:
    summary: "Crear proveedor desde lead del Radar"
    operationId: crearProveedorDesdeRadar
    requestBody:
      schema:
        $ref: '#/components/schemas/CreateSupplierFromRadarDto'
      example:
        companyId: "cmp_20601234567"
        leadId: "lead_001"

/purchase/orders:
  post: { operationId: crearOrdenCompra }
  get:  { operationId: listarOrdenesCompra }

/purchase/orders/{orderId}:
  get: { operationId: obtenerOrdenCompra }
  put: { operationId: actualizarOrdenCompra }

/purchase/orders/{orderId}/confirm:
  post: { operationId: confirmarOrdenCompra }

/purchase/orders/{orderId}/receive:
  post: { operationId: recibirOrdenCompra }

/purchase/invoices:
  post: { operationId: registrarFacturaCompra }
  get:  { operationId: listarFacturasCompra }

/purchase/invoices/{invoiceId}:
  get: { operationId: obtenerFacturaCompra }

/purchase/suppliers/search:
  get:
    summary: "Buscar proveedores por ubigeo y categoría"
    operationId: buscarProveedores
    parameters:
      - name: companyId { required: true }
      - name: ubigeo
      - name: categoria
      - name: term
```

---

### TF-03 — Flujo de compra desde Radar

```
Radar → Ver negocio → [Crear Orden Compra]
    ↓
Modal precarga: nombre, dirección, RUC (si tiene), teléfono
    ↓
¿Proveedor ya existe? → Sí → usar existente
                       → No → crear automáticamente con origin=RADAR
    ↓
Formulario Orden de Compra: ítems + montos + fecha entrega
    ↓
Guardar OC (status BORRADOR)
    ↓
Confirmar → Enviar al proveedor (email/WhatsApp)
    ↓
Recibir mercadería → Registrar factura de compra
```

---

### TF-04 — Vista Supply Chain

**Nueva vista:** `templates/compras.html`  
**View Controller:** `ComprasViewController.java` (`GET /compras`)

```
Vista Compras
├── Tabs: [Proveedores] [Órdenes de Compra] [Facturas Recibidas]
├── Tab Proveedores:
│   ├── Lista con filtros (ubigeo, categoría, estado)
│   ├── Badge "RADAR" si viene del radar
│   └── [+ Nuevo Proveedor] / [Buscar en Radar →]
├── Tab Órdenes:
│   ├── Lista con status badges (color por estado)
│   ├── [+ Nueva OC] → wizard simple
│   └── Acciones: Confirmar, Recibir, Cancelar
└── Tab Facturas Recibidas:
    ├── Lista con status
    ├── [+ Registrar Factura] → vincula a OC o standalone
    └── Exportar a Excel
```

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/purchase_from_radar.feature`

```gherkin
Feature: Supply Chain — Compras desde Radar

  Scenario: Crear proveedor desde lead del Radar
    Given un lead "Ferretería San Martín" guardado en el Radar
    When creo un proveedor desde ese lead
    Then el proveedor se crea con origin "RADAR"
    And sus datos vienen del lead

  Scenario: Crear proveedor manual
    Given datos de un proveedor con RUC "20123456789"
    When creo el proveedor manualmente
    Then el proveedor se crea con origin "MANUAL"

  Scenario: Proveedor duplicado desde Radar
    Given un proveedor ya creado desde el lead "Ferretería San Martín"
    When intento crear otro proveedor desde el mismo lead
    Then recibo error 400 "Ya existe un proveedor creado desde este lead"

  Scenario: Crear orden de compra
    Given un proveedor "Ferretería San Martín"
    When creo una OC con 3 ítems por S/ 5,000
    Then la OC se crea en status BORRADOR
    And tiene código "OC-20260409-0001"

  Scenario: Flujo completo compra
    Given una OC BORRADOR
    When la confirmo → recibo mercadería → registro factura de compra
    Then la OC pasa por CONFIRMADA → RECIBIDA
    And la factura de compra queda REGISTRADA

  Scenario: Cancelar orden de compra
    Given una OC en status BORRADOR
    When la cancelo
    Then la OC pasa a CANCELADA
    And no se puede revertir

  Scenario: No cancelar OC ya recibida
    Given una OC en status RECIBIDA
    When intento cancelarla
    Then recibo error 400 "No se puede cancelar una OC ya recibida"

  Scenario: Compra asociada a campaña
    Given una campaña de compra de "Cemento"
    When creo una OC desde la campaña
    Then la OC tiene campaign_id de la campaña

  Scenario: Factura de compra sin orden de compra
    Given un proveedor activo
    When registro una factura de compra sin OC asociada
    Then la factura se registra con purchase_order_id = null

  Scenario: OC en moneda USD con tipo de cambio
    Given una OC en moneda USD
    When la confirmo
    Then se registra el tipo de cambio vigente

  Scenario: Proveedor inactivo no permite OC
    Given un proveedor con estado INACTIVO
    When intento crear una OC para ese proveedor
    Then recibo error 400 "El proveedor no está activo"
```

---

## ✅ Criterios de Aceptación

- [ ] CRUD de proveedores (manual + desde Radar)
- [ ] CRUD de órdenes de compra con lifecycle (BORRADOR→CONFIRMADA→RECIBIDA | CANCELADA). Estado ENVIADA es opcional (futuro envío email).
- [ ] Registro de facturas de compra (con o sin OC)
- [ ] Integración con Radar (crear proveedor desde lead, sin duplicados)
- [ ] Vinculación compra ↔ campaña
- [ ] Proveedor inactivo no permite OC
- [ ] Exportar a Excel
- [ ] Company isolation en todos los endpoints
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `pom.xml` | 🔧 (sin cambios si POI ya está de FASE E) |
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag purchase, +endpoints, +schemas |
| `src/main/java/.../purchase/**` | 🆕 Paquete completo (~15 archivos) |
| `src/main/java/.../purchase/openapi/PurchaseOpenApi.java` | 🆕 |
| `src/main/java/.../purchase/service/PurchaseService.java` | 🆕 |
| `src/main/java/.../purchase/service/impl/PurchaseServiceImpl.java` | 🆕 |
| `src/main/java/.../purchase/mapper/SupplierMapper.java` | 🆕 |
| `src/main/java/.../purchase/mapper/PurchaseOrderMapper.java` | 🆕 |
| `src/main/java/.../purchase/persistence/SupplierEntity.java` | 🆕 |
| `src/main/java/.../purchase/persistence/SupplierRepository.java` | 🆕 |
| `src/main/java/.../purchase/persistence/PurchaseOrderEntity.java` | 🆕 |
| `src/main/java/.../purchase/persistence/PurchaseOrderRepository.java` | 🆕 |
| `src/main/java/.../purchase/persistence/PurchaseInvoiceEntity.java` | 🆕 |
| `src/main/java/.../purchase/persistence/PurchaseInvoiceRepository.java` | 🆕 |
| `src/main/java/.../view/ComprasViewController.java` | 🆕 |
| `src/main/resources/templates/compras.html` | 🆕 |
| `src/main/resources/static/js/purchase-view.js` | 🆕 |
| `src/main/resources/data.sql` | 🔧 +proveedores demo, +OC demo |
| `src/test/resources/features/purchase_from_radar.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Decisión F1 (PREGUNTAS_DEFINICION):** Solo registro de factura de compra, sin asiento contable. Contabilidad de partida doble es futuro.
- **Decisión F2 (PREGUNTAS_DEFINICION):** La OC no se envía por email/WhatsApp desde el sistema. Solo se genera y descarga como PDF.
- **`detalle_json` como TEXT:** En esta fase se mantiene JSON para simplicidad. Si en futuro se necesita reportes de "producto más comprado", se recomienda normalizar a tablas `purchase_order_items` y `purchase_invoice_items`.
- **Integración con Radar:** El endpoint `POST /purchase/suppliers/from-radar` busca el lead y crea el proveedor. Si el lead ya fue convertido a proveedor, rechaza.
- **Lifecycle de OC simplificado:** Estado ENVIADA es opcional ya que no se envía email en v1. Flujo mínimo: `BORRADOR → CONFIRMADA → RECIBIDA`. CANCELADA aplica desde BORRADOR o CONFIRMADA (no desde RECIBIDA).
- **Factura compra con monto diferente a OC:** Se permite. Si la diferencia es > 10%, se muestra warning (no error). Útil para descuentos, ajustes, cargos adicionales.
- **Dependencia de FASE D (Radar):** Si FASE F se ejecuta antes que FASE D, el endpoint `from-radar` se implementa pero devuelve 404 si tabla `potential_business_contacts` no existe aún. El CRUD manual de proveedores funciona independientemente.

---

*Fase F — Supply Chain. Lo que sale debe ser menor a lo que entra.*



