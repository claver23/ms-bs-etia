# 🏪 FASE C — POS Pro + Team Compass (M1)

> **Módulos:** POS Pro (`punto_venta`) + Team Compass (`employee` extendido)  
> **Paquete:** `com.etlions.ms_bs_etia.punto_venta` (ya creado en FASE A) + extensión `employee`  
> **Endpoint base:** `/api/v1/punto_venta`, `/api/v1/employee`  
> **Prioridad:** 🟡 Alta  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA, para company isolation y roles)  
> **Puede paralelizarse con:** FASE D, E, F, G, H (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Crear el flujo completo de punto de venta: un empleado asignado a un POS en una sede puede realizar ventas (wizard simplificado), y se registra quién hizo cada venta para trazabilidad y métricas de desempeño.

---

## 📋 Tareas

### TC-01 — Extender documento con `employeeId` y `pointOfSaleId`

**Entidades a modificar:** `FacturaEntity`, `BoletaEntity`

```java
// Campos opcionales — no afectan emisión SUNAT (son datos internos)
@Column(name = "employee_id")
private String employeeId;          // ¿Quién vendió?

@Column(name = "point_of_sale_id")
private String pointOfSaleId;       // ¿Desde dónde vendió?

@Column(name = "campaign_id")
private String campaignId;          // ¿Viene de campaña? (nullable, FASE E lo usará)
```

**OpenAPI:** Agregar `employeeId`, `pointOfSaleId`, `campaignId` como campos opcionales en `FacturaEmitirDto`, `BoletaEmitirDto` y en los `Data` respectivos.

---

### TC-02 — Vista Punto de Venta (vista empleado)

**Nueva vista:** `templates/pos.html`  
**View Controller:** `PosViewController.java` (`GET /pos`)

**Descripción:** Wizard simplificado para venta rápida. Básicamente el wizard actual pero:
- Pre-selecciona el empleado logueado
- Pre-selecciona la sede del POS
- Muestra productos de esa sede
- Campos reducidos (cliente + productos + pago)
- Botón "Vender" (genera factura/boleta según tipo cliente)

**Flujo:**
```
POS Vista
├── Header: [Nombre Empleado] — [Sede] — [POS: Caja 1]
├── Búsqueda rápida de cliente (DNI/RUC)
├── Lista de productos (filtrados por sede)
│   ├── Búsqueda por nombre/código
│   ├── Click → agrega al carrito
│   └── Carrito lateral con totales en tiempo real
├── Forma de pago: CONTADO / CRÉDITO
└── Botón [💳 Cobrar] → POST /factura o /boleta con employeeId + posId
```

**JS nuevo:** `pos-view.js` — Lógica del carrito y cobro rápido.

---

### TC-03 — Scoreboard (Vista rendimiento del empleado)

**Nueva vista:** `templates/scoreboard.html`  
**View Controller:** `ScoreboardViewController.java` (`GET /scoreboard`)

**Métricas:**
- Ventas del día / semana / mes
- Monto total vendido
- Ticket promedio
- Productos más vendidos por este empleado
- Comparación con otros empleados del área (ranking)

**Endpoint backend:**
```yaml
/employee/{employeeId}/metrics:
  get:
    summary: "Obtener métricas de rendimiento del empleado"
    operationId: obtenerMetricasEmpleado
    tags: [employee]
    parameters:
      - name: employeeId
        in: path
        required: true
        schema: { type: string }
      - name: periodo
        in: query
        schema: { type: string, enum: [DIA, SEMANA, MES, ANIO] }
        default: MES
    responses:
      "200":
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EmployeeMetricsData'
```

```yaml
EmployeeMetricsData:
  type: object
  properties:
    employeeId: { type: string }
    nombre: { type: string }
    area: { type: string }
    periodo: { type: string }
    totalVentas: { type: integer, example: 45 }
    montoTotal: { type: number, example: 125000.00 }
    ticketPromedio: { type: number, example: 2777.78 }
    productoMasVendido: { type: string, example: "Consultoría TI" }
    ranking: { type: integer, example: 1 }
    totalEmpleadosArea: { type: integer, example: 5 }
```

---

### TC-04 — Listar ventas por empleado / POS

**Endpoint:**
```yaml
/punto_venta/{posId}/ventas:
  get:
    summary: "Listar ventas de un punto de venta"
    operationId: listarVentasPuntoVenta
    tags: [punto_venta]
    parameters:
      - name: posId
        in: path
        required: true
      - name: desdeFecha / hastaFecha / page / size
```

**Lógica:** Query facturas + boletas WHERE `point_of_sale_id = :posId`.

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/pos_employee_assignment.feature`

```gherkin
Feature: Punto de Venta y Asignación de Empleado

  Scenario: Crear venta desde POS con empleado asignado
    Given un POS "Caja 1" con empleado "Juan Pérez" en sede "Principal"
    When el empleado vende 2 productos al cliente "María García" (DNI)
    Then se genera una boleta con employeeId del empleado
    And pointOfSaleId del POS
    And la boleta aparece en las métricas del empleado

  Scenario: POS sin empleado no puede vender
    Given un POS "Caja 2" sin empleado asignado (status EN_CONSTRUCCION)
    When intento crear una venta desde ese POS
    Then recibo error 400 "El punto de venta no tiene empleado asignado"

  Scenario: POS INACTIVO no puede vender
    Given un POS "Caja 3" con status INACTIVO
    When intento crear una venta desde ese POS
    Then recibo error 400 "El punto de venta no está activo"

  Scenario: Empleado del mes
    Given 3 empleados con ventas en el mes
    When consulto métricas del empleado con más ventas
    Then su ranking es 1
    And su montoTotal es el mayor del área

  Scenario: Venta desde POS genera factura si cliente tiene RUC
    Given un POS activo con empleado
    And un cliente con tipo documento RUC
    When realizo la venta
    Then se genera una FACTURA (no boleta)

  Scenario: Venta desde POS genera boleta si cliente tiene DNI
    Given un POS activo con empleado
    And un cliente con tipo documento DNI
    When realizo la venta
    Then se genera una BOLETA (no factura)

  Scenario: Venta en USD registra tipo de cambio
    Given un POS activo con moneda USD
    When realizo una venta en dólares
    Then se registra el tipo de cambio vigente en la factura/boleta

  Scenario: Cierre de caja del día
    Given un POS con ventas del día
    When el empleado solicita cierre de caja
    Then se genera un resumen con total vendido, cantidad de documentos y formas de pago

  Scenario: Empleado solo puede tener 1 POS activo
    Given un empleado ya asignado al POS "Caja 1"
    When intento asignarlo también al POS "Caja 2"
    Then recibo error 400 "El empleado ya está asignado a otro POS activo"

  Scenario: Descuento aplicado en POS
    Given un POS activo con empleado
    When aplico un descuento del 10% al total
    Then el descuento se refleja como AllowanceCharge en la factura/boleta
```

---

## ✅ Criterios de Aceptación

- [ ] Vista POS funcional con carrito y cobro rápido
- [ ] Ventas registran `employeeId` y `pointOfSaleId`
- [ ] Scoreboard muestra métricas reales basadas en ventas
- [ ] POS sin empleado → no puede vender
- [ ] POS inactivo → no puede vender
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +campos en Factura/Boleta, +métricas employee, +ventas POS |
| `src/main/java/.../doc_factura/persistence/FacturaEntity.java` | 🔧 +employeeId, +posId, +campaignId |
| `src/main/java/.../doc_boleta/persistence/BoletaEntity.java` | 🔧 +employeeId, +posId, +campaignId |
| `src/main/java/.../employee/service/impl/EmployeeServiceImpl.java` | 🔧 +obtenerMetricas |
| `src/main/java/.../view/PosViewController.java` | 🆕 |
| `src/main/java/.../view/ScoreboardViewController.java` | 🆕 |
| `src/main/resources/templates/pos.html` | 🆕 |
| `src/main/resources/templates/scoreboard.html` | 🆕 |
| `src/main/resources/static/js/pos-view.js` | 🆕 |
| `src/main/resources/data.sql` | 🔧 +ventas demo con employeeId |

---

## ⚠️ Notas de implementación

- **Decisión C1 (PREGUNTAS_DEFINICION):** RUC → factura automática, DNI/CE/Pasaporte → boleta automática. El empleado no decide, el tipo de documento del cliente lo determina.
- **Decisión C2 (PREGUNTAS_DEFINICION):** 1 empleado = 1 POS activo a la vez. Si ya está asignado a otro POS activo → error.
- **Decisión C3 (PREGUNTAS_DEFINICION):** Solo catálogo de productos, sin inventario. No se valida stock. Inventario es futuro.
- **Cierre de caja:** Es un reporte calculado (SUM de ventas del día por POS), no una entidad persistida. Se obtiene vía endpoint de métricas con filtro `periodo=DIA`.
- **Descuento POS:** Se implementa como `AllowanceCharge` a nivel global en la factura/boleta (misma lógica que el wizard actual).
- **Campos opcionales:** `employeeId`, `pointOfSaleId` y `campaignId` son opcionales en la factura/boleta. Las ventas creadas desde el wizard sin POS no los llevan.
- **Company isolation:** Todos los endpoints verifican que el POS, empleado y cliente pertenecen a la misma empresa del JWT.
- **Series POS = mismas que wizard:** Las ventas POS usan las mismas series (F001, B001). No hay serie especial por POS. Multi-punto-de-emisión SUNAT (F002, B002 por POS) es futuro.
- **`campaignId` sin FK validada:** Se agrega como columna String nullable. La tabla `campaigns` aún no existe (se crea en FASE E). La validación de que `campaignId` referencia una campaña real se agrega en FASE E.

---

*Fase C — Man the Fort. Cada venta tiene nombre y apellido.*



