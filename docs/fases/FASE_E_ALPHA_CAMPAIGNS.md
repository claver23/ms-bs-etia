# 📈 FASE E — Alpha Campaigns (M2)

> **Módulo:** Alpha Campaigns  
> **Paquete:** `com.etlions.ms_bs_etia.campaign`  
> **Endpoint base:** `/api/v1/campaigns`  
> **Prioridad:** 🟡 Alta  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA), FASE D (Radar — opcional para audiencia externa)  
> **Puede paralelizarse con:** FASE C, F, G, H (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Campañas publicitarias con ROI contable. Lifecycle completo: DRAFT → ACTIVE → PAUSED → ENDED. Perfilamiento de audiencia cruzando clientes propios + leads del Radar. Tracking de conversiones (venta ↔ campaña). Mock de plataforma publicitaria. Reportes exportables a Excel.

---

## 📋 Tareas

### TE-01 — Modelo de datos

```sql
CREATE TABLE campaigns (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    codigo VARCHAR(50) UNIQUE,           -- CAMP-20260409-0001
    nombre VARCHAR(300) NOT NULL,
    descripcion TEXT,
    product_id VARCHAR(255),             -- Producto/servicio a promocionar
    status VARCHAR(30) DEFAULT 'DRAFT',  -- DRAFT, ACTIVE, PAUSED, ENDED
    budget DECIMAL(12,2),                -- Presupuesto
    budget_spent DECIMAL(12,2) DEFAULT 0,
    fecha_inicio DATE,
    fecha_fin DATE,
    ubigeo_target VARCHAR(10),           -- Ubigeo objetivo
    radius_km INTEGER,
    platform_ref VARCHAR(255),           -- ID externo (futuro: Google Ads, Meta)
    created_by VARCHAR(255),             -- user_id
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

CREATE TABLE campaign_audiences (
    id VARCHAR(255) PRIMARY KEY,
    campaign_id VARCHAR(255) NOT NULL,
    tipo VARCHAR(30),                    -- INTERNA (clientes propios), EXTERNA (radar leads)
    estimated_size INTEGER,
    filtros_json TEXT,                   -- JSON con criterios de filtro
    fecha_creacion TIMESTAMP
);

CREATE TABLE campaign_metrics (
    id VARCHAR(255) PRIMARY KEY,
    campaign_id VARCHAR(255) NOT NULL,
    fecha DATE,
    impressions INTEGER DEFAULT 0,
    clicks INTEGER DEFAULT 0,
    conversions INTEGER DEFAULT 0,
    cost DECIMAL(10,2) DEFAULT 0,
    revenue DECIMAL(10,2) DEFAULT 0,
    fecha_creacion TIMESTAMP
);

CREATE TABLE campaign_conversions (
    id VARCHAR(255) PRIMARY KEY,
    campaign_id VARCHAR(255) NOT NULL,
    document_id VARCHAR(255) NOT NULL,   -- factura_id o boleta_id
    document_type VARCHAR(20),           -- FACTURA, BOLETA
    sales_amount DECIMAL(12,2),
    client_id VARCHAR(255),
    fecha_conversion TIMESTAMP
);
```

---

### TE-02 — Endpoints REST

```yaml
# ==================== ALPHA CAMPAIGNS ====================
/campaigns:
  post:
    summary: "Crear campaña (borrador)"
    operationId: crearCampana
    tags: [campaign]
  get:
    summary: "Listar campañas"
    operationId: listarCampanas
    tags: [campaign]

/campaigns/{campaignId}:
  get:
    summary: "Obtener campaña por ID"
    operationId: obtenerCampana
    tags: [campaign]
  put:
    summary: "Actualizar campaña"
    operationId: actualizarCampana
    tags: [campaign]

/campaigns/{campaignId}/launch:
  post:
    summary: "Lanzar campaña (DRAFT → ACTIVE)"
    operationId: lanzarCampana
    tags: [campaign]

/campaigns/{campaignId}/pause:
  post:
    summary: "Pausar campaña (ACTIVE → PAUSED)"
    operationId: pausarCampana
    tags: [campaign]

/campaigns/{campaignId}/end:
  post:
    summary: "Finalizar campaña (→ ENDED)"
    operationId: finalizarCampana
    tags: [campaign]

/campaigns/{campaignId}/metrics:
  get:
    summary: "Obtener métricas de campaña"
    operationId: obtenerMetricasCampana
    tags: [campaign]

/campaigns/track-conversion:
  post:
    summary: "Asociar venta a campaña"
    operationId: trackConversion
    tags: [campaign]
    requestBody:
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/TrackConversionDto'
          example:
            campaignId: "camp_001"
            documentId: "fac_123"
            documentType: "FACTURA"

/campaigns/audience-suggest:
  get:
    summary: "Sugerir audiencia para producto y ubigeo"
    operationId: sugerirAudiencia
    tags: [campaign]
    parameters:
      - name: companyId
        in: query
        required: true
      - name: productId
        in: query
        required: true
      - name: ubigeoCode
        in: query

/campaigns/{campaignId}/roi:
  get:
    summary: "Calcular ROI de campaña"
    operationId: calcularRoiCampana
    tags: [campaign]

/campaigns/export:
  get:
    summary: "Exportar reporte de campañas a Excel"
    operationId: exportarCampanasExcel
    tags: [campaign]
```

---

### TE-03 — Servicio de sugerencia de audiencia (Audience Cannon)

```java
@Service
public class AudienceBuilderServiceImpl implements AudienceBuilderService {
    
    public CampaignAudienceResult sugerirAudiencia(String companyId, String productId, String ubigeo) {
        // 1. Audiencia interna: clientes que compraron producto similar
        List<ClientEntity> clientesInternos = facturaRepo
            .findClientsByProductAndCompany(productId, companyId);
        
        // 2. Audiencia externa: leads del radar con categoría similar
        List<PotentialBusinessContact> leadsExternos = radarRepo
            .findByUbigeoAndCategory(ubigeo, mapProductToCategory(productId));
        
        return CampaignAudienceResult.builder()
            .estimatedSizeInternal(clientesInternos.size())
            .estimatedSizeExternal(leadsExternos.size())
            .totalEstimated(clientesInternos.size() + leadsExternos.size())
            .build();
    }
}
```

---

### TE-04 — Mock Ad Platform Service

```java
@Service
@ConditionalOnProperty(name = "campaign.mode", havingValue = "mock", matchIfMissing = true)
public class MockAdPlatformService implements AdPlatformService {
    
    @Override
    public CampaignMetric generateDailyMetrics(Campaign campaign) {
        // Genera métricas simuladas basadas en budget y audiencia
        int impressions = (int)(campaign.getBudget() * 10);   // $1 = 10 impressions
        int clicks = impressions / 20;                         // 5% CTR
        int conversions = clicks / 10;                         // 10% conversion
        BigDecimal cost = campaign.getBudget()
            .multiply(BigDecimal.valueOf(0.05));               // 5% del budget diario
        
        return CampaignMetric.builder()
            .impressions(impressions)
            .clicks(clicks)
            .conversions(conversions)
            .cost(cost)
            .build();
    }
}
```

---

### TE-05 — Cálculo de ROI

```
ROI = (sum(sales_amount) - campaign_cost) / campaign_cost × 100

Donde:
- sales_amount = suma de facturas/boletas asociadas a la campaña (campaign_conversions)
- campaign_cost = budget_spent de la campaña (campaign_metrics.cost acumulado)
```

---

### TE-06 — Validaciones de negocio

| Validación | Error |
|------------|-------|
| No lanzar sin audiencia definida | `"La campaña debe tener al menos una audiencia definida"` |
| `fecha_fin > fecha_inicio` | `"La fecha fin debe ser posterior a la fecha inicio"` |
| `budget > 0` | `"El presupuesto debe ser mayor a 0"` |
| Audiencia mínima 10 prospectos | `"La audiencia debe incluir al menos 10 prospectos"` |
| Solo plan PRO/BUSINESS | `@PreAuthorize("hasAnyRole('PRO','BUSINESS','ADMIN_ETIA')")` |
| Campaña pertenece a company | `WHERE company_id = :currentCompanyId` |

---

### TE-07 — Exportación a Excel (Apache POI)

**Dependencia:**
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

**Reporte:** Rendimiento de campañas (por producto, por región, por período).  
**Formato:** `.xlsx` con headers, totales y gráfico de barras básico.

---

### TE-08 — Vista Alpha Campaigns

**Nueva vista:** `templates/campaigns.html`  
**View Controller:** `CampaignsViewController.java` (`GET /campaigns`)

```
Vista Campaigns
├── Header: "Alpha Campaigns" + [+ Nueva Campaña]
├── Lista de campañas (cards):
│   ├── 📊 Nombre + Código
│   ├── Status badge (DRAFT/ACTIVE/PAUSED/ENDED)
│   ├── Budget: S/ 5,000 / S/ 2,340 gastado
│   ├── ROI: +45.2%
│   ├── Conversiones: 12
│   └── [Lanzar] [Pausar] [Ver Métricas] [Exportar]
├── Dashboard mini:
│   ├── Total campañas activas
│   ├── ROI promedio
│   ├── Producto del mes (más ventas vía campaña)
│   └── Gasto total en campañas
└── En la venta (wizard/POS): Dropdown "¿Viene de campaña?" con lista de activas
```

---

### TE-09 — Integración con ventas

En el wizard y POS, agregar campo opcional:
- **Dropdown:** "¿Esta venta proviene de una campaña?" → lista de campañas ACTIVE
- Al guardar la factura/boleta con `campaignId`, automáticamente se crea `CampaignConversion`

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/campaign_lifecycle.feature`

```gherkin
Feature: Alpha Campaigns — Lifecycle completo

  Scenario: Crear campaña borrador
    Given soy usuario PRO de empresa "ETLIONS"
    When creo una campaña para producto "Consultoría TI" con budget S/ 5000
    Then la campaña se crea en status DRAFT
    And tiene código "CAMP-20260409-0001"

  Scenario: Lanzar campaña
    Given una campaña DRAFT con audiencia de 50 prospectos
    When lanzo la campaña
    Then el status cambia a ACTIVE
    And se generan métricas mock diarias

  Scenario: Lanzar campaña sin audiencia rechazado
    Given una campaña DRAFT sin audiencia definida
    When intento lanzar la campaña
    Then recibo error 400 "La campaña debe tener al menos una audiencia definida"

  Scenario: Track de conversión
    Given una campaña ACTIVE "Promo Consultoría"
    And una factura nueva para cliente "INVERSIONES TECH"
    When marco la factura como "viene de campaña"
    Then se crea un CampaignConversion
    And el ROI se recalcula

  Scenario: Usuario FREE no puede crear campañas
    Given soy usuario FREE
    When intento crear una campaña
    Then recibo error 403 "Módulo no incluido en tu plan"

  Scenario: Exportar reporte a Excel
    Given campañas con métricas del mes
    When exporto el reporte
    Then recibo un archivo .xlsx con datos de todas las campañas

  Scenario: Pausar campaña activa
    Given una campaña ACTIVE
    When pauso la campaña
    Then el status cambia a PAUSED
    And las métricas dejan de generarse

  Scenario: Reanudar campaña pausada
    Given una campaña PAUSED
    When lanzo la campaña nuevamente
    Then el status cambia a ACTIVE
    And las métricas continúan (no se resetean)

  Scenario: Budget excedido pausa automáticamente
    Given una campaña ACTIVE con budget S/ 1,000
    And budget_spent = S/ 1,000 (agotado)
    When el CronJob genera métricas del día
    Then la campaña se pausa automáticamente con nota "Presupuesto agotado"

  Scenario: Dos campañas activas para el mismo producto
    Given campaña A ACTIVE para "Consultoría TI"
    And campaña B ACTIVE para "Consultoría TI"
    When una venta se marca como campaña A
    Then la conversión se atribuye SOLO a campaña A

  Scenario: Conversión tardía post ENDED (gracia 7 días)
    Given una campaña ENDED hace 3 días
    When se registra una venta vinculada a esa campaña
    Then se acepta la conversión (dentro del período de gracia de 7 días)

  Scenario: Conversión tardía fuera de gracia rechazada
    Given una campaña ENDED hace 10 días
    When se intenta vincular una venta
    Then recibo error 400 "Campaña finalizada fuera del período de gracia"
```

---

### TE-10 — CronJob para métricas mock (decisión aplicada de PREGUNTAS_DEFINICION E1)

**Decisión:** CronJob diario a medianoche para campañas ACTIVE.

```java
@Component
@RequiredArgsConstructor
public class CampaignMetricsCronJob {

    private final CampaignRepository campaignRepo;
    private final AdPlatformService adPlatform;
    private final CampaignMetricsRepository metricsRepo;

    @Scheduled(cron = "0 0 0 * * *") // Medianoche
    public void generateDailyMetrics() {
        List<CampaignEntity> activeCampaigns = campaignRepo.findByStatus("ACTIVE");
        for (var campaign : activeCampaigns) {
            // Verificar si budget agotado → pausar
            if (campaign.getBudgetSpent().compareTo(campaign.getBudget()) >= 0) {
                campaign.setStatus("PAUSED");
                campaignRepo.save(campaign);
                continue;
            }
            CampaignMetric metric = adPlatform.generateDailyMetrics(campaign);
            metricsRepo.save(metric);
            campaign.setBudgetSpent(campaign.getBudgetSpent().add(metric.getCost()));
            campaignRepo.save(campaign);
        }
    }
}
```

---

## ✅ Criterios de Aceptación

- [ ] CRUD completo de campañas
- [ ] Lifecycle: DRAFT → ACTIVE → PAUSED → ENDED
- [ ] Audiencia sugerida (clientes + leads del radar)
- [ ] Mock de métricas diarias
- [ ] Track de conversiones (venta ↔ campaña)
- [ ] ROI calculado correctamente
- [ ] Exportación Excel funcional
- [ ] Solo PRO/BUSINESS pueden crear campañas
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `pom.xml` | 🔧 +poi-ooxml (Apache POI para Excel) |
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag campaign, +endpoints, +schemas |
| `src/main/java/.../campaign/**` | 🆕 Paquete completo (~15 archivos) |
| `src/main/java/.../campaign/openapi/CampaignOpenApi.java` | 🆕 |
| `src/main/java/.../campaign/service/CampaignService.java` | 🆕 |
| `src/main/java/.../campaign/service/impl/CampaignServiceImpl.java` | 🆕 |
| `src/main/java/.../campaign/service/AudienceBuilderServiceImpl.java` | 🆕 |
| `src/main/java/.../campaign/service/MockAdPlatformService.java` | 🆕 |
| `src/main/java/.../campaign/service/CampaignMetricsCronJob.java` | 🆕 |
| `src/main/java/.../campaign/mapper/CampaignMapper.java` | 🆕 |
| `src/main/java/.../campaign/persistence/CampaignEntity.java` | 🆕 |
| `src/main/java/.../campaign/persistence/CampaignRepository.java` | 🆕 |
| `src/main/java/.../campaign/persistence/CampaignAudienceEntity.java` | 🆕 |
| `src/main/java/.../campaign/persistence/CampaignMetricEntity.java` | 🆕 |
| `src/main/java/.../campaign/persistence/CampaignConversionEntity.java` | 🆕 |
| `src/main/java/.../view/CampaignsViewController.java` | 🆕 |
| `src/main/resources/templates/campaigns.html` | 🆕 |
| `src/main/resources/static/js/campaign-view.js` | 🆕 |
| `src/main/resources/data.sql` | 🔧 +campañas demo, +audiencias, +métricas |
| `src/test/resources/features/campaign_lifecycle.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Decisión E1 (PREGUNTAS_DEFINICION):** CronJob diario a medianoche con `@Scheduled`. Genera métricas mock para campañas ACTIVE. Si budget agotado → pausa automática.
- **Decisión E2 (PREGUNTAS_DEFINICION):** Tracking manual por dropdown en wizard/POS. UTM es futuro.
- **Decisión E3 (PREGUNTAS_DEFINICION):** El costo se vincula al centro de costo "Publicidad" (futuro libro de compras).
- **Período de gracia:** Conversiones tardías aceptadas hasta 7 días después de ENDED. Pasado ese período → error.
- **MockAdPlatformService:** Usa `@ConditionalOnProperty(name = "campaign.mode", havingValue = "mock", matchIfMissing = true)`. Genera métricas simuladas proporcionales al budget.
- **Excel export:** Usa Apache POI (mismo que FASE J). El servicio `ExcelExportService` se crea aquí y se reutiliza en J.
- **Solo PRO/BUSINESS:** Usar `@PreAuthorize("hasAnyRole('PRO','BUSINESS','ADMIN_ETIA')")` en el controller.
- **Company isolation:** Filtrar campañas por `company_id` del JWT en todos los endpoints.
- **Protección de producto:** No se puede eliminar un producto referenciado por una campaña ACTIVE o PAUSED. Agregar validación cross-module en el servicio de eliminación de productos.
- **`fecha_actualizacion` para gracia post-ENDED:** La gracia de 7 días se calcula desde `fecha_actualizacion` (momento del cambio a ENDED), no desde `fecha_fin` planificada.

---

*Fase E — Alpha Campaigns. Cada peso invertido debe poder rastrearse hasta una venta.*



