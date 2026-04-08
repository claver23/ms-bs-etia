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

---

## PARTE 6 — Análisis Final: ¿Qué falta para 100%?

> **Fecha de corte:** 2026-04-07

### Estado del Modelo de Datos: ~95% ✅

Todos los campos del XML real `F003-00001716.xml` (218 líneas, UBL 2.1) tienen contraparte en:
- `FacturaEntity` (20 campos mapeados)
- `DetalleItem` DTO (13 campos tributarios)
- `CuotaCreditoEntity` (cuotas de crédito)
- `AllowanceChargeEntity` (cargos/descuentos Cat.53)
- `CompanyEntity` (emisor completo)
- `ClientEntity` (receptor con depto/prov/dist/país)

**12 catálogos SUNAT cubiertos:** 01, 02, 03, 04, 05, 06, 07, 13, 16, 51, 52, 53

### Pendientes para completar la cadena de facturación:

| # | Componente | Descripción | Esfuerzo | Estado |
|---|---|---|---|---|
| 1 | **`UblInvoiceXmlBuilder`** | Servicio que ensambla el XML UBL 2.1 desde las entidades. Namespaces, atributos schemeID, secciones Supplier/Customer/PaymentTerms/AllowanceCharge/TaxTotal/LegalMonetaryTotal/InvoiceLine. | 🔴 Alto | ✅ **Completado** — 36 tests |
| 2 | **Endpoint XML descarga** | `GET /factura/{facturaId}/xml` — regularizado al patrón OpenAPI-first (spec → codegen → service → controller). | 🟡 Medio | ✅ **Completado** |
| 3 | **`SunatProperties` ampliado** | Configuración type-safe con `certificado.base-path` y `sol.default-user/password` en `application.yaml`. | 🟢 Bajo | ✅ **Completado** |
| 4 | **`DigitalSignatureService`** | Firma digital X.509 enveloped-signature. Cargar .pfx/.p12, firmar con RSA-SHA, insertar ds:Signature en UBLExtensions. | 🔴 Alto | ❌ Pendiente |
| 5 | **`SunatSoapClient`** | Envío SOAP a SUNAT (sendBill/sendSummary), parseo de CDR, actualización de hashCdr/codigoRespuesta/estado en entidad. | 🟡 Medio | ❌ Pendiente |
| 6 | **Vinculación Sede → XML** | AddressTypeCode del establecimiento emisor (0001=principal, 0002+). SedeEntity ya existe. | 🟢 Bajo | ❌ Pendiente |

### Progreso global:
```
 MODELO DE DATOS  ██████████████████████░░  ~95% ✅
 GENERADOR XML    ████████████████████████  100% ✅  (36 tests)
 ENDPOINT XML     ████████████████████████  100% ✅  (OpenAPI-first)
 CONFIG SUNAT     ████████████████████████  100% ✅  (type-safe props)
 FIRMA DIGITAL    ░░░░░░░░░░░░░░░░░░░░░░░░   0% ❌
 ENVÍO SUNAT      ░░░░░░░░░░░░░░░░░░░░░░░░   0% ❌
```

> **Conclusión:** El modelo de datos está completo, el generador XML UBL 2.1 está implementado y testeado (36 tests), y el endpoint de descarga sigue el patrón OpenAPI-first del proyecto. Lo que resta es la **firma digital** y el **envío SOAP a SUNAT**. Estos 2 componentes constituyen la **FASE 3** del proyecto.

---

## PARTE 7 — Paso Detallado: Regularización Endpoint XML (2026-04-07)

### Problema Detectado
`FacturaXmlController.java` era un controller code-first (`@RestController` con `@GetMapping` manual) que rompía el patrón del proyecto: **OpenAPI YAML → codegen → interfaz generada → controller impl → service → persistence**.

### Pasos Ejecutados

#### Paso R1: Agregar endpoint al OpenAPI YAML ✅
- Nuevo path `/factura/{facturaId}/xml` con tag `factura`
- `operationId: descargarFacturaXml`
- Response `200`: `application/xml` con `type: string, format: binary` + header `Content-Disposition`
- Response `204`: factura sin XML generado
- Response `404`: factura no encontrada

#### Paso R2: Regenerar interfaces (`mvn clean compile`) ✅
- El codegen generó `descargarFacturaXml(String facturaId)` en `FacturaApi.java`
- Retorna `ResponseEntity<org.springframework.core.io.Resource>`

#### Paso R3: Agregar método al Service Layer ✅
- `DocFacturaService.descargarFacturaXml(String facturaId)` → devuelve `FacturaXmlResult`
- `FacturaXmlResult` es un `record(byte[] content, String filename, boolean hasContent)`
- Implementación en `DocFacturaServiceImpl`: busca factura, construye filename SUNAT, retorna bytes UTF-8

#### Paso R4: Override en DocFacturaOpenApi ✅
- `descargarFacturaXml()` invoca el service
- Si `!hasContent()` → `ResponseEntity.noContent().build()` (204)
- Si tiene contenido → `ResponseEntity.ok()` con `ByteArrayResource`, header `Content-Disposition`, y `MediaType.APPLICATION_XML`

#### Paso R5: Eliminar FacturaXmlController.java ✅
- Archivo eliminado del paquete `doc_factura.xml`
- `UblInvoiceXmlBuilder.java` se conserva intacto en el mismo paquete

#### Paso R6: Ampliar SunatProperties ✅
- `application.yaml`: sección `sunat.certificado.base-path` y `sunat.sol.default-user/password`
- Variables de entorno: `SUNAT_CERT_PATH`, `SUNAT_SOL_USER`, `SUNAT_SOL_PASSWORD`
- `SunatProperties.java`: clases internas `Certificado` y `Sol` para binding type-safe

#### Paso R7: Verificación ✅
- `mvn clean compile` → **BUILD SUCCESS** (254 source files)
- `mvn test` → **184 tests, 0 Failures, 0 Errors**

---

## PARTE 8 — Estabilización post-regularización (2026-04-07)

### Problema Detectado
El test `DocFacturaServiceImplTest.emitirExitoso` fallaba con:
```
TooManyActualInvocations: facturaRepository.save(<any>);
Wanted 1 time, but was 2 times.
```

**Causa raíz:** La implementación de `emitirFactura()` ahora invoca `facturaRepository.save()` **dos veces**:
1. **Línea 144:** Persistir la entidad inicial (para obtener el `id` y usarlo en cuotas/cargos)
2. **Línea 164:** Segundo save para persistir `xmlContent` y `xmlPath` generados por `UblInvoiceXmlBuilder`

El test original (`verify(facturaRepository).save(any())`) solo esperaba 1 invocación — era correcto antes de la regularización del XML, pero ya no.

### Fix Aplicado ✅
- `DocFacturaServiceImplTest.emitirExitoso`: `verify(facturaRepository).save(any())` → `verify(facturaRepository, times(2)).save(any())`
- Comentario explicativo agregado: `// save() se invoca 2 veces: 1ª persiste la entidad, 2ª agrega xmlContent/xmlPath`

### Resultado
- `mvn test` → **184 tests, 0 Failures, 0 Errors** ✅
- Desglose `DocFacturaServiceImplTest`: **52 tests** (12 nested classes, todos verdes)

### Estado actualizado del proyecto:
```
 MODELO DE DATOS  ██████████████████████░░  ~95% ✅
 GENERADOR XML    ████████████████████████  100% ✅  (36 tests)
 ENDPOINT XML     ████████████████████████  100% ✅  (OpenAPI-first)
 CONFIG SUNAT     ████████████████████████  100% ✅  (type-safe props)
 TESTS            ████████████████████████  100% ✅  (184/184 passing)
 FIRMA DIGITAL    ░░░░░░░░░░░░░░░░░░░░░░░░   0% ❌
 ENVÍO SUNAT      ░░░░░░░░░░░░░░░░░░░░░░░░   0% ❌
```

### Pendientes para FASE 3:
| # | Componente | Descripción | Esfuerzo | Estado |
|---|---|---|---|---|
| 1 | **`DigitalSignatureService`** | Firma digital X.509 enveloped-signature. Cargar .pfx/.p12, firmar con RSA-SHA, insertar ds:Signature en UBLExtensions. | 🔴 Alto | ❌ Pendiente |
| 2 | **`SunatSoapClient`** | Envío SOAP a SUNAT (sendBill/sendSummary), parseo de CDR, actualización de hashCdr/codigoRespuesta/estado en entidad. | 🟡 Medio | ❌ Pendiente |
| 3 | **Vinculación Sede → XML** | AddressTypeCode del establecimiento emisor (0001=principal, 0002+). SedeEntity ya existe. | 🟢 Bajo | ❌ Pendiente |

