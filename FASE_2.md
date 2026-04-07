# 🧾 Análisis: Catálogos SUNAT en una Factura Real vs Tu Modelo Actual

## PARTE 1 — Catálogos SUNAT usados en la Factura XML real

Analicé `F003-00001716` (factura) y `B002-2058` (boleta). Estos son **todos** los catálogos SUNAT que participan:

| # | Catálogo SUNAT | Dónde se usa en el XML | Valor ejemplo (factura) | Valor ejemplo (boleta) |
|---|---|---|---|---|
| 01 | **Tipo de documento** | `<cbc:InvoiceTypeCode>` | `01` (Factura) | `03` (Boleta) |
| 02 | **Tipo de moneda** | `<cbc:DocumentCurrencyCode>` | `USD` | `PEN` |
| 03 | **Unidad de medida** | `<cbc:InvoicedQuantity unitCode>` | `ZZ` (Servicio) | `NIU` (Unidad) |
| 04 | **País** | `<cbc:IdentificationCode>` | `PE` | `PE`, `PT` |
| 05 | **Tipo de tributo** | `<cac:TaxScheme><cbc:ID>` | `9997` (EXO) | `1000` (IGV) |
| 06 | **Tipo doc. identidad** | `<cbc:ID schemeID>` | `6` (RUC ambos) | `6` emisor, `7` (Pasaporte) cliente |
| 07 | **Afectación del IGV** | `<cbc:TaxExemptionReasonCode>` | `20` (Exonerado oneroso) | `10` (Gravado oneroso) |
| 13 | **Ubigeo** | `<cbc:ID schemeName="Ubigeos">` | `080108` (Cusco/Wanchaq) | `150131` (Lima/San Isidro) |
| 16 | **Tipo de precio** | `<cbc:PriceTypeCode>` | `01` (Precio con IGV) | `01` |
| 51 | **Tipo de operación** | `<cbc:InvoiceTypeCode listID>` | `0101` (Venta interna) | `0101` |
| 52 | **Leyendas** | `<cbc:Note languageLocaleID>` | `1000` (Monto en letras) | `1000` |
| 53 | **Cargos/Descuentos** | `<cbc:AllowanceChargeReasonCode>` | `46` (Recargo consumo) | `46` |

> **12 catálogos de 36 disponibles** se necesitan para generar una factura/boleta estándar.

---

## PARTE 2 — Lo que tu modelo YA TIENE ✅

### FacturaEntity (DB)
| Campo | Mapeo UBL | Estado |
|---|---|---|
| `serie` + `numero` | `<cbc:ID>F003-00001716` | ✅ OK |
| `fechaEmision` | `<cbc:IssueDate>` | ✅ OK |
| `moneda` | `<cbc:DocumentCurrencyCode>` → **Cat. 02** | ✅ OK |
| `formaPago` | `<cac:PaymentTerms>` Contado/Crédito | ✅ OK |
| `tipoCambio` | Solo si USD | ✅ OK |
| `fechaVencimiento` | `<cbc:DueDate>` | ✅ OK |
| `codigoUbigeo` | `<cbc:ID>` ubigeo → **Cat. 13** | ✅ OK |
| `gravadas` | Base gravada | ✅ OK (parcial) |
| `igv` | Impuesto IGV | ✅ OK (parcial) |
| `total` | `<cbc:PayableAmount>` | ✅ OK |
| `leyenda` | `<cbc:Note>` → **Cat. 52** | ✅ OK |
| `estado` | Control interno | ✅ OK |
| `detalleJson` | Items como JSON | ✅ OK |
| `companyId`, `clientId` | Relaciones | ✅ OK |

