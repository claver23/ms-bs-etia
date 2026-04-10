package com.etlions.ms_bs_etia.app_util.sunat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CdrParserService — Parseo de CDR SUNAT")
class CdrParserServiceTest {

    private final CdrParserService cdrParser = new CdrParserService();

    @Test
    @DisplayName("CDR aceptado — extrae codigo, descripcion y hash")
    void cdrAceptado() throws IOException {
        String xml = Files.readString(
                Path.of("src/test/resources/sunat/sample-cdr-aceptado.xml"), StandardCharsets.UTF_8);

        CdrResult result = cdrParser.parse(xml);

        assertTrue(result.aceptado());
        assertEquals("0", result.codigoRespuesta());
        assertNotNull(result.descripcion());
        assertTrue(result.descripcion().contains("aceptada"));
        assertNotNull(result.hashFirma());
        assertEquals("abc123hashCDR456def789", result.hashFirma());
    }

    @Test
    @DisplayName("CDR rechazado — codigo distinto de 0, no aceptado")
    void cdrRechazado() throws IOException {
        String xml = Files.readString(
                Path.of("src/test/resources/sunat/sample-cdr-rechazado.xml"), StandardCharsets.UTF_8);

        CdrResult result = cdrParser.parse(xml);

        assertFalse(result.aceptado());
        assertEquals("2100", result.codigoRespuesta());
        assertTrue(result.descripcion().contains("no cumple"));
    }

    @Test
    @DisplayName("CDR con observaciones (codigo 0xxx) — se considera aceptado")
    void cdrConObservaciones() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ar:ApplicationResponse xmlns:ar="urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2"
                    xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                    xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
                    <cac:DocumentResponse>
                        <cac:Response>
                            <cbc:ResponseCode>0100</cbc:ResponseCode>
                            <cbc:Description>Aceptada con observaciones</cbc:Description>
                        </cac:Response>
                    </cac:DocumentResponse>
                </ar:ApplicationResponse>
                """;

        CdrResult result = cdrParser.parse(xml);
        assertTrue(result.aceptado(), "Codigo 0100 debe ser aceptado (observaciones)");
        assertEquals("0100", result.codigoRespuesta());
    }

    @Test
    @DisplayName("CDR null retorna rechazado con codigo 9999")
    void cdrNull() {
        CdrResult result = cdrParser.parse(null);
        assertFalse(result.aceptado());
        assertEquals("9999", result.codigoRespuesta());
    }

    @Test
    @DisplayName("CDR vacío retorna rechazado con codigo 9999")
    void cdrVacio() {
        CdrResult result = cdrParser.parse("   ");
        assertFalse(result.aceptado());
        assertEquals("9999", result.codigoRespuesta());
    }

    @Test
    @DisplayName("CDR XML malformado retorna rechazado con codigo 9999")
    void cdrMalformado() {
        CdrResult result = cdrParser.parse("<esto no es xml valido");
        assertFalse(result.aceptado());
        assertEquals("9999", result.codigoRespuesta());
    }
}