---

## PARTE 9 — Feature: Emisión en Envío + Borrador (2026-04-07)

### Alcance
> **La Fase 2 se enfoca exclusivamente en la FACTURA.** Boleta, Notas de Crédito y Notas de Débito son universos aparte y se abordarán en fases futuras.

### Concepto
El sistema diferencia entre **guardar** (BORRADOR) y **emitir** (genera XML UBL 2.1 + notifica). La configuración "Emisión en Envío" controla el comportamiento por defecto:

- **Activado (default):** Al crear una factura, se genera XML automáticamente → estado EMITIDA
- **Desactivado:** La factura se guarda como BORRADOR → el usuario debe emitirla manualmente

### Validación del flujo existente ✅

| Componente | Estado | Detalle |
|---|---|---|
| `emisionEnEnvio` en `CompanyEntity` | ✅ Ya existe | Columna `emision_en_envio BOOLEAN DEFAULT TRUE` |
| `emisionEnEnvio` en `CompanyRegisterDto` / `CompanyUpdateDto` | ✅ Ya existe | Campo en OpenAPI YAML |
| `emitir` flag en `FacturaEmitirDto` | ✅ Ya existe | `boolean, default: true` — controla si genera XML |
| `DocFacturaServiceImpl` respeta `emitir` | ✅ Ya existe | `!Boolean.FALSE.equals(request.getEmitir())` |
| Estado `BORRADOR` en BD | ✅ Ya existe | 5 facturas + 1 boleta + 1 NC + 1 ND en seed |
| Endpoint `PUT /factura/{id}/emitir` | ✅ Ya existe | Promueve BORRADOR → genera XML + notifica |
| Config overlay toggle | ✅ Ya existe | Checkbox "Emisión en Envío" en Preferencias |
| `ETIA.config.emisionEnEnvio` | ✅ Ya existe | localStorage → `emisionEnEnvio` |
| Preferencia `emisionEnEnvio` en seed data | ✅ Ya existe | `pref-005` → `'true'` |
| Wizard UI adapta botones según config | ✅ **Agregado** | Primario/secundario se intercambian |

### Cambios implementados (solo Factura)

#### 1. Wizard UI — Adaptación dinámica según `emisionEnEnvio` ✅
- **Función `_updateWizardEmisionUI()`** se ejecuta al iniciar el wizard
- Si `emisionEnEnvio=true`:
  - 🚀 "Emitir Comprobante" es el botón primario (accent)
  - 💾 "Guardar Borrador" es secundario (ghost)
  - Step 4 label: "Emitir"
- Si `emisionEnEnvio=false`:
  - 💾 "Guardar Borrador" es el botón primario (accent)
  - 🚀 "Emitir Comprobante" sigue disponible pero secundario (ghost con borde accent)
  - Warning banner visible: "Emisión en Envío desactivada..."
  - Step 4 label: "Guardar"
  - Link directo a config para cambiar
- **Ambos botones siempre disponibles** — el usuario puede elegir emitir o guardar borrador independientemente de la config

#### 2. HTML wizard — `wiz-emision-hint` banner ✅
- Banner warning con badge BORRADOR que se muestra cuando Emisión en Envío está desactivada
- Incluye link a configuración para cambiar rápidamente

#### 3. Seed Data — XML UBL 2.1 completo para todas las ACEPTADAS y ENVIADAS ✅
- **10 facturas** ahora tienen `xml_content` y `xml_path` poblados
- Los XMLs incluyen estructura UBL 2.1 completa: Supplier, Customer, PaymentTerms, TaxTotal, LegalMonetaryTotal, InvoiceLine
- Variedad realista: Contado/Crédito, PEN/USD, con/sin cargos-descuentos, con/sin cuotas
- Basados en la estructura del XML real `F003-00001716.xml` del proyecto

#### ⚠️ Boleta / Notas — Fuera de alcance de Fase 2
- `BoletaEmitirDto` **NO** tiene campo `emitir` — la boleta siempre emite por ahora
- `DocBoletaServiceImpl` siempre emite (sin lógica borrador)
- Notas de Crédito y Débito se mantienen sin cambios
- El soporte borrador para estos documentos vendrá en fases futuras

### Tests ✅
- `mvn test` → **182 tests, 0 Failures, 0 Errors**
- `mvn clean compile` → **BUILD SUCCESS**

### Estado actualizado del proyecto:
```
 MODELO DE DATOS  ██████████████████████░░  ~95% ✅
 GENERADOR XML    ████████████████████████  100% ✅  (36 tests)
 ENDPOINT XML     ████████████████████████  100% ✅  (OpenAPI-first)
 CONFIG SUNAT     ████████████████████████  100% ✅  (type-safe props)
 TESTS            ████████████████████████  100% ✅  (182/182 passing)
 EMISIÓN/BORRADOR ████████████████████████  100% ✅  (UI + API + Service — solo Factura)
 SEED DATA + XML  ████████████████████████  100% ✅  (10 facturas con XML UBL)
 FIRMA DIGITAL    ░░░░░░░░░░░░░░░░░░░░░░░░   0% ❌
 ENVÍO SUNAT      ░░░░░░░░░░░░░░░░░░░░░░░░   0% ❌
```

### Pendientes para FASE 3:
| # | Componente | Descripción | Esfuerzo | Estado |
|---|---|---|---|---|
| 1 | **`DigitalSignatureService`** | Firma digital X.509 enveloped-signature. Cargar .pfx/.p12, firmar con RSA-SHA, insertar ds:Signature en UBLExtensions. | 🔴 Alto | ❌ Pendiente |
| 2 | **`SunatSoapClient`** | Envío SOAP a SUNAT (sendBill/sendSummary), parseo de CDR, actualización de hashCdr/codigoRespuesta/estado en entidad. | 🟡 Medio | ❌ Pendiente |
| 3 | **Vinculación Sede → XML** | AddressTypeCode del establecimiento emisor (0001=principal, 0002+). SedeEntity ya existe. | 🟢 Bajo | ❌ Pendiente |

---

## PARTE 10 — Garantía de Integridad: XML descargado = XML transmitido (2026-04-07)

### Problema Detectado
El endpoint `GET /factura/{id}/xml` tenía un **fallback on-the-fly** que regeneraba el XML cuando no existía en disco ni en BD. Esto creaba un riesgo de inconsistencia:

```
Escenario problemático:
1. Factura emitida → XML generado y transmitido a SUNAT
2. Empresa cambia su razón social / dirección
3. Usuario descarga XML → se regenera on-the-fly con datos NUEVOS
4. XML descargado ≠ XML transmitido a SUNAT → auditoría fallida
```

### Principio: Inmutabilidad del XML emitido
> **El XML descargado debe ser EXACTAMENTE el mismo que se generó al momento de emitir.** No puede regenerarse porque los datos pueden haber cambiado.

### Estrategia de descarga corregida (3 niveles)

| # | Fuente | Descripción | Garantía |
|---|---|---|---|
| 1️⃣ | **Disco** (`xmlPath`) | Archivo inmutable en `etia_storage/` | ✅ Es el original |
| 2️⃣ | **BD** (`xmlContent`) | Copia fiel guardada al emitir | ✅ Es el original |
| 3️⃣ | **Sin XML** | Factura aún no emitida (BORRADOR) o legacy | ✅ Devuelve `204 No Content` — no inventa |

### Cambio realizado
- **`DocFacturaServiceImpl.descargarFacturaXml()`**: Eliminado el fallback 3 (on-the-fly)
- Si no hay XML persistido → devuelve `FacturaXmlResult(null, filename, false)` → HTTP 204
- Frontend ya maneja 204: muestra "Esta factura aún no tiene XML generado"

### Flujo completo garantizado

```
EMITIR (emitir=true)               DESCARGAR XML
    │                                    │
    ├─ UblInvoiceXmlBuilder             ├─ 1) Leer de disco (xmlPath)
    │   genera XML                      │   ↓ no existe?
    ├─ xmlContent ← XML (BD)            ├─ 2) Leer de BD (xmlContent)
    ├─ xmlPath ← guardarXml (disco)     │   ↓ no existe?
    └─ INMUTABLE desde aquí             └─ 3) 204 No Content
                                             "Debe emitir primero"
```