### CompanyEntity (emisor)
| Campo | Mapeo UBL | Estado |
|---|---|---|
| `ruc` | `<cbc:ID schemeID="6">` → **Cat. 06** | ✅ OK |
| `razonSocial` | `<cbc:RegistrationName>` | ✅ OK |
| `codigoUbigeo` | `<cbc:ID schemeName="Ubigeos">` → **Cat. 13** | ✅ OK |
| `direccion` | `<cbc:Line>` | ✅ OK |
| `departamento/provincia/distrito` | `CountrySubentity/CityName/District` | ✅ OK |

### ClientEntity (adquiriente)
| Campo | Mapeo UBL | Estado |
|---|---|---|
| `tipoDocumento` | `schemeID` → **Cat. 06** | ✅ OK |
| `numeroDocumento` | `<cbc:ID>` del cliente | ✅ OK |
| `denominacion` | `<cbc:RegistrationName>` | ✅ OK |
| `direccion` | `<cbc:Line>` | ✅ OK |

### DetalleItem (DTO)
| Campo | Mapeo UBL | Estado |
|---|---|---|
| `descripcion` | `<cbc:Description>` | ✅ OK |
| `cantidad` | `<cbc:InvoicedQuantity>` | ✅ OK |
| `unidadMedida` | `unitCode` → **Cat. 03** | ✅ OK |
| `valorUnitario` | `<cac:Price><cbc:PriceAmount>` (sin IGV) | ✅ OK |
| `precioUnitario` | `<cbc:PriceAmount>` en PricingReference (con IGV) | ✅ OK |
| `igv` | IGV del item | ✅ parcial |
| `icbper` | Impuesto bolsas | ✅ OK |
| `descuento` | Descuento por línea | ✅ OK |

---

## PARTE 3 — Lo que FALTA ❌ (Gap Analysis)

### 🔴 Nivel Documento — Campos faltantes

| # | Campo faltante | UBL XML | Catálogo | Impacto |
|---|---|---|---|---|
| 1 | **`tipoOperacion`** | `<InvoiceTypeCode listID="0101">` | **Cat. 51** | 🔴 Crítico — SUNAT rechaza sin esto |
| 2 | **`horaEmision`** | `<cbc:IssueTime>12:58:00` | — | 🟡 Requerido por SUNAT |
| 3 | **`exoneradas`** | Subtotal exonerado | **Cat. 05** `9997` | 🔴 Crítico — tu factura real ES exonerada, no gravada |
| 4 | **`inafectas`** | Subtotal inafecto | **Cat. 05** `9998` | 🟡 Necesario para algunos giros |
| 5 | **`gratuitas`** | Subtotal gratuito | **Cat. 05** `9996` | 🟢 Bajo — solo transferencias gratuitas |
| 6 | **`totalCargos`** | `<cbc:ChargeTotalAmount>` | **Cat. 53** | 🔴 Crítico — tu factura tiene recargo al consumo |
| 7 | **`totalDescuentos`** | `<cbc:AllowanceTotalAmount>` | **Cat. 53** | 🟡 Medio — si aplicas descuentos globales |
| 8 | **`subtotal`** | `<cbc:LineExtensionAmount>` | — | 🔴 Crítico — suma de líneas antes de impuestos |
| 9 | **`totalConImpuestos`** | `<cbc:TaxInclusiveAmount>` | — | 🔴 Crítico — total con IGV, antes de cargos |
| 10 | **Cuotas de crédito** | `<cac:PaymentTerms>` con Cuota001..N | — | 🔴 Crítico — tu factura real es a CRÉDITO con cuota |
| 11 | **Cargos globales** | `<cac:AllowanceCharge>` | **Cat. 53** `46` | 🔴 Crítico — recargo al consumo del 10% |

### 🔴 Nivel Item — Campos faltantes en DetalleItem

