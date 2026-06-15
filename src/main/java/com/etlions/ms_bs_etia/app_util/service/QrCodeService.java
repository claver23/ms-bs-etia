package com.etlions.ms_bs_etia.app_util.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Servicio para generación de códigos QR en formato PNG.
 * Formato SUNAT: {RUC}|{TIPO_DOC}|{SERIE}|{NUMERO}|{IGV}|{TOTAL}|{FECHA_EMISION}|{TIPO_DOC_RECEPTOR}|{NUM_DOC_RECEPTOR}|{HASH_CDR}|
 */
@Slf4j
@Service
public class QrCodeService {

    /**
     * Genera una imagen QR en formato PNG.
     *
     * @param content Texto a codificar en el QR
     * @param size    Dimensión en píxeles (ancho = alto)
     * @return bytes del PNG
     */
    public byte[] generateQrPng(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("❌ Error generando QR: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando código QR", e);
        }
    }

    /**
     * Genera el contenido QR en formato estándar SUNAT.
     */
    public String buildSunatQrContent(String ruc, String tipoDoc, String serie, int numero,
                                       String igv, String total, String fechaEmision,
                                       String tipoDocReceptor, String numDocReceptor,
                                       String hashCdr) {
        return String.join("|",
                ruc,
                tipoDoc,
                serie,
                String.format("%08d", numero),
                igv,
                total,
                fechaEmision,
                tipoDocReceptor,
                numDocReceptor,
                hashCdr != null ? hashCdr : ""
        ) + "|";
    }
}

