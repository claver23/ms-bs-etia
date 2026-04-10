package com.etlions.ms_bs_etia.app_util.service;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.util.LogoUtil;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorServiceTest {

    @Mock
    private LogoUtil logoUtil;

    private QrCodeService qrCodeService;
    private PdfGeneratorService pdfGeneratorService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
        pdfGeneratorService = new PdfGeneratorService(qrCodeService, logoUtil);
    }

    @Test
    @DisplayName("generateFacturaPdf — genera PDF no vacío con magic bytes %PDF")
    void generateFacturaPdf_returnsValidPdf() {
        when(logoUtil.getLogoBytes(anyString())).thenReturn(Optional.empty());

        CompanyEntity company = CompanyEntity.builder()
                .ruc("20601234567")
                .razonSocial("ETLIONS SAC")
                .direccion("Av. Javier Prado Este 1234")
                .departamento("LIMA")
                .provincia("LIMA")
                .distrito("SAN ISIDRO")
                .build();

        ClientEntity client = ClientEntity.builder()
                .tipoDocumento("6")
                .numeroDocumento("20505050501")
                .denominacion("CLIENTE PRUEBA SAC")
                .direccion("Av. Arequipa 999")
                .build();

        FacturaEntity factura = FacturaEntity.builder()
                .serie("F001")
                .numero(1)
                .fechaEmision(LocalDate.of(2026, 4, 9))
                .moneda("PEN")
                .formaPago("CONTADO")
                .tipoOperacion("0101")
                .companyId("company-123")
                .clientId("client-456")
                .gravadas(new BigDecimal("2500.00"))
                .igv(new BigDecimal("450.00"))
                .total(new BigDecimal("2950.00"))
                .leyenda("SON DOS MIL NOVECIENTOS CINCUENTA Y 00/100 SOLES")
                .hashCdr("abc123hash")
                .codigoRespuestaSunat("0")
                .build();

        List<Map<String, Object>> detalle = List.of(
                Map.of(
                        "descripcion", "Consultoría TI",
                        "unidadMedida", "ZZ",
                        "cantidad", "1",
                        "precioUnitario", "2500.00",
                        "subtotal", "2500.00"
                )
        );

        byte[] pdfBytes = pdfGeneratorService.generateFacturaPdf(factura, company, client, detalle);

        assertNotNull(pdfBytes, "El PDF no debe ser null");
        assertTrue(pdfBytes.length > 0, "El PDF no debe estar vacío");

        // Verificar magic bytes: %PDF
        assertEquals((byte) '%', pdfBytes[0]);
        assertEquals((byte) 'P', pdfBytes[1]);
        assertEquals((byte) 'D', pdfBytes[2]);
        assertEquals((byte) 'F', pdfBytes[3]);
    }

    @Test
    @DisplayName("generateBoletaPdf — genera PDF válido para boleta")
    void generateBoletaPdf_returnsValidPdf() {
        when(logoUtil.getLogoBytes(anyString())).thenReturn(Optional.empty());

        CompanyEntity company = CompanyEntity.builder()
                .ruc("20601234567")
                .razonSocial("ETLIONS SAC")
                .direccion("Av. Javier Prado Este 1234")
                .build();

        ClientEntity client = ClientEntity.builder()
                .tipoDocumento("1")
                .numeroDocumento("12345678")
                .denominacion("JUAN PÉREZ")
                .build();

        BoletaEntity boleta = BoletaEntity.builder()
                .serie("B001")
                .numero(1)
                .fechaEmision(LocalDate.of(2026, 4, 9))
                .moneda("PEN")
                .formaPago("CONTADO")
                .tipoOperacion("0101")
                .companyId("company-123")
                .clientId("client-789")
                .gravadas(new BigDecimal("100.00"))
                .igv(new BigDecimal("18.00"))
                .total(new BigDecimal("118.00"))
                .build();

        List<Map<String, Object>> detalle = List.of(
                Map.of(
                        "descripcion", "Producto de prueba",
                        "unidadMedida", "NIU",
                        "cantidad", "2",
                        "precioUnitario", "50.00",
                        "subtotal", "100.00"
                )
        );

        byte[] pdfBytes = pdfGeneratorService.generateBoletaPdf(boleta, company, client, detalle);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100, "El PDF debe tener tamaño razonable");

        // Magic bytes %PDF
        assertEquals((byte) '%', pdfBytes[0]);
        assertEquals((byte) 'P', pdfBytes[1]);
        assertEquals((byte) 'D', pdfBytes[2]);
        assertEquals((byte) 'F', pdfBytes[3]);
    }

    @Test
    @DisplayName("generateFacturaPdf — funciona con valores null opcionales")
    void generateFacturaPdf_nullOptionalFields_doesNotFail() {
        when(logoUtil.getLogoBytes(anyString())).thenReturn(Optional.empty());

        CompanyEntity company = CompanyEntity.builder()
                .ruc("20601234567")
                .razonSocial("EMPRESA TEST")
                .build();

        ClientEntity client = ClientEntity.builder()
                .tipoDocumento("6")
                .numeroDocumento("20999999999")
                .denominacion("CLIENTE TEST")
                .build();

        FacturaEntity factura = FacturaEntity.builder()
                .serie("F001")
                .numero(99)
                .fechaEmision(LocalDate.now())
                .moneda("PEN")
                .companyId("c1")
                .clientId("cl1")
                .gravadas(new BigDecimal("1000.00"))
                .igv(new BigDecimal("180.00"))
                .total(new BigDecimal("1180.00"))
                // leyenda, hashCdr, codigoRespuestaSunat → null
                .build();

        List<Map<String, Object>> detalle = List.of(
                Map.of("descripcion", "Item test", "cantidad", "1", "precioUnitario", "1000", "subtotal", "1000")
        );

        assertDoesNotThrow(() ->
                pdfGeneratorService.generateFacturaPdf(factura, company, client, detalle)
        );
    }
}

