# 🚀 SPRINT 4 — ONBOARDING + PDF/QR + DOCKER DEPLOY

> **Objetivo:** Que un cliente nuevo pueda usar ETIA desde cero  
> **Entregable:** Producto desplegable. Register → empresa → factura válida → PDF  
> **Dependencias Maven nuevas:** 3  
> **Archivos nuevos:** ~10  
> **Archivos modificados:** ~4

---

## 📦 DEPENDENCIAS MAVEN

```xml
<!-- OpenPDF (fork libre de iText 4, LGPL) — generación de PDF -->
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>2.0.3</version>
</dependency>

<!-- ZXing Core — generación de QR code -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- ZXing Java SE — renderizado QR a imagen -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

---

## 📋 PASOS EN ORDEN

### Paso 4.1 — QrCodeService

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/app_util/service/QrCodeService.java`  
**Acción:** CREAR

**Método:**
```java
public byte[] generateQrPng(String content, int size)
```

**Formato del contenido QR (estándar SUNAT):**
```
{RUC}|{TIPO_DOC}|{SERIE}|{NUMERO}|{IGV}|{TOTAL}|{FECHA_EMISION}|{TIPO_DOC_RECEPTOR}|{NUM_DOC_RECEPTOR}|{HASH_CDR}|
```

**Ejemplo:**
```
20601234567|01|F001|00000001|450.00|2950.00|2026-04-09|6|20505050501|abc123hash|
```

Tipos de documento:
- `01` = Factura
- `03` = Boleta
- `07` = Nota de Crédito
- `08` = Nota de Débito

---

### Paso 4.2 — PdfGeneratorService

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/app_util/service/PdfGeneratorService.java`  
**Acción:** CREAR

**Método:**
```java
public byte[] generateFacturaPdf(
    FacturaEntity factura, 
    CompanyEntity company, 
    ClientEntity client, 
    List<Map<String,Object>> detalle  // parsed from detalleJson
)
```

**Estructura del PDF A4:**

```
┌─────────────────────────────────────────────────────────┐
│  [LOGO]   RAZÓN SOCIAL                    ┌───────────┐ │
│           RUC: 20601234567                │  FACTURA  │ │
│           Dirección                       │ F001-     │ │
│           Departamento/Provincia          │ 00000001  │ │
│                                           └───────────┘ │
├─────────────────────────────────────────────────────────┤
│  DATOS DEL RECEPTOR                                     │
│  Razón Social/Nombre: xxx                               │
│  RUC/DNI: xxx                                           │
│  Dirección: xxx                                         │
├─────────────────────────────────────────────────────────┤
│  Fecha emisión: dd/mm/yyyy    Moneda: PEN               │
│  Forma de pago: CONTADO       Tipo operación: 0101      │
├─────────────────────────────────────────────────────────┤
│  # │ Descripción    │ UM  │ Cant │ P.U.  │ Subtotal    │
│  1 │ Consultoría TI │ ZZ  │ 1    │ 2500  │ 2500.00     │
│  2 │ Licencia ERP   │ ZZ  │ 1    │ 1800  │ 1800.00     │
├─────────────────────────────────────────────────────────┤
│                              Gravadas:     S/ 4,300.00  │
│                              IGV (18%):    S/   774.00  │
│                              TOTAL:        S/ 5,074.00  │
├─────────────────────────────────────────────────────────┤
│  SON CINCO MIL SETENTA Y CUATRO Y 00/100 SOLES         │
├──────────────────────────┬──────────────────────────────┤
│  [QR CODE]               │ Hash: abc123...              │
│                          │ Código SUNAT: 0              │
│                          │ Representación impresa       │
└──────────────────────────┴──────────────────────────────┘
```

**Implementación con OpenPDF:**
- `Document` tamaño A4
- `PdfPTable` para cabecera (2 columnas: logo+datos | recuadro comprobante)
- `PdfPTable` para datos receptor
- `PdfPTable` para detalle (6 columnas)
- `PdfPTable` para totales (2 columnas: label | monto)
- `Image.getInstance(qrBytes)` para QR code
- Fuente: Helvetica (incluida en OpenPDF)

---

### Paso 4.3 — OnboardingService

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/service/OnboardingService.java`  
**Acción:** CREAR

