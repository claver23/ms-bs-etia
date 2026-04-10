# 📊 FASE J — Data Vault — Reportes Exportables (M9)

> **Módulo:** Data Vault  
> **Paquete:** `com.etlions.ms_bs_etia.reporting`  
> **Endpoint base:** `/api/v1/reporting`  
> **Prioridad:** 🟢 Media  
> **Dependencias:** FASE A, FASE B (Security — COMPLETADA), todas las fases que generen data  
> **Puede paralelizarse con:** Todas (construye sobre data existente, post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Reportes exportables a Excel (.xlsx) con filtros de fecha, empresa, empleado, producto, campaña. Métricas de valor: producto del mes, empleado del mes, impacto de campañas, proyecciones, tiempo de uso.

---

## 📋 Tareas

### TJ-01 — Dependencias

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### TJ-02 — Endpoints REST

```yaml
/reporting/ventas:
  get: { operationId: reporteVentas, params: [companyId, desdeFecha, hastaFecha, employeeId, productId] }

/reporting/ventas/export:
  get: { operationId: exportarVentasExcel, produces: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet }

/reporting/compras:
  get: { operationId: reporteCompras }

/reporting/compras/export:
  get: { operationId: exportarComprasExcel }

/reporting/producto-del-mes:
  get: { operationId: productoDelMes, params: [companyId, mes, anio] }

/reporting/empleado-del-mes:
  get: { operationId: empleadoDelMes, params: [companyId, mes, anio, area] }

/reporting/campanas:
  get: { operationId: reporteCampanas }

/reporting/campanas/export:
  get: { operationId: exportarCampanasExcel }

/reporting/dashboard:
  get:
    summary: "Dashboard principal del usuario"
    operationId: obtenerDashboardUsuario
    tags: [reporting]
    responses:
      "200":
        schema:
          $ref: '#/components/schemas/UserDashboardData'
```

**UserDashboardData:**
```yaml
UserDashboardData:
  type: object
  properties:
    productoDelMes: { type: string, example: "Consultoría TI" }
    empleadoDelMes: { type: string, example: "Juan Pérez" }
    ventasDelMes: { type: number, example: 85000.00 }
    comprasDelMes: { type: number, example: 32000.00 }
    margenBruto: { type: number, example: 53000.00 }
    campanasActivas: { type: integer, example: 3 }
    roiPromedioMes: { type: number, example: 45.2 }
    documentosEmitidosMes: { type: integer, example: 87 }
    ticketPromedio: { type: number, example: 977.01 }
    tiempoUsoHoy: { type: string, example: "2h 34m" }
    proyeccionVentasMes: { type: number, example: 110000.00 }
    proyeccionComprasMes: { type: number, example: 45000.00 }
```

### TJ-03 — Agrupaciones de reportes sugeridas

| Grupo | Reportes | Filtros |
|-------|----------|---------|
| **Ventas** | Por periodo, por producto, por cliente, por empleado, por POS | Fecha, empresa, sede |
| **Compras** | Por periodo, por proveedor, por producto | Fecha, empresa |
| **Campañas** | ROI por campaña, conversiones, costo vs ingreso | Fecha, estado, producto |
| **Empleados** | Ranking ventas, ticket promedio, productos vendidos | Área, sede, periodo |
| **Productos** | Más vendidos, margen por producto, rotación | Periodo, categoría |
| **Financiero** | Ventas vs Compras, IGV neto, flujo de caja | Periodo mensual |
| **Uso del sistema** | Tiempo de uso, docs/día, funciones más usadas | Por usuario |

### TJ-04 — Servicio de exportación genérico

```java
@Service
public class ExcelExportService {
    public byte[] exportToExcel(String title, List<String> headers, List<List<Object>> rows) {
        // Apache POI: crear workbook, sheet, headers con estilo, datos, auto-size columns
        // Retornar bytes del .xlsx
    }
}
```

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/reporting_data_vault.feature`

```gherkin
Feature: Data Vault — Reportes exportables

  Scenario: Exportar ventas del mes a Excel
    Given facturas emitidas en abril 2026
    When exporto reporte de ventas con filtro abril
    Then recibo archivo .xlsx con datos correctos
    And las columnas incluyen: serie, número, fecha, cliente, monto, IGV, total

  Scenario: Producto del mes
    Given 50 facturas en el mes con distintos productos
    When consulto producto del mes
    Then retorna el producto con más ventas (por monto total)

  Scenario: Empleado del mes
    Given empleados con ventas en el mes
    When consulto empleado del mes
    Then retorna el empleado con mayor monto vendido y su ranking

  Scenario: Dashboard usuario con métricas
    Given ventas, compras y campañas del mes
    When accedo al dashboard
    Then veo todas las métricas consolidadas incluyendo margen bruto

  Scenario: Reporte financiero ventas vs compras
    Given ventas por S/ 85,000 y compras por S/ 32,000 en el mes
    When consulto reporte financiero
    Then margenBruto = S/ 53,000
    And IGV neto = IGV ventas - IGV compras

  Scenario: Reporte sin datos en rango devuelve Excel vacío
    Given no hay facturas entre 01/01/2025 y 31/01/2025
    When exporto reporte de ventas con ese rango
    Then recibo un .xlsx con headers pero sin filas de datos
    And un mensaje "No se encontraron datos para el período seleccionado"

  Scenario: Proyección de ventas del mes
    Given ventas de S/ 40,000 al día 15 del mes
    When consulto proyección
    Then proyeccionVentasMes ≈ S/ 80,000 (extrapolación lineal)

  Scenario: Tiempo de uso del día
    Given el usuario se autenticó hace 2 horas y 34 minutos
    When accedo al dashboard
    Then tiempoUsoHoy = "2h 34m"

  Scenario: Ranking de empleados por área
    Given empleados del área "Ventas" con distintos montos
    When consulto ranking del área "Ventas"
    Then recibo lista ordenada por monto total descendente con posición

  Scenario: Exportar campañas a Excel con ROI
    Given campañas con métricas
    When exporto reporte de campañas
    Then el .xlsx incluye: nombre, status, budget, spent, conversiones, ROI%
```

---

### TJ-05 — Tracking de tiempo de uso

```java
@Component
public class SessionTimeTracker {
    // Almacena en memoria (Map<userId, loginTimestamp>)
    // Al hacer GET /reporting/dashboard, calcula diferencia
    // No requiere tabla adicional — es dato volátil
}
```

**Nota:** No es un tracking completo de sesión. Solo diferencia entre `ultimo_login` y `now()`. Suficiente para v1.

---

## ✅ Criterios de Aceptación

- [ ] Reporte de ventas por período exportable a Excel
- [ ] Reporte de compras por período exportable a Excel
- [ ] Reporte de campañas con ROI exportable a Excel
- [ ] Producto del mes calculado correctamente
- [ ] Empleado del mes calculado correctamente con ranking
- [ ] Dashboard usuario con todas las métricas consolidadas
- [ ] Margen bruto = ventas - compras
- [ ] Proyección de ventas por extrapolación lineal
- [ ] Tiempo de uso del día mostrado
- [ ] Excel vacío con headers cuando no hay datos (no error)
- [ ] Company isolation en todos los endpoints
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `pom.xml` | 🔧 +poi-ooxml (si no está de FASE E) |
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag reporting, +endpoints, +schemas |
| `src/main/java/.../reporting/**` | 🆕 Paquete completo (~10 archivos) |
| `src/main/java/.../reporting/openapi/ReportingOpenApi.java` | 🆕 |
| `src/main/java/.../reporting/service/ReportingService.java` | 🆕 |
| `src/main/java/.../reporting/service/impl/ReportingServiceImpl.java` | 🆕 |
| `src/main/java/.../reporting/service/ExcelExportService.java` | 🆕 |
| `src/main/java/.../reporting/service/SessionTimeTracker.java` | 🆕 |
| `src/main/java/.../view/ReportingViewController.java` | 🆕 |
| `src/main/resources/templates/reporting.html` | 🆕 |
| `src/main/resources/templates/dashboard.html` | 🔧 +métricas nuevas |
| `src/main/resources/static/js/reporting-view.js` | 🆕 |
| `src/test/resources/features/reporting_data_vault.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **ExcelExportService genérico:** Un servicio reutilizable que recibe título, headers y rows. Todas las fases que exportan Excel lo usan.
- **Proyección simple:** `proyeccion = (ventasActuales / diaDelMes) * diasTotalesDelMes`. No requiere machine learning.
- **Tiempo de uso:** Basado en `ultimo_login` de UserEntity. No es tracking preciso (no detecta inactividad). Suficiente para MVP.

---

*Fase J — Data Vault. Los datos sin reportes son ruido. Los reportes sin datos son mentira.*



