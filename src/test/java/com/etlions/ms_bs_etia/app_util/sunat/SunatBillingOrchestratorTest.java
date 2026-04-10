package com.etlions.ms_bs_etia.app_util.sunat;

import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SunatBillingOrchestrator — Flujo completo de facturación")
class SunatBillingOrchestratorTest {

    @Mock private SunatSigningService signingService;
    @Mock private SunatZipService zipService;
    @Mock private SunatSoapClient soapClient;
    @Mock private CdrParserService cdrParser;

    @InjectMocks
    private SunatBillingOrchestrator orchestrator;

    private CompanyEntity companyConCert() {
        return CompanyEntity.builder()
                .id("COMP-1")
                .ruc("20517863948")
                .certificadoPath("/certs/empresa.p12")
                .certificadoPassword("certpass")
                .build();
    }

    private CompanyEntity companySinCert() {
        return CompanyEntity.builder()
                .id("COMP-1")
                .ruc("20517863948")
                .build();
    }

    @Test
    @DisplayName("flujo completo exitoso con certificado → ACEPTADA")
    void flujoCompletoExitoso() {
        CompanyEntity company = companyConCert();
        String xml = "<Invoice/>";
        String signedXml = "<Invoice><Signature/></Invoice>";
        byte[] zipBytes = new byte[]{1, 2, 3};
        byte[] cdrZipBytes = new byte[]{4, 5, 6};

        when(signingService.canSign("/certs/empresa.p12", "certpass")).thenReturn(true);
        when(signingService.signXml(xml, "/certs/empresa.p12", "certpass")).thenReturn(signedXml);
        when(zipService.zipXml(eq(signedXml), contains("20517863948-01-F001"))).thenReturn(zipBytes);
        when(soapClient.sendBill(eq(zipBytes), contains(".zip"), eq("20517863948")))
                .thenReturn(new BillResult(true, "mock-hash", "0", "Aceptado", cdrZipBytes));
        when(zipService.unzipFirstXml(cdrZipBytes)).thenReturn("<CDR><ResponseCode>0</ResponseCode></CDR>");
        when(cdrParser.parse(anyString()))
                .thenReturn(CdrResult.accepted("0", "Aceptado", "hash-cdr-real"));

        BillResult result = orchestrator.procesarComprobante(xml, company, "01", "F001", 1);

        assertTrue(result.success());
        assertEquals("0", result.codigoRespuesta());
        assertEquals("hash-cdr-real", result.hashCdr());
        verify(signingService).signXml(xml, "/certs/empresa.p12", "certpass");
        verify(zipService).zipXml(eq(signedXml), anyString());
        verify(soapClient).sendBill(eq(zipBytes), anyString(), eq("20517863948"));
    }

    @Test
    @DisplayName("sin certificado → envía XML sin firmar (modo beta/mock)")
    void sinCertificadoEnviaSinFirmar() {
        CompanyEntity company = companySinCert();
        String xml = "<Invoice/>";
        byte[] zipBytes = new byte[]{1, 2, 3};

        when(zipService.zipXml(eq(xml), contains("20517863948"))).thenReturn(zipBytes);
        when(soapClient.sendBill(eq(zipBytes), anyString(), eq("20517863948")))
                .thenReturn(new BillResult(true, "mock-hash", "0", "Aceptado (MOCK)", null));

        BillResult result = orchestrator.procesarComprobante(xml, company, "01", "F001", 1);

        assertTrue(result.success());
        verify(signingService, never()).signXml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("error de firma → retorna BillResult.error")
    void errorFirma() {
        CompanyEntity company = companyConCert();

        when(signingService.canSign("/certs/empresa.p12", "certpass")).thenReturn(true);
        when(signingService.signXml(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Error al firmar"));

        BillResult result = orchestrator.procesarComprobante("<Invoice/>", company, "01", "F001", 1);

        assertFalse(result.success());
        assertEquals("9999", result.codigoRespuesta());
        assertTrue(result.descripcionRespuesta().contains("firmar"));
    }

    @Test
    @DisplayName("error de SOAP → retorna BillResult.error")
    void errorSoap() {
        CompanyEntity company = companySinCert();
        byte[] zipBytes = new byte[]{1, 2, 3};

        when(zipService.zipXml(anyString(), anyString())).thenReturn(zipBytes);
        when(soapClient.sendBill(any(), anyString(), anyString()))
                .thenThrow(new RuntimeException("SOAP timeout"));

        BillResult result = orchestrator.procesarComprobante("<Invoice/>", company, "01", "F001", 1);

        assertFalse(result.success());
        assertTrue(result.descripcionRespuesta().contains("SUNAT"));
    }

    @Test
    @DisplayName("SUNAT rechaza documento → BillResult con success=false del CDR")
    void sunatRechaza() {
        CompanyEntity company = companySinCert();
        byte[] zipBytes = new byte[]{1, 2, 3};
        byte[] cdrZipBytes = new byte[]{4, 5, 6};

        when(zipService.zipXml(anyString(), anyString())).thenReturn(zipBytes);
        when(soapClient.sendBill(any(), anyString(), anyString()))
                .thenReturn(new BillResult(true, null, "0", "OK", cdrZipBytes));
        when(zipService.unzipFirstXml(cdrZipBytes)).thenReturn("<CDR/>");
        when(cdrParser.parse("<CDR/>"))
                .thenReturn(CdrResult.rejected("2100", "Formato incorrecto"));

        BillResult result = orchestrator.procesarComprobante("<Invoice/>", company, "01", "F001", 1);

        assertFalse(result.success());
        assertEquals("2100", result.codigoRespuesta());
    }

    @Test
    @DisplayName("filename correcto: {RUC}-{tipoDoc}-{serie}-{numero}")
    void filenameFormat() {
        CompanyEntity company = companySinCert();
        byte[] zipBytes = new byte[]{1};

        when(zipService.zipXml(anyString(), anyString())).thenReturn(zipBytes);
        when(soapClient.sendBill(any(), anyString(), anyString()))
                .thenReturn(new BillResult(true, "hash", "0", "OK", null));

        orchestrator.procesarComprobante("<Invoice/>", company, "03", "B001", 42);

        verify(zipService).zipXml(anyString(), eq("20517863948-03-B001-00000042.xml"));
        verify(soapClient).sendBill(any(), eq("20517863948-03-B001-00000042.zip"), eq("20517863948"));
    }

    @Test
    @DisplayName("CDR sin ZIP (null) → retorna resultado de sendBill directamente")
    void cdrSinZip() {
        CompanyEntity company = companySinCert();

        when(zipService.zipXml(anyString(), anyString())).thenReturn(new byte[]{1});
        when(soapClient.sendBill(any(), anyString(), anyString()))
                .thenReturn(new BillResult(true, "mock-hash", "0", "Aceptado (MOCK)", null));

        BillResult result = orchestrator.procesarComprobante("<Invoice/>", company, "01", "F001", 1);

        assertTrue(result.success());
        assertEquals("mock-hash", result.hashCdr());
        verify(cdrParser, never()).parse(anyString());
    }
}

