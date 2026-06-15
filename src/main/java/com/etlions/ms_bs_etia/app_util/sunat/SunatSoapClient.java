package com.etlions.ms_bs_etia.app_util.sunat;

/**
 * Interface para comunicación SOAP con SUNAT.
 * <p>
 * TODO: FASE-3 — Implementar cliente SOAP real que conecte con:
 * - Envío: https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService (beta)
 * - Consulta: https://e-beta.sunat.gob.pe/ol-it-wsconscpegem-beta/billConsultService (beta)
 * <p>
 * Para cambiar al servicio real, crear una nueva implementación de esta interface
 * con @Profile("production") y activar el perfil correspondiente.
 */
public interface SunatSoapClient {

    /**
     * Envia un comprobante individual (factura/NC/ND) a SUNAT via sendBill.
     *
     * @param zipBytes  ZIP conteniendo el XML firmado
     * @param filename  Nombre del archivo (RUC-tipoDoc-serie-numero.zip)
     * @param ruc       RUC del emisor
     * @return BillResult con CDR si fue exitoso
     */
    BillResult sendBill(byte[] zipBytes, String filename, String ruc);

    /**
     * Envía un resumen diario (Summary Documents) a SUNAT.
     *
     * @param identificador Identificador del resumen (RC-YYYYMMDD-NNN)
     * @param ruc           RUC del emisor
     * @return SendResult con ticket si fue exitoso
     */
    SendResult sendSummary(String identificador, String ruc);

    /**
     * Consulta el estado de un ticket en SUNAT.
     *
     * @param ticket Número de ticket recibido al enviar
     * @param ruc    RUC del emisor
     * @return StatusResult con el estado del procesamiento
     */
    StatusResult getStatus(String ticket, String ruc);
}

