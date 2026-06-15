# 🗺️ FASE K — GeoInsight — Mapa + Análisis por Ubigeo (M10)

> **Módulo:** GeoInsight  
> **Paquete:** `com.etlions.ms_bs_etia.geo`  
> **Endpoint base:** `/api/v1/geo`  
> **Prioridad:** 🟢 Media-Baja (futuro próximo)  
> **Dependencias:** FASE A, FASE B (Security — COMPLETADA), FASE D (Radar), FASE E (Campaigns)  
> **Puede paralelizarse con:** FASE F, G, H, I, J (post FASE B)  
> **Estado:** ⬜ PENDIENTE

---

## 🎯 Objetivo

Mapa interactivo del Perú con drill-down por departamento → provincia → distrito (ubigeo). Heatmap de oportunidades basado en Radar. Geolocalización de sedes. Análisis zonal para perfilamiento de campañas. Auto-detección de ubigeo del usuario.

---

## 📋 Tareas

### TK-01 — Endpoints

```yaml
/geo/ubigeo/detect:
  get:
    summary: "Detectar ubigeo del usuario por geolocalización"
    operationId: detectarUbigeo
    tags: [geo]
    parameters:
      - name: lat { type: number }
      - name: lng { type: number }
    responses:
      "200":
        schema:
          $ref: '#/components/schemas/UbigeoDetectResult'

/geo/analysis/{ubigeoCode}:
  get:
    summary: "Análisis de zona por ubigeo"
    operationId: analizarZona
    tags: [geo]

/geo/heatmap:
  get:
    summary: "Datos para mapa de calor de oportunidades"
    operationId: obtenerHeatmap
    tags: [geo]
    parameters:
      - name: companyId
      - name: categoria
      - name: radiusKm

/geo/sedes/geocode:
  post:
    summary: "Geocodificar sede (obtener lat/lng desde dirección)"
    operationId: geocodificarSede
    tags: [geo]
```

### TK-02 — Vista GeoInsight

```
Vista GeoInsight
├── Mapa del Perú (Leaflet full-screen)
│   ├── Markers: Sedes del usuario
│   ├── Heatmap: Densidad de negocios (del Radar)
│   ├── Click en departamento → zoom a provincias
│   ├── Click en provincia → zoom a distritos
│   └── Sidebar: "Análisis de [UBIGEO]"
│       ├── Negocios encontrados: 45
│       ├── Por categoría: Ferreterías (12), Construcción (8)...
│       ├── Clientes en la zona: 5
│       └── [Crear Campaña para esta zona]
├── Botón: "📍 Detectar mi ubicación" → auto-ubigeo
└── Slider de radio: ═══●═══ 0km — 50km
```

### TK-03 — Auto-detección de ubigeo

```javascript
ETIA.detectarUbigeo = function() {
    navigator.geolocation.getCurrentPosition(pos => {
        fetch(`/api/v1/geo/ubigeo/detect?lat=${pos.coords.latitude}&lng=${pos.coords.longitude}`)
            .then(r => r.json())
            .then(data => {
                // Mostrar: "Tu ubicación: Lima, San Isidro (150131)"
                ETIA.showSuccess(`📍 ${data.departamento}, ${data.distrito} (${data.codigo})`);
            });
    });
};
```

**Backend:** Cruza lat/lng con tabla de ubigeos (usando bounding boxes o Nominatim reverse geocode).

### TK-04 — Geocodificación de sedes

Cuando se guarda una sede con ubigeo → intentar obtener lat/lng de Google Maps:
- Guardar `latitude`, `longitude` en `SedeEntity` (campos nuevos)
- Mostrar en mapa de GeoInsight

---

## 🧪 Escenarios Gherkin

**Archivo:** `src/test/resources/features/geoinsight_ubigeo.feature`

