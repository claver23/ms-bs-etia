package com.etlions.ms_bs_etia.company.util;

import com.etlions.ms_bs_etia.company.service.LogoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoUtil — Utilitario de logos")
class LogoUtilTest {

    @Mock
    private LogoStorageService logoStorageService;

    @InjectMocks
    private LogoUtil logoUtil;

    private byte[] createTestImage(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @Nested
    @DisplayName("validateDimensions")
    class ValidateDimensions {

        @Test
        @DisplayName("imagen válida retorna ok")
        void imagenValida_retornaOk() throws IOException {
            byte[] img = createTestImage(200, 200);
            var result = logoUtil.validateDimensions(img, "png");
            assertTrue(result.isValid());
            assertFalse(result.tooSmall());
            assertFalse(result.tooLarge());
            assertFalse(result.isVector());
            assertEquals(200, result.width());
            assertEquals(200, result.height());
        }

        @Test
        @DisplayName("imagen muy pequeña retorna tooSmall")
        void imagenMuyPequena_retornaTooSmall() throws IOException {
            byte[] img = createTestImage(30, 30);
            var result = logoUtil.validateDimensions(img, "png");
            assertFalse(result.isValid());
            assertTrue(result.tooSmall());
            assertFalse(result.tooLarge());
            assertEquals(30, result.width());
        }

        @Test
        @DisplayName("imagen muy grande retorna tooLarge")
        void imagenMuyGrande_retornaTooLarge() throws IOException {
            byte[] img = createTestImage(3000, 2500);
            var result = logoUtil.validateDimensions(img, "png");
            assertFalse(result.isValid());
            assertFalse(result.tooSmall());
            assertTrue(result.tooLarge());
            assertEquals(3000, result.width());
        }

        @Test
        @DisplayName("SVG siempre es válida (vectorial)")
        void svg_siempreValida() {
            byte[] svgBytes = "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes();
            var result = logoUtil.validateDimensions(svgBytes, "svg");
            assertTrue(result.isValid());
            assertTrue(result.isVector());
            assertEquals(0, result.width());
        }

        @Test
        @DisplayName("imagen en el límite mínimo es válida")
        void imagenLimiteMinimo_esValida() throws IOException {
            byte[] img = createTestImage(50, 50);
            var result = logoUtil.validateDimensions(img, "png");
            assertTrue(result.isValid());
            assertFalse(result.tooSmall());
        }

        @Test
        @DisplayName("imagen en el límite máximo es válida")
        void imagenLimiteMaximo_esValida() throws IOException {
            byte[] img = createTestImage(2000, 2000);
            var result = logoUtil.validateDimensions(img, "png");
            assertTrue(result.isValid());
            assertFalse(result.tooLarge());
        }
    }

    @Nested
    @DisplayName("resizeImage")
    class ResizeImage {

        @Test
        @DisplayName("reduce tamaño correctamente")
        void reduceTamano_correctamente() throws IOException {
            byte[] img = createTestImage(800, 600);
            byte[] resized = logoUtil.resizeImage(img, 400, 400, "png");
            assertNotNull(resized);
            assertTrue(resized.length > 0);
            // El resultado debe ser diferente (más pequeño)
            BufferedImage result = ImageIO.read(new java.io.ByteArrayInputStream(resized));
            assertNotNull(result);
            assertTrue(result.getWidth() <= 400);
            assertTrue(result.getHeight() <= 400);
        }

        @Test
        @DisplayName("SVG no se redimensiona")
        void svg_noRedimensiona() {
            byte[] svgBytes = "<svg></svg>".getBytes();
            byte[] result = logoUtil.resizeImage(svgBytes, 400, 400, "svg");
            assertArrayEquals(svgBytes, result);
        }

        @Test
        @DisplayName("mantiene aspect ratio")
        void mantieneAspectRatio() throws IOException {
            byte[] img = createTestImage(1000, 500); // ratio 2:1
            byte[] resized = logoUtil.resizeImage(img, 400, 400, "png");
            BufferedImage result = ImageIO.read(new java.io.ByteArrayInputStream(resized));
            assertNotNull(result);
            // Con ratio 2:1, al reducir a max 400x400, debería ser 400x200
            assertEquals(400, result.getWidth());
            assertEquals(200, result.getHeight());
        }
    }

    @Nested
    @DisplayName("getLogoBase64DataUrl")
    class GetLogoBase64DataUrl {

        @Test
        @DisplayName("con logo retorna data URL")
        void conLogo_retornaDataUrl() throws IOException {
            byte[] img = createTestImage(100, 100);
            when(logoStorageService.leerLogo("20601234567")).thenReturn(Optional.of(img));
            when(logoStorageService.getContentType("20601234567")).thenReturn("image/png");

            Optional<String> result = logoUtil.getLogoBase64DataUrl("20601234567");
            assertTrue(result.isPresent());
            assertTrue(result.get().startsWith("data:image/png;base64,"));
        }

        @Test
        @DisplayName("sin logo retorna empty")
        void sinLogo_retornaEmpty() {
            when(logoStorageService.leerLogo("99999999999")).thenReturn(Optional.empty());

            Optional<String> result = logoUtil.getLogoBase64DataUrl("99999999999");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("con logo SVG retorna data URL SVG")
        void conLogoSvg_retornaDataUrlSvg() {
            byte[] svgBytes = "<svg xmlns='http://www.w3.org/2000/svg'><circle r='50'/></svg>".getBytes();
            when(logoStorageService.leerLogo("20601234567")).thenReturn(Optional.of(svgBytes));
            when(logoStorageService.getContentType("20601234567")).thenReturn("image/svg+xml");

            Optional<String> result = logoUtil.getLogoBase64DataUrl("20601234567");
            assertTrue(result.isPresent());
            assertTrue(result.get().startsWith("data:image/svg+xml;base64,"));
        }
    }
}

