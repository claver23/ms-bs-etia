# ⚔️ FASE H — Adjuster / The Guillotine — Anulación con Slider (M7)

> **Módulo:** Adjuster  
> **Feature UI:** The Guillotine  
> **Paquete:** `com.etlions.ms_bs_etia.adjuster`  
> **Endpoint base:** `/api/v1/adjuster`  
> **Prioridad:** 🟡 Alta  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA)  
> **Puede paralelizarse con:** FASE C, D, E, F, G (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Reemplazar la anulación binaria actual con un sistema de anulación parcial mediante slider. Si el setting "Anular 100% siempre" está activo (por defecto), se anula completo. Si se desactiva, aparece el slider para elegir %. Genera comunicación de baja o nota de crédito según el caso. Experiencia creativa e impactante ("The Guillotine").

---

## 📋 Tareas

### TH-01 — Setting de usuario: "Anulación completa por defecto"

**Tabla `user_preferences`:**
```sql
INSERT INTO user_preferences (id, company_id, clave, valor)
VALUES ('pref-adj-001', 'cmp-demo-001', 'anulacion_completa_default', 'true');
```

- `true` (default): Al anular, se anula 100% directamente
- `false`: Aparece The Guillotine (slider de porcentaje)

**Endpoint:** Ya existe `PUT /api/v1/user_preference` para guardar preferencias.

---

### TH-02 — Modelo de datos

```sql
CREATE TABLE cancelation_requests (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    document_id VARCHAR(255) NOT NULL,
    document_type VARCHAR(30),           -- FACTURA, BOLETA, NOTA_CREDITO
    document_serie VARCHAR(10),
    document_numero INTEGER,
    percentage DECIMAL(5,2),             -- 0.01 a 100.00
    original_amount DECIMAL(12,2),
    cancelation_amount DECIMAL(12,2),    -- original * percentage / 100
    remaining_amount DECIMAL(12,2),      -- original - sum(cancelaciones previas) - esta
    motivo_codigo VARCHAR(10),           -- Catálogo SUNAT de motivos
    motivo_descripcion VARCHAR(500),
    notas TEXT,
    tipo_resultado VARCHAR(30),          -- COMUNICACION_BAJA (100%), NOTA_CREDITO (<100%)
    resultado_document_id VARCHAR(255),  -- ID de la NC generada o la com. de baja
    status VARCHAR(30),                  -- PENDIENTE, PROCESADA, RECHAZADA
    created_by VARCHAR(255),
    fecha_creacion TIMESTAMP
);
```

---

### TH-03 — Endpoints REST

```yaml
# ==================== ADJUSTER ====================
/adjuster/documents/{documentId}/cancelable-amount:
  get:
    summary: "Obtener saldo pendiente de cancelación"
    operationId: obtenerSaldoCancelable
    tags: [adjuster]
    parameters:
      - name: documentId
        in: path
        required: true
    responses:
      "200":
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CancelableAmountData'
            example:
              documentId: "fac_001"
              documentType: "FACTURA"
              serie: "F001"
              numero: 1
              originalAmount: 1180.00
              previousCancelations: 354.00
              remainingAmount: 826.00
              maxCancelablePercentage: 70.0

/adjuster/documents/{documentId}/cancel:
  post:
    summary: "Crear solicitud de cancelación (parcial o total)"
    operationId: cancelarDocumento
    tags: [adjuster]
    requestBody:
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/CancelDocumentDto'
          example:
            percentage: 30.0
            motivoCodigo: "01"
            motivoDescripcion: "Descuento por volumen no aplicado"
            notas: "Cliente solicita ajuste"
    responses:
      "200":
        description: "Cancelación procesada"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CancelationResultDto'

/adjuster/requests:
  get:
    summary: "Listar solicitudes de cancelación"
    operationId: listarCancelaciones
    tags: [adjuster]

/adjuster/requests/{requestId}:
  get:
    summary: "Obtener detalle de cancelación"
    operationId: obtenerCancelacion
    tags: [adjuster]
```

---

### TH-04 — Lógica de negocio

```java
@Service
public class AdjusterServiceImpl implements AdjusterService {

    public CancelationResult cancelarDocumento(String documentId, CancelDocumentDto dto) {
        // 1. Obtener documento
        var doc = findDocument(documentId);
        
        // 2. Validar que está en estado cancelable (EMITIDA, ACEPTADA)
        validateCancelableState(doc);
        
        // 3. Calcular saldo disponible
        BigDecimal previousCancelations = cancelationRepo
            .sumCancelationAmountByDocumentId(documentId);
        BigDecimal remaining = doc.getTotal().subtract(previousCancelations);
        
        // 4. Calcular monto de esta cancelación
        BigDecimal cancelAmount = remaining
            .multiply(BigDecimal.valueOf(dto.getPercentage()))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // 5. Determinar tipo de resultado
        String tipoResultado;
        if (dto.getPercentage().compareTo(new BigDecimal("100")) == 0) {
            tipoResultado = "COMUNICACION_BAJA";
            // Generar XML de comunicación de baja
        } else {
            tipoResultado = "NOTA_CREDITO";
            // Generar nota de crédito por el monto parcial
        }
        
        // 6. Persistir
        var request = CancelationRequestEntity.builder()
            .documentId(documentId)
            .percentage(dto.getPercentage())
            .originalAmount(doc.getTotal())
            .cancelationAmount(cancelAmount)
            .remainingAmount(remaining.subtract(cancelAmount))
            .motivoCodigo(dto.getMotivoCodigo())
            .tipoResultado(tipoResultado)
            .status("PROCESADA")
            .build();
        
        return buildResult(request);
    }
}
```

**Regla clave:**
- `percentage == 100%` → Comunicación de Baja (anulación total SUNAT)
- `percentage < 100%` → Nota de Crédito parcial (ajuste SUNAT)

---

### TH-05 — UI: The Guillotine (Modal con Slider)

**Flujo UX:**

```
Usuario hace click en "Anular" en documento emitido
    ↓
¿Setting "anulación_completa_default" == true?
    → SÍ: Modal directo "¿Desea crear una comunicación de baja?" [Sí/No]
    → NO: Abre The Guillotine ↓

┌─────────────────────────────────────────────────────┐
│              ⚔️ THE GUILLOTINE                       │
│                                                     │
│  Documento: F001-00000001                           │
│  Monto original: S/ 1,180.00                        │
│  Ya afectado: S/ 354.00 (30%)                       │
│  Saldo disponible: S/ 826.00 (70%)                  │
│                                                     │
│  ┌─────────────────────────────────────────────┐    │
│  │  ═══════════════●══════════════════          │    │
│  │  0%            42%                   100%    │    │
│  └─────────────────────────────────────────────┘    │
│                                                     │
│  Monto a afectar: S/ 346.92 (42%)                   │
│  Saldo restante: S/ 479.08 (58%)                    │
│                                                     │
│  Motivo: [Dropdown catálogo SUNAT ▼]                │
│  Notas:  [________________________________]         │
│                                                     │
│  ⚠️ Esto generará una NOTA DE CRÉDITO por S/ 346.92 │
│  (Si 100% → Comunicación de Baja)                   │
│                                                     │
│  [Cancelar]                    [⚔️ Ejecutar]         │
└─────────────────────────────────────────────────────┘
```

**Slider CSS/JS:**
- Estilo tipo "control de volumen" (premium, oscuro)
- Color gradiente: verde (0%) → amarillo (50%) → rojo (100%)
- Tooltip flotante con monto en tiempo real
- Feedback háptico visual al mover
- Si el saldo ya fue afectado, el slider empieza desde 0% del saldo restante

---

### TH-06 — Integración con flujo existente

- En listas de facturas/boletas: hover muestra estado con color
- Al hacer hover en documento EMITIDO/ACEPTADO: aparece ícono ⚔️ "Anular"
- Click → abre The Guillotine o modal directo según setting
- Resultado (NC o Com. Baja) aparece vinculado al documento original

**Colores de estado en hover:**
| Estado | Color |
|--------|-------|
| BORRADOR | 🔵 Azul |
| VALIDADO | 🟣 Púrpura |
| ENVIADA | 🟡 Amarillo |
| ACEPTADA | 🟢 Verde |
| RECHAZADA | 🔴 Rojo |
| ANULADA | ⚫ Gris oscuro |
| CON_ERROR | 🟠 Naranja |

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/document_cancel_slider.feature`

```gherkin
Feature: Adjuster — Anulación de documentos con slider

  Scenario: Anulación total (100%) genera comunicación de baja
    Given factura ACEPTADA F001-00000001 por S/ 1,180.00
    And setting "anulación completa" activado
    When anulo al 100%
    Then se genera comunicación de baja
    And el documento queda con remaining_amount S/ 0

  Scenario: Anulación parcial (30%) genera nota de crédito
    Given factura ACEPTADA por S/ 1,000.00
    And setting "anulación completa" desactivado
    When muevo slider al 30%
    Then se genera nota de crédito por S/ 300.00
    And remaining_amount = S/ 700.00

  Scenario: Documento ya parcialmente afectado
    Given factura por S/ 1,000 con cancelación previa de S/ 300
    When consulto saldo cancelable
    Then remainingAmount = S/ 700
    And maxCancelablePercentage = 100% (del saldo restante)

  Scenario: No se puede anular documento BORRADOR
    Given factura en BORRADOR
    When intento anular
    Then error 400 "Solo documentos emitidos pueden ser anulados"

  Scenario: Slider con motivo obligatorio
    When intento cancelar sin motivo
    Then error 400 "El motivo de cancelación es obligatorio"

  Scenario: Múltiples cancelaciones parciales acumulativas
    Given factura por S/ 1,000.00
    When anulo 30% (S/ 300) y luego 40% del restante (S/ 280)
    Then remaining_amount = S/ 420.00
    And existen 2 registros en cancelation_requests

  Scenario: Intento anular más del saldo restante
    Given factura con remaining_amount S/ 200
    When intento anular por S/ 500 (percentage > 100% del restante)
    Then error 400 "El monto excede el saldo cancelable"

  Scenario: Documento completamente anulado no permite más cancelaciones
    Given factura con remaining_amount S/ 0 (ya anulada al 100%)
    When intento anular nuevamente
    Then error 400 "El documento ya está completamente anulado"

  Scenario: Anulación de boleta (flujo diferente a factura)
    Given boleta ACEPTADA por S/ 500
    When anulo al 100%
    Then se genera entrada en Resumen Diario de baja (no comunicación de baja individual)

  Scenario: Porcentaje mínimo de cancelación
    Given factura ACEPTADA por S/ 1,000.00
    When intento anular con percentage 0.001% (S/ 0.01)
    Then la cancelación se procesa correctamente (monto mínimo S/ 0.01)

  Scenario: Motivos de catálogo SUNAT
    When abro The Guillotine en una factura
    Then el dropdown de motivos muestra catálogo 09 SUNAT
    And cada motivo tiene código y descripción
```

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag adjuster, +endpoints, +schemas |
| `src/main/java/.../adjuster/**` | 🆕 Paquete completo (~10 archivos) |
| `src/main/java/.../adjuster/openapi/AdjusterOpenApi.java` | 🆕 |
| `src/main/java/.../adjuster/service/AdjusterService.java` | 🆕 |
| `src/main/java/.../adjuster/service/impl/AdjusterServiceImpl.java` | 🆕 |
| `src/main/java/.../adjuster/mapper/CancelationMapper.java` | 🆕 |
| `src/main/java/.../adjuster/persistence/CancelationRequestEntity.java` | 🆕 |
| `src/main/java/.../adjuster/persistence/CancelationRequestRepository.java` | 🆕 |
| `src/main/java/.../view/AdjusterViewController.java` | 🆕 (si hay vista standalone) |
| `src/main/resources/static/js/components/slider-component.js` | 🆕 (slider reusable) |
| `src/main/resources/static/js/guillotine.js` | 🆕 (modal The Guillotine) |
| `src/main/resources/static/css/guillotine.css` | 🆕 (estilo slider premium) |
| `src/main/resources/data.sql` | 🔧 +user_preferences demo, +cancelation_requests demo |
| `src/test/resources/features/document_cancel_slider.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Decisión H1 (PREGUNTAS_DEFINICION):** NC parcial se genera automáticamente con datos precargados. Usuario confirma antes de crear.
- **Decisión H2 (PREGUNTAS_DEFINICION):** Catálogo SUNAT fijo (Cat. 09). No personalizable.
- **Porcentaje sobre SALDO RESTANTE:** El `percentage` del slider se aplica sobre el `remainingAmount`, NO sobre el `originalAmount`. Ejemplo: factura S/1000, ya cancelados S/300, slider al 50% → cancela S/350 (50% de S/700 restante).
- **`user_preferences` es módulo preexistente:** El paquete `user_preference/` ya existe. No se crea tabla nueva. Solo se agrega la preferencia `anulacion_completa_default` como seed en `data.sql`.
- **Boletas vs Facturas — lógica diferenciada:**
  ```
  if (percentage == 100%) {
      if (documentType == FACTURA) → Comunicación de Baja (documento SUNAT individual)
      if (documentType == BOLETA)  → Entrada en Resumen Diario tipo "anulación"
  } else {
      → Nota de Crédito parcial (igual para factura y boleta)
  }
  ```
- **Nota de Crédito:** La NC parcial generada por Adjuster es un documento real que pasa por el flujo normal: BORRADOR → VALIDADO → EMITIDO → ACEPTADO.
- **Saldo acumulativo:** La tabla `cancelation_requests` permite múltiples cancelaciones parciales. El saldo restante se calcula como: `original - SUM(cancelation_amount) de requests PROCESADAS`.
- **Comunicación de Baja y envío SUNAT:** En v1, la comunicación de baja se genera como registro interno. El envío real a SUNAT (SOAP + firma digital) es fase futura.

---

## ✅ Criterios de Aceptación

- [ ] Setting toggle en user_preferences funcional
- [ ] Slider 0-100% con feedback visual en tiempo real
- [ ] 100% → comunicación de baja, <100% → nota de crédito
- [ ] Saldo acumulativo correcto (múltiples cancelaciones parciales)
- [ ] Colores de estado en hover de documentos
- [ ] Motivo obligatorio con catálogo SUNAT
- [ ] Compilación limpia + tests pasan

---

*Fase H — The Guillotine. Preciso. Calculado. Sin desperdicio.*