### Tests (Parte 10) ✅
- `mvn test` → **182 tests, 0 Failures, 0 Errors**

---

## PARTE 11 — Wizard 5-Step Refactor + Edit Mode (2026-04-07)

### Alcance
Reestructuración del wizard de emisión de comprobantes para mejorar la UX, agregar modo edición de borradores, y mejorar la distribución visual.

### Cambios Implementados

#### 1. Wizard 5 Steps (antes 4) ✅
- **Step 1 — 📄 Documento:** Serie (dropdown dinámico), número (readonly con padding 8 dígitos), fecha, moneda, tipo de cambio, forma de pago, fecha vencimiento, tipo operación, leyenda, campos de nota (NC/ND)
- **Step 2 — 🏢 Emisor:** Datos del emisor (readonly, precargados)
- **Step 3 — 👤 Cliente:** Búsqueda y datos del adquiriente
- **Step 4 — 📦 Detalle:** Productos, tabla de ítems, cargos/descuentos globales, cuotas de crédito
- **Step 5 — 🚀 Emitir:** Revisión completa con tabla de ítems, totales, y botones de acción

#### 2. Serie como Dropdown Dinámico ✅
- La serie se carga desde `ETIA.activeSede.series[]` filtrado por tipo de documento
- Se selecciona por defecto la serie configurada en `ETIA.config.serieFac/serieBol/serieNC/serieND`
- Fallback: si no hay series en la sede, muestra la serie del config como única opción
- Control visualmente compacto (`flex: 0 0 120px`) — el número de factura (con padding) ocupa más espacio

#### 3. Distribución Visual Mejorada ✅
- **Serie:** control pequeño (120px) con `font-weight:600; letter-spacing:0.5px`
- **Número:** control grande (flex:1) con `font-family:var(--font-mono); font-size:0.95rem; letter-spacing:1px`
- El número muestra `(se asigna al emitir)` en modo creación, o el número con padding en modo edición
- Proporción visual prolija: serie compacta + número prominente

#### 4. Edit Mode — Carga de Borrador ✅
- URL: `/wizard?type=FACTURA&id=<facturaId>`
- Banner de edición: "✏️ Editando F001-00000001"
- Función `_loadDraftForEdit()` carga todos los datos del documento:
  - Step 1: Serie (readonly si ya asignada), fecha, moneda, tipo cambio, forma pago, fecha venc, tipo operación, leyenda
  - Step 3: Cliente (desde `doc.cliente` o `doc.clientId`)
  - Step 4: Ítems (reconstruye array `items[]` con todos los campos tributarios), cuotas, cargos/descuentos
- En edit mode, serie y fecha de emisión son **readonly** (ya se asignaron al crear)
- Botón "Emitir Borrador" usa `ApiClient.emitirFacturaBorrador(editDocId)` (endpoint `PUT /factura/{id}/emitir` existente)
- Botón "Guardar Borrador" se oculta (el documento ya está guardado)

#### 5. Review (Step 5) con Tabla de Ítems ✅
- Sección "📄 Documento" con todos los datos del comprobante
- Sección "🏢 Emisor" y "👤 Adquiriente" en layout de 2 columnas
- **Tabla de ítems completa** en review: #, Descripción, Cant., V.Unit., IGV, Subtotal
- Conteo de ítems: "3 ítems"
- Totales: Op. Gravadas, IGV (18%), TOTAL A PAGAR
- Fechas formateadas con `ETIA.formatDate()` (dd/MM/yyyy)

#### 6. Facturas.html — Links de Edición ✅
- **BORRADOR:** `href="/wizard?type=FACTURA&id=<facturaId>"` (antes no pasaba el id)
- **RECHAZADA:** `href="/wizard?type=FACTURA&id=<facturaId>"` (permite corregir cargando datos)

#### 7. Helper `ETIA.formatNumero()` ✅
- Formatea serie + número al estándar SUNAT: `F001-00000011`
- Padding automático a 8 dígitos

#### 8. Cargos/Descuentos Globales — Documentación ✅
- **Solo disponible en Facturas** (boletas y notas no usan descuentos globales según UBL 2.1 estándar)
- Códigos Cat. 53 SUNAT aplicables:
  - `00` — Descuento global que afecta la base imponible
  - `02` — Descuento global que NO afecta la base imponible
  - `45` — FISE (Fondo de Inclusión Social Energético)
  - `46` — Recargo al consumo (10% restaurantes/hoteles)
  - `47` — Cargo por servicio
  - `48` — Percepción SUNAT
  - `49` — Detracción SUNAT
  - `50` — Bonificaciones
  - `51` — Descuento por volumen
  - `52` — Descuento especial
  - `53` — Otros cargos
- Hint visual en el wizard: "Aplican descuentos/recargos por volumen, recargo al consumo (Cat.53 código 46), etc."

---

## PARTE 12 — Fix: Editar desde Preview + Cargos/Descuentos en Review

### Problemas Detectados
1. **El wizard no distinguía correctamente modo nuevo vs edición** — la data demo del primer cliente se precargaba automáticamente en modo nuevo
2. **Números de documento sin padding** — en la lista de facturas se mostraba `F001-1` en vez de `F001-00000001`
3. **En modo edición, el detalle de ítems se perdía** — el wizard intentaba leer `doc.cliente` (que no existe en `FacturaData`, solo hay `clientId`)
4. **`_applyClientsData()` auto-seleccionaba el primer cliente** como `ETIA.client`, causando que el wizard pre-llenara campos

### Cambios Implementados

#### 1. `app-state.js` — Eliminar auto-selección de cliente demo ✅
- **`ETIA.clients = []`** — nuevo array que almacena TODOS los clientes cargados
- **`_applyClientsData()`** — ahora solo almacena en `ETIA.clients`, NO auto-selecciona `ETIA.client`/`ETIA.clientId`
- **`ETIA.findClientById(id)`** — nuevo helper para buscar un cliente por ID en la lista cacheada
- **`_renderNotifBadge()`** — usa `ETIA.formatNumero()` para números con padding en notificaciones

#### 2. `factura.html` (wizard init) — Eliminar pre-carga demo ✅
- Eliminado el bloque que llenaba campos de cliente desde `ETIA.client` en modo nuevo
- Los campos de cliente ahora inician **vacíos** — el usuario selecciona explícitamente vía modal de búsqueda
- Comentario claro: `/* Client fields start EMPTY — user selects via modal or edit mode loads from API */`

#### 3. `wizard.js` `_loadDraftForEdit()` — Carga robusta de cliente y detalle ✅
- **Cliente**: Ahora busca en `ETIA.findClientById(doc.clientId)`. Si no está en cache, hace fetch de todos los clientes y reintenta
- **Helper `_applyClientToWizard(cl)`**: Función dedicada para poblar los campos de cliente del Step 3
- **Detalle de ítems**: Logging de debug para diagnóstico (`console.log`), fallback mejorado para `baseImponible` y `precioUnitario`
- **Null-safe**: Todos los accesos a DOM con guard `if (el)` para evitar NPE si un elemento no existe
- **Estado BORRADOR → RECHAZADA**: Ambos cargan correctamente en modo edición

#### 4. `facturas.html` — Números formateados con padding 8 dígitos ✅
- `ETIA.formatNumero(d.serie, d.numero)` en todas las ocurrencias: header de la card, botones de anular/eliminar, links de edición
- Antes: `F001-1` → Ahora: `F001-00000001`

#### 5. `boletas.html` — Números formateados ✅
- Misma corrección con `ETIA.formatNumero()`

#### 6. `doc-preview.js` — Números formateados en 4 lugares ✅
- Preview header: `Serie / Número` ahora con padding
- Botón anular: confirmación con número formateado
- Botón eliminar: confirmación con número formateado  
- Export PDF: serie-número con padding

### Archivos Modificados
| Archivo | Cambio |
|---|---|
| `app-state.js` | `ETIA.clients[]`, `findClientById()`, no auto-select, padding en notifs |
| `wizard/factura.html` | Eliminado pre-fill demo de cliente |
| `wizard.js` | `_loadDraftForEdit()` robusto, `_applyClientToWizard()` helper |
| `facturas.html` | `ETIA.formatNumero()` en todos los números |
| `boletas.html` | `ETIA.formatNumero()` en todos los números |
| `doc-preview.js` | `ETIA.formatNumero()` en 4 ubicaciones |

