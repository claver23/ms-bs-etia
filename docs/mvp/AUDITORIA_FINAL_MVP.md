# 🔍 AUDITORÍA FINAL — MVP ETIA v1.0 vs Realidad del Proyecto

> **Fecha:** 09/04/2026  
> **Auditor:** Copilot — Revisión exhaustiva final  
> **Build:** ✅ 305 tests, 0 failures, 0 errors, 0 warnings — BUILD SUCCESS  

---

## 📊 RESUMEN EJECUTIVO

| Sprint | Estado | Completitud |
|--------|--------|-------------|
| **Sprint 1 — Persistencia (Flyway + PostgreSQL)** | 🔴 **NO IMPLEMENTADO** | ~15% (solo `application-prod.yaml` existe) |
| **Sprint 2 — Firma SUNAT + SOAP + CDR** | 🔴 **NO IMPLEMENTADO** | ~5% (solo interface `SunatSoapClient` + mock básico) |
| **Sprint 3 — Seguridad (JWT + Isolation)** | 🔴 **NO IMPLEMENTADO** | 0% |
| **Sprint 4 — Onboarding + PDF + Deploy** | 🟢 **IMPLEMENTADO** | ~90% |

---

## SPRINT 1 — PERSISTENCIA 🔴

### Checklist: Plan vs Realidad

| # | Tarea | Plan | Estado Real | Observación |
|---|-------|------|-------------|-------------|
| 1.1 | Dependencias Flyway en `pom.xml` | `flyway-core` + `flyway-database-postgresql` | ❌ **No existe** | `pom.xml` no tiene ninguna referencia a Flyway |
| 1.2 | `db/migration/V1__initial_schema.sql` (19 tablas) | Crear en `src/main/resources/db/migration/` | ❌ **No existe** | No existe el directorio `db/migration/` |
| 1.3 | `db/migration/V2__seed_data.sql` | Copiar datos de `data.sql` | ❌ **No existe** | — |
| 1.4 | `application-prod.yaml` (PostgreSQL) | Crear con PostgreSQL + Flyway + `ddl-auto: none` | ⚠️ **Parcial** | ✅ Existe, pero usa `ddl-auto: update` en vez de `none` y **no tiene bloque `flyway:`** |
| 1.5 | Modificar `application.yaml` (`ddl-auto: none`, `flyway: true`) | Cambiar ddl-auto, agregar flyway | ❌ **No hecho** | Sigue con `ddl-auto: update` y `sql.init.mode: always` |
| 1.6 | Renombrar `data.sql` → `data.sql.bak` | Eliminar ejecución de data.sql | ❌ **No hecho** | `data.sql` sigue activo y ejecutándose en cada arranque |
| 1.7 | `mvn clean test` pasa | 295 tests | ✅ **305 tests pasan** | Más tests que los planificados originalmente |
| 1.8 | App arranca con H2 + Flyway | Verificar | ❌ **N/A** | Flyway no existe |
| 1.9 | App arranca con PostgreSQL local | Verificar | ❓ **No verificado** | `application-prod.yaml` existe pero no se ha probado |

### Impacto

- **Sin Flyway**, el schema se genera por `ddl-auto: update` (riesgoso en producción — no hay control de versiones del schema).
- **`data.sql` se ejecuta en cada reinicio** — datos duplicados si se usa base persistente.
- **En Docker prod**, `ddl-auto: update` podría corromper datos en una actualización de entidades.

---

## SPRINT 2 — FIRMA SUNAT 🔴

### Checklist: Plan vs Realidad

