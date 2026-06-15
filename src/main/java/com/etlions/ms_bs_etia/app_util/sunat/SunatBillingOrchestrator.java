package com.etlions.ms_bs_etia.app_util.sunat;

import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orquestador del flujo completo de facturacion electronica SUNAT:
 * 1. Firmar XML con certificado digital
 * 2. Comprimir en ZIP
 * 3. Enviar a SUNAT via SOAP (sendBill)
 * 4. Parsear CDR de respuesta
 * 5. Retornar resultado consolidado
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SunatBillingOrchestrator {

    private final SunatSigningService signingService;
    private final SunatZipService zipService;
    private final SunatSoapClient soapClient;
    private final CdrParserService cdrParser;

    /**
     * Ejecuta el flujo completo: firma → zip → envio → parseo CDR.
     *
     * @param xmlContent   XML UBL 2.1 sin firmar
     * @param company      Empresa emisora (contiene certPath, certPassword, RUC)
     * @param tipoDoc      Tipo documento SUNAT: "01"=Factura, "03"=Boleta, "07"=NC, "08"=ND
     * @param serie        Serie del documento (F001, B001, etc.)
     * @param numero       Numero correlativo
     * @return BillResult con el resultado del envio
     */
    public BillResult procesarComprobante(String xmlContent, CompanyEntity company,
                                           String tipoDoc, String serie, int numero) {
        String ruc = company.getRuc();
        String xmlFilename = String.format("%s-%s-%s-%08d.xml", ruc, tipoDoc, serie, numero);
        String zipFilename = xmlFilename.replace(".xml", ".zip");

        log.info("📦 Orquestador SUNAT: procesando {} para RUC={}", xmlFilename, ruc);

        // 1. Firma digital (si hay certificado configurado)
        String signedXml;
        String certPath = company.getCertificadoPath();
        String certPass = company.getCertificadoPassword();

        if (certPath != null && !certPath.isBlank() && signingService.canSign(certPath, certPass)) {
            try {
                signedXml = signingService.signXml(xmlContent, certPath, certPass);
                log.info("✅ XML firmado exitosamente");
            } catch (Exception e) {
                log.error("❌ Error firmando XML: {}", e.getMessage());
                return BillResult.error("Error al firmar XML: " + e.getMessage());
            }
        } else {
            log.warn("⚠️ Sin certificado digital — enviando XML sin firmar (solo valido en entorno beta/mock)");
            signedXml = xmlContent;
        }

        // 2. Comprimir en ZIP
        byte[] zipBytes;
        try {
            zipBytes = zipService.zipXml(signedXml, xmlFilename);
            log.info("✅ ZIP creado: {} ({} bytes)", zipFilename, zipBytes.length);
        } catch (Exception e) {
            log.error("❌ Error comprimiendo XML: {}", e.getMessage());
            return BillResult.error("Error al comprimir XML: " + e.getMessage());
        }

        // 3. Enviar a SUNAT via sendBill
        BillResult billResult;
        try {
            billResult = soapClient.sendBill(zipBytes, zipFilename, ruc);
            log.info("✅ Enviado a SUNAT — respuesta: success={}, codigo={}", 
                     billResult.success(), billResult.codigoRespuesta());
        } catch (Exception e) {
            log.error("❌ Error enviando a SUNAT: {}", e.getMessage());
            return BillResult.error("Error en comunicacion con SUNAT: " + e.getMessage());
        }

        // 4. Si SUNAT devolvio CDR en ZIP, parsearlo
        if (billResult.success() && billResult.cdrZipBytes() != null) {
            try {
                String cdrXml = zipService.unzipFirstXml(billResult.cdrZipBytes());
                if (cdrXml != null) {
                    CdrResult cdr = cdrParser.parse(cdrXml);
                    log.info("✅ CDR parseado: codigo={}, aceptado={}", cdr.codigoRespuesta(), cdr.aceptado());
                    return new BillResult(
                            cdr.aceptado(),
                            cdr.hashFirma(),
                            cdr.codigoRespuesta(),
                            cdr.descripcion(),
                            billResult.cdrZipBytes()
                    );
                }
            } catch (Exception e) {
                log.warn("⚠️ Error parseando CDR: {}", e.getMessage());
            }
        }

        return billResult;
    }
}