### Tests ✅
- `mvn test` → **184 tests, 0 Failures, 0 Errors**

### Comportamiento Corregido

| Escenario | Antes (roto) | Ahora (correcto) |
|---|---|---|
| Wizard nuevo | Pre-llenaba cliente demo "INVERSIONES TECH PERU" | Campos vacíos, usuario busca cliente |
| Wizard edición - cliente | Campos vacíos (doc.cliente undefined) | Busca por doc.clientId en cache/API |
| Wizard edición - ítems | Se perdían (no reportado como bug pero reforzado) | Carga robusta con logging |
| Lista facturas - números | `F001-1` | `F001-00000001` |
| Preview - números | `F001-1` | `F001-00000001` |
| Notificaciones - números | `F001-1` | `F001-00000001` |

---

## PARTE 13 — Fix: Preview, PDF, Quick-Emit + Seed Data Mejorado (2026-04-07)

### Problemas Detectados

1. **`exportPdf()` incompleto** — El PDF generado desde el previsualizador no incluía cargos/descuentos globales, cuotas de crédito, ni las filas de cargos/descuentos en el resumen de totales.

2. **`quickEmit()` creaba factura nueva** — En `facturas.html`, el botón "📤 Emitir" de la lista de borradores llamaba a `ApiClient.emitirFactura(payload)` con un payload reconstruido, lo que **creaba una NUEVA factura** en vez de emitir el borrador existente.

3. **Test desactualizado** — `SunatValidatorTest.transicionesNoPermitidas[8]` esperaba que `RECHAZADA → ENVIADA` fuera inválido, pero el mapa `TRANSICIONES_VALIDAS` en `SunatValidator.java` permite esta transición (re-emisión directa de rechazada). El test fue escrito antes de agregar esta transición.

4. **Seed data sin borrador multi-ítem** — Todos los borradores tenían un solo ítem. No había escenario de prueba con múltiples productos + cargos/descuentos combinados.

### Cambios Implementados

#### 1. `doc-preview.js` — `exportPdf()` completo ✅
- **Cargos/Descuentos en PDF**: Nueva función `_pdfCargosDescuentosHtml(doc)` genera tabla HTML con tipo, código Cat.53, descripción, factor, base y monto.
- **Cuotas en PDF**: Nueva función `_pdfCuotasHtml(doc, monSym)` genera tabla HTML con # cuota, monto, fecha vencimiento y moneda.
- **Totales Cargos/Descuentos en PDF**: Se muestran filas "+ Cargos" y "− Descuentos" en el resumen antes de TOTAL, consistente con el previsualizador.
- El PDF ahora refleja exactamente la misma información que el modal de preview.

#### 2. `facturas.html` — `quickEmit()` corregido ✅
**Antes (roto):**
```javascript
quickEmit → getFactura → reconstruir payload → emitirFactura(payload)  // ❌ crea nueva
```
**Después (correcto):**
```javascript
quickEmit → emitirFacturaBorrador(facturaId)  // ✅ emite el borrador existente
```
- Usa el endpoint `PUT /factura/{id}/emitir` que genera XML UBL 2.1 y transiciona BORRADOR → ENVIADA.
- El código se simplifica de 15 líneas a 7 líneas.

#### 3. `SunatValidatorTest` — Test corregido ✅
- `RECHAZADA, ENVIADA` movido de `transicionesNoPermitidas` a `transicionesPermitidas`
- Alineado con `TRANSICIONES_VALIDAS` y con `DocFacturaServiceImpl.emitirFacturaBorrador()` que permite re-emisión directa desde RECHAZADA

#### 4. `data.sql` — Borrador multi-ítem (`fac-demo-018`) ✅
- **Antes**: 1 ítem (Hosting Cloud), S/ 531.00
- **Después**: 5 ítems variados:
  - Hosting Cloud (×2), Soporte Técnico (×3), Certificado Digital (×2), Consultoría TI (×1), Capacitación (×2)
  - Total gravado: S/ 5,530.00 → IGV: S/ 995.40 → Total: S/ 6,525.40
  - **Cargos/Descuentos**: Descuento comercial global 5% (S/ 276.50) + Recargo al consumo 10% (S/ 553.00)
  - Primer borrador del seed que combina multi-ítem + descuento + cargo global

---

## PARTE 14 — Paso Detallado: Regularización Endpoint XML (2026-04-07)

### Problema Detectado
`FacturaXmlController.java` era un controller code-first (`@RestController` con `@GetMapping` manual) que rompía el patrón del proyecto: **OpenAPI YAML → codegen → interfaz generada → controller impl → service → persistence**.

### Pasos Ejecutados

#### Paso R1: Agregar endpoint al OpenAPI YAML ✅
- Nuevo path `/factura/{facturaId}/xml` con tag `factura`
- `operationId: descargarFacturaXml`
- Response `200`: `application/xml` con `type: string, format: binary` + header `Content-Disposition`
- Response `204`: factura sin XML generado
- Response `404`: factura no encontrada

#### Paso R2: Regenerar interfaces (`mvn clean compile`) ✅
- El codegen generó `descargarFacturaXml(String facturaId)` en `FacturaApi.java`
- Retorna `ResponseEntity<org.springframework.core.io.Resource>`

#### Paso R3: Agregar método al Service Layer ✅
- `DocFacturaService.descargarFacturaXml(String facturaId)` → devuelve `FacturaXmlResult`
- `FacturaXmlResult` es un `record(byte[] content, String filename, boolean hasContent)`
- Implementación en `DocFacturaServiceImpl`: busca factura, construye filename SUNAT, retorna bytes UTF-8

#### Paso R4: Override en DocFacturaOpenApi ✅
- `descargarFacturaXml()` invoca el service
- Si `!hasContent()` → `ResponseEntity.noContent().build()` (204)
- Si tiene contenido → `ResponseEntity.ok()` con `ByteArrayResource`, header `Content-Disposition`, y `MediaType.APPLICATION_XML`

#### Paso R5: Eliminar FacturaXmlController.java ✅
- Archivo eliminado del paquete `doc_factura.xml`
- `UblInvoiceXmlBuilder.java` se conserva intacto en el mismo paquete

#### Paso R6: Ampliar SunatProperties ✅
- `application.yaml`: sección `sunat.certificado.base-path` y `sunat.sol.default-user/password`
- Variables de entorno: `SUNAT_CERT_PATH`, `SUNAT_SOL_USER`, `SUNAT_SOL_PASSWORD`
- `SunatProperties.java`: clases internas `Certificado` y `Sol` para binding type-safe

#### Paso R7: Verificación ✅
- `mvn clean compile` → **BUILD SUCCESS** (254 source files)
- `mvn test` → **184 tests, 0 Failures, 0 Errors**

---

## PARTE 15 — Plan Estructurado: Fase 2.5 (2026-04-07) — CONFIRMADO ✅

> **Fecha:** 2026-04-07 | **Estado:** ✅ CONFIRMADO — todas las preguntas respondidas, listo para implementar
>
> **Evidencia clave analizada:** `sample_docs/20517863948-03-B002-2058.xml` — XML UBL 2.1 de boleta real.
> Confirma que la boleta usa **exactamente la misma estructura** `<Invoice>` que la factura,
> con `<InvoiceTypeCode>03</InvoiceTypeCode>`, firma digital ds:Signature, namespaces idénticos.
> Esto significa que **cada boleta individual SÍ necesita su propio XML UBL 2.1** (tipo "03").

---

### Respuestas del usuario (todas cerradas)

#### REQ-A — Boleta + Resumen Diario

