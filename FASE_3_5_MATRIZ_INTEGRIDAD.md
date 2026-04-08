# 📊 FASE 3.5 — MATRIZ DE INTEGRIDAD API

> **Fecha:** 2026-04-08  
> **Fuente JS:** `api-client.js` (508 líneas, ~55 funciones)  
> **Fuente Swagger:** `ms-bs-etia.yaml` (~80 operationIds)  
> **Fuente Backend:** 21 ServiceImpl + OpenAPI-generated controllers

---

## 1. FUNCIONES JS → SWAGGER → BACKEND

| # | Función JS (api-client.js) | HTTP | Path | operationId Swagger | Api Generated | ServiceImpl | Estado |
|---|---------------------------|------|------|---------------------|--------------|-------------|--------|
| **COMPANY** |
| 1 | `listCompanies()` | GET | `/company` | `listarEmpresas` | CompanyApi ✅ | CompanyServiceImpl ✅ | ✅ OK |
| 2 | `updateCompany()` | PUT | `/company` | `actualizarEmpresa` | CompanyApi ✅ | CompanyServiceImpl ✅ | ✅ OK |
| 3 | `uploadCertificado()` | POST | `/company/certificado` | `subirCertificado` | CompanyApi ✅ | CompanyServiceImpl ✅ | ✅ OK |
| 4 | `probarConexionSunat()` | POST | `/company/probar-conexion` | `probarConexionSunat` | CompanyApi ✅ | CompanyServiceImpl ✅ | ✅ OK |
| 5 | `uploadLogo()` | POST | `/company/logo` | `subirLogo` | CompanyApi ✅ | CompanyServiceImpl ✅ | ✅ OK |
| 6 | `getLogoUrl()` | — | `/company/logo` | `obtenerLogo` | CompanyApi ✅ | CompanyServiceImpl ✅ | ✅ URL-only (no fetch) |
| **CLIENT** |
| 7 | `listClients()` | GET | `/client` | `listarClientes` | ClientApi ✅ | ClientServiceImpl ✅ | ✅ OK |
| 8 | `registerClient()` | POST | `/client` | `registrarCliente` | ClientApi ✅ | ClientServiceImpl ✅ | ✅ OK |
| 9 | `updateClient()` | PUT | `/client` | `actualizarCliente` | ClientApi ✅ | ClientServiceImpl ✅ | ✅ OK |
| 10 | `deleteClient()` | DELETE | `/client` | `eliminarCliente` | ClientApi ✅ | ClientServiceImpl ✅ | ✅ OK |
| **RENIEC** |
| 11 | `consultarReniec()` | GET | `/reniec` | `consultarDocumento` | ReniecApi ✅ | ReniecServiceImpl ✅ | ✅ OK |
| **TIPO CAMBIO** |
| 12 | `getTipoCambio()` | GET | `/tipo_cambio` | `obtenerTipoCambio` | TipoCambioApi ✅ | TipoCambioServiceImpl ✅ | ✅ OK |
| 13 | `listTiposCambio()` | GET | `/tipo_cambio/list` | `listarTiposCambio` | TipoCambioApi ✅ | TipoCambioServiceImpl ✅ | ✅ OK |
| **EMPLOYEE** |
| 14 | `listEmployees()` | GET | `/employee` | `listarEmpleados` | EmployeeApi ✅ | EmployeeServiceImpl ✅ | ✅ OK |
| 15 | `registerEmployee()` | POST | `/employee` | `registrarEmpleado` | EmployeeApi ✅ | EmployeeServiceImpl ✅ | ✅ OK |
| 16 | `updateEmployee()` | PUT | `/employee` | `actualizarEmpleado` | EmployeeApi ✅ | EmployeeServiceImpl ✅ | ✅ OK |
| 17 | `deleteEmployee()` | DELETE | `/employee` | `eliminarEmpleado` | EmployeeApi ✅ | EmployeeServiceImpl ✅ | ✅ OK |
| **PRODUCT** |
| 18 | `listProducts()` | GET | `/product` | `buscarProductos` | ProductApi ✅ | ProductServiceImpl ✅ | ✅ OK |
| 19 | `searchProducts()` | GET | `/product` | `buscarProductos` | ProductApi ✅ | ProductServiceImpl ✅ | ✅ OK (same endpoint, diff params) |
| 20 | `createProduct()` | POST | `/product` | `crearProducto` | ProductApi ✅ | ProductServiceImpl ✅ | ✅ OK |
| 21 | `getProduct()` | GET | `/product/{id}` | `obtenerProducto` | ProductApi ✅ | ProductServiceImpl ✅ | ✅ OK |
| 22 | `updateProduct()` | PUT | `/product` | `actualizarProducto` | ProductApi ✅ | ProductServiceImpl ✅ | ✅ OK |
| 23 | `deleteProduct()` | DELETE | `/product` | `eliminarProducto` | ProductApi ✅ | ProductServiceImpl ✅ | ✅ OK |
| **FACTURA** |
| 24 | `emitirFactura()` | POST | `/factura` | `emitirFactura` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 25 | `listarFacturas()` | GET | `/factura` | `listarFacturas` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 26 | `anularFactura()` | PUT | `/factura` | `anularFactura` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 27 | `eliminarFactura()` | DELETE | `/factura` | `eliminarFactura` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 28 | `emitirFacturaBorrador()` | PUT | `/factura/{id}/emitir` | `emitirFacturaBorrador` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 29 | `actualizarFacturaBorrador()` | PUT | `/factura/{id}/actualizar` | `actualizarFacturaBorrador` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 30 | `validarFactura()` | POST | `/factura/{id}/validar` | `validarFactura` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 31 | `descargarFacturaXml()` | GET | `/factura/{id}/xml` | `descargarFacturaXml` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| 32 | `getFactura()` | GET | `/factura/{id}` | `obtenerFactura` | FacturaApi ✅ | DocFacturaServiceImpl ✅ | ✅ OK |
| **BOLETA** |
| 33 | `emitirBoleta()` | POST | `/boleta` | `emitirBoleta` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| 34 | `listarBoletas()` | GET | `/boleta` (list) | `listarBoletas` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| 35 | `validarBoleta()` | POST | `/boleta/{id}/validar` | `validarBoleta` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| 36 | `actualizarBoletaBorrador()` | PUT | `/boleta/{id}/actualizar` | `actualizarBoletaBorrador` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| 37 | `eliminarBoleta()` | DELETE | `/boleta` | `eliminarBoletaBorrador` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| 38 | `descargarBoletaXml()` | GET | `/boleta/{id}/xml` | `descargarBoletaXml` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| 39 | `anularBoleta()` | PUT | `/boleta` | `anularBoleta` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK (PASO 3) |
| 40 | `getBoleta()` | GET | `/boleta/{id}` | `obtenerBoleta` | BoletaApi ✅ | DocBoletaServiceImpl ✅ | ✅ OK |
| **RESUMEN DIARIO** |
| 41 | `crearResumenDiario()` | POST | `/resumen-diario` | `crearResumenDiario` | ResumenDiarioApi ✅ | ResumenDiarioServiceImpl ✅ | ✅ OK |
| 42 | `listarResumenesDiarios()` | GET | `/resumen-diario` | `listarResumenesDiarios` | ResumenDiarioApi ✅ | ResumenDiarioServiceImpl ✅ | ✅ OK |
| 43 | `obtenerResumenDiario()` | GET | `/resumen-diario/{id}` | `obtenerResumenDiario` | ResumenDiarioApi ✅ | ResumenDiarioServiceImpl ✅ | ✅ OK |
| 44 | `consultarEstadoResumen()` | POST | `/resumen-diario/{id}/consultar-estado` | `consultarEstadoResumen` | ResumenDiarioApi ✅ | ResumenDiarioServiceImpl ✅ | ✅ OK |
| **NOTA CREDITO** |
| 45 | `emitirNotaCredito()` | POST | `/nota_credito` | `emitirNotaCredito` | NotaCreditoApi ✅ | DocNotacredServiceImpl ✅ | ✅ OK |
| 46 | `listarNotasCredito()` | GET | `/nota_credito` | `listarNotasCredito` | NotaCreditoApi ✅ | DocNotacredServiceImpl ✅ | ✅ OK |
| 47 | `getNotaCredito()` | GET | `/nota_credito/{id}` | `obtenerNotaCredito` | NotaCreditoApi ✅ | DocNotacredServiceImpl ✅ | ✅ OK |
| **NOTA DEBITO** |
| 48 | `emitirNotaDebito()` | POST | `/nota_debito` | `emitirNotaDebito` | NotaDebitoApi ✅ | DocNotadebServiceImpl ✅ | ✅ OK |
| 49 | `listarNotasDebito()` | GET | `/nota_debito` | `listarNotasDebito` | NotaDebitoApi ✅ | DocNotadebServiceImpl ✅ | ✅ OK |
| 50 | `getNotaDebito()` | GET | `/nota_debito/{id}` | `obtenerNotaDebito` | NotaDebitoApi ✅ | DocNotadebServiceImpl ✅ | ✅ OK |
| **SEDE** |
| 51 | `listSedes()` | GET | `/sede` | `listarSedes` | SedeApi ✅ | SedeServiceImpl ✅ | ✅ OK |
| 52 | `getSede()` | GET | `/sede/{id}` | `obtenerSede` | SedeApi ✅ | SedeServiceImpl ✅ | ✅ OK |
| 53 | `createSede()` | POST | `/sede` | `crearSede` | SedeApi ✅ | SedeServiceImpl ✅ | ✅ OK |
| 54 | `updateSede()` | PUT | `/sede` | `actualizarSede` | SedeApi ✅ | SedeServiceImpl ✅ | ✅ OK |
| 55 | `deleteSede()` | DELETE | `/sede` | `eliminarSede` | SedeApi ✅ | SedeServiceImpl ✅ | ✅ OK |
| 56 | `generarSeriesSede()` | POST | `/sede/{id}/generar_series` | `generarSeriesSede` | SedeApi ✅ | SedeServiceImpl ✅ | ✅ OK |
| **REFERENCIA** |
| 57 | `listUbigeos()` | GET | `/ubigeo` | `listarUbigeos` | UbigeoApi ✅ | UbigeoServiceImpl ✅ | ✅ OK |
| 58 | `listUnidadesMedida()` | GET | `/unidad_medida` | `listarUnidadesMedida` | UnidadMedidaApi ✅ | UnidadMedidaServiceImpl ✅ | ✅ OK |
| 59 | `listSeriesConfig()` | GET | `/serie_config` | `listarSeriesConfig` | SerieConfigApi ✅ | SerieConfigServiceImpl ✅ | ✅ OK |
| **USER PREFERENCE** |
| 60 | `listPreferences()` | GET | `/user_preference` | `listarPreferencias` | UserPreferenceApi ✅ | UserPreferenceServiceImpl ✅ | ✅ OK |
| 61 | `savePreference()` | PUT | `/user_preference` | `guardarPreferencia` | UserPreferenceApi ✅ | UserPreferenceServiceImpl ✅ | ✅ OK |
| **TABLAS SUNAT** |
| 62 | `listMenuTablasSunat()` | GET | `/tabla_sunat/menu` | `listarMenuTablasSunat` | TablaSunatApi ✅ | TablaSunatServiceImpl ✅ | ✅ OK |
| 63 | `obtenerTablaSunat()` | GET | `/tabla_sunat` | `obtenerTablaSunat` | TablaSunatApi ✅ | TablaSunatServiceImpl ✅ | ✅ OK |
| **CATÁLOGO SUNAT** |
| 64 | `listMenuCatalogosSunat()` | GET | `/catalogo_sunat/menu` | `listarMenuCatalogosSunat` | CatalogoSunatApi ✅ | CatalogoSunatServiceImpl ✅ | ✅ OK |
| 65 | `obtenerCatalogoSunat()` | GET | `/catalogo_sunat` | `obtenerCatalogoSunat` | CatalogoSunatApi ✅ | CatalogoSunatServiceImpl ✅ | ✅ OK |
| **NOTIFICATION** |
| 66 | `listNotifications()` | GET | `/notification` | `listarNotificaciones` | NotificationApi ✅ | NotificationServiceImpl ✅ | ✅ OK |
| 67 | `markAllNotifRead()` | PUT | `/notification/read-all` | `marcarTodasLeidas` | NotificationApi ✅ | NotificationServiceImpl ✅ | ✅ OK |
| 68 | `markNotifRead()` | PUT | `/notification/{id}/read` | `marcarNotificacionLeida` | NotificationApi ✅ | NotificationServiceImpl ✅ | ✅ OK |
| 69 | `clearNotifications()` | DELETE | `/notification` | `limpiarNotificaciones` | NotificationApi ✅ | NotificationServiceImpl ✅ | ✅ OK |

