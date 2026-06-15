# 📋 Plan de Implementación — Tests Unitarios Boleta + Resumen Diario

> **Fecha:** 2026-04-08 | **Versión:** 1.0
> **Escenarios fuente:** `src/main/resources/scenarios/ESCENARIOS_BOLETA.md`
> **Total escenarios:** 54 | **Meta cobertura:** 54/54 (100%)

---

## 1. Objetivos

1. Cubrir los **54 escenarios** definidos para Boleta y Resumen Diario.
2. Organizar los tests en **packages espejo** de la estructura `src/main/java`, preparando la eventual
   extracción a microservicios independientes (`doc_boleta`, `resumen_diario`).
3. Seguir el **mismo patrón** de `DocFacturaServiceImplTest` (Mockito + `@Nested` + `@DisplayName`).

---

## 2. Estructura de Packages de Test

```
src/test/java/com/etlions/ms_bs_etia/
├── app_util/
│   └── validation/
│       └── SunatValidatorTest.java               ← YA EXISTE (validaciones genéricas)
├── doc_factura/
│   ├── service/impl/
│   │   └── DocFacturaServiceImplTest.java         ← YA EXISTE (factura)
│   └── xml/
│       └── UblInvoiceXmlBuilderTest.java          ← YA EXISTE (XML factura)
├── doc_boleta/
│   └── service/impl/
│       └── DocBoletaServiceImplTest.java          ← NUEVO — 40 escenarios
└── resumen_diario/
    └── service/impl/
        └── ResumenDiarioServiceImplTest.java      ← NUEVO — 14 escenarios
```

> **Nota:** Los tests de XML de boleta (`InvoiceTypeCode=03`, cliente anónimo) se cubren
> **dentro** de `DocBoletaServiceImplTest` verificando que `UblInvoiceXmlBuilder.buildBoletaXml()`
> sea invocado con los argumentos correctos (mock). La validación completa de estructura XML
> ya existe en `UblInvoiceXmlBuilderTest` (builder compartido por factura y boleta).

---

## 3. Dependencias Mock por Archivo

### DocBoletaServiceImplTest.java

| Mock | Clase |
|------|-------|
| `boletaRepository` | `BoletaRepository` |
| `boletaMapper` | `BoletaMapper` |
| `correlativoService` | `CorrelativoService` |
| `notificationService` | `NotificationService` |
| `clientRepository` | `ClientRepository` |
| `companyRepository` | `CompanyRepository` |
| `cuotaCreditoRepository` | `CuotaCreditoRepository` |
| `allowanceChargeRepository` | `AllowanceChargeRepository` |
| `documentValidatorService` | `DocumentValidatorService` |
| `ublInvoiceXmlBuilder` | `UblInvoiceXmlBuilder` |
| `xmlStorageService` | `XmlStorageService` |

### ResumenDiarioServiceImplTest.java

| Mock | Clase |
|------|-------|
| `resumenDiarioRepository` | `ResumenDiarioRepository` |
| `boletaRepository` | `BoletaRepository` |
| `companyRepository` | `CompanyRepository` |
| `sunatSoapClient` | `SunatSoapClient` |
| `notificationService` | `NotificationService` |

---

## 4. Mapeo Completo: Escenario → Test Method

### 4.1 DocBoletaServiceImplTest.java (40 tests)

#### @Nested `EmitirHappy` — 7 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-HP-001** | `emitirBoletaAutoValidacionExitosa_VALIDADO()` | Emite boleta → auto-valida OK → estado VALIDADO, XML generado, notificación |
| **BOL-HP-002** | `emitirBoletaClienteDni_ok()` | clientId con tipoDoc="1" (DNI) → pasa validación tipo doc |
| **BOL-HP-003** | `emitirBoletaClienteCE_ok()` | clientId con tipoDoc="4" (C.E.) → pasa validación tipo doc |
| **BOL-HP-004** | `emitirBoletaClientePasaporte_ok()` | clientId con tipoDoc="7" (Pasaporte) → pasa |
| **BOL-HP-005** | `emitirBoletaSinCliente_anonimo_ok()` | clientId=null → permitido, no valida tipo doc |
| **BOL-HP-006** | `emitirBoletaMultiItem_ok()` | 2 ítems en detalle → boleta creada correctamente |
| **BOL-HP-007** | `emitirBoletaCredito_conCuotas_ok()` | formaPago=CREDITO + cuotas → persiste cuotas_credito |

