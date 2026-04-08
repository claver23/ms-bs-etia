package com.etlions.ms_bs_etia.app_util.validation;

import java.util.List;

/**
 * Resultado de validación interna de un documento electrónico.
 * <p>
 * A diferencia de {@link SunatValidator} (que lanza excepciones),
 * este record acumula errores y advertencias sin interrumpir el flujo.
 * </p>
 *
 * @param valid        true si el documento pasó todas las validaciones
 * @param errores      errores que bloquean la emisión
 * @param advertencias advertencias informativas (no bloquean)
 */
public record DocumentValidationResult(
        boolean valid,
        List<String> errores,
        List<String> advertencias
) {

    /** Documento válido sin advertencias */
    public static DocumentValidationResult ok() {
        return new DocumentValidationResult(true, List.of(), List.of());
    }

    /** Documento válido con advertencias */
    public static DocumentValidationResult ok(List<String> advertencias) {
        return new DocumentValidationResult(true, List.of(), advertencias != null ? advertencias : List.of());
    }

    /** Documento inválido con errores */
    public static DocumentValidationResult fail(List<String> errores) {
        return new DocumentValidationResult(false, errores != null ? errores : List.of(), List.of());
    }

    /** Documento inválido con errores y advertencias */
    public static DocumentValidationResult fail(List<String> errores, List<String> advertencias) {
        return new DocumentValidationResult(false,
                errores != null ? errores : List.of(),
                advertencias != null ? advertencias : List.of());
    }
}