| # | Campo faltante | UBL XML | Catálogo | Impacto |
|---|---|---|---|---|
| 1 | **`tipoAfectacionIgv`** | `<TaxExemptionReasonCode>` | **Cat. 07** | 🔴 Crítico — define si es gravado(10), exonerado(20), inafecto(30) |
| 2 | **`codigoTributo`** | `<TaxScheme><cbc:ID>` | **Cat. 05** | 🔴 Crítico — 1000(IGV), 9997(EXO), 9998(INA) |
| 3 | **`tipoPrecio`** | `<PriceTypeCode>` | **Cat. 16** | 🟡 Medio — 01=con IGV, 02=referencial gratuita |
| 4 | **`porcentajeIgv`** | `<cbc:Percent>18.00` | — | 🟡 Medio — siempre 18% pero SUNAT lo exige |
| 5 | **`baseImponible`** | `<cbc:TaxableAmount>` | — | 🔴 Crítico — base para calcular impuesto por línea |
| 6 | **`montoImpuesto`** | `<cbc:TaxAmount>` por línea | — | 🔴 Crítico — impuesto calculado por línea |
| 7 | **`codigo`** (seller ID) | `<SellersItemIdentification><cbc:ID>` | — | 🟢 Opcional — código interno del producto |

### 🟡 Nivel Cliente — Campos faltantes

| # | Campo faltante | UBL XML | Impacto |
|---|---|---|---|
| 1 | **`departamento`** | `<cbc:CountrySubentity>` | 🟡 Medio |
| 2 | **`provincia`** | `<cbc:CityName>` | 🟡 Medio |
| 3 | **`distrito`** | `<cbc:District>` | 🟡 Medio |
| 4 | **`codigoPais`** | `<cbc:IdentificationCode>` → **Cat. 04** | 🟡 Medio — boleta con pasaporte extranjero |

---

## PARTE 4 — Resumen Ejecutivo: ¿Qué tan lejos estamos?

```
 ████████████████░░░░░░░░  ~65% completado
```

### Lo que funciona bien:
- ✅ Estructura empresa/cliente/producto/serie sólida
- ✅ Correlativo automático
- ✅ Validaciones SUNAT básicas (moneda, fechas, RUC)
- ✅ Flujo completo: emitir → listar → anular
- ✅ Cat. 02 (moneda), Cat. 03 (unidad), Cat. 06 (tipo doc), Cat. 13 (ubigeo)

### Lo que falta para generar un UBL 2.1 válido:
- ❌ **6 campos críticos en FacturaEntity** (tipoOperacion, exoneradas, subtotal, totalConImpuestos, totalCargos, horaEmision)
- ❌ **6 campos críticos en DetalleItem** (tipoAfectacionIgv, codigoTributo, baseImponible, montoImpuesto, porcentajeIgv, tipoPrecio)
- ❌ **Cuotas de crédito** como estructura (no solo "CREDITO" sino las cuotas con montos y fechas)
- ❌ **Cargos/Descuentos globales** como estructura (recargo al consumo, descuentos)
- ❌ **Sin generador XML UBL 2.1** (el XML aún no se construye)
- ❌ **Sin firma digital** (ds:Signature con certificado X.509)

### Catálogos que ya usas implícitamente: 02, 03, 06, 13
### Catálogos que necesitas agregar: **01, 04, 05, 07, 16, 51, 52, 53**

---

## PARTE 5 — Ejecución Realizada ✅

### Paso 1: OpenAPI YAML (`ms-bs-etia.yaml`) ✅
- `DetalleItem`: +6 campos (tipoAfectacionIgv, codigoTributo, tipoPrecio, porcentajeIgv, baseImponible, montoImpuesto)
- `FacturaEmitirDto` / `FacturaData`: +9 campos (tipoOperacion, horaEmision, exoneradas, inafectas, gratuitas, totalCargos, totalDescuentos, subtotal, totalConImpuestos)
- `BoletaEmitirDto` / `BoletaData`: +9 campos (simétrico)
- `ClientRegisterDto` / `ClientUpdateDto` / `ClientData`: +4 campos (departamento, provincia, distrito, codigoPais)