| # | Pregunta | ✅ Respuesta |
|---|----------|-------------|
| A.1 | ¿Boleta necesita XML UBL 2.1 propio? | **SÍ** — Confirmado por `B002-2058.xml`: mismo `<Invoice>` con `InvoiceTypeCode=03`, firma digital, estructura idéntica a factura. Cada boleta genera su XML individual. |
| A.2 | ¿SOAP real o mock? | **MOCK** por ahora. Implementar estructura alineada con lo existente (`SunatSoapClient` interface + `MockSunatSoapClient` impl). Documentar dónde cambiar cuando se implemente el servicio real. |
| A.3 | ¿Anuladas en resumen diario? | **NO**. Boletas anuladas ya fueron emitidas/recibidas, no vuelven al resumen. Filtro activo: solo boletas BORRADOR (o VALIDADO) del día para el resumen. |
| A.4 | ¿Múltiples resúmenes por día? | **SÍ** — RC-{YYYYMMDD}-001, 002... Normalmente uno al día (posible automatización futura), pero manual permite varios si hay boletas pendientes. |
| A.5 | ¿Corregir bug emitirBoleta()? | **SÍ** — Boleta NO se emite, solo se guarda. Además: implementar **validación interna al guardar**. Si hay errores → estado `CON_ERROR` (similar a ANULADA: muestra opción de corregir). Si pasa validación → estado `VALIDADO`. |
| A.6 | ¿Wizard boleta botón "Guardar"? | **SÍ** — Botón principal "💾 Guardar Boleta". No hay "Emitir" directo para boletas. |

#### REQ-B — Logo Empresa

| # | Pregunta | ✅ Respuesta |
|---|----------|-------------|
| B.1 | ¿Formatos? | **PNG preferido**. Validar tamaño, redimensionar si es excesivo. |
| B.2 | ¿Tamaño máximo? | **10 MB** (generoso por si acaso). |
| B.3 | ¿Dónde va el upload? | **NUEVO panel dedicado "Configuración Empresa"** — UI/UX premium nivel "bolsa de valores". No es un simple campo en el overlay existente. Es un panel con identidad visual que transmita: *"Las personas comercian con nosotros, esto es tener una empresa"*. |
| B.4 | ¿Notificación persistente? | **SÍ** — Se mantiene activa hasta que el logo se configure. |

#### REQ-C — PDF Preview

| # | Pregunta | ✅ Respuesta |
|---|----------|-------------|
| C.1 | ¿Formato de referencia? | **NUEVO REQ-F: "FORMATO DOCUMENTOS"** — Opción en el menú principal. 3 formatos base de distribución de espacio. El usuario puede elegir formato. Fase inicial: marcar como "🚧 En Desarrollo" en el menú, pero **levantar estructura** para el feature. Aislar la lógica de templates para que sea extensible. |
| C.2 | ¿PDF wizard = PDF preview? | Misma plantilla unificada (`PdfTemplate`), usa el formato seleccionado en config. |

#### REQ-D — Settings

| # | Pregunta | ✅ Respuesta |
|---|----------|-------------|
| D.1 | ¿Botón "Probar Conexión"? | **SÍ** — Necesario en este tipo de sistemas. Valida usuario/clave SOL contra web service beta de SUNAT. |
| D.2 | ¿Campos editables? | **SÍ** — emailContacto y telefono editables en panel Empresa. |

#### Generales

| # | Pregunta | ✅ Respuesta |
|---|----------|-------------|
| G.1 | ¿REQ-1 a REQ-8 en esta fase? | **SÍ** — Si se puede, se implementan. |
| G.2 | ¿Prioridad? | Decidir orden óptimo según dependencias (ver orden actualizado abajo). |

---

### Resumen Ejecutivo (actualizado con respuestas)

| Bloque | Título | Descripción | Esfuerzo |
|--------|--------|-------------|----------|
| **REQ-A** | Rediseño Flujo Boleta + Resumen Diario | Boleta→BORRADOR→VALIDADO/CON_ERROR, XML UBL 2.1 tipo "03", ResumenDiario, mock SOAP, validación interna (homologada a facturas) | 🔴 Alto (10–12 días) |
| **REQ-B** | Panel Empresa Premium + Logo | Nuevo panel "Configuración Empresa" con UX premium, upload logo PNG, storage, notificación persistente si falta | 🟡 Medio (3–4 días) |
| **REQ-C** | PDF Preview con Logo en Wizard Step 5 | Descargar PDF desde wizard sin persistir, con logo, plantilla unificada | 🟡 Medio (2–3 días) |
| **REQ-D** | Auditoría Settings + Credenciales SUNAT | Mapear campos faltantes, nuevo tab credenciales, botón "Probar Conexión" | 🟡 Medio (2–3 días) |
| **REQ-E** | Implementar REQ-1 a REQ-8 (UX fixes) | Padding backend, modales, búsqueda, descuentos, cuotas, seed data | 🟡 Medio (3–4 días) |
| **REQ-F** | Formato Documentos (estructura) | Menú "Formato Documentos" con 3 templates base, marcar "En Desarrollo", aislar lógica de templates | 🟢 Bajo (1–2 días) |
| **REQ-G** | Validación Interna de Documentos | Servicio `DocumentValidator` que valida XML/datos antes de emitir, estados VALIDADO/CON_ERROR, homologado facturas+boletas | 🟡 Medio (2–3 días) |

---

### Orden de ejecución propuesto

```
           ┌──────────────────────────────────────────────────────────────┐
           │                                                              │
    REQ-G ──► Validación Interna (independiente, base para A y E)        │
           │     └── Nuevo estado VALIDADO/CON_ERROR                     │
           │         Homologado para Facturas y Boletas                  │
           │                                                              │
    REQ-D ──► Auditoría Settings + Credenciales SUNAT                    │
           │     └── Desbloquea "Probar Conexión" (mock)                 │
           │                                                              │
    REQ-E ──► UX Fixes REQ-1–8              (3–4 días)                 │
           │                                                              │
    REQ-B ──► Panel Empresa Premium + Logo   (3–4 días)                │
           │     └── Desbloquea REQ-C (PDF con logo)                     │
           │                                                              │
    REQ-C ──► PDF Preview con Logo           (2–3 días)                │
           │     └── Fusiona REQ-9                                       │
           │                                                              │
    REQ-F ──► Formato Documentos (estructura) (1–2 días)               │
           │     └── Menú + página + 1 template funcional + extensible   │
           │                                                              │
    REQ-A ──► Boleta + Resumen Diario (el más grande)                    │
           │     ├── Depende de REQ-G (validación)                       │
           │     ├── Usa mock de SunatSoapClient (REQ-A.3.6)            │
           │     └── Se beneficia de REQ-D (credenciales)                │
           └──────────────────────────────────────────────────────────────┘
```

### Esfuerzo total estimado (actualizado)

| Bloque | Días | Complejidad | Dependencias |
|--------|------|-------------|--------------|
| REQ-G | 2–3 | 🟡 Media | Ninguna (base para todo) |
| REQ-D | 2–3 | 🟡 Media | Ninguna |
| REQ-E | 3–4 | 🟡 Media | Ninguna |
| REQ-B | 3–4 | 🟡 Media | Ninguna |
| REQ-F | 1–2 | 🟢 Baja | Ninguna |
| REQ-C | 2–3 | 🟡 Media | REQ-B + REQ-F |
| REQ-A | 8–12 | 🔴 Alta | REQ-G (validación) |
| **TOTAL** | **21–31 días** | | |

---

## PARTE 24 — REQ-B: Panel Empresa Premium + Logo — COMPLETADO ✅ (2026-04-08)

### Cambios Implementados

#### B1 — CompanyEntity: nuevo campo `logoPath` ✅
- `@Column(name = "logo_path") private String logoPath;`
- H2 DDL auto-update crea la columna

#### B2 — OpenAPI YAML: 2 nuevos endpoints + schema updates ✅
| Endpoint | operationId | Descripción |
|---|---|---|
| `POST /company/logo?companyId=` | `subirLogo` | Upload multipart PNG/JPG/JPEG, max 10MB |
| `GET /company/logo?companyId=` | `obtenerLogo` | Serve logo como image/png o image/jpeg, cache 1h |

CompanyData schema: nuevos campos `logoPath` (string) y `tieneLogo` (boolean).

#### B3 — LogoStorageService ✅
- `company/service/LogoStorageService.java`
- Almacena en `etia_storage/company_logos/{ruc}/logo.{ext}`
- Validaciones: extensión (.png/.jpg/.jpeg), tamaño (max 10MB), content-type
- Métodos: `guardarLogo()`, `leerLogo()`, `getContentType()`, `eliminarLogo()`

#### B4 — CompanyMapper ✅
- `toEntity()`: `@Mapping(target = "logoPath", ignore = true)`
- `updateEntity()`: `@Mapping(target = "logoPath", ignore = true)` (se actualiza via `subirLogo`)
- `toData()`: `@Mapping(target = "tieneLogo", expression = "...")`

