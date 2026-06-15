package com.etlions.ms_bs_etia.doc_factura.xml;

import com.etlions.ms_bs_etia.app_util.validation.SunatFormatter;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.AllowanceChargeEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 🧾 Generador de XML UBL 2.1 para Facturas Electrónicas SUNAT — Perú
 * <p>
 * Construye un documento XML 100% compatible con el estándar UBL 2.1
 * requerido por SUNAT para facturación electrónica.
 * <p>
 * Namespaces:
 * - Invoice: urn:oasis:names:specification:ubl:schema:xsd:Invoice-2
 * - cac: CommonAggregateComponents-2
 * - cbc: CommonBasicComponents-2
 * - ext: CommonExtensionComponents-2
 * - ds: xmldsig (para firma digital)
 */
@Slf4j
@Component
public class UblInvoiceXmlBuilder {

    /* ═══ Namespaces UBL 2.1 ═══ */
    private static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
    private static final String NS_DS  = "http://www.w3.org/2000/09/xmldsig#";

    /* ═══ Constantes SUNAT ═══ */
    private static final String SUNAT_AGENCY   = "PE:SUNAT";
    private static final String UNECE_AGENCY   = "United Nations Economic Commission for Europe";
    private static final String CAT05_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo05";
    private static final String CAT06_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06";
    private static final String CAT07_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07";
    private static final String CAT16_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16";
    private static final String CAT51_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo51";
    private static final String CAT53_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo53";
    private static final String CAT01_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Genera el XML UBL 2.1 completo para una factura electrónica (tipo "01").
     */
    public String buildInvoiceXml(FacturaEntity factura,
                                   CompanyEntity company,
                                   ClientEntity client,
                                   List<CuotaCreditoEntity> cuotas,
                                   List<AllowanceChargeEntity> cargosDescuentos) {
        return buildXml(XmlBuildContext.fromFactura(factura), company, client, cuotas, cargosDescuentos);
    }

    /**
     * Genera el XML UBL 2.1 completo para una boleta de venta (tipo "03").
     */
    public String buildBoletaXml(BoletaEntity boleta,
                                  CompanyEntity company,
                                  ClientEntity client,
                                  List<CuotaCreditoEntity> cuotas,
                                  List<AllowanceChargeEntity> cargosDescuentos) {
        return buildXml(XmlBuildContext.fromBoleta(boleta), company, client, cuotas, cargosDescuentos);
    }

    /**
     * Método unificado: genera XML UBL 2.1 desde XmlBuildContext (factura "01" o boleta "03").
     */
    public String buildXml(XmlBuildContext ctx,
                            CompanyEntity company,
                            ClientEntity client,
                            List<CuotaCreditoEntity> cuotas,
                            List<AllowanceChargeEntity> cargosDescuentos) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            doc.setXmlStandalone(true);

            // ── Root: <Invoice>
            Element invoice = doc.createElementNS(NS_INVOICE, "Invoice");
            invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cac", NS_CAC);
            invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cbc", NS_CBC);
            invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ext", NS_EXT);
            invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", NS_DS);
            doc.appendChild(invoice);

            String moneda = ctx.moneda() != null ? ctx.moneda() : "PEN";

            // ── Parsear detalle items
            List<Map<String, Object>> items = parseDetalleJson(ctx.detalleJson());

            // ═══ Secciones del XML ═══
            buildUblExtensions(doc, invoice);
            buildMetadata(doc, invoice);
            buildDocumentId(doc, invoice, ctx);
            buildInvoiceTypeCode(doc, invoice, ctx);
            buildNote(doc, invoice, ctx);
            buildCurrencyCode(doc, invoice, moneda);
            buildLineCountNumeric(doc, invoice, items.size());
            buildSupplierParty(doc, invoice, company);
            buildCustomerParty(doc, invoice, client);
            buildPaymentTerms(doc, invoice, ctx, cuotas, moneda);
            buildAllowanceCharges(doc, invoice, cargosDescuentos, moneda);
            buildTaxTotal(doc, invoice, ctx, moneda);
            buildLegalMonetaryTotal(doc, invoice, ctx, moneda);
            buildInvoiceLines(doc, invoice, items, moneda);

