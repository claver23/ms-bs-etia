package com.etlions.ms_bs_etia.app_util.sunat;

/**
 * Resultado de consultar el estado de un ticket en SUNAT.
 */
public record StatusResult(boolean exitoso, String estado, String codigoRespuesta, String descripcion) {}

