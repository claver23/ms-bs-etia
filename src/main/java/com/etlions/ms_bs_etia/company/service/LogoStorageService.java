package com.etlions.ms_bs_etia.company.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;

/**
 * 🖼️ Servicio de almacenamiento de logos de empresa.
 * <p>
 * Estructura:
 * <pre>
 *   etia_storage/company_logos/{ruc}/logo.png
 * </pre>
 */
@Slf4j
@Service
public class LogoStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".svg");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/svg+xml");

    private final Path basePath;

    public LogoStorageService(@Value("${etia.storage.base-path:./etia_storage}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize().resolve("company_logos");
        log.info("🖼️ Logo storage configurado en: {}", this.basePath);
    }

    /**
     * Guarda el logo de una empresa.
     *
     * @param ruc     RUC de la empresa
     * @param archivo Archivo de imagen subido
     * @return Ruta relativa donde se guardó el archivo
     */
    public String guardarLogo(String ruc, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de logo está vacío");
        }
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de 10 MB");
        }

        String originalFilename = archivo.getOriginalFilename();
        String contentType = archivo.getContentType();

        // Validate extension
        if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("Solo se permiten archivos .png, .jpg, .jpeg o .svg");
        }

        // Validate content type
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de contenido no permitido: " + contentType);
        }

        // Always save as logo.png (regardless of original extension)
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        String filename = "logo" + extension;

        Path dir = basePath.resolve(ruc);
        Path file = dir.resolve(filename);

        try {
            Files.createDirectories(dir);
            // Remove existing logos first
            eliminarLogo(ruc);
            Files.copy(archivo.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
            log.info("🖼️ Logo guardado: {}", file);
            return "company_logos/" + ruc + "/" + filename;
        } catch (IOException e) {
            log.error("❌ Error guardando logo en {}: {}", file, e.getMessage());
            throw new RuntimeException("No se pudo guardar el logo: " + e.getMessage(), e);
        }
    }

    /**
     * Lee el logo de una empresa como bytes.
     *
     * @param ruc RUC de la empresa
     * @return bytes del logo, o empty si no existe
     */
    public Optional<byte[]> leerLogo(String ruc) {
        Path dir = basePath.resolve(ruc);
        if (!Files.exists(dir)) return Optional.empty();
        try (var files = Files.list(dir)) {
            return files.filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.startsWith("logo");
            }).findFirst().map(p -> {
                try { return Files.readAllBytes(p); }
                catch (IOException e) { return null; }
            });
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene el content type del logo.
     */
    public String getContentType(String ruc) {
        Path dir = basePath.resolve(ruc);
        if (!Files.exists(dir)) return "image/png";
        try (var files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().toLowerCase().startsWith("logo"))
                    .findFirst()
                    .map(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
                        if (name.endsWith(".svg")) return "image/svg+xml";
                        return "image/png";
                    })
                    .orElse("image/png");
        } catch (IOException e) {
            return "image/png";
        }
    }

    /**
     * Obtiene el formato del logo (extensión sin punto).
     * @return "png", "jpg", "jpeg", "svg" o "png" por defecto
     */
    public String getLogoFormat(String ruc) {
        Path dir = basePath.resolve(ruc);
        if (!Files.exists(dir)) return "png";
        try (var files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().toLowerCase().startsWith("logo"))
                    .findFirst()
                    .map(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        int dotIdx = name.lastIndexOf('.');
                        return dotIdx >= 0 ? name.substring(dotIdx + 1) : "png";
                    })
                    .orElse("png");
        } catch (IOException e) {
            return "png";
        }
    }

    /**
     * Elimina logos anteriores de la empresa.
     */
    public void eliminarLogo(String ruc) {
        Path dir = basePath.resolve(ruc);
        if (!Files.exists(dir)) return;
        try (var files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().toLowerCase().startsWith("logo"))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException e) {
            log.warn("⚠️ Error eliminando logo de {}: {}", ruc, e.getMessage());
        }
    }

    private boolean hasAllowedExtension(String filename) {
        String lower = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }
}

