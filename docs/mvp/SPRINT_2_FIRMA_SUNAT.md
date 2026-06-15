# 🔏 SPRINT 2 — FIRMA DIGITAL SUNAT + ENVÍO SOAP + CDR

> **Objetivo:** Que SUNAT acepte nuestras facturas  
> **Entregable:** Al emitir factura → firma → envía a SUNAT → recibe CDR → estado real  
> **Dependencias Maven nuevas:** 0 (todo viene en JDK 21)  
> **Archivos nuevos:** ~13 (7 src + 4 tests + 2 test resources)  
> **Archivos modificados:** ~4

---

## 📦 DEPENDENCIAS MAVEN

**Ninguna.** Se usa:
- `java.security.KeyStore` — cargar certificado .p12
- `javax.xml.crypto.dsig.*` — firma XMLDSig
- `java.net.http.HttpClient` — envío SOAP
- `java.util.zip.*` — comprimir/descomprimir ZIP

---

## 📁 ARCHIVOS A CREAR

### 1. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/SunatSigningService.java` — NUEVO

**Responsabilidad:** Firmar XML UBL 2.1 con certificado .p12

```
Flujo:
1. Cargar KeyStore.getInstance("PKCS12") desde certPath
2. Extraer PrivateKey y X509Certificate
3. Parsear XML string a Document
4. Crear XMLSignatureFactory → newSignedInfo con:
   - CanonicalizationMethod.INCLUSIVE
   - SignatureMethod.RSA_SHA256  
   - Reference("") con enveloped transform + DigestMethod.SHA256
5. Crear KeyInfo con X509Data (certificado)
6. Firmar: insertar Signature en ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent
7. Serializar Document a String
```

**Método público:**
```java
public String firmarXml(String xmlContent, String certPath, String certPassword)
```

**Punto clave:** ✅ **VERIFICADO** — `UblInvoiceXmlBuilder.buildUblExtensions()` (líneas 148-152) **SÍ genera** el nodo `<ext:UBLExtensions><ext:UBLExtension><ext:ExtensionContent/>` vacío como placeholder. No se necesita acción adicional.

---

### 2. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/BillResult.java` — NUEVO

```java
public record BillResult(
    boolean exitoso, 
    byte[] cdrZip, 
    String codigoRespuesta, 
    String descripcion
) {}
```

---

### 3. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/CdrResult.java` — NUEVO

```java
public record CdrResult(
    String codigoRespuesta,  // "0" = aceptado, "0xxx" = observaciones, otros = rechazado
    String descripcion,
    String hashCdr           // DigestValue del CDR
) {}
```

---

### 4. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/SunatZipService.java` — NUEVO

**Métodos:**
```java
public byte[] zipXml(String filename, String xmlContent)   // Crea ZIP en memoria
public String unzipFirstEntry(byte[] zipBytes)              // Extrae primer archivo como String
```

---

### 5. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/CdrParserService.java` — NUEVO

**Responsabilidad:** Parsear XML de respuesta SUNAT (ApplicationResponse)

```
Flujo:
1. Recibir byte[] del CDR ZIP
2. Descomprimir con SunatZipService.unzipFirstEntry()
3. Parsear XML → extraer:
   - <cbc:ResponseCode>  → codigoRespuesta
   - <cbc:Description>   → descripcion  
   - <ds:DigestValue>    → hashCdr
4. Retornar CdrResult
```

**Método público:**
```java
public CdrResult parseCdr(byte[] cdrZipBytes)
```

---

### 6. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/RealSunatSoapClient.java` — NUEVO

**Anotación:** `@Service @Profile("sunat-real")`

**Responsabilidad:** Enviar factura/boleta a SUNAT por SOAP

