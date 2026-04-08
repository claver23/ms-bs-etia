package com.etlions.ms_bs_etia.app_util.validation;

/**
 * 🧾 Utilidades de formato SUNAT para comprobantes electrónicos.
 * Centraliza el formateo de serie-número con padding a 8 dígitos.
 */
public final class SunatFormatter {

    private SunatFormatter() { /* utility class */ }

    /**
     * Formatea serie + número al estándar SUNAT: F001-00000011
     *
     * @param serie  Serie del comprobante (ej: "F001")
     * @param numero Número correlativo
     * @return String formateado (ej: "F001-00000011")
     */
    public static String formatNumeroComprobante(String serie, int numero) {
        return String.format("%s-%08d", serie != null ? serie : "F001", numero);
    }

    /**
     * Genera el nombre de archivo SUNAT para un XML de comprobante.
     * Formato: {RUC}-{tipoDoc}-{serie}-{numero08d}.xml
     *
     * @param ruc     RUC del emisor
     * @param tipoDoc Tipo de documento SUNAT (01=factura, 03=boleta, 07=NC, 08=ND)
     * @param serie   Serie del comprobante
     * @param numero  Número correlativo
     * @return Nombre de archivo (ej: "20517863948-01-F003-00001716.xml")
     */
    public static String formatFilename(String ruc, String tipoDoc, String serie, int numero) {
        return String.format("%s-%s-%s-%08d.xml", ruc, tipoDoc, serie, numero);
    }
}

