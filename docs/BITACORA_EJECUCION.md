# 📒 BITÁCORA DE EJECUCIÓN — ETIA Suite v3.0

> **Última actualización:** 04/09/2026  
> **Responsable:** Agente de desarrollo ETLIONS

---

## 📊 Estado General

| Fase | Nombre | Estado | Fecha Inicio | Fecha Fin | Compilación |
|------|--------|--------|-------------|-----------|-------------|
| 00 | Auditoría de Data | ⬜ PENDIENTE | — | — | — |
| A | Core (Base) | ⬜ PENDIENTE | — | — | — |
| B | Seguridad + OAuth2 | ⬜ PENDIENTE | — | — | — |
| C | POS Pro | ⬜ PENDIENTE | — | — | — |
| D | Radar de Mercado | ⬜ PENDIENTE | — | — | — |
| E | Alpha Campaigns | ⬜ PENDIENTE | — | — | — |
| F | Supply Chain | ⬜ PENDIENTE | — | — | — |
| G | Overlord Admin | ⬜ PENDIENTE | — | — | — |
| H | Adjuster (Guillotine) | ⬜ PENDIENTE | — | — | — |
| I | Deal Flow CRM | ⬜ PENDIENTE | — | — | — |
| J | Data Vault Reportes | ⬜ PENDIENTE | — | — | — |
| K | GeoInsight | ⬜ PENDIENTE | — | — | — |
| L | Códigos | ⬜ PENDIENTE | — | — | — |
| M | Frontend Modular | ⬜ PENDIENTE | — | — | — |
| N | Integración Final | ⬜ PENDIENTE | — | — | — |

**Leyenda:** ⬜ Pendiente | 🟡 En Progreso | ✅ Completada | ❌ Bloqueada

---

## 🔄 Orden de ejecución recomendado

```
FASE 00 (Auditoría)
  └→ FASE A (Core)
       └→ FASE B (Seguridad) ← BLOQUEANTE, ejecutar ANTES de C-M
            └→ FASE L (Códigos) ← Ejecutar temprano, lo usan C-K
            ├── FASE C (POS Pro)
            ├── FASE D (Radar)
            ├── FASE H (Adjuster)
            ├── FASE E (Campaigns) ← Depende de D opcionalmente
            ├── FASE F (Supply Chain) ← Depende de D
            ├── FASE G (Overlord)
            ├── FASE I (CRM) ← Depende de D, E
            ├── FASE J (Reportes) ← Depende de C-I para data
            ├── FASE K (GeoInsight) ← Depende de D
            └── FASE M (Frontend) ← Paralela, va con cada fase
                 └→ FASE N (Integración Final)
```

> **NOTA:** FASE B es BLOQUEANTE. No ejecutar C-M sin tener B completada.

---

## 📝 Log de ejecución

### [Fecha] — [FASE] — [Tarea]
*Se llenará durante la ejecución. Formato:*

```
04/09/2026 — FASE 00 — T00-01: Auditoría completada. 3 inconsistencias encontradas.
04/09/2026 — FASE 00 — T00-02: Flujos auditados. Brecha en NC sin validación de monto máximo.
04/10/2026 — FASE A — TA-01: website_url agregado a CompanyEntity. Compilación OK.
...
```

---

## 🐛 Issues encontrados

| # | Fase | Descripción | Severidad | Estado |
|---|------|-------------|-----------|--------|
| — | — | — | — | — |

---

## 📦 Dependencias nuevas agregadas

| Fase | Dependencia | Versión | Propósito |
|------|------------|---------|-----------|
| B | spring-boot-starter-security | — | Seguridad |
| B | spring-boot-starter-oauth2-client | — | Google OAuth |
| B | jjwt-api/impl/jackson | 0.12.6 | JWT tokens |
| D | google-maps-services | 2.2.0 | Google Places API |
| E/J | poi-ooxml | 5.2.5 | Excel export |

---

## ✅ Verificación post-fase

Después de cada fase completada:
1. `.\mvnw.cmd clean compile` → ✅ / ❌
2. `.\mvnw.cmd test` → ✅ (N tests) / ❌ (N fallos)
3. Renombrar archivo de fase: `FASE_X_NOMBRE.md` → `FASE_X_NOMBRE_EJECUTADO.md`
4. Actualizar esta bitácora

---

*Esta bitácora es el registro de guerra. Sin ella, el caos nos alcanza.*