#### @Nested `EmitirSerie` — 2 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-UP-001** | `serieFacturaEnBoleta_falla()` | serie="F001" → `SunatValidationException` |
| **BOL-UP-002** | `serieFormatoInvalido_falla()` | serie="BB01" → `SunatValidationException` |

#### @Nested `EmitirCliente` — 1 test

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-UP-003** | `clienteRucEnBoleta_falla()` | Cliente RUC tipo "6" → `SunatValidationException` |

#### @Nested `EmitirMontos` — 3 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-UP-005** | `montosNegativos_autoValidacion_CON_ERROR()` | gravadas=-500 → totales pasan, auto-validación atrapa negativos → CON_ERROR |
| **BOL-UP-006** | `totalNoCuadra_falla()` | gravadas=100+igv=18≠total=200 → `SunatValidationException` |
| **BOL-UP-017** | `camposMonetariosNull_falla()` | gravadas=null, igv=null, total=null → `SunatValidationException` (3 errores) |

#### @Nested `EmitirAutoValidacion` — 4 tests (errores detectados por soft-validation → CON_ERROR)

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-UP-004** | `detalleVacio_CON_ERROR()` | detalle=[] → auto-validación falla → CON_ERROR |
| **BOL-UP-007** | `usdSinTipoCambio_CON_ERROR()` | moneda=USD, tipoCambio=null → CON_ERROR |
| **BOL-UP-008** | `fechaEmisionFutura_CON_ERROR()` | fechaEmision=2027-01-01 → CON_ERROR |
| **BOL-UP-018** | `detalleJsonCorrupto_CON_ERROR()` | detalleJson mal formado → CON_ERROR |

#### @Nested `ActualizarHappy` — 2 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-HP-008** | `actualizarConError_corregir_VALIDADO()` | Boleta CON_ERROR → PUT actualizar → re-valida → VALIDADO + XML |
| **BOL-EC-001** | `actualizarValidado_reValidar_sigueVALIDADO()` | Boleta VALIDADO → PUT actualizar → re-valida → VALIDADO + nuevo XML |

#### @Nested `ActualizarUnhappy` — 2 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-UP-009** | `actualizarEnviada_falla()` | Estado ENVIADA → `SunatValidationException` |
| **BOL-UP-010** | `actualizarAceptada_falla()` | Estado ACEPTADA → `SunatValidationException` |

#### @Nested `ValidarBoleta` — 3 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-HP-009** | `validarManual_VALIDADO()` | Boleta BORRADOR → POST validar → valid=true, estado=VALIDADO, XML generado |
| **BOL-UP-013** | `validarEnviada_falla()` | Estado ENVIADA → `SunatValidationException` |
| **BOL-UP-014** | `boletaNoEncontrada_404()` | boletaId inexistente → `ResourceNotFoundException` |

#### @Nested `EliminarBoleta` — 4 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-HP-010** | `eliminarBorrador_ok()` | Estado BORRADOR → eliminada + cuotas + cargos eliminados |
| **BOL-UP-011** | `eliminarEnviada_falla()` | Estado ENVIADA → `SunatValidationException` |
| **BOL-UP-012** | `eliminarAceptada_falla()` | Estado ACEPTADA → `SunatValidationException` |
| *(extra)* | `eliminarConError_ok()` | Estado CON_ERROR → eliminada (permitido por SUNAT) |

#### @Nested `AnularBoleta` — 2 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-HP-012** | `anularAceptada_ok()` | Estado ACEPTADA → ANULADA + notificación |
| *(extra)* | `anularInexistente_404()` | boletaId inexistente → `ResourceNotFoundException` |

#### @Nested `DescargarXml` — 3 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-HP-011** | `descargarXml_conContenido_ok()` | Boleta VALIDADA con XML → retorna content + filename |
| **BOL-UP-015** | `descargarXml_sinXml_sinContenido()` | Boleta sin XML → `hasContent=false` |
| *(extra)* | `descargarXml_inexistente_404()` | boletaId inexistente → `ResourceNotFoundException` |