| # | Tarea | Archivo planificado | Estado Real |
|---|-------|---------------------|-------------|
| 2.1 | `SunatSigningService.java` | `app_util/sunat/SunatSigningService.java` | ❌ **No existe** |
| 2.2 | `BillResult.java` (record) | `app_util/sunat/BillResult.java` | ❌ **No existe** |
| 2.3 | `CdrResult.java` (record) | `app_util/sunat/CdrResult.java` | ❌ **No existe** |
| 2.4 | `SunatZipService.java` | `app_util/sunat/SunatZipService.java` | ❌ **No existe** |
| 2.5 | `CdrParserService.java` | `app_util/sunat/CdrParserService.java` | ❌ **No existe** |
| 2.6 | Extender `SunatSoapClient` con `sendBill()` | Agregar método a interface | ❌ **No hecho** — Interface solo tiene `sendSummary()` y `getStatus()` |
| 2.7 | `RealSunatSoapClient.java` con `@Profile("sunat-real")` | `app_util/sunat/RealSunatSoapClient.java` | ❌ **No existe** |
| 2.8 | Adaptar `MockSunatSoapClient` con `@Profile("!sunat-real")` | Agregar profile + `sendBill()` mock | ❌ **No hecho** — No tiene `@Profile` ni `sendBill()` |
| 2.9 | `SunatBillingOrchestrator.java` | `app_util/sunat/SunatBillingOrchestrator.java` | ❌ **No existe** |
| 2.10 | Modificar `DocFacturaServiceImpl.generateXmlAndNotify()` | Integrar firma + envío | ❌ **No hecho** — Flujo actual: generar XML → guardar → estado "ENVIADA" (sin firma ni envío real) |
| 2.11 | Agregar bloque `sunat:` en `application.yaml` | Endpoints, certificados, SOL | ❌ **No hecho** |
| 2.12 | Verificar `UBLExtensions` placeholder | Ya verificado como existente | ✅ **Sí existe** en `UblInvoiceXmlBuilder` |
| 2.13 | `SunatSigningServiceTest.java` + cert test | Tests de firma | ❌ **No existe** |
| 2.14 | `CdrParserServiceTest.java` + CDR ejemplo | Tests de CDR | ❌ **No existe** |
| 2.15 | `SunatZipServiceTest.java` | Tests de zip | ❌ **No existe** |
| 2.16 | `SunatBillingOrchestratorTest.java` | Tests del orquestador | ❌ **No existe** |

### Lo que SÍ existe (pre-existente, no del MVP)

| Archivo | Contenido |
|---------|-----------|
| `SunatSoapClient.java` | Interface con `sendSummary()` + `getStatus()` (para resúmenes diarios, NO facturas) |
| `MockSunatSoapClient.java` | Mock básico sin `@Profile`, sin `sendBill()` |
| `SendResult.java` | Record para resultado de `sendSummary` |
| `StatusResult.java` | Record para resultado de `getStatus` |

### Impacto

- **Las facturas/boletas emitidas NO son válidas ante SUNAT** — no están firmadas digitalmente.
- **El estado "ENVIADA" es falso** — no se envía nada a SUNAT, solo se guarda el XML.
- **No se puede usar en producción real** hasta implementar este sprint.

---

## SPRINT 3 — SEGURIDAD 🔴

### Checklist: Plan vs Realidad

| # | Tarea | Estado Real |
|---|-------|-------------|
| 3.1 | Dependencia `spring-boot-starter-security` en `pom.xml` | ❌ **No existe** |
| 3.2 | Dependencias JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) en `pom.xml` | ❌ **No existe** |
| 3.3 | `V3__add_users_table.sql` (Flyway) | ❌ **No existe** (además Flyway tampoco está) |
| 3.4 | `UserEntity.java` en `auth/persistence/` | ❌ **No existe** — No existe el paquete `auth/` |
| 3.5 | `UserRepository.java` | ❌ **No existe** |
| 3.6 | `JwtService.java` | ❌ **No existe** |
| 3.7 | `JwtAuthFilter.java` | ❌ **No existe** |
| 3.8 | `SecurityConfig.java` | ❌ **No existe** |
| 3.9 | `AuthController.java` (login/register/me) | ❌ **No existe** |
| 3.10 | `CompanyIsolationInterceptor.java` | ❌ **No existe** |
| 3.11 | `login.html` + `register.html` | ❌ **No existen** |
| 3.12 | `AuthViewController.java` | ❌ **No existe** |
| 3.13 | Modificar `api-client.js` (Bearer token) | ❌ **No hecho** |
| 3.14 | Modificar `app-state.js` (companyId del JWT) | ❌ **No hecho** |
| 3.15 | Config JWT en `application.yaml` | ❌ **No hecho** |
| 3.16 | Tests con security config | ❌ **N/A** |

