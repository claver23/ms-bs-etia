package com.etlions.ms_bs_etia.company.util;

import com.etlions.ms_bs_etia.company.service.LogoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

/**
 * 🖼️ Utilitario para manipulación de logos: bytes → base64, validación dimensiones, resize.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogoUtil {

    private final LogoStorageService logoStorageService;

    public static final int MIN_WIDTH = 50;
    public static final int MIN_HEIGHT = 50;
    public static final int MAX_WIDTH = 2000;
    public static final int MAX_HEIGHT = 2000;
    public static final int RECOMMENDED_WIDTH = 400;
    public static final int RECOMMENDED_HEIGHT = 400;

    /**
     * Obtiene bytes del logo de una empresa.
     */
    public Optional<byte[]> getLogoBytes(String ruc) {
        return logoStorageService.leerLogo(ruc);
    }

    /**
     * Convierte logo a data URL base64 para embeber en PDF.
     * Retorna "data:image/{format};base64,..." o empty si no hay logo.
     */
    public Optional<String> getLogoBase64DataUrl(String ruc) {
        Optional<byte[]> bytesOpt = logoStorageService.leerLogo(ruc);
        if (bytesOpt.isEmpty() || bytesOpt.get() == null) {
            return Optional.empty();
        }
        byte[] bytes = bytesOpt.get();
        String contentType = logoStorageService.getContentType(ruc);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return Optional.of("data:" + contentType + ";base64," + base64);
    }

    /**
     * Valida dimensiones de una imagen PNG/JPG.
     * SVG siempre pasa (es vectorial).
     */
    public LogoDimensionResult validateDimensions(byte[] imageBytes, String format) {
        if (format != null && format.equalsIgnoreCase("svg")) {
            return new LogoDimensionResult(0, 0, false, false, true,
                    "SVG es vectorial — dimensiones no aplican");
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) {
                return new LogoDimensionResult(0, 0, false, false, false,
                        "No se pudo leer la imagen");
            }
            int w = img.getWidth();
            int h = img.getHeight();
            boolean tooSmall = w < MIN_WIDTH || h < MIN_HEIGHT;
            boolean tooLarge = w > MAX_WIDTH || h > MAX_HEIGHT;
            String message;
            if (tooSmall) {
                message = "Imagen muy pequeña (" + w + "×" + h + "). Mínimo: " + MIN_WIDTH + "×" + MIN_HEIGHT;
            } else if (tooLarge) {
                message = "Imagen muy grande (" + w + "×" + h + "). Máximo: " + MAX_WIDTH + "×" + MAX_HEIGHT;
            } else {
                message = "Dimensiones válidas: " + w + "×" + h;
            }
            return new LogoDimensionResult(w, h, tooSmall, tooLarge, false, message);
        } catch (IOException e) {
            log.warn("⚠️ Error leyendo dimensiones de imagen: {}", e.getMessage());
            return new LogoDimensionResult(0, 0, false, false, false,
                    "Error leyendo imagen: " + e.getMessage());
        }
    }

    /**
     * Redimensiona imagen a dimensiones objetivo manteniendo aspect ratio.
     * Solo aplica a PNG/JPG (no SVG).
     */
    public byte[] resizeImage(byte[] imageBytes, int targetWidth, int targetHeight, String format) {
        if (format != null && format.equalsIgnoreCase("svg")) {
            return imageBytes; // SVG es vectorial, no se redimensiona
        }
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) return imageBytes;

            int origW = original.getWidth();
            int origH = original.getHeight();

            // Mantener aspect ratio
            double ratio = Math.min((double) targetWidth / origW, (double) targetHeight / origH);
            int newW = (int) Math.round(origW * ratio);
            int newH = (int) Math.round(origH * ratio);

            BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(original, 0, 0, newW, newH, null);
            g2d.dispose();

            String outputFormat = "png";
            if (format != null && (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg"))) {
                outputFormat = "jpg";
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, outputFormat, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.warn("⚠️ Error redimensionando imagen: {}", e.getMessage());
            return imageBytes;
        }
    }

    /**
     * Resultado de validación de dimensiones.
     */
    public record LogoDimensionResult(
            int width, int height,
            boolean tooSmall, boolean tooLarge, boolean isVector,
            String message
    ) {
        public boolean isValid() {
            return isVector || (!tooSmall && !tooLarge && width > 0 && height > 0);
        }
    }
}

