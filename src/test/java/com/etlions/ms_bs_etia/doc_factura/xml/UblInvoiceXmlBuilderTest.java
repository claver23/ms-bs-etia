package com.etlions.ms_bs_etia.doc_factura.xml;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.AllowanceChargeEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UblInvoiceXmlBuilder — Generador XML UBL 2.1")
class UblInvoiceXmlBuilderTest {

    private UblInvoiceXmlBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new UblInvoiceXmlBuilder();
    }

    // ─── Helpers ───

    private CompanyEntity sampleCompany() {
        return CompanyEntity.builder()
                .id("COMP-1")
                .ruc("20517863948")
                .razonSocial("ETLIONS TECHNOLOGY S.A.C.")
                .nombreComercial("ETLIONS")
                .codigoUbigeo("150131")
                .direccion("AV. JAVIER PRADO ESTE 1234")
                .departamento("LIMA")
                .provincia("LIMA")
                .distrito("SAN ISIDRO")
                .build();
    }

    private ClientEntity sampleClient() {
        return ClientEntity.builder()
                .id("CLI-1")
                .tipoDocumento("6")
                .numeroDocumento("20387589181")
                .denominacion("CORPORACION ACME S.A.C.")
                .direccion("AV. AREQUIPA 456")
                .departamento("LIMA")
                .provincia("LIMA")
                .distrito("MIRAFLORES")
                .codigoPais("PE")
                .build();
    }

    private FacturaEntity sampleFactura() {
        // Detalle JSON con un item gravado y campos tributarios completos
        String detalleJson = """
                [
                  {
                    "codigo": "PROD-001",
                    "descripcion": "LAPTOP HP PROBOOK 450 G10",
                    "unidadMedida": "NIU",
                    "cantidad": 2,
                    "valorUnitario": 3389.83,
                    "precioUnitario": 4000.00,
                    "baseImponible": 6779.66,
                    "montoImpuesto": 1220.34,
                    "porcentajeIgv": 18.00,
                    "tipoAfectacionIgv": "10",
                    "codigoTributo": "1000",
                    "tipoPrecio": "01"
                  },
                  {
                    "codigo": "SERV-002",
                    "descripcion": "SERVICIO DE CONFIGURACION",
                    "unidadMedida": "ZZ",
                    "cantidad": 1,
                    "valorUnitario": 500.00,
                    "precioUnitario": 590.00,
                    "baseImponible": 500.00,
                    "montoImpuesto": 90.00,
                    "porcentajeIgv": 18.00,
                    "tipoAfectacionIgv": "10",
                    "codigoTributo": "1000",
                    "tipoPrecio": "01"
                  }
                ]
                """;

        return FacturaEntity.builder()
                .id("FAC-001")
                .companyId("COMP-1")
                .clientId("CLI-1")
                .serie("F003")
                .numero(1716)
                .fechaEmision(LocalDate.of(2026, 4, 5))
                .horaEmision(LocalTime.of(12, 30, 0))
                .fechaVencimiento(LocalDate.of(2026, 4, 12))
                .moneda("PEN")
                .formaPago("Contado")
                .tipoOperacion("0101")
                .gravadas(new BigDecimal("7279.66"))
                .exoneradas(BigDecimal.ZERO)
                .inafectas(BigDecimal.ZERO)
                .gratuitas(BigDecimal.ZERO)
                .igv(new BigDecimal("1310.34"))
                .total(new BigDecimal("8590.00"))
                .subtotal(new BigDecimal("7279.66"))
                .totalConImpuestos(new BigDecimal("8590.00"))
                .totalCargos(BigDecimal.ZERO)
                .totalDescuentos(BigDecimal.ZERO)
                .leyenda("OCHO MIL QUINIENTOS NOVENTA CON 00/100 SOLES")
                .detalleJson(detalleJson)
                .build();
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder db = factory.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("XML bien formado")
    class XmlBienFormado {

        @Test @DisplayName("genera XML válido y parseable")
        void xmlValido() throws Exception {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());

            assertNotNull(xml);
            assertFalse(xml.isBlank());
            // Debe ser XML bien formado
            Document doc = parseXml(xml);
            assertNotNull(doc);
            assertEquals("Invoice", doc.getDocumentElement().getLocalName());
        }

        @Test @DisplayName("contiene declaración XML con UTF-8")
        void declaracionXml() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.startsWith("<?xml"));
            assertTrue(xml.contains("UTF-8"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Namespaces UBL 2.1")
    class Namespaces {

        @Test @DisplayName("contiene todos los namespaces requeridos")
        void namespacesPresentes() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());

            assertTrue(xml.contains("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"));
            assertTrue(xml.contains("urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"));
            assertTrue(xml.contains("urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"));
            assertTrue(xml.contains("urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"));
            assertTrue(xml.contains("http://www.w3.org/2000/09/xmldsig#"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Identificación del documento")
    class IdentificacionDoc {

        @Test @DisplayName("ID = serie-numero")
        void idCorrecto() throws Exception {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            Document doc = parseXml(xml);
            String ns = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
            NodeList ids = doc.getElementsByTagNameNS(ns, "ID");
            // El primer cbc:ID es el del documento
            assertEquals("F003-00001716", ids.item(0).getTextContent());
        }

        @Test @DisplayName("UBLVersionID = 2.1")
        void ublVersion() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">2.1</cbc:UBLVersionID>"));
        }

        @Test @DisplayName("CustomizationID = 2.0")
        void customization() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">2.0</cbc:CustomizationID>"));
        }

        @Test @DisplayName("InvoiceTypeCode = 01 con listID=0101")
        void invoiceTypeCode() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">01</cbc:InvoiceTypeCode>"));
            assertTrue(xml.contains("listID=\"0101\""));
        }

        @Test @DisplayName("fechas correctas")
        void fechas() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">2026-04-05</cbc:IssueDate>"));
            assertTrue(xml.contains(">12:30:00</cbc:IssueTime>"));
            assertTrue(xml.contains(">2026-04-12</cbc:DueDate>"));
        }

        @Test @DisplayName("moneda PEN")
        void moneda() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">PEN</cbc:DocumentCurrencyCode>"));
        }

        @Test @DisplayName("leyenda presente")
        void leyenda() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("OCHO MIL QUINIENTOS NOVENTA CON 00/100 SOLES"));
            assertTrue(xml.contains("languageLocaleID=\"1000\""));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Emisor y Receptor")
    class EmisorReceptor {

        @Test @DisplayName("RUC del emisor")
        void rucEmisor() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">20517863948</cbc:ID>"));
            assertTrue(xml.contains("schemeID=\"6\""));
        }

        @Test @DisplayName("razón social del emisor")
        void razonSocial() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">ETLIONS TECHNOLOGY S.A.C.</cbc:RegistrationName>"));
        }

        @Test @DisplayName("documento del receptor")
        void docReceptor() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">20387589181</cbc:ID>"));
            assertTrue(xml.contains(">CORPORACION ACME S.A.C.</cbc:RegistrationName>"));
        }

        @Test @DisplayName("dirección del emisor")
        void direccion() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">AV. JAVIER PRADO ESTE 1234</cbc:Line>"));
            assertTrue(xml.contains(">150131</cbc:ID>"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Líneas de detalle (InvoiceLine)")
    class LineasDetalle {

        @Test @DisplayName("2 líneas de detalle")
        void dosLineas() throws Exception {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            Document doc = parseXml(xml);
            String ns = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
            NodeList lines = doc.getElementsByTagNameNS(ns, "InvoiceLine");
            assertEquals(2, lines.getLength());
        }

        @Test @DisplayName("LineCountNumeric = 2")
        void lineCount() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">2</cbc:LineCountNumeric>"));
        }

        @Test @DisplayName("descripciones de items presentes")
        void descripciones() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("LAPTOP HP PROBOOK 450 G10"));
            assertTrue(xml.contains("SERVICIO DE CONFIGURACION"));
        }

        @Test @DisplayName("códigos de producto presentes")
        void codigos() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">PROD-001</cbc:ID>"));
            assertTrue(xml.contains(">SERV-002</cbc:ID>"));
        }

        @Test @DisplayName("unidades de medida correctas")
        void unidades() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("unitCode=\"NIU\""));
            assertTrue(xml.contains("unitCode=\"ZZ\""));
        }

        @Test @DisplayName("impuestos por línea — IGV 1000")
        void igvPorLinea() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            // Cat.05 — tributo 1000 = IGV
            assertTrue(xml.contains(">1000</cbc:ID>"));
            assertTrue(xml.contains(">IGV</cbc:Name>"));
            assertTrue(xml.contains(">VAT</cbc:TaxTypeCode>"));
            // Cat.07 — tipo afectación 10 = Gravado
            assertTrue(xml.contains(">10</cbc:TaxExemptionReasonCode>"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Impuestos y totales")
    class ImpuestosTotales {

        @Test @DisplayName("TaxTotal con IGV")
        void taxTotal() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">1310.34</cbc:TaxAmount>"));
        }

        @Test @DisplayName("LegalMonetaryTotal — PayableAmount")
        void payableAmount() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">8590.00</cbc:PayableAmount>"));
        }

        @Test @DisplayName("LegalMonetaryTotal — LineExtensionAmount")
        void lineExtension() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">7279.66</cbc:LineExtensionAmount>"));
        }

        @Test @DisplayName("currencyID=PEN en montos")
        void currency() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("currencyID=\"PEN\""));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Forma de pago")
    class FormaPago {

        @Test @DisplayName("Contado — sin monto")
        void contado() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains(">Contado</cbc:PaymentMeansID>"));
        }

        @Test @DisplayName("Crédito con cuotas")
        void credito() {
            FacturaEntity f = sampleFactura();
            f.setFormaPago("Credito");

            CuotaCreditoEntity cuota1 = CuotaCreditoEntity.builder()
                    .numeroCuota("Cuota001")
                    .monto(new BigDecimal("4295.00"))
                    .fechaVencimiento(LocalDate.of(2026, 5, 5))
                    .moneda("PEN")
                    .build();
            CuotaCreditoEntity cuota2 = CuotaCreditoEntity.builder()
                    .numeroCuota("Cuota002")
                    .monto(new BigDecimal("4295.00"))
                    .fechaVencimiento(LocalDate.of(2026, 6, 5))
                    .moneda("PEN")
                    .build();

            String xml = builder.buildInvoiceXml(
                    f, sampleCompany(), sampleClient(), List.of(cuota1, cuota2), List.of());

            assertTrue(xml.contains(">Credito</cbc:PaymentMeansID>"));
            assertTrue(xml.contains(">Cuota001</cbc:PaymentMeansID>"));
            assertTrue(xml.contains(">Cuota002</cbc:PaymentMeansID>"));
            assertTrue(xml.contains(">4295.00</cbc:Amount>"));
            assertTrue(xml.contains(">2026-05-05</cbc:PaymentDueDate>"));
            assertTrue(xml.contains(">2026-06-05</cbc:PaymentDueDate>"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Cargos y descuentos globales")
    class CargosDescuentos {

        @Test @DisplayName("cargo global presente")
        void cargo() {
            AllowanceChargeEntity cargo = AllowanceChargeEntity.builder()
                    .chargeIndicator(true)
                    .reasonCode("50")
                    .amount(new BigDecimal("100.00"))
                    .baseAmount(new BigDecimal("1000.00"))
                    .moneda("PEN")
                    .build();

            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of(cargo));

            assertTrue(xml.contains(">true</cbc:ChargeIndicator>"));
            assertTrue(xml.contains(">50</cbc:AllowanceChargeReasonCode>"));
            assertTrue(xml.contains("catalogo53"));
        }

        @Test @DisplayName("descuento global presente")
        void descuento() {
            AllowanceChargeEntity desc = AllowanceChargeEntity.builder()
                    .chargeIndicator(false)
                    .reasonCode("02")
                    .amount(new BigDecimal("50.00"))
                    .multiplierFactor(new BigDecimal("0.05"))
                    .baseAmount(new BigDecimal("1000.00"))
                    .moneda("PEN")
                    .build();

            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of(desc));

            assertTrue(xml.contains(">false</cbc:ChargeIndicator>"));
            assertTrue(xml.contains(">0.05</cbc:MultiplierFactorNumeric>"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("UBLExtensions (firma digital)")
    class UblExtensions {

        @Test @DisplayName("placeholder ExtensionContent presente")
        void extensionPresente() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("ext:UBLExtensions"));
            assertTrue(xml.contains("ext:UBLExtension"));
            assertTrue(xml.contains("ext:ExtensionContent"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Items con diferentes tipos de tributo")
    class TiposTributo {

        @Test @DisplayName("item exonerado — 9997 EXO")
        void exonerado() {
            FacturaEntity f = sampleFactura();
            f.setExoneradas(new BigDecimal("500.00"));
            f.setDetalleJson("""
                    [{
                      "descripcion": "LIBRO EDUCATIVO",
                      "cantidad": 1,
                      "valorUnitario": 500.00,
                      "precioUnitario": 500.00,
                      "baseImponible": 500.00,
                      "montoImpuesto": 0,
                      "porcentajeIgv": 0,
                      "tipoAfectacionIgv": "20",
                      "codigoTributo": "9997",
                      "tipoPrecio": "01"
                    }]
                    """);

            String xml = builder.buildInvoiceXml(
                    f, sampleCompany(), sampleClient(), List.of(), List.of());

            assertTrue(xml.contains(">9997</cbc:ID>"));
            assertTrue(xml.contains(">EXO</cbc:Name>"));
            assertTrue(xml.contains(">20</cbc:TaxExemptionReasonCode>"));
        }

        @Test @DisplayName("item inafecto — 9998 INA")
        void inafecto() {
            FacturaEntity f = sampleFactura();
            f.setInafectas(new BigDecimal("300.00"));
            f.setDetalleJson("""
                    [{
                      "descripcion": "SERVICIO EDUCATIVO",
                      "cantidad": 1,
                      "valorUnitario": 300.00,
                      "precioUnitario": 300.00,
                      "baseImponible": 300.00,
                      "montoImpuesto": 0,
                      "porcentajeIgv": 0,
                      "tipoAfectacionIgv": "30",
                      "codigoTributo": "9998",
                      "tipoPrecio": "01"
                    }]
                    """);

            String xml = builder.buildInvoiceXml(
                    f, sampleCompany(), sampleClient(), List.of(), List.of());

            assertTrue(xml.contains(">9998</cbc:ID>"));
            assertTrue(xml.contains(">INA</cbc:Name>"));
            assertTrue(xml.contains(">FRE</cbc:TaxTypeCode>"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    @Nested @DisplayName("Catálogos SUNAT — URIs y atributos")
    class CatalogosSunat {

        @Test @DisplayName("Cat.05 — Código de tributos URI")
        void cat05() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo05"));
        }

        @Test @DisplayName("Cat.06 — Documento de identidad URI")
        void cat06() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06"));
        }

        @Test @DisplayName("Cat.07 — Afectación del IGV URI")
        void cat07() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07"));
        }

        @Test @DisplayName("Cat.16 — Tipo de precio URI")
        void cat16() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16"));
        }

        @Test @DisplayName("schemeAgencyName=PE:SUNAT presente")
        void agencyName() {
            String xml = builder.buildInvoiceXml(
                    sampleFactura(), sampleCompany(), sampleClient(), List.of(), List.of());
            assertTrue(xml.contains("schemeAgencyName=\"PE:SUNAT\""));
        }
    }
}