---

## 2. ENDPOINTS SWAGGER SIN FUNCIÓN JS

Estos `operationId` existen en el swagger pero **NO** tienen una función correspondiente en `api-client.js`:

| # | operationId | HTTP | Path | ¿Se necesita en JS? | Acción |
|---|-------------|------|------|---------------------|--------|
| 1 | `registrarEmpresa` | POST | `/company` | ❌ No (empresas se crean en onboarding/backend) | OK — no se necesita |
| 2 | `eliminarEmpresa` | DELETE | `/company` | ❌ No (no hay UI para eliminar empresa) | OK — no se necesita |
| 3 | `obtenerEmpleado` | GET | `/employee/{id}` | ⚠️ Podría usarse para edición detallada | FASE 5 — evaluar |
| 4 | `actualizarNotaCreditoBorrador` | PUT | `/nota_credito?notaCreditoId=X` | ✅ Agregado en FASE 4 PASO 1 | ✅ RESUELTO |
| 5 | `eliminarNotaCreditoBorrador` | DELETE | `/nota_credito?notaCreditoId=X` | ✅ Agregado en FASE 4 PASO 1 | ✅ RESUELTO |
| 6 | `actualizarNotaDebitoBorrador` | PUT | `/nota_debito?notaDebitoId=X` | ✅ Agregado en FASE 4 PASO 1 | ✅ RESUELTO |
| 7 | `eliminarNotaDebitoBorrador` | DELETE | `/nota_debito?notaDebitoId=X` | ✅ Agregado en FASE 4 PASO 1 | ✅ RESUELTO |
| 8 | `generarLibroElectronico` | POST | `/libro_electronico` | ✅ Agregado en FASE 4 PASO 4 | ✅ RESUELTO |
| 9 | `listarLibrosElectronicos` | GET | `/libro_electronico` | ✅ Agregado en FASE 4 PASO 4 | ✅ RESUELTO |
| 10 | `publicarNotificacion` | POST | `/notification` | ❌ No (solo backend publica) | OK — solo server-side |
| 11 | `registrarTipoCambio` | POST | `/tipo_cambio` | ❌ No (se registra automáticamente) | OK — solo server-side |

