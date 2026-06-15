package com.etlions.ms_bs_etia.app_util.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * 📂 Servicio de almacenamiento de XMLs generados.
 * <p>
 * Estructura de carpetas:
 * <pre>
 *   {base-path}/{ruc}/{serie}/{numero}/{ruc}-{tipoDoc}-{serie}-{numero}.xml
 * </pre>
 * Ejemplo:
 * <pre>
 *   etia_storage/20517863948/F003/00001716/20517863948-01-F003-00001716.xml
 * </pre>
 */
@Slf4j
@Service
public class XmlStorageService {

    private final Path basePath;

    public XmlStorageService(@Value("${etia.storage.base-path:./etia_storage}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        log.info("📂 ETIA Storage configurado en: {}", this.basePath);
    }

    /**
     * Guarda el XML de un comprobante en disco.
     *
     * @param ruc       RUC del emisor
     * @param tipoDoc   Tipo de documento SUNAT (01=factura, 03=boleta)
     * @param serie     Serie del comprobante (ej: F003)
     * @param numero    Número correlativo
     * @param xmlContent Contenido XML como String
     * @return Ruta relativa donde se guardó el archivo
     */
    public String guardarXml(String ruc, String tipoDoc, String serie, int numero,
                             String xmlContent) {
        String numeroFormatted = String.format("%08d", numero);
        String filename = ruc + "-" + tipoDoc + "-" + serie + "-" + numeroFormatted + ".xml";

        // Ruta: {base}/{ruc}/{serie}/{numero}/{filename}
        Path dir = basePath.resolve(ruc).resolve(serie).resolve(numeroFormatted);
        Path file = dir.resolve(filename);

        try {
            Files.createDirectories(dir);
            Files.writeString(file, xmlContent, StandardCharsets.UTF_8);
            log.info("📄 XML guardado: {}", file);

            // Retornar ruta relativa desde basePath
            return basePath.relativize(file).toString();
        } catch (IOException e) {
            log.error("❌ Error guardando XML en {}: {}", file, e.getMessage());
            throw new RuntimeException("No se pudo guardar el XML: " + e.getMessage(), e);
        }
    }

    /**
     * Lee un XML previamente guardado por su ruta relativa.
     *
     * @param relativePath Ruta relativa (como la devuelve guardarXml)
     * @return Contenido del XML, o empty si no existe
     */
    public Optional<String> leerXml(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return Optional.empty();
        Path file = basePath.resolve(relativePath);
        if (!Files.exists(file)) {
            log.warn("⚠️ XML no encontrado: {}", file);
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("❌ Error leyendo XML de {}: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verifica si un XML existe en disco.
     */
    public boolean existe(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return false;
        return Files.exists(basePath.resolve(relativePath));
    }
}