            // ── Serializar a String
            return serializeToString(doc);

        } catch (Exception e) {
            log.error("Error generando XML UBL 2.1 para {} {}-{}: {}",
                    ctx.tipoDocumento(), ctx.serie(), ctx.numero(), e.getMessage(), e);
            throw new RuntimeException("Error generando XML UBL 2.1: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SECCIONES DEL XML
    // ═══════════════════════════════════════════════════════════════

    /** UBLExtensions — placeholder para firma digital futura */
    private void buildUblExtensions(Document doc, Element invoice) {
        Element extensions = appendElement(doc, invoice, NS_EXT, "ext:UBLExtensions");
        Element extension = appendElement(doc, extensions, NS_EXT, "ext:UBLExtension");
        appendElement(doc, extension, NS_EXT, "ext:ExtensionContent");
    }

    /** UBLVersionID + CustomizationID */
    private void buildMetadata(Document doc, Element invoice) {
        appendTextElement(doc, invoice, NS_CBC, "cbc:UBLVersionID", "2.1");
        Element custom = appendTextElement(doc, invoice, NS_CBC, "cbc:CustomizationID", "2.0");
        custom.setAttribute("schemeAgencyName", SUNAT_AGENCY);
    }

    /** ID, IssueDate, IssueTime, DueDate */
    private void buildDocumentId(Document doc, Element invoice, XmlBuildContext ctx) {
        String id = SunatFormatter.formatNumeroComprobante(ctx.serie(), ctx.numero());
        appendTextElement(doc, invoice, NS_CBC, "cbc:ID", id);
        appendTextElement(doc, invoice, NS_CBC, "cbc:IssueDate",
                ctx.fechaEmision().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (ctx.horaEmision() != null) {
            appendTextElement(doc, invoice, NS_CBC, "cbc:IssueTime",
                    ctx.horaEmision().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        if (ctx.fechaVencimiento() != null) {
            appendTextElement(doc, invoice, NS_CBC, "cbc:DueDate",
                    ctx.fechaVencimiento().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    /** InvoiceTypeCode — Cat.01 (tipo doc) + Cat.51 (tipo operación) */
    private void buildInvoiceTypeCode(Document doc, Element invoice, XmlBuildContext ctx) {
        Element typeCode = appendTextElement(doc, invoice, NS_CBC, "cbc:InvoiceTypeCode", ctx.tipoDocumento());
        typeCode.setAttribute("listAgencyName", SUNAT_AGENCY);
        typeCode.setAttribute("listID", ctx.tipoOperacion() != null ? ctx.tipoOperacion() : "0101");
        typeCode.setAttribute("listName", "Tipo de Documento");
        typeCode.setAttribute("listSchemeURI", CAT51_URI);
        typeCode.setAttribute("listURI", CAT01_URI);
        typeCode.setAttribute("name", "Tipo de Operacion");
    }

    /** Note — Leyenda / monto en letras (Cat.52) */
    private void buildNote(Document doc, Element invoice, XmlBuildContext ctx) {
        if (ctx.leyenda() != null && !ctx.leyenda().isBlank()) {
            Element note = appendTextElement(doc, invoice, NS_CBC, "cbc:Note", ctx.leyenda());
            note.setAttribute("languageLocaleID", "1000");
        }
    }

    /** DocumentCurrencyCode — Cat.02 */
    private void buildCurrencyCode(Document doc, Element invoice, String moneda) {
        Element curr = appendTextElement(doc, invoice, NS_CBC, "cbc:DocumentCurrencyCode", moneda);
        curr.setAttribute("listAgencyName", UNECE_AGENCY);
        curr.setAttribute("listName", "Currency");
    }

    /** LineCountNumeric */
    private void buildLineCountNumeric(Document doc, Element invoice, int count) {
        appendTextElement(doc, invoice, NS_CBC, "cbc:LineCountNumeric", String.valueOf(count));
    }

    /** AccountingSupplierParty — Emisor */
    private void buildSupplierParty(Document doc, Element invoice, CompanyEntity c) {
        Element supplier = appendElement(doc, invoice, NS_CAC, "cac:AccountingSupplierParty");
        Element party = appendElement(doc, supplier, NS_CAC, "cac:Party");

        // PartyIdentification
        Element partyId = appendElement(doc, party, NS_CAC, "cac:PartyIdentification");
        Element id = appendTextElement(doc, partyId, NS_CBC, "cbc:ID", c.getRuc());
        id.setAttribute("schemeAgencyName", SUNAT_AGENCY);
        id.setAttribute("schemeID", "6");
        id.setAttribute("schemeName", "Documento de Identidad");
        id.setAttribute("schemeURI", CAT06_URI);

        // PartyName (nombre comercial, opcional)
        if (c.getNombreComercial() != null && !c.getNombreComercial().isBlank()) {
            Element partyName = appendElement(doc, party, NS_CAC, "cac:PartyName");
            appendTextElement(doc, partyName, NS_CBC, "cbc:Name", c.getNombreComercial());
        }

        // PartyLegalEntity
        Element legal = appendElement(doc, party, NS_CAC, "cac:PartyLegalEntity");
        appendTextElement(doc, legal, NS_CBC, "cbc:RegistrationName", c.getRazonSocial());

        // RegistrationAddress
        Element addr = appendElement(doc, legal, NS_CAC, "cac:RegistrationAddress");
        Element ubigeo = appendTextElement(doc, addr, NS_CBC, "cbc:ID",
                c.getCodigoUbigeo() != null ? c.getCodigoUbigeo() : "150101");
        ubigeo.setAttribute("schemeAgencyName", "PE:INEI");
        ubigeo.setAttribute("schemeName", "Ubigeos");

        Element addrType = appendTextElement(doc, addr, NS_CBC, "cbc:AddressTypeCode", "0001");
        addrType.setAttribute("listAgencyName", SUNAT_AGENCY);

        appendTextElement(doc, addr, NS_CBC, "cbc:CityName", safe(c.getProvincia()));
        appendTextElement(doc, addr, NS_CBC, "cbc:CountrySubentity", safe(c.getDepartamento()));
        appendTextElement(doc, addr, NS_CBC, "cbc:District", safe(c.getDistrito()));

        Element addrLine = appendElement(doc, addr, NS_CAC, "cac:AddressLine");
        appendTextElement(doc, addrLine, NS_CBC, "cbc:Line", safe(c.getDireccion()));

        Element country = appendElement(doc, addr, NS_CAC, "cac:Country");
        Element countryCode = appendTextElement(doc, country, NS_CBC, "cbc:IdentificationCode", "PE");
        countryCode.setAttribute("listAgencyName", UNECE_AGENCY);
    }

    /** AccountingCustomerParty — Receptor */
    private void buildCustomerParty(Document doc, Element invoice, ClientEntity cl) {
        Element customer = appendElement(doc, invoice, NS_CAC, "cac:AccountingCustomerParty");
        Element party = appendElement(doc, customer, NS_CAC, "cac:Party");

        // PartyIdentification
        Element partyId = appendElement(doc, party, NS_CAC, "cac:PartyIdentification");
        Element id = appendTextElement(doc, partyId, NS_CBC, "cbc:ID", cl.getNumeroDocumento());
        id.setAttribute("schemeAgencyName", SUNAT_AGENCY);
        id.setAttribute("schemeID", cl.getTipoDocumento() != null ? cl.getTipoDocumento() : "6");
        id.setAttribute("schemeName", "Documento de Identidad");
        id.setAttribute("schemeURI", CAT06_URI);

        // PartyLegalEntity
        Element legal = appendElement(doc, party, NS_CAC, "cac:PartyLegalEntity");
        appendTextElement(doc, legal, NS_CBC, "cbc:RegistrationName", cl.getDenominacion());

        // RegistrationAddress
        Element addr = appendElement(doc, legal, NS_CAC, "cac:RegistrationAddress");
        if (cl.getProvincia() != null) appendTextElement(doc, addr, NS_CBC, "cbc:CityName", cl.getProvincia());
        if (cl.getDepartamento() != null) appendTextElement(doc, addr, NS_CBC, "cbc:CountrySubentity", cl.getDepartamento());
        if (cl.getDistrito() != null) appendTextElement(doc, addr, NS_CBC, "cbc:District", cl.getDistrito());
        if (cl.getDireccion() != null) {
            Element addrLine = appendElement(doc, addr, NS_CAC, "cac:AddressLine");
            appendTextElement(doc, addrLine, NS_CBC, "cbc:Line", cl.getDireccion());
        }
        Element country = appendElement(doc, addr, NS_CAC, "cac:Country");
        Element countryCode = appendTextElement(doc, country, NS_CBC, "cbc:IdentificationCode",
                cl.getCodigoPais() != null ? cl.getCodigoPais() : "PE");
        countryCode.setAttribute("listAgencyName", UNECE_AGENCY);
    }

    /** PaymentTerms — Forma de pago + cuotas crédito */
    private void buildPaymentTerms(Document doc, Element invoice, XmlBuildContext ctx,
                                    List<CuotaCreditoEntity> cuotas, String moneda) {
        // Forma de pago principal
        Element pt = appendElement(doc, invoice, NS_CAC, "cac:PaymentTerms");
        appendTextElement(doc, pt, NS_CBC, "cbc:ID", "FormaPago");
        String formaPago = ctx.formaPago() != null ? ctx.formaPago() : "Contado";
        appendTextElement(doc, pt, NS_CBC, "cbc:PaymentMeansID", formaPago);
        if ("Credito".equalsIgnoreCase(formaPago) || "CREDITO".equalsIgnoreCase(formaPago)) {
            Element amount = appendTextElement(doc, pt, NS_CBC, "cbc:Amount", formatDecimal(ctx.total()));
            amount.setAttribute("currencyID", moneda);
        }

        // Cuotas de crédito
        if (cuotas != null) {
            for (CuotaCreditoEntity cuota : cuotas) {
                Element cpt = appendElement(doc, invoice, NS_CAC, "cac:PaymentTerms");
                appendTextElement(doc, cpt, NS_CBC, "cbc:ID", "FormaPago");
                appendTextElement(doc, cpt, NS_CBC, "cbc:PaymentMeansID", cuota.getNumeroCuota());
                Element cAmount = appendTextElement(doc, cpt, NS_CBC, "cbc:Amount", formatDecimal(cuota.getMonto()));
                cAmount.setAttribute("currencyID", cuota.getMoneda() != null ? cuota.getMoneda() : moneda);
                if (cuota.getFechaVencimiento() != null) {
                    appendTextElement(doc, cpt, NS_CBC, "cbc:PaymentDueDate",
                            cuota.getFechaVencimiento().format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
        }
    }

    /** AllowanceCharge — Cargos/descuentos globales (Cat.53) */
    private void buildAllowanceCharges(Document doc, Element invoice,
                                        List<AllowanceChargeEntity> charges, String moneda) {
        if (charges == null) return;
        for (AllowanceChargeEntity ac : charges) {
            Element el = appendElement(doc, invoice, NS_CAC, "cac:AllowanceCharge");
            appendTextElement(doc, el, NS_CBC, "cbc:ChargeIndicator",
                    String.valueOf(Boolean.TRUE.equals(ac.getChargeIndicator())));

            Element reason = appendTextElement(doc, el, NS_CBC, "cbc:AllowanceChargeReasonCode",
                    ac.getReasonCode() != null ? ac.getReasonCode() : "00");
            reason.setAttribute("listAgencyName", SUNAT_AGENCY);
            reason.setAttribute("listName", "Cargo/descuento");
            reason.setAttribute("listURI", CAT53_URI);

            if (ac.getMultiplierFactor() != null) {
                appendTextElement(doc, el, NS_CBC, "cbc:MultiplierFactorNumeric",
                        ac.getMultiplierFactor().toPlainString());
            }

            Element amt = appendTextElement(doc, el, NS_CBC, "cbc:Amount", formatDecimal(ac.getAmount()));
            amt.setAttribute("currencyID", ac.getMoneda() != null ? ac.getMoneda() : moneda);

            if (ac.getBaseAmount() != null) {
                Element base = appendTextElement(doc, el, NS_CBC, "cbc:BaseAmount", formatDecimal(ac.getBaseAmount()));
                base.setAttribute("currencyID", ac.getMoneda() != null ? ac.getMoneda() : moneda);
            }
        }
    }

    /** TaxTotal — Impuestos a nivel documento */
    private void buildTaxTotal(Document doc, Element invoice, XmlBuildContext ctx, String moneda) {
        Element taxTotal = appendElement(doc, invoice, NS_CAC, "cac:TaxTotal");
        Element totalTax = appendTextElement(doc, taxTotal, NS_CBC, "cbc:TaxAmount",
                formatDecimal(ctx.igv() != null ? ctx.igv() : BigDecimal.ZERO));
        totalTax.setAttribute("currencyID", moneda);

        // IGV (Gravado) — Cat.05 código 1000
        if (ctx.gravadas() != null && ctx.gravadas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda,
                    ctx.gravadas(), ctx.igv() != null ? ctx.igv() : BigDecimal.ZERO,
                    "1000", "IGV", "VAT");
        }

        // Exonerado — Cat.05 código 9997
        if (ctx.exoneradas() != null && ctx.exoneradas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda,
                    ctx.exoneradas(), BigDecimal.ZERO,
                    "9997", "EXO", "VAT");
        }

        // Inafecto — Cat.05 código 9998
        if (ctx.inafectas() != null && ctx.inafectas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda,
                    ctx.inafectas(), BigDecimal.ZERO,
                    "9998", "INA", "FRE");
        }

        // Gratuito — Cat.05 código 9996
        if (ctx.gratuitas() != null && ctx.gratuitas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda,
                    ctx.gratuitas(), BigDecimal.ZERO,
                    "9996", "GRA", "FRE");
        }
    }

    /** Helper: construye un TaxSubtotal */
    private void buildTaxSubtotal(Document doc, Element parent, String moneda,
                                   BigDecimal taxableAmount, BigDecimal taxAmount,
                                   String schemeId, String schemeName, String typeCode) {
        Element sub = appendElement(doc, parent, NS_CAC, "cac:TaxSubtotal");

        Element taxable = appendTextElement(doc, sub, NS_CBC, "cbc:TaxableAmount", formatDecimal(taxableAmount));
        taxable.setAttribute("currencyID", moneda);

        Element tax = appendTextElement(doc, sub, NS_CBC, "cbc:TaxAmount", formatDecimal(taxAmount));
        tax.setAttribute("currencyID", moneda);

        Element category = appendElement(doc, sub, NS_CAC, "cac:TaxCategory");
        Element scheme = appendElement(doc, category, NS_CAC, "cac:TaxScheme");

        Element id = appendTextElement(doc, scheme, NS_CBC, "cbc:ID", schemeId);
        id.setAttribute("schemeAgencyName", SUNAT_AGENCY);
        id.setAttribute("schemeName", "Codigo de tributos");
        id.setAttribute("schemeURI", CAT05_URI);

        appendTextElement(doc, scheme, NS_CBC, "cbc:Name", schemeName);
        appendTextElement(doc, scheme, NS_CBC, "cbc:TaxTypeCode", typeCode);
    }

    /** LegalMonetaryTotal */
    private void buildLegalMonetaryTotal(Document doc, Element invoice, XmlBuildContext ctx, String moneda) {
        Element lmt = appendElement(doc, invoice, NS_CAC, "cac:LegalMonetaryTotal");

        appendCurrencyElement(doc, lmt, "cbc:LineExtensionAmount", moneda,
                ctx.subtotal() != null ? ctx.subtotal() : BigDecimal.ZERO);
        appendCurrencyElement(doc, lmt, "cbc:TaxInclusiveAmount", moneda,
                ctx.totalConImpuestos() != null ? ctx.totalConImpuestos() : BigDecimal.ZERO);

        if (ctx.totalDescuentos() != null && ctx.totalDescuentos().compareTo(BigDecimal.ZERO) > 0) {
            appendCurrencyElement(doc, lmt, "cbc:AllowanceTotalAmount", moneda, ctx.totalDescuentos());
        }
        if (ctx.totalCargos() != null && ctx.totalCargos().compareTo(BigDecimal.ZERO) > 0) {
            appendCurrencyElement(doc, lmt, "cbc:ChargeTotalAmount", moneda, ctx.totalCargos());
        }

        appendCurrencyElement(doc, lmt, "cbc:PayableAmount", moneda, ctx.total());
    }

    /** InvoiceLine — Líneas de detalle */
    private void buildInvoiceLines(Document doc, Element invoice,
                                    List<Map<String, Object>> items, String moneda) {
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Element line = appendElement(doc, invoice, NS_CAC, "cac:InvoiceLine");

            // ID
            appendTextElement(doc, line, NS_CBC, "cbc:ID", String.valueOf(i + 1));

            // Quantity
            BigDecimal cantidad = toBigDecimal(item.get("cantidad"), BigDecimal.ONE);
            BigDecimal valorUnit = toBigDecimal(item.get("valorUnitario"), BigDecimal.ZERO);
            BigDecimal baseImponible = toBigDecimal(item.get("baseImponible"),
                    cantidad.multiply(valorUnit));
            String unidad = item.get("unidadMedida") != null ? item.get("unidadMedida").toString() : "NIU";

            Element qty = appendTextElement(doc, line, NS_CBC, "cbc:InvoicedQuantity",
                    formatDecimal3(cantidad));
            qty.setAttribute("unitCode", unidad);
            qty.setAttribute("unitCodeListAgencyName", UNECE_AGENCY);
            qty.setAttribute("unitCodeListID", "UN/ECE rec 20");

            // LineExtensionAmount
            appendCurrencyElement(doc, line, "cbc:LineExtensionAmount", moneda, baseImponible);

            // PricingReference (precio con IGV) — Cat.16
            BigDecimal precioUnit = toBigDecimal(item.get("precioUnitario"), valorUnit);
            String tipoPrecio = item.get("tipoPrecio") != null ? item.get("tipoPrecio").toString() : "01";
            Element pricingRef = appendElement(doc, line, NS_CAC, "cac:PricingReference");
            Element altPrice = appendElement(doc, pricingRef, NS_CAC, "cac:AlternativeConditionPrice");
            appendCurrencyElement(doc, altPrice, "cbc:PriceAmount", moneda, precioUnit);
            Element priceType = appendTextElement(doc, altPrice, NS_CBC, "cbc:PriceTypeCode", tipoPrecio);
            priceType.setAttribute("listAgencyName", SUNAT_AGENCY);
            priceType.setAttribute("listName", "Tipo de Precio");
            priceType.setAttribute("listURI", CAT16_URI);

            // Descuento por línea (si aplica)
            BigDecimal descuento = toBigDecimal(item.get("descuento"), null);
            if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
                Element lineAc = appendElement(doc, line, NS_CAC, "cac:AllowanceCharge");
                appendTextElement(doc, lineAc, NS_CBC, "cbc:ChargeIndicator", "false");
                Element lineReason = appendTextElement(doc, lineAc, NS_CBC, "cbc:AllowanceChargeReasonCode", "00");
                lineReason.setAttribute("listAgencyName", SUNAT_AGENCY);
                lineReason.setAttribute("listName", "Cargo/descuento");
                lineReason.setAttribute("listURI", CAT53_URI);
                appendCurrencyElement(doc, lineAc, "cbc:Amount", moneda, descuento);
            }

            // TaxTotal por línea — Cat.05 + Cat.07
            BigDecimal montoImpuesto = toBigDecimal(item.get("montoImpuesto"), BigDecimal.ZERO);
            BigDecimal porcentajeIgv = toBigDecimal(item.get("porcentajeIgv"), new BigDecimal("18.00"));
            String tipoAfectacion = item.get("tipoAfectacionIgv") != null
                    ? item.get("tipoAfectacionIgv").toString() : "10";
            String codigoTributo = item.get("codigoTributo") != null
                    ? item.get("codigoTributo").toString() : "1000";

            // Resolver nombre y tipo del tributo según código
            String tributoName = resolveTributoName(codigoTributo);
            String tributoTypeCode = resolveTributoTypeCode(codigoTributo);

            Element lineTaxTotal = appendElement(doc, line, NS_CAC, "cac:TaxTotal");
            Element lineTaxAmt = appendTextElement(doc, lineTaxTotal, NS_CBC, "cbc:TaxAmount",
                    formatDecimal(montoImpuesto));
            lineTaxAmt.setAttribute("currencyID", moneda);

            Element lineSub = appendElement(doc, lineTaxTotal, NS_CAC, "cac:TaxSubtotal");
            appendCurrencyElement(doc, lineSub, "cbc:TaxableAmount", moneda, baseImponible);
            Element lineSubTax = appendTextElement(doc, lineSub, NS_CBC, "cbc:TaxAmount",
                    formatDecimal(montoImpuesto));
            lineSubTax.setAttribute("currencyID", moneda);

            Element taxCat = appendElement(doc, lineSub, NS_CAC, "cac:TaxCategory");
            appendTextElement(doc, taxCat, NS_CBC, "cbc:Percent", formatDecimal(porcentajeIgv));

            Element exemption = appendTextElement(doc, taxCat, NS_CBC, "cbc:TaxExemptionReasonCode",
                    tipoAfectacion);
            exemption.setAttribute("listAgencyName", SUNAT_AGENCY);
            exemption.setAttribute("listName", "Afectacion del IGV");
            exemption.setAttribute("listURI", CAT07_URI);

            Element taxScheme = appendElement(doc, taxCat, NS_CAC, "cac:TaxScheme");
            Element schemeId = appendTextElement(doc, taxScheme, NS_CBC, "cbc:ID", codigoTributo);
            schemeId.setAttribute("schemeAgencyName", SUNAT_AGENCY);
            schemeId.setAttribute("schemeName", "Codigo de tributos");
            schemeId.setAttribute("schemeURI", CAT05_URI);
            appendTextElement(doc, taxScheme, NS_CBC, "cbc:Name", tributoName);
            appendTextElement(doc, taxScheme, NS_CBC, "cbc:TaxTypeCode", tributoTypeCode);

            // Item description
            Element itemEl = appendElement(doc, line, NS_CAC, "cac:Item");
            appendTextElement(doc, itemEl, NS_CBC, "cbc:Description",
                    item.get("descripcion") != null ? item.get("descripcion").toString() : "");
            if (item.get("codigo") != null && !item.get("codigo").toString().isBlank()) {
                Element sellerId = appendElement(doc, itemEl, NS_CAC, "cac:SellersItemIdentification");
                appendTextElement(doc, sellerId, NS_CBC, "cbc:ID", item.get("codigo").toString());
            }

            // Price (valor unitario sin IGV)
            Element price = appendElement(doc, line, NS_CAC, "cac:Price");
            appendCurrencyElement(doc, price, "cbc:PriceAmount", moneda, valorUnit);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════════════════════════════

    private String resolveTributoName(String codigoTributo) {
        return switch (codigoTributo) {
            case "1000" -> "IGV";
            case "9997" -> "EXO";
            case "9998" -> "INA";
            case "9996" -> "GRA";
            case "2000" -> "ISC";
            case "7152" -> "ICBPER";
            default -> "OTR";
        };
    }

    private String resolveTributoTypeCode(String codigoTributo) {
        return switch (codigoTributo) {
            case "1000" -> "VAT";
            case "9997" -> "VAT";
            case "9998" -> "FRE";
            case "9996" -> "FRE";
            case "2000" -> "EXC";
            case "7152" -> "OTH";
            default -> "OTH";
        };
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseDetalleJson(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("Error parseando detalleJson: {}", e.getMessage());
            return List.of();
        }
    }

    private BigDecimal toBigDecimal(Object val, BigDecimal defaultVal) {
        if (val == null) return defaultVal;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private String formatDecimal(BigDecimal val) {
        if (val == null) return "0.00";
        return val.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDecimal3(BigDecimal val) {
        if (val == null) return "0.000";
        return val.setScale(3, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String val) {
        return val != null ? val : "";
    }

    private String serializeToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    // ═══ DOM Helpers ═══

    private Element appendElement(Document doc, Element parent, String ns, String qName) {
        Element el = doc.createElementNS(ns, qName);
        parent.appendChild(el);
        return el;
    }

    private Element appendTextElement(Document doc, Element parent, String ns, String qName, String text) {
        Element el = doc.createElementNS(ns, qName);
        el.setTextContent(text);
        parent.appendChild(el);
        return el;
    }

    private void appendCurrencyElement(Document doc, Element parent, String qName,
                                        String currency, BigDecimal amount) {
        Element el = doc.createElementNS(NS_CBC, qName);
        el.setTextContent(formatDecimal(amount));
        el.setAttribute("currencyID", currency);
        parent.appendChild(el);
    }
}

