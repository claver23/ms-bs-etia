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

/**
 * 📜 Servicio de almacenamiento de certificados digitales (.p12/.pfx).
 * <p>
 * Estructura de carpetas:
 * <pre>
 *   etia_storage/company_certs/{ruc}/certificado.p12
 * </pre>
 */
@Slf4j
@Service
public class CertificadoStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final String[] ALLOWED_EXTENSIONS = {".p12", ".pfx"};

    private final Path basePath;

    public CertificadoStorageService(@Value("${etia.storage.base-path:./etia_storage}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize().resolve("company_certs");
        log.info("📜 Certificado storage configurado en: {}", this.basePath);
    }

    /**
     * Guarda el certificado digital de una empresa.
     *
     * @param ruc     RUC de la empresa
     * @param archivo Archivo .p12 o .pfx subido
     * @return Ruta relativa donde se guardó el archivo
     * @throws IllegalArgumentException si el archivo es inválido
     */
    public String guardarCertificado(String ruc, MultipartFile archivo) {
        // Validar que no esté vacío
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de certificado está vacío");
        }

        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de 10 MB");
        }

        // Validar extensión
        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("Solo se permiten archivos .p12 o .pfx");
        }

        // Determinar extensión
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        String filename = "certificado" + extension;

        // Guardar en disco
        Path dir = basePath.resolve(ruc);
        Path file = dir.resolve(filename);

        try {
            Files.createDirectories(dir);
            Files.copy(archivo.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
            log.info("📜 Certificado guardado: {}", file);

            // Retornar ruta relativa desde el directorio padre del basePath
            return "company_certs/" + ruc + "/" + filename;
        } catch (IOException e) {
            log.error("❌ Error guardando certificado en {}: {}", file, e.getMessage());
            throw new RuntimeException("No se pudo guardar el certificado: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un certificado existe en disco.
     */
    public boolean existe(String ruc) {
        Path dir = basePath.resolve(ruc);
        if (!Files.exists(dir)) return false;
        try (var files = Files.list(dir)) {
            return files.anyMatch(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.startsWith("certificado") && (name.endsWith(".p12") || name.endsWith(".pfx"));
            });
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Elimina el certificado de una empresa.
     */
    public void eliminarCertificado(String ruc) {
        Path dir = basePath.resolve(ruc);
        if (!Files.exists(dir)) return;
        try (var files = Files.list(dir)) {
            files.filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.startsWith("certificado");
            }).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
            log.info("📜 Certificado eliminado para RUC: {}", ruc);
        } catch (IOException e) {
            log.warn("⚠️ Error eliminando certificado de {}: {}", ruc, e.getMessage());
        }
    }

    private boolean hasAllowedExtension(String filename) {
        String lower = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }
}