---

## 3. FUNCIONES JS "SOLO FRONTEND" (no llaman API)

| # | Función / Módulo | Almacenamiento | Descripción |
|---|-----------------|----------------|-------------|
| 1 | `DocPreview.exportPdf()` | N/A | Genera PDF vía html2canvas (local) |
| 2 | `DocPreview.exportJson()` | N/A | Copia JSON al clipboard (local) |
| 3 | `DocPreview.executeDuplicate()` | N/A | Redirige a wizard con params URL |
| 4 | `ETIA.config` | localStorage + backend sync | Config read/write via `savePreference` |
| 5 | `etia_notif_prefs` | localStorage + backend sync | Notif prefs via `savePreference` (PASO 5) |
| 6 | `PdfTemplate.render()` | N/A | Template rendering engine (local) |

---

## 4. DEAD-ENDS RESUELTOS EN FASE 3.5

| # | Problema | Resolución | Paso |
|---|---------|-----------|------|
| 1 | `annulDocument()` → endpoint inexistente | Eliminado. `anularDocumento()` despacha a `anularFactura`/`anularBoleta` | PASO 1 |
| 2 | `doc-preview` acciones solo-factura | Refactorizado: `emitir()`, `anular()`, `eliminar()`, `descargarXml()` multi-tipo | PASO 2 |
| 3 | `anularBoleta` faltaba en JS | Agregada función `anularBoleta()` en api-client.js | PASO 3 |
| 4 | `toastDuration` no sincronizaba | Agregados 3 claves a `prefsToSync` | PASO 6 |
| 5 | `notif_prefs` solo localStorage | Ahora sincroniza al backend via `savePreference()` | PASO 5 |

---

## 5. RESUMEN

- **75 funciones JS** mapeadas a endpoints → **75/75 ✅ conectadas al backend** (69 originales + 6 nuevas en FASE 4)
- **11 operationIds swagger** sin función JS → **3 correctos (server-only), 6 resueltos en FASE 4, 1 para FASE 5**
- **0 dead-ends activos** — todos resueltos en FASE 3.5 + FASE 4
- **6 funciones solo-frontend** — documentadas como N/A backend

### Funciones agregadas en FASE 4
1. `actualizarNotaCreditoBorrador()` — PUT `/nota_credito`
2. `eliminarNotaCreditoBorrador()` — DELETE `/nota_credito`
3. `actualizarNotaDebitoBorrador()` — PUT `/nota_debito`
4. `eliminarNotaDebitoBorrador()` — DELETE `/nota_debito`
5. `generarLibroElectronico()` — POST `/libro_electronico`
6. `listarLibrosElectronicos()` — GET `/libro_electronico`

### Prioridades FASE 5 (derivadas de esta matriz)
1. 🟢 Evaluar si `obtenerEmpleado` se necesita en frontend



