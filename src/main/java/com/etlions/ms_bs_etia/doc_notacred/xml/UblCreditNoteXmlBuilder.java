package com.etlions.ms_bs_etia.doc_notacred.xml;

import com.etlions.ms_bs_etia.app_util.validation.SunatFormatter;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
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
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 🧾 Generador de XML UBL 2.1 para Notas de Crédito Electrónicas SUNAT — Perú
 * <p>
 * Tipo de documento: "07" (CreditNote)
 * Catálogo 09: Códigos de motivo de nota de crédito
 * </p>
 */
@Slf4j
@Component
public class UblCreditNoteXmlBuilder {

    /* ═══ Namespaces UBL 2.1 ═══ */
    private static final String NS_CREDIT_NOTE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2";
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
    private static final String CAT09_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo09";
    private static final String CAT16_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16";
    private static final String CAT53_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo53";
    private static final String CAT01_URI      = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Genera el XML UBL 2.1 CreditNote completo.
     */
    public String buildCreditNoteXml(NotaCreditoEntity nc,
                                      CompanyEntity company,
                                      ClientEntity client) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            doc.setXmlStandalone(true);

            // ── Root: <CreditNote>
            Element root = doc.createElementNS(NS_CREDIT_NOTE, "CreditNote");
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cac", NS_CAC);
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cbc", NS_CBC);
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ext", NS_EXT);
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", NS_DS);
            doc.appendChild(root);

            String moneda = nc.getMoneda() != null ? nc.getMoneda() : "PEN";
            List<Map<String, Object>> items = parseDetalleJson(nc.getDetalleJson());

            // ═══ Secciones del XML ═══
            buildUblExtensions(doc, root);
            buildMetadata(doc, root);
            buildDocumentId(doc, root, nc);
            buildDiscrepancyResponse(doc, root, nc);
            buildBillingReference(doc, root, nc);
            buildNote(doc, root, nc);
            buildCurrencyCode(doc, root, moneda);
            buildSupplierParty(doc, root, company);
            buildCustomerParty(doc, root, client);
            buildTaxTotal(doc, root, nc, moneda);
            buildLegalMonetaryTotal(doc, root, nc, moneda);
            buildCreditNoteLines(doc, root, items, moneda);

