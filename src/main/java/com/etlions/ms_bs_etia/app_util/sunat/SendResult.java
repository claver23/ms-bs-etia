package com.etlions.ms_bs_etia.app_util.sunat;

/**
 * Resultado de enviar un resumen diario a SUNAT.
 */
public record SendResult(boolean exitoso, String ticket, String mensaje) {}