### Impacto

- **La API está 100% abierta** — cualquiera puede acceder a todos los endpoints sin autenticación.
- **No hay aislamiento por empresa** — un usuario puede ver/modificar datos de cualquier empresa.
- **No hay flujo login/register** — el frontend asume acceso libre.
- **Riesgo de seguridad CRÍTICO** si se despliega a producción.

---

## SPRINT 4 — ONBOARDING + PDF + DEPLOY 🟢

### Checklist: Plan vs Realidad

| # | Tarea | Estado Real | Detalle |
|---|-------|-------------|---------|
| 4.1 | Dependencias OpenPDF + ZXing en `pom.xml` | ✅ **Implementado** | `openpdf:2.0.3`, `zxing-core:3.5.3`, `zxing-javase:3.5.3` |
| 4.2 | `QrCodeService.java` | ✅ **Implementado** | `app_util/service/QrCodeService.java` — genera QR PNG con formato SUNAT |
| 4.3 | `PdfGeneratorService.java` | ✅ **Implementado** | `app_util/service/PdfGeneratorService.java` — genera PDF con layout A4 completo |
| 4.4 | `OnboardingService.java` | ✅ **Implementado** | `app_util/service/OnboardingService.java` — crea company + sede + 4 series + 4 correlativos |
| 4.5 | Endpoint PDF en OpenAPI (`ms-bs-etia.yaml`) | ✅ **Implementado** | Endpoints en YAML + controllers separados |
| 4.6 | `GET /factura/{id}/pdf` controller | ✅ **Implementado** | `FacturaPdfController.java` — funcional |
| 4.6b | `GET /boleta/{id}/pdf` controller | ✅ **Implementado** | `BoletaPdfController.java` — funcional (adicional al plan) |
| 4.7 | Endpoint onboarding en controller | ⚠️ **Diferente ubicación** | Plan: `AuthController.java` en `auth/openapi/`. Real: `OnboardingOpenApi.java` en `app_util/openapi/` — funciona pero no está ligado a auth |
| 4.8 | `onboarding.html` (vista wizard) | ✅ **Implementado** | `templates/onboarding.html` existe + `OnboardingViewController.java` |
| 4.9 | `Dockerfile` (multi-stage) | ✅ **Implementado** | Multi-stage con `ttf-dejavu`, JVM tuning, directorios storage/certs |
| 4.10 | `docker-compose.yml` | ✅ **Implementado** | PostgreSQL 16 + app con healthcheck, volumes |
| 4.11 | `.env.example` | ✅ **Implementado** | Variables DB + JWT + Storage + Certs documentadas |
| 4.12 | Tests: QrCodeServiceTest | ✅ **Implementado** | 5 tests (PNG magic bytes, dimensiones, tamaño pequeño, formato SUNAT, hash null) |
| 4.12b | Tests: PdfGeneratorServiceTest | ✅ **Implementado** | 3 tests (factura PDF, boleta PDF, campos null opcionales) |
| 4.12c | Tests: OnboardingServiceTest | ✅ **Implementado** | 2 tests (flujo completo, campos null opcionales) |
| 4.13 | `mvn clean test` pasa | ✅ **305 tests, 0 failures** | 10 más que los 295 planeados |
| 4.14 | `docker-compose up` funcional | ❓ **No verificado** | Los archivos existen pero no se ha testeado el deploy Docker |
| 4.15 | Test E2E completo | ❌ **No posible** | Requiere Sprint 3 (register) — no hay auth → no hay flujo completo |

### Desviaciones del Sprint 4 respecto al Plan

