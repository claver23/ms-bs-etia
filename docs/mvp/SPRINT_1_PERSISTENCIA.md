# 🗄️ SPRINT 1 — PERSISTENCIA (Flyway + PostgreSQL)

> **Objetivo:** Que los datos no se borren al reiniciar  
> **Entregable:** App funciona igual, con migraciones versionadas y perfil PostgreSQL  
> **Dependencias Maven nuevas:** 2  
> **Archivos nuevos:** ~4  
> **Archivos modificados:** ~3

---

## 📦 DEPENDENCIAS MAVEN

```xml
<!-- Flyway Core — gestionado por Spring Boot parent, sin versión explícita -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- Flyway PostgreSQL — necesario para dialect PostgreSQL -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

**Ubicación:** En `pom.xml`, después del bloque de H2.

---

## 📋 PASOS EN ORDEN

### Paso 1.1 — Agregar dependencias Flyway al pom.xml

**Archivo:** `pom.xml`  
**Acción:** MODIFICAR  
**Detalle:** Agregar las 2 dependencias Flyway después de la dependencia H2.

---

### Paso 1.2 — Crear V1__initial_schema.sql

**Archivo:** `src/main/resources/db/migration/V1__initial_schema.sql`  
**Acción:** CREAR  
**Detalle:** CREATE TABLE para las 19 tablas, en orden de dependencias:

| # | Tabla | Entity fuente | Columnas clave |
|---|-------|---------------|----------------|
| 1 | `companies` | CompanyEntity | 22 cols: id, ruc (UNIQUE), razon_social, nombre_comercial, codigo_ubigeo, direccion, urbanizacion, departamento, provincia, distrito, usuario_sol, clave_sol, certificado_path, certificado_password, email_contacto, telefono, regimen, estado, logo_path, logo_format, emision_en_envio (BOOLEAN DEFAULT TRUE), es_demo (BOOLEAN DEFAULT FALSE), fecha_creacion, fecha_actualizacion |
| 2 | `clients` | ClientEntity | 13 cols: id, company_id, tipo_documento, numero_documento, denominacion, direccion, departamento, provincia, distrito, codigo_pais, email, telefono, es_demo, fecha_creacion |
| 3 | `products` | ProductEntity | 13 cols: id, company_id, codigo_producto, codigo_producto_sunat, descripcion, unidad_medida, tipo_afectacion_igv, valor_unitario (DECIMAL 12,2), precio_unitario (DECIMAL 12,2), igv (DECIMAL 12,2), estado, es_demo, fecha_creacion |
| 4 | `employees` | EmployeeEntity | 9 cols: id, company_id, nombre, cargo, dni (UNIQUE), codigo (UNIQUE), estado, es_demo, fecha_creacion |
| 5 | `ubigeos` | UbigeoEntity | 5 cols: id, codigo (UNIQUE), departamento, provincia, distrito |
| 6 | `unidades_medida` | UnidadMedidaEntity | 3 cols: id, codigo (UNIQUE), descripcion |
| 7 | `sedes` | SedeEntity | 14 cols: id, company_id, codigo, nombre, direccion, codigo_ubigeo, departamento, provincia, distrito, telefono, email, estado, es_default (BOOLEAN), fecha_creacion + UNIQUE(company_id, codigo) |
| 8 | `series_config` | SerieConfigEntity | 9 cols: id, company_id, sede_id, tipo_documento, serie, descripcion, es_default (BOOLEAN), activa (BOOLEAN DEFAULT TRUE), fecha_creacion + UNIQUE(company_id, tipo_documento, serie) |
| 9 | `correlativos` | CorrelativoEntity | 4 cols: id, company_id, serie, ultimo_numero (INTEGER DEFAULT 0) + UNIQUE(company_id, serie) |
| 10 | `facturas` | FacturaEntity | 30 cols: id, company_id, client_id, serie, numero, fecha_emision (DATE), moneda, forma_pago, tipo_cambio (DECIMAL 10,4), fecha_vencimiento (DATE), codigo_ubigeo, tipo_operacion, hora_emision (TIME), gravadas (DECIMAL 12,2), exoneradas, inafectas, gratuitas, igv, total, total_cargos, total_descuentos, subtotal, total_con_impuestos, estado, hash_cdr, codigo_respuesta_sunat, descripcion_respuesta_sunat, leyenda, detalle_json (TEXT), xml_content (TEXT), xml_path, errores_validacion (TEXT), fecha_creacion |
| 11 | `boletas` | BoletaEntity | 31 cols: igual que facturas + resumen_diario_id |
| 12 | `notas_credito` | NotaCreditoEntity | 37 cols: igual que facturas + documento_referencia_id, serie_doc_ref, numero_doc_ref, tipo_documento_referencia, motivo, codigo_motivo_sunat, monto_descuento, descripcion |
| 13 | `notas_debito` | NotaDebitoEntity | 37 cols: igual que facturas + documento_referencia_id, serie_doc_ref, numero_doc_ref, tipo_documento_referencia, motivo, codigo_motivo_sunat, monto_incremento, descripcion |
| 14 | `cuotas_credito` | CuotaCreditoEntity | 7 cols: id, factura_id, boleta_id, numero_cuota, monto (DECIMAL 12,2), fecha_vencimiento (DATE), moneda |
| 15 | `allowance_charges` | AllowanceChargeEntity | 9 cols: id, factura_id, boleta_id, charge_indicator (BOOLEAN), reason_code, reason_description, multiplier_factor (DECIMAL 8,4), amount (DECIMAL 12,2), base_amount (DECIMAL 12,2), moneda |
| 16 | `user_preferences` | UserPreferenceEntity | 5 cols: id, company_id, clave, valor, fecha_actualizacion + UNIQUE(company_id, clave) |
| 17 | `tipo_cambio` | TipoCambioEntity | 8 cols: id, fecha (DATE), moneda_origen, moneda_destino, tasa_compra (DECIMAL 10,4), tasa_venta (DECIMAL 10,4), fuente, fecha_creacion |
| 18 | `libros_electronicos` | LibroElectronicoEntity | 10 cols: id, company_id, tipo, periodo, formato, estado, archivo_url, total_registros, fecha_generacion, fecha_creacion |
| 19 | `resumenes_diarios` | ResumenDiarioEntity | 11 cols: id, company_id, identificador (UNIQUE), fecha_generacion (DATE), fecha_referencia (DATE), estado, ticket_sunat, codigo_respuesta, descripcion_respuesta, cantidad_boletas, xml_content (TEXT), fecha_creacion |

**Reglas de tipos SQL (compatible H2 + PostgreSQL):**
- IDs: `VARCHAR(255) PRIMARY KEY` (UUID strings generados por JPA)
- Strings default: `VARCHAR(255)`
- Strings con longitud: `VARCHAR(N)` según @Column(length=N)
- Montos: `DECIMAL(12,2)`
- Tasas: `DECIMAL(10,4)`
- Factores: `DECIMAL(8,4)`
- Fechas: `DATE`
- Timestamps: `TIMESTAMP`
- Horas: `TIME`
- Textos largos: `TEXT`
- Booleanos: `BOOLEAN`

---

### Paso 1.3 — Crear V2__seed_data.sql

**Archivo:** `src/main/resources/db/migration/V2__seed_data.sql`  
**Acción:** CREAR  
**Detalle:** Copiar contenido de `data.sql` (153 líneas) con estas adaptaciones:
- Usar `MERGE INTO ... KEY(id) VALUES (...)` para H2 compatibility (idempotencia)
- O alternativamente: Mantener `INSERT INTO` puro y aceptar que la migración solo corre 1 vez
- **Decisión:** Usar INSERT puro — Flyway solo ejecuta cada migración una vez, no necesita idempotencia.
- `CURRENT_TIMESTAMP` funciona en ambos motores — mantener sin cambios.

---

### Paso 1.4 — Crear application-prod.yaml

**Archivo:** `src/main/resources/application-prod.yaml`  
**Acción:** CREAR  
**Contenido exacto:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:etia}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USER:etia}
    password: ${DB_PASS:etia}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
  flyway:
    enabled: true
  sql:
    init:
      mode: never
  h2:
    console:
      enabled: false
```

