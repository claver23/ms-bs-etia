package com.etlions.ms_bs_etia.app_util.sunat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock de SunatSoapClient para desarrollo y pruebas.
 * <p>
 * Simula respuestas exitosas de SUNAT sin realizar llamadas SOAP reales.
 * <p>
 * TODO: FASE-3 — Reemplazar con implementación real cuando se tenga acceso
 * al servidor beta de SUNAT. Para ello:
 * 1. Crear clase RealSunatSoapClient implements SunatSoapClient
 * 2. Anotarla con @Service @Profile("production")
 * 3. Anotar esta clase con @Profile("!production")
 * 4. Activar perfil "production" en application.yaml
 */
@Slf4j
@Service
public class MockSunatSoapClient implements SunatSoapClient {

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

