# 🎯 PLAN DE EJECUCIÓN MVP — ETIA v1.0 "Facturador Legal"

> **Fecha:** 09/04/2026  
> **Alcance:** 4 Sprints → Producto vendible  
> **Base existente:** 20 dominios, 60+ endpoints, XML UBL 2.1, 295 tests, 14 vistas MPA  
> **Objetivo:** PYMEs peruanas emitiendo facturas válidas ante SUNAT

---

## 📦 PRODUCTO FINAL

```
ETIA v1.0 — Facturador Electrónico SUNAT para PYMEs Perú

✅ Emite facturas y boletas con XML UBL 2.1
✅ Firma digital XMLDSig con certificado .p12
✅ Envía a SUNAT por SOAP (beta + producción)
✅ Recibe CDR (aceptado / rechazado / observaciones)
✅ Base de datos PostgreSQL real (datos persistentes)
✅ Migraciones versionadas con Flyway
✅ Login seguro con JWT (email + contraseña)
✅ Aislamiento por empresa (multi-tenant)
✅ Flujo de onboarding (registro → empresa → primera factura)
✅ PDF con QR code (representación impresa SUNAT)
✅ Desplegable con Docker

💰 LISTO PARA COBRAR: S/ 29.90 - 79.90 / mes
```

---

## 📊 RESUMEN DE ARCHIVOS POR SPRINT

| Sprint | Archivos nuevos | Archivos modificados | Dependencias Maven |
|--------|----------------|---------------------|--------------------|
| **1 — Persistencia** | ~4 | ~3 | Flyway (2) |
| **2 — Firma SUNAT** | ~13 (7 src + 4 tests + 2 resources) | ~4 | Ninguna (JDK 21) |
| **3 — Seguridad** | ~12 (9 src + 2 views + 1 test config) | ~5 | Security + JJWT (4) |
| **4 — PDF/Deploy** | ~10 (4 src + 3 tests + 3 infra) | ~4 | OpenPDF + ZXing (3) |
| **TOTAL** | **~39 archivos nuevos** | **~16 modificaciones** | **9 dependencias** |

---

## DEPENDENCIAS CROSS-SPRINT

| De Sprint | A Sprint | Qué depende |
|-----------|----------|-------------|
| 1 | 2 | Schema Flyway debe incluir columnas `certificado_path`, `hash_cdr`, etc. |
| 1 | 4 | Docker usa `application-prod.yaml` creado en Sprint 1 |
| 2 | 4 | PDF necesita `hashCdr` de la integración SUNAT |
| 3 | 4 | Onboarding necesita `UserEntity` y `AuthController` de Sprint 3 |

**Orden obligatorio:** Sprint 1 → Sprint 2 → Sprint 3 → Sprint 4

---

*Plan detallado de cada sprint en archivos separados dentro de esta carpeta.*