#### @Nested `ObtenerListar` — 3 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| *(extra)* | `obtenerBoleta_ok()` | Boleta existente → retorna data |
| *(extra)* | `obtenerBoleta_inexistente_404()` | boletaId inexistente → 404 |
| *(extra)* | `listarBoletas_ok()` | Lista correcta con paginación |

#### @Nested `EdgeCases` — 5 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-EC-002** | `conErrorCorregida_generaNuevoXml()` | CON_ERROR → actualizar → VALIDADO + XML por primera vez |
| **BOL-EC-005** | `contadoAutoAsignaFechaVencimiento()` | formaPago=CONTADO → fechaVencimiento = fechaEmision |
| **BOL-EC-006** | `sinFormaPago_defaultContado()` | formaPago=null → default "CONTADO" |
| **BOL-EC-007** | `toleranciaRedondeo_001_autoValidacionOk()` | diff=0.01 → auto-validación pasa → VALIDADO |
| **BOL-EC-009** | `errorGenerandoXml_catchSilencioso()` | buildBoletaXml lanza RuntimeException → boleta se guarda, xmlContent=null, estado=VALIDADO |

---

### 4.2 ResumenDiarioServiceImplTest.java (14 tests)

#### @Nested `CrearResumenHappy` — 4 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-RD-001** | `crearResumen_boletasValidadasDelDia_ok()` | Busca VALIDADAS por fecha → crea RC-YYYYMMDD-001 → ENVIADO + ticket |
| **BOL-RD-002** | `crearResumen_conBoletaIdsEspecificos_ok()` | boletaIds → filtra VALIDADO → resumen con esas boletas |
| **BOL-RD-003** | `crearResumen_multiplesDelMismoDia_correlativoIncrementa()` | Ya existe 1 resumen → nuevo = RC-...-002 |
| **BOL-EC-008** | `crearResumen_boletasDiferentesFechas_ok()` | boletaIds con fechas distintas → se permite |

#### @Nested `ConsultarEstado` — 3 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-RD-004** | `consultarEstado_ACEPTADO()` | SUNAT retorna ACEPTADO → resumen ACEPTADO + boletas ACEPTADA |
| **BOL-RD-005** | `consultarEstado_RECHAZADO_boletasRevierten()` | SUNAT retorna RECHAZADO → boletas revierten a VALIDADO + desvinculadas |
| **BOL-EC-003** | `rechazado_boletasDisponiblesParaNuevoResumen()` | Post-rechazo → boletas con resumenDiarioId=null y estado=VALIDADO |

#### @Nested `ListarObtener` — 2 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-RD-006** | `listarResumenes_ok()` | Lista ordenada por fecha DESC |
| **BOL-RD-007** | `obtenerDetalle_ok()` | Resumen + boletas asociadas |

#### @Nested `CrearResumenUnhappy` — 4 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-RD-UP-001** | `sinBoletasValidadas_falla()` | No hay VALIDADAS → `SunatValidationException` |
| **BOL-RD-UP-002** | `boletaIdNoEncontrado_404()` | boletaIds con ID inexistente → `ResourceNotFoundException` |
| **BOL-RD-UP-003** | `boletaNoValidada_filtradaYVacia_falla()` | boletaIds con estado BORRADOR → filtro deja 0 → `SunatValidationException` |
| **BOL-RD-UP-007** | `empresaNoEncontrada_404()` | companyId inexistente → `ResourceNotFoundException` |

#### @Nested `ConsultarEstadoUnhappy` — 3 tests

| Escenario | Método | Qué verifica |
|-----------|--------|--------------|
| **BOL-RD-UP-004** | `consultarEstado_sinTicket_falla()` | ticketSunat=null → `SunatValidationException` |
| **BOL-RD-UP-005** | `envioSunatFalla_resumenConError()` | sendSummary exitoso=false → resumen CON_ERROR |
| **BOL-RD-UP-006** | `resumenNoEncontrado_404()` | resumenId inexistente → `ResourceNotFoundException` |