---

### Paso 1.5 — Modificar application.yaml

**Archivo:** `src/main/resources/application.yaml`  
**Acción:** MODIFICAR  
**Cambios:**

```yaml
spring:
  # ...existente...
  jpa:
    # ...existente...
    hibernate:
      ddl-auto: none              # CAMBIO: era "update"
  # ...existente...
  sql:
    init:
      mode: never                 # CAMBIO: era "always"
  # ...existente...
  flyway:                         # NUEVO bloque
    enabled: true
    locations: classpath:db/migration
```

---

### Paso 1.6 — Eliminar data.sql

**Archivo:** `src/main/resources/data.sql`  
**Acción:** RENOMBRAR a `data.sql.bak` (backup, no se ejecuta)  
**Razón:** Los datos seed ahora están en `V2__seed_data.sql`

---

### Paso 1.7 — Verificar tests

**Acción:** Ejecutar `mvn clean test`  
**Esperado:** 295 tests pasan  
**Riesgo:** Si algún test dependía de `ddl-auto: update` + `data.sql`, necesitará ajuste.  
**Mitigación:** Los tests usan H2 + Flyway ahora. Si Flyway ejecuta V1+V2 en H2, los tests deberían funcionar igual.

---

## ⚠️ RIESGOS Y MITIGACIONES

| Riesgo | Probabilidad | Mitigación |
|--------|-------------|------------|
| SQL incompatible H2/PostgreSQL | Media | Usar tipos ANSI SQL. H2 soporta `MODE=PostgreSQL` si necesario. |
| Tests fallan por cambio ddl-auto | Baja | Flyway aplica V1+V2 antes de los tests — mismo resultado que ddl-auto+data.sql |
| `CURRENT_TIMESTAMP` distinto | Baja | Funciona igual en H2 y PostgreSQL |

---

## ✅ CRITERIOS DE DONE

- [ ] `mvn clean test` → 295 tests pasan
- [ ] Flyway crea schema y seed en H2 (dev) sin errores
- [ ] App arranca con `spring.profiles.active=prod` contra PostgreSQL local
- [ ] Datos seed visibles en ambos entornos
- [ ] `data.sql` eliminado/renombrado — Flyway es la única fuente