**Método:**
```java
public OnboardingResult registrarEmpresa(
    String userId,      // del JWT
    String ruc, 
    String razonSocial, 
    String direccion,
    String departamento,
    String provincia,
    String distrito,
    String codigoUbigeo
)
```

**Flujo:**
```
1. Crear CompanyEntity con datos proporcionados
   - estado = "ACTIVO"
   - usuarioSol = "MODDATOS" (default beta)
   - claveSol = "MODDATOS" (default beta)
   - emisionEnEnvio = true
2. Crear SedeEntity "Sede Principal"
   - codigo = "001"
   - esDefault = true
   - misma dirección que company
3. Crear 4 SerieConfigEntity:
   - F001 (Factura), B001 (Boleta), FC01 (NC), FD01 (ND)
   - Todas esDefault=true, activa=true, sedeId=sede.id
4. Crear 4 CorrelativoEntity (todos ultimoNumero=0)
5. Actualizar UserEntity.companyId = company.id
6. Retornar OnboardingResult(companyId, sedeId, JWT actualizado)
```

**Record:**
```java
public record OnboardingResult(String companyId, String sedeId, String token) {}
```

---

### Paso 4.4 — Endpoint POST /api/v1/auth/onboarding

**Archivo:** `src/main/java/com/etlions/ms_bs_etia/auth/openapi/AuthController.java`  
**Acción:** MODIFICAR — agregar endpoint

```
POST /api/v1/auth/onboarding
  Headers: Authorization: Bearer {token}
  Body: { ruc, razonSocial, direccion, departamento, provincia, distrito, codigoUbigeo }
  Response: { isSuccess, data: { companyId, sedeId, token } }
```

---

### Paso 4.5 — Endpoint PDF en OpenAPI

**Archivo:** `src/main/resources/swagger/ms-bs-etia.yaml`  
**Acción:** MODIFICAR — agregar paths

```yaml
/factura/{facturaId}/pdf:
  get:
    tags: [factura]
    operationId: descargarFacturaPdf
    summary: Descargar representación impresa PDF de la factura
    parameters:
      - name: facturaId
        in: path
        required: true
        schema:
          type: string
    responses:
      '200':
        description: PDF generado exitosamente
        content:
          application/pdf:
            schema:
              type: string
              format: binary
      '204':
        description: Factura no tiene datos suficientes para generar PDF
      '404':
        description: Factura no encontrada

# Repetir para boleta:
/boleta/{boletaId}/pdf:
  get:
    tags: [boleta]
    operationId: descargarBoletaPdf
    # ... similar
```

---

### Paso 4.6 — Implementar endpoint PDF en controller

**Archivo:** `DocFacturaOpenApi.java` (o el controller de factura)  
**Acción:** MODIFICAR — implementar `descargarFacturaPdf`

```
Flujo:
1. Buscar FacturaEntity por ID
2. Buscar CompanyEntity por factura.companyId
3. Buscar ClientEntity por factura.clientId
4. Parsear detalleJson
5. Llamar pdfGeneratorService.generateFacturaPdf(...)
6. Retornar ResponseEntity<byte[]> con:
   - Content-Type: application/pdf
   - Content-Disposition: inline; filename="F001-00000001.pdf"
```

---

### Paso 4.7 — Vista de onboarding

**Archivo:** `src/main/resources/templates/onboarding.html`  
**Acción:** CREAR

**Wizard de 3 pasos:**
```
Paso 1: Datos de Empresa
  - RUC (obligatorio)
  - Razón Social (obligatorio)
  - Dirección
  - Ubigeo (dropdown de ubigeos existentes)

Paso 2: Certificado Digital (opcional — skip para MVP)
  - Subir .p12
  - Clave del certificado
  - O "Usar modo demo (SUNAT beta)"

Paso 3: ¡Listo!
  - "Tu empresa está configurada"
  - "Series creadas: F001, B001, FC01, FD01"
  - Botón: "Emitir mi primera factura" → /wizard?type=FACTURA
```