```gherkin
Feature: GeoInsight — Análisis geográfico

  Scenario: Detectar ubigeo del usuario
    Given el navegador reporta lat -12.0964, lng -77.0350
    When presiono "Detectar mi ubicación"
    Then el sistema muestra "LIMA, SAN ISIDRO (150131)"

  Scenario: Permiso de geolocalización denegado
    Given el navegador deniega acceso a ubicación
    When presiono "Detectar mi ubicación"
    Then veo mensaje "Permiso de ubicación denegado. Selecciona tu ubigeo manualmente."

  Scenario: Heatmap de oportunidades
    Given búsquedas previas en el Radar para "Ferreterías"
    When accedo al mapa
    Then veo puntos de calor más intensos donde hay más ferreterías

  Scenario: Heatmap sin datos
    Given no hay búsquedas previas en el Radar
    When accedo al mapa
    Then veo el mapa limpio con mensaje "Usa el Radar para descubrir oportunidades"

  Scenario: Análisis por distrito
    Given selecciono distrito "San Isidro" en el mapa
    When solicito análisis
    Then veo categorías de negocios y cantidad de clientes en la zona

  Scenario: Geocodificar sede
    Given una sede con dirección "Av. Javier Prado 1234, San Isidro, Lima"
    When geocodifico la sede
    Then se guardan latitude y longitude en SedeEntity

  Scenario: Sede sin coordenadas aparece sin marker
    Given una sede sin latitude/longitude
    When accedo al mapa
    Then la sede no aparece como marker pero sí en la lista lateral

  Scenario: Crear campaña desde zona del mapa
    Given selecciono distrito "Miraflores" en el mapa
    When hago click en "Crear Campaña para esta zona"
    Then redirige a crear campaña con ubigeo_target pre-cargado
```

---

## ✅ Criterios de Aceptación

- [ ] Mapa del Perú con Leaflet funcional
- [ ] Drill-down: Departamento → Provincia → Distrito
- [ ] Heatmap basado en datos del Radar
- [ ] Auto-detección de ubigeo por geolocalización
- [ ] Manejo de permiso denegado
- [ ] Geocodificación de sedes (guardar lat/lng)
- [ ] Sedes mostradas como markers en el mapa
- [ ] Análisis de zona con conteo por categoría
- [ ] Crear campaña desde zona del mapa
- [ ] Company isolation en endpoints
- [ ] Solo plan BUSINESS (o módulo extra)
- [ ] Compilación limpia + tests pasan

---

## 📁 Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/main/resources/swagger/ms-bs-etia.yaml` | 🔧 +tag geo, +endpoints, +schemas |
| `src/main/java/.../geo/**` | 🆕 Paquete completo (~8 archivos) |
| `src/main/java/.../geo/openapi/GeoOpenApi.java` | 🆕 |
| `src/main/java/.../geo/service/GeoService.java` | 🆕 |
| `src/main/java/.../geo/service/impl/GeoServiceImpl.java` | 🆕 |
| `src/main/java/.../geo/service/UbigeoDetectorService.java` | 🆕 |
| `src/main/java/.../sede/persistence/SedeEntity.java` | 🔧 +latitude, +longitude |
| `src/main/java/.../view/GeoViewController.java` | 🆕 |
| `src/main/resources/templates/geoinsight.html` | 🆕 |
| `src/main/resources/static/js/geo-view.js` | 🆕 |
| `src/main/resources/static/js/lib/leaflet.min.js` | 🆕 (o CDN) |
| `src/test/resources/features/geoinsight_ubigeo.feature` | 🆕 |

---

## ⚠️ Notas de implementación

- **Leaflet:** Usar CDN (`https://unpkg.com/leaflet@1.9.4/`) en vez de bundear. Más ligero.
- **Heatmap plugin:** Usar `leaflet.heat` plugin para el mapa de calor.
- **Ubigeo detector:** Cruza lat/lng con bounding boxes de distritos. Usar tabla `ubigeo_inei` con coords aproximadas o Nominatim reverse geocode (gratis).
- **SedeEntity:** Agregar campos `latitude DECIMAL(10,7)` y `longitude DECIMAL(10,7)` opcionales.

---

*Fase K — GeoInsight. El mapa no es el territorio, pero sin mapa, estás ciego.*



