package com.etlions.ms_bs_etia.app_util.sunat;

/**
 * Resultado del envio de un comprobante individual (factura/NC/ND) a SUNAT via sendBill.
 */
public record BillResult(
    boolean success,
    String hashCdr,
    String codigoRespuesta,
    String descripcionRespuesta,
    byte[] cdrZipBytes
) {
    public static BillResult error(String descripcion) {
        return new BillResult(false, null, "9999", descripcion, null);
    }
}