**View Controller:**
- Agregar `GET /onboarding` en `AuthViewController.java` (o nuevo controller)

---

### Paso 4.8 — Dockerfile

**Archivo:** `Dockerfile` (raíz del proyecto)  
**Acción:** CREAR

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY mvnw mvnw.cmd ./
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache ttf-dejavu  # Fuentes para PDF
WORKDIR /app
COPY --from=build /app/target/ms-bs-etia-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Nota:** `ttf-dejavu` es necesario para que OpenPDF renderice texto correctamente en Alpine Linux.

---

### Paso 4.9 — docker-compose.yml

**Archivo:** `docker-compose.yml` (raíz del proyecto)  
**Acción:** CREAR

```yaml
version: '3.8'

services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: etia
      POSTGRES_USER: etia
      POSTGRES_PASSWORD: ${DB_PASS:-etia_secret}
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U etia"]
      interval: 5s
      timeout: 5s
      retries: 5

  app:
    build: .
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: etia
      DB_USER: etia
      DB_PASS: ${DB_PASS:-etia_secret}
      JWT_SECRET: ${JWT_SECRET:-cambiar-en-produccion-minimo-32-caracteres}
      ETIA_STORAGE_PATH: /app/storage
      ETIA_CERTS_PATH: /app/certs
    volumes:
      - etia_storage:/app/storage
      - etia_certs:/app/certs
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy

volumes:
  pgdata:
  etia_storage:
  etia_certs:
```

---

### Paso 4.10 — .env.example

**Archivo:** `.env.example` (raíz del proyecto)  
**Acción:** CREAR

```bash
# Base de datos PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=etia
DB_USER=etia
DB_PASS=etia_secret

# JWT
JWT_SECRET=cambiar-en-produccion-clave-secreta-de-al-menos-32-caracteres
JWT_EXPIRATION=24

# ETIA Storage (XMLs, logos, etc.)
ETIA_STORAGE_PATH=./etia_storage

# Certificados digitales SUNAT
ETIA_CERTS_PATH=./certs

# SUNAT
# Para producción real, cambiar a: produccion
# SUNAT_MODO=beta

# Spring profile
# SPRING_PROFILES_ACTIVE=prod
# Para usar SUNAT real: prod,sunat-real
```

---

## 🧪 TESTS A CREAR

### `src/test/java/.../service/QrCodeServiceTest.java`
- Generar QR con datos SUNAT → verificar que retorna PNG no vacío
- Verificar dimensiones de la imagen

### `src/test/java/.../service/PdfGeneratorServiceTest.java`
- Generar PDF con datos de prueba → verificar que retorna bytes no vacíos
- Verificar que empieza con `%PDF` (magic bytes)

### `src/test/java/.../auth/service/OnboardingServiceTest.java`
- Mock repos → verificar que crea company + sede + series + correlativos
- Verificar que actualiza user.companyId

---

## ⚠️ RIESGOS Y MITIGACIONES

| Riesgo | Probabilidad | Mitigación |
|--------|-------------|------------|
| OpenPDF sin fuentes en Docker Alpine | **Alta** | `apk add ttf-dejavu` en Dockerfile |
| Tamaño imagen Docker grande | Media | Multi-stage build con JRE Alpine (~200MB) |
| QR muy pequeño para escanear | Baja | Generar a 150x150 px mínimo |
| Wizard de onboarding complejo | Media | Simplificar a 2 pasos: datos empresa + listo |

---

## ✅ CRITERIOS DE DONE

- [ ] Flujo completo: register → onboarding → emitir factura → descargar PDF con QR
- [ ] PDF incluye: logo, datos emisor/receptor, detalle, totales, QR con formato SUNAT
- [ ] `docker-compose up` levanta app + PostgreSQL funcional en ~30 segundos
- [ ] Variables de entorno documentadas en `.env.example`
- [ ] Todos los tests pasan
- [ ] App accesible en http://localhost:8080 después de `docker-compose up`