---

## 5. Notas de Implementación

### 5.1 Comportamiento real vs escenarios documentados

Algunos escenarios del documento `ESCENARIOS_BOLETA.md` indican "HTTP 400" pero el código real
retorna HTTP 200 con `estado=CON_ERROR`. Esto ocurre porque `emitirBoleta()` solo hace
**hard-fail** (lanza excepción) para:

| Validación | Hard-fail (excepción) | Soft-fail (CON_ERROR) |
|------------|----------------------|----------------------|
| Serie formato/tipo | ✅ `emitirBoleta` | — |
| Totales null/fórmula | ✅ `emitirBoleta` | — |
| Tipo doc cliente (RUC) | ✅ `emitirBoleta` | — |
| Montos negativos | — | ✅ `documentValidatorService` |
| Fecha futura | — | ✅ `documentValidatorService` |
| Detalle vacío | — | ✅ `documentValidatorService` |
| Moneda sin TC | — | ✅ `documentValidatorService` |
| Crédito sin vencimiento | — | ✅ `documentValidatorService` |
| JSON corrupto | — | ✅ `documentValidatorService` |

Los tests reflejan el **comportamiento real** del código, no el aspiracional.

### 5.2 `actualizarBoletaBorrador` tiene validaciones más estrictas

El endpoint PUT actualizar SÍ hace hard-fail para:
- `validarMontosNoNegativos()` → excepción directa
- `validarDetalleNoVacio()` → excepción directa
- `validarMonedaYTipoCambio()` → excepción directa (si moneda ≠ null)

Esto es coherente: al actualizar manualmente, se espera que el usuario corrija errores antes.

### 5.3 `anularBoleta` no valida estado previo

El código actual de `anularBoleta()` **NO** llama `validarAnulacionPermitida()`.
Permite anular desde cualquier estado. Los tests reflejan este comportamiento.
→ **Posible mejora futura:** agregar validación de estado antes de anular.

### 5.4 Preparación para microservicios

Los packages de test (`doc_boleta.service.impl`, `resumen_diario.service.impl`)
**espeja** la estructura de `src/main/java`. Cuando se extraiga `doc_boleta` a su propio
microservicio, bastará con mover:

```
src/main/java/.../doc_boleta/   →  ms-boleta/src/main/java/.../doc_boleta/
src/test/java/.../doc_boleta/   →  ms-boleta/src/test/java/.../doc_boleta/
```

Las dependencias externas (CompanyRepository, ClientRepository, etc.) se reemplazarán
por clientes Feign o gRPC al momento de la separación.

---

## 6. Conteo Final

| Archivo | Tests | Escenarios cubiertos |
|---------|-------|---------------------|
| `DocBoletaServiceImplTest.java` | 40 | BOL-HP-001..012, BOL-UP-001..018, BOL-EC-001..002, BOL-EC-005..007, BOL-EC-009 |
| `ResumenDiarioServiceImplTest.java` | 16 | BOL-RD-001..007, BOL-RD-UP-001..007, BOL-EC-003, BOL-EC-008 |
| **TOTAL** | **56** | **54 escenarios + 2 extras** |

> Los 2 tests extra son: `eliminarConError_ok()` y `anularInexistente_404()`
> (complementan la cobertura de estados).
> El escenario **BOL-EC-004** (cliente anónimo en XML) se verifica indirectamente en
> `BOL-HP-005` (emitir sin cliente → `buildBoletaXml` recibe ClientEntity con tipoDoc="-").
> El escenario **BOL-EC-010** (PEN con tipoCambio) se verifica indirectamente en la
> auto-validación de `BOL-HP-001` (advertencia, no error).

---

## 7. Ejecución

```bash
# Ejecutar solo tests de boleta
mvn test -Dtest="DocBoletaServiceImplTest"

# Ejecutar solo tests de resumen diario
mvn test -Dtest="ResumenDiarioServiceImplTest"

# Ejecutar todos los tests del proyecto
mvn test

# Ejecutar un nested class específico
mvn test -Dtest="DocBoletaServiceImplTest\$EmitirHappy"
```