```
Flujo sendBill:
1. Construir SOAP envelope XML con:
   - <soapenv:Envelope xmlns:soapenv="..." xmlns:ser="...">
   - <ser:sendBill>
   -   <fileName>{filename}</fileName>
   -   <contentFile>{base64ZipContent}</contentFile>
   - </ser:sendBill>
2. Enviar HTTP POST con HttpClient al endpoint SUNAT (de SunatProperties)
3. Headers: Content-Type: text/xml, SOAPAction: ""
4. Auth: HTTP Basic con {RUC}{usuario_sol} : {clave_sol}
5. Parsear respuesta SOAP → extraer <applicationResponse> (base64)
6. Decodificar base64 → byte[] del CDR ZIP
7. Retornar BillResult
```

**Timeout:** 30 segundos  
**Retry:** Máximo 2 intentos en caso de timeout/connection error

**Implementa también:** `sendSummary()` y `getStatus()` de la interface existente.

---

### 7. `src/main/java/com/etlions/ms_bs_etia/app_util/sunat/SunatBillingOrchestrator.java` — NUEVO

**Anotación:** `@Service`

**Responsabilidad:** Orquestar el flujo completo de envío

```
Flujo submit:
1. Firmar XML → SunatSigningService.firmarXml(xml, company.certPath, company.certPassword)
2. Construir filename: {RUC}-{tipoDoc}-{serie}-{numero}.xml
3. Zipear → SunatZipService.zipXml(filename, signedXml)
4. Enviar → SunatSoapClient.sendBill(filename.replace(".xml",".zip"), zip, ruc, user, pass)
5. Si exitoso → parsear CDR → CdrParserService.parseCdr(billResult.cdrZip)
6. Determinar estado:
   - codigoRespuesta == "0" → "ACEPTADA"
   - codigoRespuesta empieza con "0" y tiene 4 dígitos → "ACEPTADA" (con observaciones)
   - cualquier otro → "RECHAZADA"
7. Retornar SunatSubmissionResult
```

**Record de resultado:**
```java
public record SunatSubmissionResult(
    String signedXml,
    String codigoRespuesta,
    String descripcion,
    String hashCdr,
    String estadoResultante  // ACEPTADA, RECHAZADA
) {}
```

---

## 📁 ARCHIVOS A MODIFICAR

### 8. `SunatSoapClient.java` — MODIFICAR

**Agregar método:**
```java
BillResult sendBill(String filename, byte[] zipContent, String ruc, String user, String password);
```

---

### 9. `MockSunatSoapClient.java` — MODIFICAR

- Agregar `@Profile("!sunat-real")` a la clase
- Implementar `sendBill()` retornando mock:
  ```java
  return new BillResult(true, mockCdrZipBytes(), "0", "Aceptado (MOCK)");
  ```
- Generar CDR ZIP mock con XML de respuesta simulada

---

### 10. `DocFacturaServiceImpl.java` — MODIFICAR

**Método `generateXmlAndNotify()` (líneas 531-556):**

Flujo actual:
```
XML = ublInvoiceXmlBuilder.buildInvoiceXml(...)
entity.setXmlContent(xml)
xmlStorageService.guardarXml(...)
entity.setEstado("ENVIADA")
save + notify
```

Flujo nuevo:
```
XML = ublInvoiceXmlBuilder.buildInvoiceXml(...)

try {
    result = sunatBillingOrchestrator.submit(xml, company, filename)
    entity.setXmlContent(result.signedXml())       // XML firmado, no el original
    xmlStorageService.guardarXml(...)               // Guardar XML firmado
    entity.setEstado(result.estadoResultante())     // ACEPTADA o RECHAZADA
    entity.setHashCdr(result.hashCdr())
    entity.setCodigoRespuestaSunat(result.codigoRespuesta())
    entity.setDescripcionRespuestaSunat(result.descripcion())
} catch (Exception e) {
    entity.setXmlContent(xml)                       // Guardar XML sin firmar
    entity.setEstado("ERROR_ENVIO")                 // No hacer rollback
    log.error("Error enviando a SUNAT", e)
}

save + notify
```

**Dependencia nueva a inyectar:** `SunatBillingOrchestrator`

---

### 11. `application.yaml` — MODIFICAR

