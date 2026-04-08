# 🚀 PLAN DE EJECUCIÓN — FASE 4: NOTAS COMPLETAS + LIBROS ELECTRÓNICOS + ROBUSTEZ

> **Fecha:** 2026-04-08  
> **Estado: ✅ COMPLETADO** — 295 tests ✅ · 0 failures · BUILD SUCCESS  
> **Regla:** Cada paso termina con `mvn clean compile` + `mvn test` → 0 failures  
> **Base:** Continuación de PLAN_EJECUCION_FASE_3_5.md (completado — 295 tests ✅)  
> **Origen:** Hallazgos H1–H5 de FASE 3.5 + Prioridades de Matriz de Integridad  
> **Todos los 6 pasos COMPLETADOS.**

---

## TABLA DE CONTENIDOS

### EJE A — Soporte completo Notas Crédito/Débito (Prioridad Alta)
1. [✅ PASO 1 — api-client.js: Funciones faltantes NC/ND (actualizar + eliminar)](#paso-1)
2. [✅ PASO 2 — doc-preview.js: Acciones completas para notas (editar, eliminar, ver detalle)](#paso-2)
3. [✅ PASO 3 — notas-credito.html / notas-debito.html: Acciones inline (eliminar borrador)](#paso-3)

### EJE B — Libros Electrónicos (Prioridad Media)
4. [✅ PASO 4 — api-client.js: Funciones para Libros Electrónicos](#paso-4)
5. [✅ PASO 5 — Vista frontend: libros-electronicos.html](#paso-5)

### EJE C — Robustez (Prioridad Baja)
6. [✅ PASO 6 — logout / cambiarEmpresa: Limpiar estado correctamente](#paso-6)

---

## DEPENDENCIAS Y ORDEN

```
EJE A (Notas completas) — Prioridad ALTA
  PASO 1 ── api-client funciones NC/ND (independiente, CRÍTICO)
  PASO 2 ── doc-preview notas (depende de PASO 1)
  PASO 3 ── listas notas inline actions (depende de PASO 1)

EJE B (Libros Electrónicos) — Prioridad MEDIA, paralelo con EJE A
  PASO 4 ── api-client libros (independiente)
  PASO 5 ── vista HTML libros (depende de PASO 4)

EJE C (Robustez) — Prioridad BAJA
  PASO 6 ── logout/cambiarEmpresa (independiente)
```

**Ruta crítica:** PASO 1 → PASO 2 → PASO 3

---

## PASO 1 — api-client.js: Funciones faltantes NC/ND {#paso-1}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Agregadas 4 funciones: `actualizarNotaCreditoBorrador`, `eliminarNotaCreditoBorrador`, `actualizarNotaDebitoBorrador`, `eliminarNotaDebitoBorrador`.

### Diagnóstico

El swagger define endpoints `actualizarNotaCreditoBorrador` (PUT), `eliminarNotaCreditoBorrador` (DELETE) para NC, y los equivalentes para ND. El backend los implementa. Pero `api-client.js` **NO tiene** estas funciones.

| Función faltante | HTTP | Path | operationId |
|-----------------|------|------|-------------|
| `actualizarNotaCreditoBorrador` | PUT | `/nota_credito?notaCreditoId=X` | actualizarNotaCreditoBorrador |
| `eliminarNotaCreditoBorrador` | DELETE | `/nota_credito?notaCreditoId=X` | eliminarNotaCreditoBorrador |
| `actualizarNotaDebitoBorrador` | PUT | `/nota_debito?notaDebitoId=X` | actualizarNotaDebitoBorrador |
| `eliminarNotaDebitoBorrador` | DELETE | `/nota_debito?notaDebitoId=X` | eliminarNotaDebitoBorrador |

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/api-client.js` | Agregar 4 funciones + exponerlas en return |

---

## PASO 2 — doc-preview.js: Acciones completas para notas {#paso-2}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Bloque `else` expandido con switch por estado: editar, corregir, ver detalle, eliminar.  
> `eliminar()` refactorizado para despachar NC/ND vía `eliminarNotaCreditoBorrador`/`eliminarNotaDebitoBorrador`.

### Diagnóstico

Actualmente para `nota_credito` y `nota_debito`, el `else` block (L459-465) solo muestra "Editar" para BORRADOR/CREADO. Faltan:
- 🗑️ Eliminar (para BORRADOR)
- 👁️ Ver detalle (para estados no editables)
- Validar/Re-validar (si existe endpoint de validación para notas)

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/doc-preview.js` | Expandir bloque `else` para notas con acciones por estado |
| 2 | `static/js/doc-preview.js` | Actualizar `eliminar()` para soportar notas |

---

## PASO 3 — Listas notas: Acciones inline {#paso-3}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Botones inline 🗑️ Eliminar + ✏️ Editar para borradores en `notas-credito.html` y `notas-debito.html`.  
> `deleteDoc()` implementado con `event.stopPropagation()` para evitar abrir preview al clickear.

### Diagnóstico

Las vistas `notas-credito.html` y `notas-debito.html` muestran cards clickeables que abren DocPreview. Pero no tienen acciones rápidas inline (eliminar borrador sin abrir preview).

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `templates/notas-credito.html` | Agregar botón eliminar en cards de borradores |
| 2 | `templates/notas-debito.html` | Agregar botón eliminar en cards de borradores |

---

## PASO 4 — api-client.js: Funciones Libros Electrónicos {#paso-4}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Agregadas `generarLibroElectronico(payload)` y `listarLibrosElectronicos(companyId, tipo, periodo, page, size)`.  
> Expuestas en return del módulo ApiClient.

### Diagnóstico

El swagger define `generarLibroElectronico` (POST) y `listarLibrosElectronicos` (GET). El backend los implementa. No hay funciones JS.

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/api-client.js` | Agregar `generarLibroElectronico()` + `listarLibrosElectronicos()` |

---

## PASO 5 — Vista frontend: libros-electronicos.html {#paso-5}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> Creada vista `libros-electronicos.html` con selectores de tipo/periodo/formato + botón generar + lista de libros.  
> Creado `LibrosElectronicosViewController.java` con ruta `/libros-electronicos`.  
> Card "Reportes" en menú reemplazada por "Libros Electrónicos" (activa, enlace a vista).

### Diagnóstico

No existe vista para libros electrónicos. Crear vista con:
- Selector de periodo (mes/año)
- Selector de tipo (ventas, compras, anexo)
- Botón generar
- Lista de libros generados

### Archivos a crear/modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `templates/libros-electronicos.html` | Crear vista completa |
| 2 | `view/ViewController.java` | Agregar ruta `/libros-electronicos` |
| 3 | `templates/menu.html` | Agregar enlace al menú |

---

## PASO 6 — logout / cambiarEmpresa {#paso-6}
> **COMPLETADO** — 2026-04-08 · BUILD SUCCESS  
> `logout()` en `navigation.js` ahora llama `_clearAllEtiaState()` que limpia todos los `etia_*` keys de localStorage, sessionStorage, y resetea estado in-memory de ETIA.  
> `cambiarEmpresa()` en `app-state.js` reutiliza `_clearAllEtiaState()` con preservación del tema.

### Diagnóstico

`navigation.js` solo limpia localStorage en logout. `ETIA.cambiarEmpresa()` cambia empresa sin invalidar nada en backend. No hay endpoint de logout.

### Solución

Crear limpieza más robusta: limpiar todos los caches de ETIA, limpiar sessionStorage, y agregar manejo de estado al cambiar empresa.

### Archivos a modificar
| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `static/js/navigation.js` | Mejorar logout con limpieza completa |
| 2 | `static/js/app-state.js` | Mejorar cambiarEmpresa con reset de estado |

---

## RESUMEN DE ESFUERZO

| Paso | Descripción | Esfuerzo | Riesgo |
|------|------------|----------|--------|
| PASO 1 | api-client NC/ND funciones | 🟢 20 min | Bajo |
| PASO 2 | doc-preview notas completo | 🟡 1-1.5h | Medio |
| PASO 3 | Listas notas inline actions | 🟢 30 min | Bajo |
| PASO 4 | api-client libros electrónicos | 🟢 15 min | Bajo |
| PASO 5 | Vista libros electrónicos | 🟡 1.5-2h | Medio |
| PASO 6 | logout/cambiarEmpresa robustez | 🟢 30 min | Bajo |

**Total estimado: ~4-5 horas**