#### B5 — CompanyService + CompanyServiceImpl ✅
- `subirLogo(companyId, archivo)` — guarda en disco, actualiza `logoPath` en entity
- `obtenerLogo(companyId)` → `Optional<byte[]>`
- `getLogoContentType(companyId)` → `String`

#### B6 — CompanyOpenApi ✅
- `@Override subirLogo()` → delega a service
- `@Override obtenerLogo()` → serve como `ByteArrayResource` con `MediaType` correcto, cache headers

#### B7 — Frontend: api-client.js ✅
- `uploadLogo(companyId, file)` — POST multipart
- `getLogoUrl(companyId)` — retorna URL para `<img src>`

#### B8 — Frontend: config-overlay.html ✅
- **Rediseño completo del panel Empresa** en config overlay: layout premium tipo dashboard con: logo grande (drag & drop / click upload), card de empresa con stats, datos editables (email, teléfono), datos readonly (RUC, razón social), indicadores de estado (logo ✅/❌, SUNAT conectada ✅/❌, certificado ✅/❌) |
- Logo preview sustituye `🏢` icon en company card
- Click para subir nuevo logo (file input hidden)
- Overlay "📷 Logo" en la parte inferior
- Zona de resultado (éxito/error) temporal

#### B9 — Frontend: config-overlay.js ✅
- `handleLogoFileSelect(input)` — validación client-side + upload inmediato
- `loadConfigValues()` — carga logo existente via `getLogoUrl()` con cache-bust

### Tests ✅
- `mvn clean compile` → **BUILD SUCCESS**
- `mvn test` → **217 tests, 0 Failures, 0 Errors**

---

## PARTE 23 — REQ-E: UX Fixes REQ-1–8 — COMPLETADO ✅ (2026-04-08)

### Verificación — Todos los REQ ya estaban implementados

| REQ | Título | Estado | Evidencia |
|-----|--------|--------|-----------|
| REQ-1 | Padding número backend | ✅ Ya implementado | `SunatFormatter.formatNumeroComprobante()` usado en XML builder, filename, notificaciones, frontend |
| REQ-2 | Cabeceras de modales | ✅ Ya implementado | Todos los modales tienen `modal-header-std` con título + botón ✕, sin "Cerrar" redundante |
| REQ-3 | Scroll + botón cerrar | ✅ Ya implementado | `modal-body-scroll` en todos los modales, ✕ solo en header |
| REQ-4 | Filtro/búsqueda persistente | ✅ Ya implementado | `facturas.html`: chips, sessionStorage, highlight, auto-scroll, búsqueda por serie+estado+cliente |
| REQ-5 | Calibrar descuento global | ✅ Ya implementado | `items.js`: base auto-calculada, factor como %, validación excede subtotal, colores correctos |
| REQ-6 | Auto-generación cuotas | ✅ Ya implementado | `items.js`: `autoGenerarCuotas()` con FIJA_30D/PROPORCIONAL, config panel con defaultCuotas |
| REQ-7 | Cuotas en Step 5 review | ✅ Ya implementado | `wizard.js`: tabla condicional `r-cuotas-info` con total + fechas |
| REQ-8 | Seed data completo | ✅ Ya implementado | `fac-demo-025`: BORRADOR, CRÉDITO, 4 ítems, cargo 10% + descuento 5%, 3 cuotas |

---

## PARTE 22 — REQ-D: Settings + Credenciales SUNAT — COMPLETADO ✅ (2026-04-08)

### Alcance
Implementación completa del flujo de credenciales SUNAT: upload de certificado digital (.p12/.pfx), 
prueba de conexión con SUNAT (mock backend), y auditoría de campos en Settings UI.

### Cambios Implementados

#### D1 — OpenAPI YAML: 2 nuevos endpoints ✅
| Endpoint | operationId | Descripción |
|---|---|---|
| `POST /company/certificado?companyId=` | `subirCertificado` | Upload multipart de .p12/.pfx, max 10MB, valida extensión |
| `POST /company/probar-conexion?companyId=` | `probarConexionSunat` | Prueba de conexión con SUNAT (mock), retorna `ConexionSunatResultDto` |

Nuevo schema: `ConexionSunatResultDto` con: exitoso, mensaje, modo, tieneUsuarioSol, tieneClaveSol, tieneCertificado, timestamp.

#### D2 — CertificadoStorageService ✅
- Nuevo: `company/service/CertificadoStorageService.java`
- Almacena en `etia_storage/company_certs/{ruc}/certificado.p12`
- Validaciones: extensión (.p12/.pfx), tamaño (max 10MB), archivo no vacío
- Métodos: `guardarCertificado()`, `existe()`, `eliminarCertificado()`

#### D3 — CompanyService + CompanyServiceImpl ✅
- `subirCertificado(companyId, archivo)` — guarda archivo en disco, actualiza `certificadoPath` en entity
- `probarConexionSunat(companyId)` — valida credenciales SOL configuradas, retorna resultado mock
  - Si no hay usuario/clave SOL → `exitoso=false`
  - Si credenciales configuradas → `exitoso=true` con mensaje mock según modo (beta/producción)
  - Advierte si falta certificado digital
  - `// TODO: FASE-3 — Reemplazar mock con llamada real a SUNAT`

#### D4 — CompanyOpenApi ✅
- `@Override subirCertificado()` → delega a service
- `@Override probarConexionSunat()` → delega a service

#### D5 — Frontend: api-client.js ✅
- `uploadCertificado(companyId, file)` — POST multipart/form-data
- `probarConexionSunat(companyId)` — POST JSON, retorna resultado

#### D6 — Frontend: config-overlay.html ✅
- **Upload zone** con drag & drop: zona punteada, click o arrastrar .p12/.pfx
- Indicador de archivo seleccionado con nombre y tamaño
- Botón "📤 Subir Certificado" con barra de progreso
- Zona de resultado (éxito/error)
- Eliminado el banner "próxima actualización"

#### D7 — Frontend: config-overlay.js ✅
- `handleCertFileSelect(input)` / `handleCertFileDrop(event)` — handlers de selección
- `_setCertFile(file)` — validación client-side (extensión, tamaño 10MB), actualiza UI
- `uploadCertificado()` — sube via API, actualiza indicadores, muestra resultado
- `testSunatConnection()` — **refactorizado**: ahora llama `ApiClient.probarConexionSunat()` 
  en vez del setTimeout mock anterior. Muestra resultado del backend con detalle de modo/credenciales.

### Auditoría de campos Settings (verificado)

| Campo | En Entity | En OpenAPI | En Settings UI | En Mapper | Estado |
|---|---|---|---|---|---|
| emailContacto | ✅ | ✅ UpdateDto | ✅ editable + guardar | ✅ mapeado | ✅ OK |
| telefono | ✅ | ✅ UpdateDto | ✅ editable + guardar | ✅ mapeado | ✅ OK |
| usuarioSol | ✅ | ✅ UpdateDto | ✅ input + guardar | ✅ mapeado | ✅ OK |
| claveSol | ✅ | ✅ UpdateDto | ✅ password + guardar | ✅ mapeado | ✅ OK |
| certificadoPath | ✅ | ✅ (readonly en Data) | ✅ upload + indicador | ✅ ignored en update (se actualiza via subirCertificado) | ✅ OK |
| certificadoPassword | ✅ | ✅ UpdateDto | ✅ password + guardar | ✅ mapeado | ✅ OK |
| emisionEnEnvio | ✅ | ✅ UpdateDto | ✅ checkbox + guardar | ✅ mapeado | ✅ OK |

### Tests ✅
- `mvn clean compile` → **BUILD SUCCESS**
- `mvn test` → **217 tests, 0 Failures, 0 Errors**

---

## PARTE 25 — REQ-F: Formato Documentos — COMPLETADO ✅ (2026-04-08)

### Alcance
Implementación de la estructura para selección de formato de documentos PDF:
- 1 formato funcional (`standard`)
- 2 formatos marcados como "Próximamente" (`compact`, `detailed`)
- Motor de templates extensible (`PdfTemplate`) para REQ-C
- Menú, página, selector en config, preferencia guardada

### Cambios Implementados

#### F1 — Spring Controller ✅
- Nuevo: `FormatoDocumentosViewController.java`
- Ruta: `GET /formato-documentos` → template `formato-documentos`

