# 🔍 FASE D — Radar de Mercado — Google Places (M3)

> **Módulo:** Radar de Mercado  
> **Paquete:** `com.etlions.ms_bs_etia.radar`  
> **Endpoint base:** `/api/v1/radar`  
> **Prioridad:** 🟡 Alta  
> **Dependencias:** FASE A (Core), FASE B (Security — COMPLETADA)  
> **Puede paralelizarse con:** FASE C, E, F, G, H (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Implementar búsqueda asistida de proveedores/clientes potenciales usando Google Places API (New). NO scraping masivo. Búsqueda bajo demanda con caché efímero de 30 días. Integración con ubigeo y catálogos SUNAT para perfilamiento.

---

## 📋 Tareas

### TD-01 — Dependencias y configuración Google Places API

**pom.xml:**
```xml
<dependency>
    <groupId>com.google.maps</groupId>
    <artifactId>google-maps-services</artifactId>
    <version>2.2.0</version>
</dependency>
```

**application.yaml:**
```yaml
google:
  places:
    api-key: ${GOOGLE_PLACES_API_KEY:demo-key}
    rate-limit:
      max-per-user-per-hour: 10
    cache-ttl-days: 30
```

---

### TD-02 — Modelo de datos

```sql
-- Caché de búsquedas (TTL 30 días)
CREATE TABLE exploration_cache (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    query_text VARCHAR(500),
    ubigeo_code VARCHAR(10),
    radius_km INTEGER,
    results_json TEXT,                   -- JSON array de resultados de Google
    result_count INTEGER,
    fecha_busqueda TIMESTAMP,
    fecha_expiracion TIMESTAMP           -- busqueda + 30 días
);

-- Negocios guardados por el usuario (leads)
CREATE TABLE potential_business_contacts (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    google_place_id VARCHAR(500),
    business_name VARCHAR(500),
    address VARCHAR(1000),
    ubigeo_inei VARCHAR(10),
    category VARCHAR(200),
    website_url VARCHAR(500),
    international_phone VARCHAR(50),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    origin VARCHAR(30) DEFAULT 'GOOGLE_RADAR',  -- GOOGLE_RADAR, MANUAL
    status VARCHAR(30) DEFAULT 'LEAD',           -- LEAD, PROVEEDOR, CLIENTE, EXCLUIDO
    excluded_from_search BOOLEAN DEFAULT FALSE,
    notas TEXT,
    fecha_guardado TIMESTAMP
);
```

---

### TD-03 — Endpoints REST

```yaml
# ==================== RADAR DE MERCADO ====================
/radar/search:
  post:
    summary: "Buscar negocios cercanos"
    operationId: buscarNegociosCercanos
    tags: [radar]
    requestBody:
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/RadarSearchDto'
          example:
            companyId: "cmp_20601234567"
            query: "Ferreterías"
            ubigeoCode: "150131"
            radiusKm: 10
    responses:
      "200":
        description: "Resultados de búsqueda"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RadarSearchResponseDto'
      "429":
        description: "Límite de búsquedas alcanzado (10/hora)"

/radar/business/{placeId}:
  get:
    summary: "Obtener detalles de un negocio"
    operationId: obtenerDetalleNegocio
    tags: [radar]

/radar/save-lead:
  post:
    summary: "Guardar negocio como lead (proveedor/cliente potencial)"
    operationId: guardarLead
    tags: [radar]
    requestBody:
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/SaveLeadDto'

/radar/leads:
  get:
    summary: "Listar leads guardados"
    operationId: listarLeads
    tags: [radar]

/radar/leads/{leadId}/exclude:
  put:
    summary: "Excluir negocio de búsquedas futuras"
    operationId: excluirLead
    tags: [radar]

/radar/search-history:
  get:
    summary: "Historial de búsquedas del usuario"
    operationId: listarHistorialBusquedas
    tags: [radar]
```

---

### TD-04 — Servicio de búsqueda con Rate Limiter

```java
@Service
@RequiredArgsConstructor
public class RadarSearchServiceImpl implements RadarSearchService {
    
    private final GooglePlacesClient googlePlacesClient;
    private final ExplorationCacheRepository cacheRepo;
    private final RateLimiterService rateLimiter;
    
    @Override
    public RadarSearchResult buscarNegocios(RadarSearchDto request) {
        // 1. Verificar rate limit (10/usuario/hora)
        rateLimiter.checkLimit(request.getUserId(), 10, Duration.ofHours(1));
        
        // 2. Buscar en caché primero (misma query + ubigeo, no expirada)
        Optional<ExplorationCacheEntity> cached = cacheRepo
            .findByQueryAndUbigeoNotExpired(request.getQuery(), request.getUbigeoCode());
        
        if (cached.isPresent()) {
            return parseFromCache(cached.get());
        }
        
        // 3. Llamar a Google Places API (Text Search)
        List<PlaceResult> results = googlePlacesClient.textSearch(
            request.getQuery(), 
            ubigeoToLatLng(request.getUbigeoCode()),
            request.getRadiusKm() * 1000  // metros
        );
        
        // 4. Guardar en caché (TTL 30 días)
        saveToCache(request, results);
        
        // 5. Cruzar con ubigeo INEI
        enrichWithUbigeo(results);
        
        return buildResponse(results);
    }
}
```

**Fallback:** Si Google Places falla → mostrar mensaje `"Servicio de búsqueda pausado temporalmente. Puedes registrar el proveedor manualmente."` + devolver leads guardados previamente.

---

### TD-05 — Contacto inteligente (Quick Strike)

**Botones en la UI al ver un negocio:**
- 🟢 **WhatsApp:** `wa.me/51{numero}?text=Hola, vi que venden [Producto] y me interesa. Saludos desde ETIA.`
- 🌐 **Website:** Abre `website_url` en nueva pestaña
- 📧 **Email:** Abre `mailto:` si existe
- 📞 **Llamar:** `tel:+51{numero}`
- ➕ **Agregar como Lead:** `POST /radar/save-lead` → crea `potential_business_contacts`

**Regla legal:** Solo mostrar datos PÚBLICOS de la ficha de Google. No almacenar `rating` ni `reviews`.

---

### TD-06 — Cruce con catálogo contable SUNAT

Cuando el usuario busca "Ferreterías":
1. El sistema cruza la categoría de Google con Catálogos SUNAT (Tablas 5, 13, 25)
2. Sugiere búsquedas relacionadas: "Materiales de Construcción", "Distribuidores de Acero"
3. El usuario puede elegir una categoría para perfilamiento

**Servicio:** `CategoryMatchingService` en `radar/util/`

---

### TD-07 — Vista Radar

**Nueva vista:** `templates/radar.html`  
**View Controller:** `RadarViewController.java` (`GET /radar`)

```
Vista Radar
├── Barra de búsqueda: [Buscar...] + [Ubigeo ▼] + [Radio: ═══●═══ 10km]
├── Resultados (cards):
│   ├── 📍 Nombre del negocio
│   ├── 📍 Dirección + Ubigeo
│   ├── 🏷️ Categoría
│   ├── 🌐 Website (si tiene)
│   ├── [WhatsApp] [Web] [Guardar] [Crear Orden Compra →]
│   └── Estado: LEAD / PROVEEDOR / CLIENTE
├── Mapa lateral (Leaflet): Markers de resultados
└── Historial de búsquedas recientes
```

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/google_places_search.feature`

```gherkin
Feature: Radar de Mercado — Búsqueda de negocios

  Scenario: Buscar ferreterías en Lima
    Given soy usuario de empresa "ETLIONS"
    When busco "Ferreterías" en ubigeo "150131" con radio 10km
    Then recibo una lista de negocios con nombre, dirección y categoría
    And la búsqueda se guarda en exploration_cache

  Scenario: Caché evita llamada duplicada a Google
    Given ya busqué "Ferreterías" en "150131" hace 5 minutos
    When busco lo mismo de nuevo
    Then los resultados vienen del caché (no se llama a Google)

  Scenario: Rate limit alcanzado
    Given ya hice 10 búsquedas en la última hora
    When intento una búsqueda más
    Then recibo error 429 "Límite de búsquedas alcanzado"

  Scenario: Guardar negocio como lead
    Given veo un resultado "Ferretería San Martín"
    When hago click en "Guardar como Lead"
    Then se crea un registro en potential_business_contacts
    And su status es "LEAD"

  Scenario: Fallback manual si Google falla
    Given la API de Google Places no responde
    When intento buscar
    Then veo el mensaje "Servicio pausado temporalmente"
    And puedo registrar manualmente

  Scenario: Búsqueda sin resultados
    Given busco "Joyerías espaciales" en ubigeo "150131"
    When no hay resultados
    Then veo mensaje "No se encontraron negocios. Intenta con otro término o amplía el radio."
    And puedo registrar lead manualmente

  Scenario: Lead ya existe como cliente
    Given un lead "Ferretería San Martín" guardado
    And un cliente con el mismo RUC ya registrado
    When guardo el lead
    Then el sistema advierte "Este negocio ya existe como cliente en tu empresa"

  Scenario: Excluir lead de búsquedas futuras
    Given un lead guardado con status EXCLUIDO
    When realizo una nueva búsqueda que incluiría ese negocio
    Then el negocio excluido NO aparece en los resultados

  Scenario: Actualizar status de lead
    Given un lead con status "LEAD"
    When cambio su status a "PROVEEDOR"
    Then el lead tiene status "PROVEEDOR"

  Scenario: Ubigeo inválido rechazado
    Given un ubigeo "999999" que no existe en tablas SUNAT
    When intento buscar en ese ubigeo
    Then recibo error 400 "Código de ubigeo no válido"

  Scenario: Mock en desarrollo devuelve datos de ejemplo
    Given el perfil activo es "dev" (GOOGLE_PLACES_API_KEY=demo-key)
    When busco negocios
    Then recibo ~20 negocios ficticios de Lima hardcoded
    And el sistema funciona sin gastar dinero en API
```

---

### TD-08 — Modo Mock vs Real (decisión aplicada de PREGUNTAS_DEFINICION D1)

**Decisión:** Mock en dev con JSON estáticos, real en staging/prod.

```java
@Service
@ConditionalOnProperty(name = "google.places.mode", havingValue = "mock", matchIfMissing = true)
public class MockGooglePlacesClient implements GooglePlacesClient {
    // ~20 negocios ficticios de Lima: Ferreterías, Restaurantes, Bodegas, Distribuidores
    // Hardcoded en src/main/resources/sample_docs/mock_google_places.json
}

@Service
@ConditionalOnProperty(name = "google.places.mode", havingValue = "real")
public class RealGooglePlacesClient implements GooglePlacesClient {
    // Usa Google Places API real con GOOGLE_PLACES_API_KEY
}
```

**application.yaml:**
```yaml
google:
  places:
    mode: ${GOOGLE_PLACES_MODE:mock}  # mock | real
    api-key: ${GOOGLE_PLACES_API_KEY:demo-key}
```

---

## ✅ Criterios de Aceptación

- [ ] Búsqueda funcional contra Google Places API (o mock en dev)
- [ ] Caché de 30 días funcionando
- [ ] Rate limiter: 10 búsquedas/usuario/hora
- [ ] Guardar leads con datos públicos
- [ ] Botones de contacto (WhatsApp, Web, Email, Tel)
- [ ] Cruce con catálogo SUNAT
- [ ] Vista con mapa Leaflet
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `pom.xml` | 🔧 +google-maps-services |
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag radar, +endpoints, +schemas |
| `src/main/java/.../radar/**` | 🆕 Paquete completo (~12 archivos) |
| `src/main/java/.../radar/util/CategoryMatchingService.java` | 🆕 |
| `src/main/java/.../radar/util/RateLimiterService.java` | 🆕 |
| `src/main/java/.../view/RadarViewController.java` | 🆕 |
| `src/main/resources/templates/radar.html` | 🆕 |
| `src/main/resources/static/js/radar-view.js` | 🆕 |
| `src/main/resources/application.yaml` | 🔧 +google.places config |
| `src/main/resources/data.sql` | 🔧 +leads demo (opcional) |

---

## ⚠️ Advertencias legales

- **GDPR / Ley Peruana 29733:** Solo datos de perfil público comercial
- **No almacenar:** `rating`, `reviews` (propiedad de Google, volátil)
- **Derecho de exclusión:** Enlace "No ser contactado" → `excluded_from_search = true`
- **Costo API:** ~$0.017-$0.040 por búsqueda. Sostenible en plan PRO.

---

## ⚠️ Notas de implementación

- **Decisión D1 (PREGUNTAS_DEFINICION):** Mock en dev con JSON estáticos, real en staging/prod. Implementado vía `@ConditionalOnProperty` con `MockGooglePlacesClient` y `RealGooglePlacesClient`.
- **Decisión D2 (PREGUNTAS_DEFINICION):** Rate limiter por usuario (10/hora). Implementar con `Map<userId, List<Timestamp>>` en memoria. No requiere Redis para v1.
- **Decisión D3 (PREGUNTAS_DEFINICION):** Leads visibles a nivel empresa (company_id), no por usuario individual.
- **Mock data:** Crear `src/main/resources/sample_docs/mock_google_places.json` con ~20 negocios ficticios de Lima (ferreterías, restaurantes, bodegas, distribuidores).
- **Leaflet:** Usar CDN (`https://unpkg.com/leaflet@1.9.4/`). No bundear la librería.
- **CategoryMatchingService:** Mapea categorías de Google Places a catálogos SUNAT (tablas 5, 13, 25). Es un mapping estático hardcoded, no requiere ML.
- **Deduplicación:** Al guardar lead, verificar por `google_place_id` si ya existe. Si existe → advertir al usuario, no crear duplicado.
- **Company isolation:** Todos los endpoints filtran por `company_id` del JWT.

---

*Fase D — Radar de Mercado. Ojos en todas partes, siempre dentro de la ley.*