| Aspecto | Plan MVP | Realidad | ¿Problema? |
|---------|----------|----------|------------|
| **Ubicación OnboardingService** | `auth/service/` | `app_util/service/` | ⚠️ Menor — Funcional pero no ligado al módulo auth (que no existe) |
| **Ubicación OnboardingController** | `auth/openapi/AuthController.java` como endpoint adicional | `app_util/openapi/OnboardingOpenApi.java` independiente | ⚠️ Menor — Implementa `OnboardingApi` de OpenAPI codegen (mejor que el plan) |
| **OnboardingService.registrarEmpresa() firma** | Incluye `String userId` como primer param (del JWT) | **No incluye userId** | ⚠️ Importante — Sin Sprint 3, no hay userId. Se necesitará agregar cuando exista auth |
| **OnboardingResult** | `record(companyId, sedeId, token)` — retorna JWT actualizado | `record(companyId, sedeId, message)` — retorna mensaje | ⚠️ Menor — Sin JWT no puede retornar token |
| **Endpoint PDF** | `descargarFacturaPdf` en el controller OpenAPI existente | Controllers separados (`FacturaPdfController`, `BoletaPdfController`) | ✅ Mejor — Separación de responsabilidades más limpia |
| **Wizard onboarding** | 3 pasos: Datos empresa → Certificado → Listo | Wizard de 5 pasos (existente pre-MVP) | ✅ Más completo que el plan |

---

## 🔗 DEPENDENCIAS CROSS-SPRINT — Análisis de Impacto

El plan MVP define este orden obligatorio: **Sprint 1 → 2 → 3 → 4**

| Dependencia planificada | ¿Afecta la realidad? |
|------------------------|---------------------|
| Sprint 1 → Sprint 2: Schema Flyway con columnas `certificado_path`, `hash_cdr` | ⚠️ Las columnas YA existen vía `ddl-auto: update` — JPA las genera. Pero sin Flyway no hay control de versiones |
| Sprint 1 → Sprint 4: Docker usa `application-prod.yaml` | ✅ `application-prod.yaml` existe y Docker lo usa |
| Sprint 2 → Sprint 4: PDF necesita `hashCdr` de integración SUNAT | ⚠️ PDF funciona con `hashCdr = null` — muestra "Sin verificar" |
| Sprint 3 → Sprint 4: Onboarding necesita `UserEntity` y `AuthController` | ⚠️ **Se implementó Onboarding SIN auth** — funciona standalone pero no liga usuario a empresa |

---

## 📋 ITEMS FALTANTES CONSOLIDADOS

### 🔴 CRÍTICOS (bloquean producción)

| # | Item | Sprint | Impacto |
|---|------|--------|---------|
| 1 | **Flyway no implementado** — sin control de migraciones | S1 | Datos se pueden corromper en actualizaciones |
| 2 | **`ddl-auto: update` en prod** — peligroso en producción | S1 | Puede borrar columnas/datos al cambiar entidades |
| 3 | **`data.sql` se ejecuta siempre** — duplica datos en base persistente | S1 | Error al reiniciar con PostgreSQL |
| 4 | **Firma digital SUNAT no existe** — XMLs no válidos legalmente | S2 | Facturas emitidas NO son aceptadas por SUNAT |
| 5 | **Envío SOAP a SUNAT no existe** — no hay comunicación real | S2 | No se puede facturar legalmente |
| 6 | **Sin seguridad** — API 100% abierta | S3 | Cualquiera accede a todo |
| 7 | **Sin aislamiento por empresa** — datos cruzados | S3 | Empresa A ve datos de empresa B |

### ⚠️ IMPORTANTES (funcionalidad incompleta)

| # | Item | Sprint | Impacto |
|---|------|--------|---------|
| 8 | **Onboarding no liga usuario** — `userId` no existe en firma del método | S4/S3 | Al implementar auth, hay que modificar `OnboardingService.registrarEmpresa()` para recibir userId y actualizar `UserEntity.companyId` |
| 9 | **`application-prod.yaml` usa `ddl-auto: update`** — debería ser `none` | S1 | Plan dice `none` con Flyway, realidad es `update` sin Flyway |
| 10 | **Bloque `sunat:` falta en `application.yaml`** — endpoints, certificados, SOL | S2 | No hay configuración de endpoints SUNAT |
| 11 | **`MockSunatSoapClient` sin `@Profile`** — siempre activo | S2 | Cuando se cree `RealSunatSoapClient`, habrá conflicto de beans |
| 12 | **`docker-compose.yml` falta `JWT_SECRET`** — existe en plan pero no en realidad | S3/S4 | El plan del Sprint 4 incluye `JWT_SECRET` en docker-compose, no está (porque Sprint 3 no existe) |

