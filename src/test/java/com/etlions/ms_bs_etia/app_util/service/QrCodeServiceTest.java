package com.etlions.ms_bs_etia.app_util.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    @DisplayName("generateQrPng — genera PNG no vacío con datos SUNAT")
    void generateQrPng_sunatContent_returnsNonEmptyPng() {
        String content = "20601234567|01|F001|00000001|450.00|2950.00|2026-04-09|6|20505050501|abc123hash|";
        byte[] pngBytes = qrCodeService.generateQrPng(content, 150);

        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 0, "El PNG generado no debe estar vacío");

        // Verificar magic bytes PNG: 0x89 0x50 0x4E 0x47
        assertEquals((byte) 0x89, pngBytes[0]);
        assertEquals((byte) 0x50, pngBytes[1]);
        assertEquals((byte) 0x4E, pngBytes[2]);
        assertEquals((byte) 0x47, pngBytes[3]);
    }

    @Test
    @DisplayName("generateQrPng — verifica dimensiones de la imagen generada")
    void generateQrPng_checkDimensions() throws Exception {
        byte[] pngBytes = qrCodeService.generateQrPng("TEST", 200);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(pngBytes));

        assertNotNull(img, "La imagen debe ser legible");
        assertEquals(200, img.getWidth(), "Ancho debe ser 200px");
        assertEquals(200, img.getHeight(), "Alto debe ser 200px");
    }

    @Test
    @DisplayName("generateQrPng — tamaño pequeño (50px) no falla")
    void generateQrPng_smallSize_works() {
        byte[] pngBytes = qrCodeService.generateQrPng("small", 50);
        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 0);
    }

    @Test
    @DisplayName("buildSunatQrContent — formato correcto estándar SUNAT")
    void buildSunatQrContent_correctFormat() {
        String result = qrCodeService.buildSunatQrContent(
                "20601234567", "01", "F001", 1,
                "450.00", "2950.00", "2026-04-09",
                "6", "20505050501", "abc123hash"
        );

        assertEquals("20601234567|01|F001|00000001|450.00|2950.00|2026-04-09|6|20505050501|abc123hash|", result);
    }

    @Test
    @DisplayName("buildSunatQrContent — hash null se convierte en vacío")
    void buildSunatQrContent_nullHash() {
        String result = qrCodeService.buildSunatQrContent(
                "20601234567", "03", "B001", 5,
                "18.00", "118.00", "2026-04-09",
                "1", "12345678", null
        );

        assertTrue(result.endsWith("||"), "Debe terminar con || cuando hash es null");
        assertTrue(result.contains("B001|00000005"), "Debe formatear número a 8 dígitos");
    }
}

