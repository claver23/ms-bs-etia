package com.etlions.ms_bs_etia.app_util.sunat;

/**
 * Resultado del parsing de un CDR (Constancia de Recepcion) de SUNAT.
 */
public record CdrResult(
    String codigoRespuesta,
    String descripcion,
    String hashFirma,
    boolean aceptado
) {
    public static CdrResult accepted(String codigo, String desc, String hash) {
        return new CdrResult(codigo, desc, hash, true);
    }

    public static CdrResult rejected(String codigo, String desc) {
        return new CdrResult(codigo, desc, null, false);
    }
}