**Agregar bloque `sunat:`:**
```yaml
sunat:
  modo: beta
  endpoints:
    factura-beta: https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService
    factura-prod: https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService
    consulta-cdr-beta: https://e-beta.sunat.gob.pe/ol-it-wsconscpegem-beta/billConsultService
    consulta-cdr-prod: https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService
  certificado:
    base-path: ${ETIA_CERTS_PATH:./certs}
  sol:
    default-user: MODDATOS
    default-password: MODDATOS
```

---

## 🧪 TESTS A CREAR

### 12. `src/test/java/.../sunat/SunatSigningServiceTest.java`
- Test con certificado .p12 autofirmado
- Verificar que el XML firmado contiene `<ds:Signature>`
- Verificar que la firma es válida

### 13. `src/test/java/.../sunat/CdrParserServiceTest.java`
- Test con XML CDR de ejemplo (aceptado)
- Test con XML CDR rechazado
- Verificar extracción correcta de código, descripción, hash

### 14. `src/test/java/.../sunat/SunatZipServiceTest.java`
- Roundtrip: zip → unzip → contenido igual

### 15. `src/test/java/.../sunat/SunatBillingOrchestratorTest.java`
- Mock de SunatSoapClient + SunatSigningService
- Verificar flujo completo con respuesta exitosa
- Verificar manejo de error SOAP

### Test resources:
- `src/test/resources/certs/test-cert.p12` — Certificado autofirmado (generar con keytool)
- `src/test/resources/sunat/sample-cdr-aceptado.xml` — XML CDR de ejemplo SUNAT

---

## ⚠️ PUNTOS TÉCNICOS CLAVE

### Posición de la firma en el XML UBL
SUNAT requiere la firma dentro de:
```xml
<ext:UBLExtensions>
  <ext:UBLExtension>
    <ext:ExtensionContent>
      <!-- AQUÍ va <ds:Signature> -->
    </ext:ExtensionContent>
  </ext:UBLExtension>
</ext:UBLExtensions>
```
**Verificar** que `UblInvoiceXmlBuilder.java` genera este nodo vacío. Si no, agregarlo.

### SOAP Envelope para SUNAT
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.sunat.gob.pe">
  <soapenv:Header/>
  <soapenv:Body>
    <ser:sendBill>
      <fileName>20601234567-01-F001-00000001.zip</fileName>
      <contentFile>{BASE64_DEL_ZIP}</contentFile>
    </ser:sendBill>
  </soapenv:Body>
</soapenv:Envelope>
```

### Tipos de documento SUNAT
- `01` = Factura
- `03` = Boleta
- `07` = Nota de Crédito
- `08` = Nota de Débito

### Nombre del archivo ZIP
Formato: `{RUC}-{TipoDoc}-{Serie}-{Numero}.zip`
Ejemplo: `20601234567-01-F001-00000001.zip`

---

## ⚠️ RIESGOS Y MITIGACIONES

| Riesgo | Probabilidad | Mitigación |
|--------|-------------|------------|
| Firma en posición incorrecta del XML | Media | Verificar UblInvoiceXmlBuilder genera UBLExtensions placeholder |
| SOAP envelope rechazado por SUNAT | Media | Usar envelopes de referencia de la guía CPE SUNAT |
| XMLDSig API cambió en JDK 21 | Baja | Classpath convencional en Spring Boot, no module-path |
| Certificado test inválido | Baja | Generar con keytool: `keytool -genkeypair -alias cert -keyalg RSA -keysize 2048 -storetype PKCS12` |

---

## ✅ CRITERIOS DE DONE

- [ ] Tests de firma digital pasan con certificado de prueba
- [ ] Tests de zip/unzip y parseo CDR pasan
- [ ] Mock SUNAT funciona por defecto (sin profile `sunat-real`)
- [ ] Con profile `sunat-real` + cert beta válido → factura aceptada en SUNAT beta
- [ ] `FacturaEntity` se actualiza con hashCdr, codigoRespuesta, descripcionRespuesta
- [ ] Estado correcto: ACEPTADA / RECHAZADA / ERROR_ENVIO
- [ ] 295+ tests siguen pasando