#### F2 — Template `formato-documentos.html` ✅
- 3 cards con radio buttons para selección de formato
- **Estándar** (📄): Activo y seleccionable. Mini-preview visual con layout clásico peruano
- **Compacto** (📋): Deshabilitado con badge "🚧 Próximamente"
- **Detallado** (📑): Deshabilitado con badge "🚧 Próximamente"
- Banner "🚧 En Desarrollo" informativo
- Indicador de formato activo
- Sección informativa sobre cómo funciona
- CSS inline completo con estilos del design system (fd-* classes)
- Persistencia en `ETIA.config.documentFormat` via `localStorage`

#### F3 — `pdf-template.js` — Motor de Templates Extensible ✅
- Nuevo: `static/js/pdf-template.js`
- **`PdfTemplate.register(id, renderFn)`**: Registra un template por ID
- **`PdfTemplate.render(opts)`**: Genera HTML usando el template activo (lee `ETIA.config.documentFormat`)
- **`PdfTemplate.print(opts)`**: Abre ventana de impresión con el HTML generado
- **`PdfTemplate.helpers`**: Expone `money()`, `formatDate()`, `formatNumero()`, `escapeHtml()` para templates custom
- **Template `standard` implementado**: Formato clásico peruano con:
  - Header: Logo + datos empresa + recuadro tipo documento con RUC y número
  - Client box: Adquiriente con tipo/número doc, dirección, fecha, moneda, forma pago
  - Tabla de ítems: #, Descripción, U.M., Cant., V.Unit., IGV, Subtotal
  - Cargos/Descuentos globales (Cat. 53)
  - Cuotas de crédito
  - Totales: Gravadas, Exoneradas, Inafectas, IGV, Cargos, Descuentos, TOTAL
  - Leyenda (SON: ...)
  - Footer con sello ETIA
  - CSS para impresión (`@media print`)
- **Extensible**: Agregar nuevos formatos con `PdfTemplate.register('compact', fn)` sin tocar código existente

#### F4 — Menú Principal ✅
- Nuevo card "📐 Formato Documentos" en sección Herramientas de `menu.html`
- Badge "🚧 En Desarrollo" con clase `mc-chip-dev`
- Link funcional a `/formato-documentos`

#### F5 — CSS ✅
- Nueva clase `.mc-chip-dev` para badge "En Desarrollo" (color warning, pulsing dot)
- Estilos `fd-*` para la página formato-documentos (inline en template)

#### F6 — Config Overlay — Selector de Formato ✅
- Nuevo selector "📐 Formato de Documentos" en panel Preferencias
- Dropdown con 3 opciones (compact y detailed deshabilitadas)
- Link directo a `/formato-documentos` para ver formatos
- Load/save via `ETIA.saveConfig('documentFormat', value)`

#### F7 — App State ✅
- `ETIA.config.documentFormat` con default `'standard'` (localStorage)

#### F8 — Seed Data ✅
- `pref-008`: `documentFormat` → `standard`

### Archivos Creados
| Archivo | Descripción |
|---|---|
| `view/FormatoDocumentosViewController.java` | Spring Controller |
| `templates/formato-documentos.html` | Página con 3 formatos y mini-previews |
| `static/js/pdf-template.js` | Motor de templates extensible |

### Archivos Modificados
| Archivo | Cambio |
|---|---|
| `templates/menu.html` | +Card "📐 Formato Documentos" con badge dev |
| `static/css/main.css` | +`.mc-chip-dev` clase CSS |
| `static/js/app-state.js` | +`documentFormat` en `ETIA.config` |
| `fragments/config-overlay.html` | +Selector formato en Preferencias |
| `static/js/config-overlay.js` | +Load/save `documentFormat` |
| `data.sql` | +`pref-008` documentFormat seed |

### Tests ✅
- `mvn clean compile` → **BUILD SUCCESS**
- `mvn test` → **215 tests, 0 Failures, 0 Errors**

---

## PARTE 26 — REQ-C: PDF Preview con Logo — COMPLETADO ✅ (2026-04-08)

### Alcance
Implementación del preview PDF en vivo en el Wizard Step 5 usando `PdfTemplate`, refactorización de
`exportPdf()` en doc-preview para eliminar HTML duplicado, y carga del logo de empresa como base64
para embebido directo en los PDFs generados.

### Cambios Implementados

#### C1 — Helper compartido `ETIA.getLogoBase64()` ✅
- Nuevo en `app-state.js`: Fetch del logo vía `ApiClient.getLogoUrl()` → `blob` → `FileReader.readAsDataURL()`
- Cache en memoria: `ETIA._logoBase64Cache` evita re-fetch en cada toggle de tab/export
- `ETIA.invalidateLogoCache()` para resetear cuando se sube un logo nuevo
- Retorna `Promise<string>` — data URL o `''` si no hay logo

#### C2 — Wizard Step 5: Vista Previa PDF en vivo ✅
- **`_loadPdfPreview()`** completamente reescrito en `wizard.js` (~100 líneas)
- Recopila datos del formulario (doc, company, client, items, totals) directamente del DOM
- Invoca `PdfTemplate.render(opts)` con el template configurado (`ETIA.config.documentFormat`)
- Renderiza el HTML en un `<iframe id="pdf-preview-frame">` con `srcdoc`
- Incluye logo de empresa como base64 (cargado asíncronamente con cache)
- Spinner de carga mientras se obtiene el logo
- **Botón "🖨️ Imprimir"** sobre el iframe para imprimir directamente
- **`printPdfPreview()`** — invoca `iframe.contentWindow.print()`

#### C3 — `factura.html` Step 5: UI actualizada ✅
- `#review-pdf` simplificado: div vacío que se llena dinámicamente por `_loadPdfPreview()`
- `<script th:src="@{/js/pdf-template.js}">` agregado antes de `wizard.js`

#### C4 — Refactorización `exportPdf()` en `doc-preview.js` ✅
- **Antes**: ~80 líneas de HTML inline con estilos, tabla de items, secciones de cargos/descuentos, cuotas, totales — todo duplicado del template standard
- **Después**: ~30 líneas que construyen `opts` y llaman a `PdfTemplate.print(opts)`
- Incluye logo base64 (cargado con `ETIA.getLogoBase64()`)
- Resuelve cliente desde `ETIA.findClientById(doc.clientId)`
- **Eliminados** los helpers ahora obsoletos: `_pdfCargosDescuentosHtml()`, `_pdfCuotasHtml()`

#### C5 — `pdf-template.js` incluido en 5 páginas ✅
| Página | Motivo |
|---|---|
| `wizard/factura.html` | Vista previa en Step 5 |
| `facturas.html` | Export PDF desde doc-preview |
| `boletas.html` | Export PDF desde doc-preview |
| `notas-credito.html` | Export PDF desde doc-preview |
| `notas-debito.html` | Export PDF desde doc-preview |

#### C6 — Invalidación de cache al subir logo nuevo ✅
- `config-overlay.js → handleLogoFileSelect()`: llama a `ETIA.invalidateLogoCache()` después del upload exitoso
- Garantiza que el próximo PDF generado use el logo actualizado

### Archivos Modificados (8)
| Archivo | Cambio |
|---|---|
| `static/js/app-state.js` | +`ETIA.getLogoBase64()`, `ETIA.invalidateLogoCache()`, cache helpers |
| `static/js/wizard.js` | Reescrito `_loadPdfPreview()` + nuevo `printPdfPreview()` |
| `static/js/doc-preview.js` | Refactorizado `exportPdf()` → usa `PdfTemplate.print()`, eliminados helpers obsoletos |
| `static/js/config-overlay.js` | +`ETIA.invalidateLogoCache()` después de upload logo |
| `templates/wizard/factura.html` | +`pdf-template.js` script, simplificado `#review-pdf` |
| `templates/facturas.html` | +`pdf-template.js` script |
| `templates/boletas.html` | +`pdf-template.js` script |
| `templates/notas-credito.html` | +`pdf-template.js` script |
| `templates/notas-debito.html` | +`pdf-template.js` script |

### Tests ✅
- `mvn clean compile` → **BUILD SUCCESS**
- `mvn test` → **217 tests, 0 Failures, 0 Errors**

### Flujo Completo

