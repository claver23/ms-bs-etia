package com.etlions.ms_bs_etia.app_util.sunat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SunatZipService — Comprimir/descomprimir ZIP")
class SunatZipServiceTest {

    private final SunatZipService zipService = new SunatZipService();

    @Test
    @DisplayName("zipXml → unzipFirstXml roundtrip preserva contenido")
    void roundtripOk() {
        String xmlContent = "<Invoice><cbc:ID>F001-00000001</cbc:ID></Invoice>";
        String filename = "20517863948-01-F001-00000001.xml";

        byte[] zipBytes = zipService.zipXml(xmlContent, filename);

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        String extracted = zipService.unzipFirstXml(zipBytes);
        assertEquals(xmlContent, extracted);
    }

    @Test
    @DisplayName("zipXml produce bytes no vacíos")
    void zipProduceBytes() {
        byte[] zip = zipService.zipXml("<root/>", "test.xml");
        assertNotNull(zip);
        assertTrue(zip.length > 10, "ZIP debe tener contenido");
    }

    @Test
    @DisplayName("unzipFirstXml con null retorna null")
    void unzipNull() {
        assertNull(zipService.unzipFirstXml(null));
    }

    @Test
    @DisplayName("unzipFirstXml con array vacío retorna null")
    void unzipEmpty() {
        assertNull(zipService.unzipFirstXml(new byte[0]));
    }

    @Test
    @DisplayName("roundtrip con XML UBL 2.1 grande")
    void roundtripXmlGrande() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\">");
        for (int i = 0; i < 100; i++) {
            sb.append("<cbc:Note>Linea ").append(i).append(" del documento de prueba</cbc:Note>");
        }
        sb.append("</Invoice>");
        String xml = sb.toString();

        byte[] zip = zipService.zipXml(xml, "test-grande.xml");
        String extracted = zipService.unzipFirstXml(zip);
        assertEquals(xml, extracted);
    }
}

