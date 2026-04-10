package com.etlions.ms_bs_etia.app_util.sunat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SunatSigningService — Firma digital XMLDSig")
class SunatSigningServiceTest {

    private static final String CERT_PATH = "src/test/resources/certs/test-cert.p12";
    private static final String CERT_PASS = "testpass";

    private final SunatSigningService signingService = new SunatSigningService();

    private String sampleInvoiceXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                         xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                         xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                         xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2">
                    <ext:UBLExtensions>
                        <ext:UBLExtension>
                            <ext:ExtensionContent/>
                        </ext:UBLExtension>
                    </ext:UBLExtensions>
                    <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
                    <cbc:ID>F001-00000001</cbc:ID>
                    <cbc:IssueDate>2026-04-10</cbc:IssueDate>
                </Invoice>
                """;
    }

    @Test
    @DisplayName("firmar XML con certificado válido produce ds:Signature")
    void firmaExitosa() {
        String certAbsPath = Path.of(CERT_PATH).toAbsolutePath().toString();
        String signedXml = signingService.signXml(sampleInvoiceXml(), certAbsPath, CERT_PASS);

        assertNotNull(signedXml);
        assertTrue(signedXml.contains("Signature"), "El XML firmado debe contener ds:Signature");
        assertTrue(signedXml.contains("SignatureValue"), "Debe tener SignatureValue");
        assertTrue(signedXml.contains("DigestValue"), "Debe tener DigestValue");
        assertTrue(signedXml.contains("X509Certificate"), "Debe incluir el certificado X509");
    }

    @Test
    @DisplayName("firmar XML preserva contenido original")
    void firmaPreservaContenido() {
        String certAbsPath = Path.of(CERT_PATH).toAbsolutePath().toString();
        String signedXml = signingService.signXml(sampleInvoiceXml(), certAbsPath, CERT_PASS);

        assertTrue(signedXml.contains("F001-00000001"));
        assertTrue(signedXml.contains("2026-04-10"));
        assertTrue(signedXml.contains("UBLVersionID"));
    }

    @Test
    @DisplayName("certificado inexistente lanza excepción")
    void certInexistente() {
        assertThrows(RuntimeException.class,
                () -> signingService.signXml(sampleInvoiceXml(), "/no/existe/cert.p12", "pass"));
    }

    @Test
    @DisplayName("password incorrecto lanza excepción")
    void passIncorrecto() {
        String certAbsPath = Path.of(CERT_PATH).toAbsolutePath().toString();
        assertThrows(RuntimeException.class,
                () -> signingService.signXml(sampleInvoiceXml(), certAbsPath, "wrongpass"));
    }

    @Test
    @DisplayName("canSign retorna true con certificado válido")
    void canSignTrue() {
        String certAbsPath = Path.of(CERT_PATH).toAbsolutePath().toString();
        assertTrue(signingService.canSign(certAbsPath, CERT_PASS));
    }

    @Test
    @DisplayName("canSign retorna false con ruta null")
    void canSignNull() {
        assertFalse(signingService.canSign(null, CERT_PASS));
    }

    @Test
    @DisplayName("canSign retorna false con ruta vacía")
    void canSignBlank() {
        assertFalse(signingService.canSign("  ", CERT_PASS));
    }

    @Test
    @DisplayName("canSign retorna false con certificado inexistente")
    void canSignInexistente() {
        assertFalse(signingService.canSign("/no/existe.p12", "pass"));
    }

    @Test
    @DisplayName("XML sin UBLExtensions firma en root")
    void firmaSinExtensions() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
                    <cbc:ID xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">F001-00000001</cbc:ID>
                </Invoice>
                """;
        String certAbsPath = Path.of(CERT_PATH).toAbsolutePath().toString();
        String signedXml = signingService.signXml(xml, certAbsPath, CERT_PASS);
        assertNotNull(signedXml);
        assertTrue(signedXml.contains("Signature"));
    }
}