            return serializeToString(doc);

        } catch (Exception e) {
            log.error("Error generando XML CreditNote para {}-{}: {}",
                    nc.getSerie(), nc.getNumero(), e.getMessage(), e);
            throw new RuntimeException("Error generando XML CreditNote: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SECCIONES DEL XML
    // ═══════════════════════════════════════════════════════════════

    private void buildUblExtensions(Document doc, Element root) {
        Element extensions = appendElement(doc, root, NS_EXT, "ext:UBLExtensions");
        Element extension = appendElement(doc, extensions, NS_EXT, "ext:UBLExtension");
        appendElement(doc, extension, NS_EXT, "ext:ExtensionContent");
    }

    private void buildMetadata(Document doc, Element root) {
        appendTextElement(doc, root, NS_CBC, "cbc:UBLVersionID", "2.1");
        Element custom = appendTextElement(doc, root, NS_CBC, "cbc:CustomizationID", "2.0");
        custom.setAttribute("schemeAgencyName", SUNAT_AGENCY);
    }

    private void buildDocumentId(Document doc, Element root, NotaCreditoEntity nc) {
        String id = SunatFormatter.formatNumeroComprobante(nc.getSerie(), nc.getNumero());
        appendTextElement(doc, root, NS_CBC, "cbc:ID", id);
        appendTextElement(doc, root, NS_CBC, "cbc:IssueDate",
                nc.getFechaEmision().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (nc.getHoraEmision() != null) {
            appendTextElement(doc, root, NS_CBC, "cbc:IssueTime",
                    nc.getHoraEmision().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    /** DiscrepancyResponse — Motivo de la nota de crédito (Cat.09) */
    private void buildDiscrepancyResponse(Document doc, Element root, NotaCreditoEntity nc) {
        Element dr = appendElement(doc, root, NS_CAC, "cac:DiscrepancyResponse");
        String refId = SunatFormatter.formatNumeroComprobante(
                nc.getSerieDocRef() != null ? nc.getSerieDocRef() : "F001",
                nc.getNumeroDocRef() != null ? nc.getNumeroDocRef() : 0);
        appendTextElement(doc, dr, NS_CBC, "cbc:ReferenceID", refId);

        Element responseCode = appendTextElement(doc, dr, NS_CBC, "cbc:ResponseCode",
                nc.getCodigoMotivoSunat() != null ? nc.getCodigoMotivoSunat() : "01");
        responseCode.setAttribute("listAgencyName", SUNAT_AGENCY);
        responseCode.setAttribute("listName", "Tipo de nota de credito");
        responseCode.setAttribute("listURI", CAT09_URI);

        appendTextElement(doc, dr, NS_CBC, "cbc:Description",
                nc.getMotivo() != null ? nc.getMotivo() : "");
    }

    /** BillingReference — Referencia al documento original */
    private void buildBillingReference(Document doc, Element root, NotaCreditoEntity nc) {
        Element br = appendElement(doc, root, NS_CAC, "cac:BillingReference");
        Element invRef = appendElement(doc, br, NS_CAC, "cac:InvoiceDocumentReference");

        String refId = SunatFormatter.formatNumeroComprobante(
                nc.getSerieDocRef() != null ? nc.getSerieDocRef() : "F001",
                nc.getNumeroDocRef() != null ? nc.getNumeroDocRef() : 0);
        appendTextElement(doc, invRef, NS_CBC, "cbc:ID", refId);

        // Tipo de documento referenciado: 01=factura, 03=boleta
        String tipoDocRef = "01";
        if (nc.getTipoDocumentoReferencia() != null) {
            String ref = nc.getTipoDocumentoReferencia().toLowerCase();
            if (ref.contains("boleta")) tipoDocRef = "03";
        }
        Element docTypeCode = appendTextElement(doc, invRef, NS_CBC, "cbc:DocumentTypeCode", tipoDocRef);
        docTypeCode.setAttribute("listAgencyName", SUNAT_AGENCY);
        docTypeCode.setAttribute("listName", "Tipo de Documento");
        docTypeCode.setAttribute("listURI", CAT01_URI);
    }

    private void buildNote(Document doc, Element root, NotaCreditoEntity nc) {
        String leyenda = nc.getLeyenda();
        if (leyenda == null || leyenda.isBlank()) {
            leyenda = nc.getMotivo() != null ? nc.getMotivo() : "";
        }
        if (!leyenda.isBlank()) {
            Element note = appendTextElement(doc, root, NS_CBC, "cbc:Note", leyenda);
            note.setAttribute("languageLocaleID", "1000");
        }
    }

    private void buildCurrencyCode(Document doc, Element root, String moneda) {
        Element curr = appendTextElement(doc, root, NS_CBC, "cbc:DocumentCurrencyCode", moneda);
        curr.setAttribute("listAgencyName", UNECE_AGENCY);
        curr.setAttribute("listName", "Currency");
    }

    private void buildSupplierParty(Document doc, Element root, CompanyEntity c) {
        Element supplier = appendElement(doc, root, NS_CAC, "cac:AccountingSupplierParty");
        Element party = appendElement(doc, supplier, NS_CAC, "cac:Party");

        Element partyId = appendElement(doc, party, NS_CAC, "cac:PartyIdentification");
        Element id = appendTextElement(doc, partyId, NS_CBC, "cbc:ID", c.getRuc());
        id.setAttribute("schemeAgencyName", SUNAT_AGENCY);
        id.setAttribute("schemeID", "6");
        id.setAttribute("schemeName", "Documento de Identidad");
        id.setAttribute("schemeURI", CAT06_URI);

        if (c.getNombreComercial() != null && !c.getNombreComercial().isBlank()) {
            Element partyName = appendElement(doc, party, NS_CAC, "cac:PartyName");
            appendTextElement(doc, partyName, NS_CBC, "cbc:Name", c.getNombreComercial());
        }

        Element legal = appendElement(doc, party, NS_CAC, "cac:PartyLegalEntity");
        appendTextElement(doc, legal, NS_CBC, "cbc:RegistrationName", c.getRazonSocial());

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

    private void buildCustomerParty(Document doc, Element root, ClientEntity cl) {
        Element customer = appendElement(doc, root, NS_CAC, "cac:AccountingCustomerParty");
        Element party = appendElement(doc, customer, NS_CAC, "cac:Party");

        Element partyId = appendElement(doc, party, NS_CAC, "cac:PartyIdentification");
        Element id = appendTextElement(doc, partyId, NS_CBC, "cbc:ID", cl.getNumeroDocumento());
        id.setAttribute("schemeAgencyName", SUNAT_AGENCY);
        id.setAttribute("schemeID", cl.getTipoDocumento() != null ? cl.getTipoDocumento() : "6");
        id.setAttribute("schemeName", "Documento de Identidad");
        id.setAttribute("schemeURI", CAT06_URI);

        Element legal = appendElement(doc, party, NS_CAC, "cac:PartyLegalEntity");
        appendTextElement(doc, legal, NS_CBC, "cbc:RegistrationName", cl.getDenominacion());

        Element addr = appendElement(doc, legal, NS_CAC, "cac:RegistrationAddress");
        if (cl.getDireccion() != null) {
            Element addrLine = appendElement(doc, addr, NS_CAC, "cac:AddressLine");
            appendTextElement(doc, addrLine, NS_CBC, "cbc:Line", cl.getDireccion());
        }
        Element country = appendElement(doc, addr, NS_CAC, "cac:Country");
        Element countryCode = appendTextElement(doc, country, NS_CBC, "cbc:IdentificationCode",
                cl.getCodigoPais() != null ? cl.getCodigoPais() : "PE");
        countryCode.setAttribute("listAgencyName", UNECE_AGENCY);
    }

    private void buildTaxTotal(Document doc, Element root, NotaCreditoEntity nc, String moneda) {
        Element taxTotal = appendElement(doc, root, NS_CAC, "cac:TaxTotal");
        Element totalTax = appendTextElement(doc, taxTotal, NS_CBC, "cbc:TaxAmount",
                formatDecimal(nc.getIgv() != null ? nc.getIgv() : BigDecimal.ZERO));
        totalTax.setAttribute("currencyID", moneda);

        if (nc.getGravadas() != null && nc.getGravadas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda,
                    nc.getGravadas(), nc.getIgv() != null ? nc.getIgv() : BigDecimal.ZERO,
                    "1000", "IGV", "VAT");
        }
        if (nc.getExoneradas() != null && nc.getExoneradas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda, nc.getExoneradas(), BigDecimal.ZERO, "9997", "EXO", "VAT");
        }
        if (nc.getInafectas() != null && nc.getInafectas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda, nc.getInafectas(), BigDecimal.ZERO, "9998", "INA", "FRE");
        }
        if (nc.getGratuitas() != null && nc.getGratuitas().compareTo(BigDecimal.ZERO) > 0) {
            buildTaxSubtotal(doc, taxTotal, moneda, nc.getGratuitas(), BigDecimal.ZERO, "9996", "GRA", "FRE");
        }
    }

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

    private void buildLegalMonetaryTotal(Document doc, Element root, NotaCreditoEntity nc, String moneda) {
        Element lmt = appendElement(doc, root, NS_CAC, "cac:LegalMonetaryTotal");
        appendCurrencyElement(doc, lmt, "cbc:LineExtensionAmount", moneda,
                nc.getSubtotal() != null ? nc.getSubtotal() : BigDecimal.ZERO);
        appendCurrencyElement(doc, lmt, "cbc:TaxInclusiveAmount", moneda,
                nc.getTotalConImpuestos() != null ? nc.getTotalConImpuestos() : BigDecimal.ZERO);
        if (nc.getTotalDescuentos() != null && nc.getTotalDescuentos().compareTo(BigDecimal.ZERO) > 0) {
            appendCurrencyElement(doc, lmt, "cbc:AllowanceTotalAmount", moneda, nc.getTotalDescuentos());
        }
        if (nc.getTotalCargos() != null && nc.getTotalCargos().compareTo(BigDecimal.ZERO) > 0) {
            appendCurrencyElement(doc, lmt, "cbc:ChargeTotalAmount", moneda, nc.getTotalCargos());
        }
        appendCurrencyElement(doc, lmt, "cbc:PayableAmount", moneda, nc.getTotal());
    }

    /** CreditNoteLine — Líneas de detalle */
    private void buildCreditNoteLines(Document doc, Element root,
                                       List<Map<String, Object>> items, String moneda) {
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Element line = appendElement(doc, root, NS_CAC, "cac:CreditNoteLine");

            appendTextElement(doc, line, NS_CBC, "cbc:ID", String.valueOf(i + 1));

            BigDecimal cantidad = toBigDecimal(item.get("cantidad"), BigDecimal.ONE);
            BigDecimal valorUnit = toBigDecimal(item.get("valorUnitario"), BigDecimal.ZERO);
            BigDecimal baseImponible = toBigDecimal(item.get("baseImponible"), cantidad.multiply(valorUnit));
            String unidad = item.get("unidadMedida") != null ? item.get("unidadMedida").toString() : "NIU";

            // CreditedQuantity (en vez de InvoicedQuantity)
            Element qty = appendTextElement(doc, line, NS_CBC, "cbc:CreditedQuantity", formatDecimal3(cantidad));
            qty.setAttribute("unitCode", unidad);
            qty.setAttribute("unitCodeListAgencyName", UNECE_AGENCY);
            qty.setAttribute("unitCodeListID", "UN/ECE rec 20");

            appendCurrencyElement(doc, line, "cbc:LineExtensionAmount", moneda, baseImponible);

            // PricingReference
            BigDecimal precioUnit = toBigDecimal(item.get("precioUnitario"), valorUnit);
            String tipoPrecio = item.get("tipoPrecio") != null ? item.get("tipoPrecio").toString() : "01";
            Element pricingRef = appendElement(doc, line, NS_CAC, "cac:PricingReference");
            Element altPrice = appendElement(doc, pricingRef, NS_CAC, "cac:AlternativeConditionPrice");
            appendCurrencyElement(doc, altPrice, "cbc:PriceAmount", moneda, precioUnit);
            Element priceType = appendTextElement(doc, altPrice, NS_CBC, "cbc:PriceTypeCode", tipoPrecio);
            priceType.setAttribute("listAgencyName", SUNAT_AGENCY);
            priceType.setAttribute("listName", "Tipo de Precio");
            priceType.setAttribute("listURI", CAT16_URI);

            // TaxTotal por línea
            BigDecimal montoImpuesto = toBigDecimal(item.get("montoImpuesto"), BigDecimal.ZERO);
            BigDecimal porcentajeIgv = toBigDecimal(item.get("porcentajeIgv"), new BigDecimal("18.00"));
            String tipoAfectacion = item.get("tipoAfectacionIgv") != null
                    ? item.get("tipoAfectacionIgv").toString() : "10";
            String codigoTributo = item.get("codigoTributo") != null
                    ? item.get("codigoTributo").toString() : "1000";

            Element lineTaxTotal = appendElement(doc, line, NS_CAC, "cac:TaxTotal");
            Element lineTaxAmt = appendTextElement(doc, lineTaxTotal, NS_CBC, "cbc:TaxAmount", formatDecimal(montoImpuesto));
            lineTaxAmt.setAttribute("currencyID", moneda);

            Element lineSub = appendElement(doc, lineTaxTotal, NS_CAC, "cac:TaxSubtotal");
            appendCurrencyElement(doc, lineSub, "cbc:TaxableAmount", moneda, baseImponible);
            Element lineSubTax = appendTextElement(doc, lineSub, NS_CBC, "cbc:TaxAmount", formatDecimal(montoImpuesto));
            lineSubTax.setAttribute("currencyID", moneda);

            Element taxCat = appendElement(doc, lineSub, NS_CAC, "cac:TaxCategory");
            appendTextElement(doc, taxCat, NS_CBC, "cbc:Percent", formatDecimal(porcentajeIgv));
            Element exemption = appendTextElement(doc, taxCat, NS_CBC, "cbc:TaxExemptionReasonCode", tipoAfectacion);
            exemption.setAttribute("listAgencyName", SUNAT_AGENCY);
            exemption.setAttribute("listName", "Afectacion del IGV");
            exemption.setAttribute("listURI", CAT07_URI);

            Element taxScheme = appendElement(doc, taxCat, NS_CAC, "cac:TaxScheme");
            Element schemeId = appendTextElement(doc, taxScheme, NS_CBC, "cbc:ID", codigoTributo);
            schemeId.setAttribute("schemeAgencyName", SUNAT_AGENCY);
            schemeId.setAttribute("schemeName", "Codigo de tributos");
            schemeId.setAttribute("schemeURI", CAT05_URI);
            appendTextElement(doc, taxScheme, NS_CBC, "cbc:Name", resolveTributoName(codigoTributo));
            appendTextElement(doc, taxScheme, NS_CBC, "cbc:TaxTypeCode", resolveTributoTypeCode(codigoTributo));

            // Item
            Element itemEl = appendElement(doc, line, NS_CAC, "cac:Item");
            appendTextElement(doc, itemEl, NS_CBC, "cbc:Description",
                    item.get("descripcion") != null ? item.get("descripcion").toString() : "");
            if (item.get("codigo") != null && !item.get("codigo").toString().isBlank()) {
                Element sellerId = appendElement(doc, itemEl, NS_CAC, "cac:SellersItemIdentification");
                appendTextElement(doc, sellerId, NS_CBC, "cbc:ID", item.get("codigo").toString());
            }

            // Price
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
        try { return new BigDecimal(val.toString()); }
        catch (NumberFormatException e) { return defaultVal; }
    }

    private String formatDecimal(BigDecimal val) {
        if (val == null) return "0.00";
        return val.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDecimal3(BigDecimal val) {
        if (val == null) return "0.000";
        return val.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String val) { return val != null ? val : ""; }

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