### 🔵 MENORES (desviaciones del plan, no bloqueantes)

| # | Item | Detalle |
|---|------|---------|
| 13 | OnboardingService en `app_util/` vs planificado en `auth/` | Funciona — se puede mover cuando exista módulo auth |
| 14 | OnboardingResult retorna `message` vs planificado `token` | Correcto dado que no hay JWT aún |
| 15 | PDF controllers separados vs plan de usar controller OpenAPI | Mejor práctica — separación limpia |
| 16 | 305 tests vs 295 planificados | Positivo — 10 tests adicionales |

---

## ✅ LO QUE SÍ ESTÁ ALINEADO Y FUNCIONA

| Componente | Estado |
|-----------|--------|
| `QrCodeService` con formato SUNAT (10 campos pipe-separated) | ✅ Perfecto |
| `PdfGeneratorService` con layout A4, logo, QR, totales, leyenda | ✅ Perfecto |
| `OnboardingService` crea company + sede + 4 series + 4 correlativos | ✅ Perfecto |
| `FacturaPdfController` → `GET /factura/{id}/pdf` | ✅ Perfecto |
| `BoletaPdfController` → `GET /boleta/{id}/pdf` | ✅ Perfecto (extra al plan) |
| `OnboardingOpenApi` → `POST /api/v1/onboarding` | ✅ Perfecto |
| `onboarding.html` vista wizard | ✅ Existe |
| `Dockerfile` multi-stage con fuentes | ✅ Perfecto |
| `docker-compose.yml` con PostgreSQL + healthcheck | ✅ Perfecto |
| `.env.example` documentado | ✅ Perfecto |
| Tests QrCodeService (5) + PdfGeneratorService (3) + OnboardingService (2) | ✅ Todos pasan |
| Validación RUC en onboarding (formato, duplicados, prefijo) | ✅ Perfecto (extra al plan) |
| 305 tests, 0 failures, 0 warnings | ✅ Excelente |

---

## 📐 ORDEN DE EJECUCIÓN RECOMENDADO PARA COMPLETAR MVP

```
Estado actual: Sprint 4 completado (90%) — Sprints 1, 2 y 3 pendientes

Orden original planificado:  S1 → S2 → S3 → S4
Orden real de ejecución:     S4 (hecho) → S1 → S3 → S2

Orden recomendado para completar:
  1. Sprint 1 (Persistencia) — ~2-3 horas
     - Agregar Flyway + migraciones
     - Cambiar ddl-auto a none
     - Mover data.sql a V2__seed_data.sql
     
  2. Sprint 3 (Seguridad) — ~6-8 horas
     - Spring Security + JWT + login/register
     - Modificar OnboardingService para recibir userId
     - CompanyIsolationInterceptor
     
  3. Sprint 2 (Firma SUNAT) — ~8-12 horas
     - SunatSigningService (XMLDSig)
     - SunatBillingOrchestrator
     - RealSunatSoapClient
     - CDR parsing
     - Integrar en DocFacturaServiceImpl
```

---

## 🏁 VEREDICTO FINAL

**El Sprint 4 está bien implementado y alineado con el plan MVP** (con desviaciones menores justificadas). La calidad del código es alta: tests pasan, 0 warnings, patrones consistentes.

**Los Sprints 1, 2 y 3 están completamente pendientes.** El proyecto NO es un MVP funcional vendible hasta que se implementen al menos:
- Sprint 1 (persistencia real — ~2h)
- Sprint 3 (seguridad mínima — ~8h)  
- Sprint 2 (firma SUNAT — ~12h)

**Estimación para completar MVP:** ~22-25 horas de desarrollo senior.

**Estado real del producto:** Es un **prototipo funcional con Sprint 4 completo**, no un MVP vendible. Necesita ~25 horas más para ser "listo para cobrar S/ 29.90/mes" como indica el plan.

---

*Auditoría completada: 09/04/2026 — 305 tests ✅ — BUILD SUCCESS*