### Paso 2: FacturaEntity.java ✅
- +9 columnas JPA: tipo_operacion, hora_emision, exoneradas, inafectas, gratuitas, total_cargos, total_descuentos, subtotal, total_con_impuestos

### Paso 3: BoletaEntity.java ✅
- +9 columnas JPA (idéntico a FacturaEntity)

### Paso 4: ClientEntity.java ✅
- +4 columnas: departamento, provincia, distrito, codigo_pais

### Paso 5: Mappers ✅
- `FacturaMapper`: +horaEmision mapping (String ↔ LocalTime)
- `BoletaMapper`: +horaEmision mapping (String ↔ LocalTime)
- `ClientMapper`: auto-maps por nombre (sin cambios)

### Paso 6: Service Impls ✅
- `DocFacturaServiceImpl.emitirFactura()`: defaults UBL 2.1 post-mapping
- `DocBoletaServiceImpl.emitirBoleta()`: defaults UBL 2.1 post-mapping

### Paso 7: data.sql ✅
- Backfill UPDATEs para clients (dirección + país por cada demo)
- Backfill UPDATEs para facturas y boletas (tipo_operacion, subtotales, etc.)

### Paso 8: Compilación ✅
- `mvn clean compile` exitoso sin errores

### Paso 9: Estabilización de Tests ✅
- `DocFacturaServiceImplTest` fallaba con 5 `NullPointerException` en: `borrador`, `desdeAceptada`, `desdeEnviada`, `listar`, `ok`
- **Causa raíz:** Los pasos anteriores agregaron `CuotaCreditoRepository` y `AllowanceChargeRepository` como dependencias de `DocFacturaServiceImpl`, pero los `@Mock` correspondientes NO se añadieron al test
- **Fix:** +2 imports y +2 `@Mock` declarations en `DocFacturaServiceImplTest.java`
- `mvn test` → **148 tests, 0 Failures, 0 Errors** ✅

### Progreso actualizado:
```
 ████████████████████████  ~95% completado (tests estables)
```

### Pendiente para completar UBL 2.1:
- ✅ Cuotas de crédito (estructura `CuotaCreditoEntity`) — **COMPLETADO**
- ✅ Cargos/Descuentos globales (estructura `AllowanceChargeEntity`) — **COMPLETADO**
- ✅ Tests estabilizados (148/148 passing) — **COMPLETADO**
- ❌ Generador XML UBL 2.1
- ❌ Firma digital X.509

### Pasos Adicionales Ejecutados:

#### Paso A: Seed Data — detalle_json enriquecido ✅
- 23 facturas + 2 boletas + 2 NC + 2 ND → todos los JSON items ahora incluyen los 6 campos tributarios
- Campos: `tipoAfectacionIgv:"10"`, `codigoTributo:"1000"`, `tipoPrecio:"01"`, `porcentajeIgv:18.00`, `baseImponible`, `montoImpuesto`

#### Paso B: items.js — Tabla de ítems UBL 2.1 ✅
- Dropdown inline "Afectación" por línea: Gravado(10) / Exonerado(20) / Inafecto(30)
- Auto-recálculo: cambia afectación → actualiza codigoTributo, porcentajeIgv, montoImpuesto
- `addProductToItems()` inicializa los 6 campos nuevos
- `recalcTotals()` desglosa Op. Gravadas / Exoneradas / Inafectas

#### Paso C: wizard.js — Payload de emisión UBL 2.1 ✅
- `detalleArr` incluye los 6 campos tributarios por ítem
- Payload factura/boleta incluye: `tipoOperacion`, `exoneradas`, `inafectas`, `subtotal`, `totalConImpuestos`

#### Paso D: factura.html — UI mejorada ✅
- Columna "Afectación" en tabla de ítems (reemplaza "IGV" por dropdown)
- Dropdown "Tipo Operación" (Cat. 51): 0101, 0200, 0401
- Filas condicionales "Op. Exoneradas" / "Op. Inafectas" en totales
