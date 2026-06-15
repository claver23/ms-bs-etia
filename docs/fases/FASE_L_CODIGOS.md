# 🔢 FASE L — Códigos Correlativos (M11)

> **Módulo:** Códigos  
> **Paquete:** `com.etlions.ms_bs_etia.codigos`  
> **Endpoint base:** `/api/v1/codigos`  
> **Prioridad:** 🟢 Media  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA)  
> **Puede paralelizarse con:** Todas (post FASE B). **Se recomienda ejecutar temprano** porque todas las fases C-K usan códigos correlativos.  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Centralizar la generación de códigos correlativos para TODAS las entidades del sistema: clientes, productos, proveedores, campañas, deals, órdenes de compra, etc. Panel de gestión de secuencias.

---

## 📋 Tareas

### TL-01 — Modelo de datos

```sql
CREATE TABLE sequence_control (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,    -- CLIENTE, PRODUCTO, PROVEEDOR, CAMPANA, DEAL, OC, POS
    prefix VARCHAR(20) NOT NULL,         -- CLI, PRD, SUP, CAMP, DEAL, OC, POS
    last_number INTEGER DEFAULT 0,
    format_pattern VARCHAR(100),         -- {PREFIX}-{YYYYMMDD}-{CORRELATIVO}
    padding INTEGER DEFAULT 4,           -- Dígitos del correlativo
    UNIQUE(company_id, entity_type)
);
```

**Formato estándar:** `{PREFIJO}-{YYYYMMDD}-{CORRELATIVO}`  
Ejemplo: `CLI-20260409-0001`, `CAMP-20260409-0015`, `OC-20260409-0003`

### TL-02 — Servicio centralizado

```java
@Service
public class CorrelativeCodeServiceImpl implements CorrelativeCodeService {
    
    @Transactional
    public String generateCode(String companyId, String entityType) {
        var seq = sequenceRepo.findByCompanyIdAndEntityType(companyId, entityType)
            .orElseGet(() -> createDefault(companyId, entityType));
        
        seq.setLastNumber(seq.getLastNumber() + 1);
        sequenceRepo.save(seq);
        
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String number = String.format("%0" + seq.getPadding() + "d", seq.getLastNumber());
        
        return seq.getPrefix() + "-" + date + "-" + number;
    }
}
```

### TL-03 — Endpoints

```yaml
/codigos/sequences:
  get: { operationId: listarSecuencias, params: [companyId] }

/codigos/sequences/{entityType}:
  get: { operationId: obtenerSecuencia }
  put: { operationId: actualizarSecuencia }

/codigos/generate:
  post:
    summary: "Generar código para una entidad"
    operationId: generarCodigo
    requestBody:
      schema:
        properties:
          companyId: { type: string }
          entityType: { type: string, enum: [CLIENTE, PRODUCTO, PROVEEDOR, CAMPANA, DEAL, OC, POS] }
```

### TL-04 — Vista Panel de Códigos

```
Vista Códigos
├── Tabla de secuencias:
│   ├── Tipo Entidad | Prefijo | Último Nro | Formato | Acciones
│   ├── CLIENTE       | CLI     | 0010       | CLI-{DATE}-{N}  | [Editar]
│   ├── CAMPANA       | CAMP    | 0003       | CAMP-{DATE}-{N} | [Editar]
│   └── ...
└── [Generar código manual] (raro pero posible)
```

---

## 🧪 Escenarios Gherkin

```gherkin
Feature: Códigos correlativos

  Scenario: Generar código de cliente
    Given secuencia CLIENTE con último número 10
    When genero un nuevo código
    Then recibo "CLI-20260409-0011"

  Scenario: Múltiples entidades tienen secuencias independientes
    Given secuencia CLIENTE en 10 y CAMPANA en 3
    When genero código de CAMPANA
    Then recibo "CAMP-20260409-0004" (no afecta CLIENTE)

  Scenario: Secuencia se crea automáticamente si no existe
    Given no existe secuencia para PROVEEDOR en empresa "cmp-001"
    When genero código de PROVEEDOR
    Then se crea la secuencia con last_number=1
    And recibo "SUP-20260409-0001"

  Scenario: Thread safety — generación concurrente
    Given 10 requests simultáneos para generar código CLIENTE
    When todos se procesan
    Then los 10 códigos son únicos (no hay duplicados)
    And last_number = original + 10

  Scenario: Actualizar prefijo de secuencia
    Given secuencia CLIENTE con prefijo "CLI"
    When cambio el prefijo a "CLT"
    When genero un nuevo código
    Then recibo "CLT-20260409-XXXX"

  Scenario: Reset de secuencia (solo admin)
    Given secuencia CAMPANA con last_number = 50
    When el admin resetea la secuencia a 0
    Then last_number = 0
    And el siguiente código es "CAMP-20260409-0001"

  Scenario: Código de entidad existente no se regenera
    Given un cliente con código "CLI-20260409-0005"
    When consulto ese cliente
    Then su código sigue siendo "CLI-20260409-0005" (inmutable una vez asignado)

  Scenario: Diferentes empresas tienen secuencias independientes
    Given empresa "cmp-001" con secuencia CLIENTE en 10
    And empresa "cmp-002" con secuencia CLIENTE en 5
    When cmp-002 genera código CLIENTE
    Then recibo "CLI-20260409-0006"
    And la secuencia de cmp-001 no se afecta
```

---

## ✅ Criterios de Aceptación

- [ ] Generación centralizada de códigos para TODAS las entidades
- [ ] Secuencias independientes por empresa (company isolation)
- [ ] Secuencias independientes por tipo de entidad
- [ ] Auto-creación de secuencia si no existe
- [ ] Thread safety con `@Transactional` y `@Lock(PESSIMISTIC_WRITE)`
- [ ] Prefijo y padding configurables por empresa
- [ ] Vista de panel de secuencias para administradores
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag codigos, +endpoints, +schemas |
| `src/main/java/.../codigos/**` | 🆕 Paquete completo (~7 archivos) |
| `src/main/java/.../codigos/openapi/CodigosOpenApi.java` | 🆕 |
| `src/main/java/.../codigos/service/CorrelativeCodeService.java` | 🆕 |
| `src/main/java/.../codigos/service/impl/CorrelativeCodeServiceImpl.java` | 🆕 |
| `src/main/java/.../codigos/persistence/SequenceControlEntity.java` | 🆕 |
| `src/main/java/.../codigos/persistence/SequenceControlRepository.java` | 🆕 |
| `src/main/java/.../view/CodigosViewController.java` | 🆕 |
| `src/main/resources/templates/codigos.html` | 🆕 |
| `src/main/resources/data.sql` | 🔧 +secuencias demo para cada entity_type |
| `src/test/resources/features/correlative_codes.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Thread safety:** Usar `@Lock(LockModeType.PESSIMISTIC_WRITE)` en el query de `findByCompanyIdAndEntityType` para evitar duplicados en concurrencia.
- **Este servicio es usado por TODAS las fases posteriores:** Campaigns (CAMP-), Supply Chain (OC-, SUP-), CRM (DEAL-), POS (POS-). Idealmente ejecutar esta fase temprano.
- **No reemplaza correlativos SUNAT:** Las series de factura (F001, B001) y números correlativos SUNAT se siguen manejando con el sistema de `correlativos` existente. Este módulo es para entidades internas.

---

*Fase L — Códigos. Todo tiene un nombre. Todo tiene un número.*