```
┌─── Wizard Step 5 ───────────────────────────────────────────────┐
│                                                                   │
│   [📊 Contable]  [📄 Vista Previa]  [{ } JSON]                  │
│                       ↓                                           │
│   _loadPdfPreview()                                               │
│      ├─ Recopila doc/company/client/items/totals del DOM         │
│      ├─ ETIA.getLogoBase64() → data:image/png;base64,...         │
│      ├─ PdfTemplate.render(opts) → HTML completo                 │
│      └─ iframe.srcdoc = html                                     │
│                                                                   │
│   [🖨️ Imprimir] → iframe.contentWindow.print()                  │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘

┌─── Doc Preview Modal (facturas/boletas/notas) ──────────────────┐
│                                                                   │
│   [📥 PDF] → exportPdf()                                         │
│      ├─ ETIA.getLogoBase64() → data:image/png;base64,...         │
│      ├─ Construye opts con _currentDoc + ETIA.company + client   │
│      └─ PdfTemplate.print(opts) → window.open + print()         │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

### Diseño clave
- **Template unificado**: Wizard y Doc-Preview usan EXACTAMENTE el mismo template `PdfTemplate` — un solo punto de mantenimiento
- **Logo embebido como base64**: No depende de URLs externas, funciona offline y en impresión
- **Cache inteligente**: El logo se descarga una sola vez por sesión, se invalida al subir nuevo
- **Extensible**: Cambiar el formato (standard/compact/detailed) en `ETIA.config.documentFormat` afecta automáticamente ambos puntos de generación PDF

### Estado actualizado del proyecto
```
 REQ-G  Validación Interna      ████████████████████████  100% ✅
 REQ-D  Settings + Credenciales ████████████████████████  100% ✅
 REQ-E  UX Fixes REQ-1–8       ████████████████████████  100% ✅
 REQ-B  Panel Empresa + Logo    ████████████████████████  100% ✅
 REQ-F  Formato Documentos      ████████████████████████  100% ✅
 REQ-C  PDF Preview con Logo    ████████████████████████  100% ✅
 REQ-A  Boleta + Resumen Diario ████████████████████████  100% ✅
```

### Fase 2.5 — COMPLETADA ✅
Todos los REQ (A–G) han sido implementados.

---

## PARTE 27 — REQ-A: Boleta + Resumen Diario — COMPLETADO ✅ (2026-04-08)

### Alcance
Implementación completa del flujo de boletas (BORRADOR → VALIDADO/CON_ERROR → Resumen Diario → ENVIADA → ACEPTADA)
y la estructura de Resumen Diario con envío mock a SUNAT.

### Backend (Pasos 1–8) — COMPLETADO ✅

#### A1 — OpenAPI YAML: nuevos endpoints ✅
| Endpoint | operationId | Descripción |
|---|---|---|
| `PUT /boleta/{boletaId}/actualizar` | `actualizarBoletaBorrador` | Actualiza boleta en BORRADOR/CON_ERROR |
| `GET /boleta/{boletaId}/xml` | `descargarBoletaXml` | Descarga XML UBL 2.1 tipo "03" |
| `POST /resumen-diario` | `crearResumenDiario` | Crea resumen diario RC-{YYYYMMDD}-NNN |
| `GET /resumen-diario` | `listarResumenesDiarios` | Lista resúmenes por empresa |
| `GET /resumen-diario/{resumenId}` | `obtenerResumenDiario` | Obtiene detalle de resumen |
| `POST /resumen-diario/{resumenId}/consultar-estado` | `consultarEstadoResumen` | Consulta ticket en SUNAT |

#### A2 — BoletaEntity: nuevos campos ✅
- `xmlContent` (TEXT), `xmlPath`, `erroresValidacion` (TEXT), `resumenDiarioId`

#### A3 — BoletaRepository: nuevas queries ✅
- `findByCompanyIdAndEstado`, `findByCompanyIdAndEstadoAndFechaEmision`, `findByResumenDiarioId`

#### A4 — UblInvoiceXmlBuilder: refactorizado con XmlBuildContext ✅
- `buildBoletaXml()` genera XML tipo "03" usando el mismo motor que factura ("01")
- `XmlBuildContext` record abstrae campos comunes

#### A5 — DocBoletaService reescrito ✅
- `emitirBoleta()` → guarda como BORRADOR → valida → VALIDADO/CON_ERROR → genera XML si válido
- `actualizarBoletaBorrador()` → actualiza campos + re-valida
- `descargarBoletaXml()` → descarga inmutable (disco → BD → 204)
- `validarBoleta()` → re-ejecuta validación interna

#### A6 — SunatSoapClient + MockSunatSoapClient ✅
- Interface: `sendSummary(identificador, ruc)` y `getStatus(ticket, ruc)`
- Mock: siempre exitoso, ticket con timestamp, ACEPTADO en consulta

#### A7 — Resumen Diario completo ✅
- `ResumenDiarioEntity` + `ResumenDiarioRepository`
- `ResumenDiarioServiceImpl`: crear (agrupa VALIDADAS), consultar estado (actualiza a ACEPTADO/RECHAZADO)
- `ResumenDiarioOpenApi`: controller OpenAPI-first

### Frontend (Pasos 9–13) — COMPLETADO ✅

#### A8 — api-client.js ✅
+7 métodos: `actualizarBoletaBorrador`, `eliminarBoleta`, `descargarBoletaXml`, `crearResumenDiario`, `listarResumenesDiarios`, `obtenerResumenDiario`, `consultarEstadoResumen`

#### A9 — boletas.html: acciones completas ✅
- BORRADOR: Editar + Validar + Eliminar
- VALIDADO: Editar + "📨 Lista para Resumen Diario" + Eliminar + XML + PDF
- CON_ERROR: Corregir + Re-validar + Eliminar + banner de errores
- ENVIADA: "📨 Incluida en Resumen Diario" (readonly) + XML + PDF
- ACEPTADA/RECHAZADA: readonly + XML + PDF
- Funciones: `quickDelete`, `quickXml`, `quickPdf`

#### A10 — resumenes-diarios.html: reescrito con APIs reales ✅
- Lista resúmenes desde `ApiClient.listarResumenesDiarios()`
- "Generar Resumen del Día" → `ApiClient.crearResumenDiario()`
- "Consultar Estado" → `ApiClient.consultarEstadoResumen()` (solo estado ENVIADO)
- Detalle: copia JSON al portapapeles
- Badges por estado: GENERADO(gray), ENVIADO(blue), ACEPTADO(green), RECHAZADO(red)

#### A11 — wizard.js: adaptado para BOLETA ✅
- BOLETA modo creación: botón primario "💾 Guardar Boleta", sin "Emitir" directo
- BOLETA modo edición: "💾 Guardar Cambios" → `ApiClient.actualizarBoletaBorrador()`
- Hint: "Las boletas se envían a SUNAT mediante Resúmenes Diarios"
- `resetAll()` redirige a `/boletas` cuando `selectedDocType === 'BOLETA'`

#### A12 — data.sql: seed data ampliada ✅
- 8 boletas: 2×ACEPTADA, 3×VALIDADO, 1×CON_ERROR, 1×BORRADOR, 1×ENVIADA
- 2 resúmenes diarios: 1×ACEPTADO (RC-20260405-001), 1×ENVIADO (RC-20260406-001)
- Correlativo B001 actualizado a 8
- Errores de validación para bol-demo-005

### Tests ✅
- `mvn clean compile` → **BUILD SUCCESS**
- `mvn test` → **217 tests, 0 Failures, 0 Errors**

### Estado actualizado del proyecto
```
 REQ-G  Validación Interna      ████████████████████████  100% ✅
 REQ-D  Settings + Credenciales ████████████████████████  100% ✅
 REQ-E  UX Fixes REQ-1–8       ████████████████████████  100% ✅
 REQ-B  Panel Empresa + Logo    ████████████████████████  100% ✅
 REQ-F  Formato Documentos      ████████████████████████  100% ✅
 REQ-C  PDF Preview con Logo    ████████████████████████  100% ✅
 REQ-A  Boleta + Resumen Diario ████████████████████████  100% ✅
```

### Pendientes para FASE 3:
| # | Componente | Descripción | Esfuerzo | Estado |
|---|---|---|---|---|
| 1 | **`DigitalSignatureService`** | Firma digital X.509 enveloped-signature. Cargar .pfx/.p12, firmar con RSA-SHA | 🔴 Alto | ❌ Pendiente |
| 2 | **`RealSunatSoapClient`** | Reemplazar mock con llamada SOAP real a beta SUNAT | 🟡 Medio | ❌ Pendiente |
| 3 | **Vinculación Sede → XML** | AddressTypeCode del establecimiento emisor | 🟢 Bajo | ❌ Pendiente |
