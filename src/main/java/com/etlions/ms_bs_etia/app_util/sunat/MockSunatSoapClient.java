package com.etlions.ms_bs_etia.app_util.sunat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock de SunatSoapClient para desarrollo y pruebas.
 * Simula respuestas exitosas de SUNAT sin realizar llamadas SOAP reales.
 * Activo cuando NO se usa el perfil "sunat-real".
 */
@Slf4j
@Service
@Profile("!sunat-real")
public class MockSunatSoapClient implements SunatSoapClient {

    @Override
    public BillResult sendBill(byte[] zipBytes, String filename, String ruc) {
        log.info("[MOCK-SUNAT] sendBill({}, {}) → aceptado", filename, ruc);
        String mockHash = "mock-hash-" + System.currentTimeMillis();
        return new BillResult(true, mockHash, "0", "La Factura fue aceptada (MOCK)", null);
    }

    @Override
    public SendResult sendSummary(String identificador, String ruc) {
        String ticket = "mock-ticket-" + System.currentTimeMillis();
        log.info("[MOCK-SUNAT] sendSummary({}, {}) → ticket={}", identificador, ruc, ticket);
        return new SendResult(true, ticket, "Resumen recibido exitosamente (MOCK)");
    }

    @Override
    public StatusResult getStatus(String ticket, String ruc) {
        log.info("[MOCK-SUNAT] getStatus({}, {})", ticket, ruc);
        // Mock: siempre devuelve ACEPTADO
        return new StatusResult(true, "ACEPTADO", "0",
                "El Resumen diario ha sido aceptado (MOCK)");
    }
}

