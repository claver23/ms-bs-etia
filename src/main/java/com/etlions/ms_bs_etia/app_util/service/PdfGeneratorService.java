package com.etlions.ms_bs_etia.app_util.service;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.util.LogoUtil;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para generación de representación impresa (PDF) de comprobantes electrónicos.
 * Usa OpenPDF (fork libre de iText 4).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final QrCodeService qrCodeService;
    private final LogoUtil logoUtil;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.HELVETICA, 7, Font.NORMAL);
    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font FONT_DOC_TYPE = new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED);
    private static final Color HEADER_BG = new Color(240, 240, 240);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    /**
     * Genera PDF de factura.
     */
    public byte[] generateFacturaPdf(FacturaEntity factura, CompanyEntity company,
                                      ClientEntity client, List<Map<String, Object>> detalle) {
        return generateDocumentPdf(
                "FACTURA ELECTRÓNICA", "01",
                company, client, detalle,
                factura.getSerie(), factura.getNumero(),
                factura.getFechaEmision() != null ? factura.getFechaEmision().format(DATE_FMT) : "",
                factura.getMoneda(), factura.getFormaPago(), factura.getTipoOperacion(),
                factura.getGravadas(), factura.getExoneradas(), factura.getInafectas(),
                factura.getIgv(), factura.getTotal(),
                factura.getLeyenda(), factura.getHashCdr(), factura.getCodigoRespuestaSunat()
        );
    }

    /**
     * Genera PDF de boleta.
     */
    public byte[] generateBoletaPdf(BoletaEntity boleta, CompanyEntity company,
                                     ClientEntity client, List<Map<String, Object>> detalle) {
        return generateDocumentPdf(
                "BOLETA DE VENTA ELECTRÓNICA", "03",
                company, client, detalle,
                boleta.getSerie(), boleta.getNumero(),
                boleta.getFechaEmision() != null ? boleta.getFechaEmision().format(DATE_FMT) : "",
                boleta.getMoneda(), boleta.getFormaPago(), boleta.getTipoOperacion(),
                boleta.getGravadas(), boleta.getExoneradas(), boleta.getInafectas(),
                boleta.getIgv(), boleta.getTotal(),
                boleta.getLeyenda(), boleta.getHashCdr(), boleta.getCodigoRespuestaSunat()
        );
    }

    private byte[] generateDocumentPdf(String docTitle, String tipoDoc,
                                        CompanyEntity company, ClientEntity client,
                                        List<Map<String, Object>> detalle,
                                        String serie, Integer numero,
                                        String fechaEmision, String moneda,
                                        String formaPago, String tipoOperacion,
                                        BigDecimal gravadas, BigDecimal exoneradas,
                                        BigDecimal inafectas, BigDecimal igv, BigDecimal total,
                                        String leyenda, String hashCdr, String codigoSunat) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            String numFormatted = String.format("%08d", numero != null ? numero : 0);
            String serieNumero = serie + "-" + numFormatted;
            String monedaSymbol = "USD".equals(moneda) ? "US$" : "S/";

            // ═══ CABECERA: Logo + datos emisor | Recuadro comprobante ═══
            addHeader(doc, company, docTitle, serieNumero);

            doc.add(Chunk.NEWLINE);

            // ═══ DATOS DEL RECEPTOR ═══
            addReceptorData(doc, client);

            doc.add(Chunk.NEWLINE);

            // ═══ DATOS DE EMISIÓN ═══
            addEmisionData(doc, fechaEmision, moneda, formaPago, tipoOperacion);

            doc.add(Chunk.NEWLINE);

            // ═══ DETALLE DE ITEMS ═══
            addDetalleTable(doc, detalle, monedaSymbol);

            doc.add(Chunk.NEWLINE);

            // ═══ TOTALES ═══
            addTotales(doc, gravadas, exoneradas, inafectas, igv, total, monedaSymbol);

            // ═══ LEYENDA ═══
            if (leyenda != null && !leyenda.isBlank()) {
                doc.add(new Paragraph(leyenda, FONT_SMALL));
                doc.add(Chunk.NEWLINE);
            }

            // ═══ QR + HASH ═══
            addQrAndHash(doc, company, tipoDoc, serie, numero, igv, total,
                    fechaEmision, client, hashCdr, codigoSunat);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("❌ Error generando PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private void addHeader(Document doc, CompanyEntity company,
                           String docTitle, String serieNumero) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});

        // Columna izquierda: Logo + datos emisor
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(0);
        leftCell.setPadding(5);

        // Intentar agregar logo
        try {
            Optional<byte[]> logoBytes = logoUtil.getLogoBytes(company.getRuc());
            if (logoBytes.isPresent() && logoBytes.get().length > 0) {
                Image logoImg = Image.getInstance(logoBytes.get());
                logoImg.scaleToFit(80, 80);
                leftCell.addElement(logoImg);
            }
        } catch (Exception e) {
            log.debug("No se pudo cargar logo para RUC {}: {}", company.getRuc(), e.getMessage());
        }

        leftCell.addElement(new Paragraph(
                company.getRazonSocial() != null ? company.getRazonSocial() : "", FONT_TITLE));
        leftCell.addElement(new Paragraph("RUC: " + safe(company.getRuc()), FONT_BOLD));
        leftCell.addElement(new Paragraph(safe(company.getDireccion()), FONT_NORMAL));
        String ubicacion = buildUbicacion(company.getDepartamento(), company.getProvincia(), company.getDistrito());
        if (!ubicacion.isEmpty()) {
            leftCell.addElement(new Paragraph(ubicacion, FONT_NORMAL));
        }

        // Columna derecha: recuadro comprobante
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorderColor(Color.RED);
        rightCell.setBorderWidth(2);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        rightCell.setPadding(10);

        Paragraph docTypePara = new Paragraph(docTitle, FONT_DOC_TYPE);
        docTypePara.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(docTypePara);

        Paragraph seriePara = new Paragraph(serieNumero, new Font(Font.HELVETICA, 11, Font.BOLD));
        seriePara.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(seriePara);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        doc.add(headerTable);
    }

    private void addReceptorData(Document doc, ClientEntity client) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell titleCell = createHeaderCell("DATOS DEL RECEPTOR");
        table.addCell(titleCell);

        StringBuilder sb = new StringBuilder();
        sb.append("Razón Social / Nombre: ").append(safe(client.getDenominacion())).append("\n");
        sb.append("Tipo Doc: ").append(safe(client.getTipoDocumento()))
                .append("    Número: ").append(safe(client.getNumeroDocumento())).append("\n");
        if (client.getDireccion() != null && !client.getDireccion().isBlank()) {
            sb.append("Dirección: ").append(client.getDireccion());
        }

        PdfPCell dataCell = new PdfPCell(new Phrase(sb.toString(), FONT_NORMAL));
        dataCell.setPadding(6);
        dataCell.setBorderColor(BORDER_COLOR);
        table.addCell(dataCell);

        doc.add(table);
    }

    private void addEmisionData(Document doc, String fechaEmision, String moneda,
                                 String formaPago, String tipoOperacion) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 20, 25, 25});

        addLabelValueCell(table, "Fecha emisión:", safe(fechaEmision));
        addLabelValueCell(table, "Moneda:", safe(moneda));
        addLabelValueCell(table, "Forma de pago:", safe(formaPago));
        addLabelValueCell(table, "Tipo operación:", safe(tipoOperacion));

        doc.add(table);
    }

    private void addDetalleTable(Document doc, List<Map<String, Object>> detalle,
                                  String monedaSymbol) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5, 35, 8, 10, 15, 17});

        // Headers
        String[] headers = {"#", "Descripción", "UM", "Cant", "P.U.", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_HEADER));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            cell.setBorderColor(BORDER_COLOR);
            table.addCell(cell);
        }

        // Rows
        int idx = 1;
        if (detalle != null) {
            for (Map<String, Object> item : detalle) {
                addDetailCell(table, String.valueOf(idx++), Element.ALIGN_CENTER);
                addDetailCell(table, safeMap(item, "descripcion"), Element.ALIGN_LEFT);
                addDetailCell(table, safeMap(item, "unidadMedida"), Element.ALIGN_CENTER);
                addDetailCell(table, safeMap(item, "cantidad"), Element.ALIGN_CENTER);
                addDetailCell(table, formatMoney(safeMap(item, "precioUnitario")), Element.ALIGN_RIGHT);
                addDetailCell(table, formatMoney(safeMap(item, "subtotal")), Element.ALIGN_RIGHT);
            }
        }

        doc.add(table);
    }

    private void addTotales(Document doc, BigDecimal gravadas, BigDecimal exoneradas,
                             BigDecimal inafectas, BigDecimal igv, BigDecimal total,
                             String monedaSymbol) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{50, 50});

        addTotalRow(table, "Op. Gravadas:", monedaSymbol + " " + formatBd(gravadas));
        if (exoneradas != null && exoneradas.compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(table, "Op. Exoneradas:", monedaSymbol + " " + formatBd(exoneradas));
        }
        if (inafectas != null && inafectas.compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(table, "Op. Inafectas:", monedaSymbol + " " + formatBd(inafectas));
        }
        addTotalRow(table, "IGV (18%):", monedaSymbol + " " + formatBd(igv));

        // Total final en negrita
        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL:", FONT_BOLD));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(4);
        labelCell.setBorderColor(BORDER_COLOR);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(monedaSymbol + " " + formatBd(total), FONT_BOLD));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(4);
        valueCell.setBorderColor(BORDER_COLOR);
        table.addCell(valueCell);

        doc.add(table);
    }

    private void addQrAndHash(Document doc, CompanyEntity company, String tipoDoc,
                               String serie, Integer numero, BigDecimal igv, BigDecimal total,
                               String fechaEmision, ClientEntity client,
                               String hashCdr, String codigoSunat) throws DocumentException {
        // Generar QR
        String tipoDocReceptor = mapTipoDocIdentidad(client.getTipoDocumento());
        String qrContent = qrCodeService.buildSunatQrContent(
                safe(company.getRuc()), tipoDoc, safe(serie),
                numero != null ? numero : 0,
                formatBd(igv), formatBd(total), safe(fechaEmision),
                tipoDocReceptor, safe(client.getNumeroDocumento()),
                hashCdr
        );

        byte[] qrBytes = qrCodeService.generateQrPng(qrContent, 150);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 70});

        // QR Cell
        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(0);
        qrCell.setPadding(5);
        try {
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(100, 100);
            qrCell.addElement(qrImage);
        } catch (Exception e) {
            log.warn("No se pudo insertar QR en PDF: {}", e.getMessage());
        }
        table.addCell(qrCell);

        // Info Cell
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(0);
        infoCell.setPadding(5);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (hashCdr != null && !hashCdr.isBlank()) {
            infoCell.addElement(new Paragraph("Hash: " + hashCdr, FONT_SMALL));
        }
        infoCell.addElement(new Paragraph(
                "Código SUNAT: " + (codigoSunat != null ? codigoSunat : "—"), FONT_SMALL));
        infoCell.addElement(new Paragraph(
                "Representación impresa de la " + (tipoDoc.equals("01") ? "factura" : "boleta") + " electrónica",
                FONT_SMALL));
        infoCell.addElement(new Paragraph("Autorizado mediante Resolución de Superintendencia", FONT_SMALL));

        table.addCell(infoCell);
        doc.add(table);
    }

    // ═══ Utilidades ═══

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_HEADER));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(5);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private void addLabelValueCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(3);
        cell.addElement(new Paragraph(label, FONT_BOLD));
        cell.addElement(new Paragraph(value, FONT_NORMAL));
        table.addCell(cell);
    }

    private void addDetailCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(3);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_NORMAL));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(4);
        labelCell.setBorderColor(BORDER_COLOR);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_NORMAL));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(4);
        valueCell.setBorderColor(BORDER_COLOR);
        table.addCell(valueCell);
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private String safeMap(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private String formatBd(BigDecimal bd) {
        return bd != null ? String.format("%.2f", bd) : "0.00";
    }

    private String formatMoney(String value) {
        if (value == null || value.isBlank()) return "0.00";
        try {
            return String.format("%.2f", Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private String buildUbicacion(String departamento, String provincia, String distrito) {
        StringBuilder sb = new StringBuilder();
        if (departamento != null && !departamento.isBlank()) sb.append(departamento);
        if (provincia != null && !provincia.isBlank()) {
            if (!sb.isEmpty()) sb.append(" / ");
            sb.append(provincia);
        }
        if (distrito != null && !distrito.isBlank()) {
            if (!sb.isEmpty()) sb.append(" / ");
            sb.append(distrito);
        }
        return sb.toString();
    }

    /**
     * Mapea tipo de documento de identidad al código SUNAT para QR.
     */
    private String mapTipoDocIdentidad(String tipoDoc) {
        if (tipoDoc == null) return "0";
        return switch (tipoDoc.toUpperCase()) {
            case "DNI", "1" -> "1";
            case "RUC", "6" -> "6";
            case "CE", "4" -> "4";
            case "PASAPORTE", "7" -> "7";
            default -> tipoDoc;
        };
    }
}

