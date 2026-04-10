package com.etlions.ms_bs_etia.app_util.sunat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Servicio para comprimir/descomprimir archivos ZIP requeridos por SUNAT.
 * SUNAT exige enviar el XML dentro de un ZIP y devuelve el CDR tambien en ZIP.
 */
@Slf4j
@Service
public class SunatZipService {

    /**
     * Comprime un XML en formato ZIP con el nombre de archivo SUNAT.
     * Formato: {RUC}-{tipoDoc}-{serie}-{numero}.zip conteniendo el .xml
     */
    public byte[] zipXml(String xmlContent, String xmlFilename) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(xmlFilename));
            zos.write(xmlContent.getBytes("UTF-8"));
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error comprimiendo XML: {}", e.getMessage());
            throw new RuntimeException("Error al comprimir XML para SUNAT", e);
        }
    }

    /**
     * Extrae el primer archivo XML de un ZIP (para parsear CDR de SUNAT).
     */
    public String unzipFirstXml(byte[] zipBytes) {
        if (zipBytes == null || zipBytes.length == 0) return null;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".xml")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    return baos.toString("UTF-8");
                }
            }
        } catch (IOException e) {
            log.error("Error descomprimiendo ZIP: {}", e.getMessage());
        }
        return null;
    }
}

