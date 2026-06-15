package com.etlions.ms_bs_etia.app_util.sunat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementacion real del cliente SOAP para SUNAT.
 * Activo solo con perfil "sunat-real".
 *
 * Endpoints SUNAT:
 * - Beta:  https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService
 * - Prod:  https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService
 *
 * TODO: Implementar llamadas SOAP reales con JAX-WS o Apache CXF
 *       cuando se tenga acceso al servidor beta de SUNAT.
 */
@Slf4j
@Service
@Profile("sunat-real")
public class RealSunatSoapClient implements SunatSoapClient {

    // TODO: @Value("${etia.sunat.endpoint}") String endpoint;

    @Override
    public BillResult sendBill(byte[] zipBytes, String filename, String ruc) {
        log.info("[SUNAT-REAL] sendBill({}, {}) — invocando servicio SOAP...", filename, ruc);
        // TODO: Implementar llamada SOAP real
        // 1. Construir envelope SOAP con zipBytes en Base64
        // 2. Enviar a endpoint SUNAT
        // 3. Parsear respuesta applicationResponse (CDR en Base64)
        // 4. Retornar BillResult
        throw new UnsupportedOperationException(
                "Implementacion SOAP real pendiente. Usar perfil sin 'sunat-real' para mock.");
    }

    @Override
    public SendResult sendSummary(String identificador, String ruc) {
        log.info("[SUNAT-REAL] sendSummary({}, {})", identificador, ruc);
        // TODO: Implementar llamada SOAP real para resumenes diarios
        throw new UnsupportedOperationException(
                "Implementacion SOAP real pendiente para resumenes diarios.");
    }

    @Override
    public StatusResult getStatus(String ticket, String ruc) {
        log.info("[SUNAT-REAL] getStatus({}, {})", ticket, ruc);
        // TODO: Implementar consulta de estado SOAP real
        throw new UnsupportedOperationException(
                "Implementacion SOAP real pendiente para consulta de estado.");
    }
}

