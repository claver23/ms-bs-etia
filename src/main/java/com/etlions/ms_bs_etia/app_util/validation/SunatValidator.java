package com.etlions.ms_bs_etia.app_util.validation;

import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SunatValidator {

    private SunatValidator() {
    }

    /* Tipos de documento de identidad permitidos por tipo de comprobante (SUNAT) */
    private static final Set<String> FACTURA_DOC_TYPES = Set.of("6");               // RUC
    private static final Set<String> BOLETA_DOC_TYPES  = Set.of("1", "4", "7");     // DNI, C.E., Pasaporte

    /**
     * Valida que el tipo de documento de identidad del cliente sea compatible
     * con el tipo de comprobante (FACTURA o BOLETA) según reglas SUNAT.
     */
    public static void validarTipoDocumentoCliente(String tipoComprobante, String tipoDocCliente) {
        if (tipoDocCliente == null || tipoDocCliente.isBlank()) {
            throw new SunatValidationException(
                    "SUNAT: El tipo de documento de identidad del cliente es obligatorio.");
        }
        if ("FACTURA".equalsIgnoreCase(tipoComprobante)) {
            if (!FACTURA_DOC_TYPES.contains(tipoDocCliente)) {
                throw new SunatValidationException(
                        "SUNAT: Para una Factura, el cliente debe tener RUC (tipo documento '6'). "
                                + "Tipo recibido: '" + tipoDocCliente + "'.");
            }
        } else if ("BOLETA".equalsIgnoreCase(tipoComprobante)) {
            if (!BOLETA_DOC_TYPES.contains(tipoDocCliente)) {
                throw new SunatValidationException(
                        "SUNAT: Para una Boleta, el cliente debe tener DNI ('1'), Carnet de Extranjería ('4') "
                                + "o Pasaporte ('7'). Tipo recibido: '" + tipoDocCliente + "'.");
            }
        }
    }

    /**
     * Valida que la serie de una factura comience con "F".
     */
    public static void validarSerieFactura(String serie) {
        if (serie == null || !serie.toUpperCase().startsWith("F")) {
            throw new SunatValidationException(
                    "SUNAT: La serie de una factura debe iniciar con 'F' (ej: F001). Serie recibida: " + serie);
        }
    }

    /**
     * Valida que la serie de una boleta comience con "B".
     */
    public static void validarSerieBoleta(String serie) {
        if (serie == null || !serie.toUpperCase().startsWith("B")) {
            throw new SunatValidationException(
                    "SUNAT: La serie de una boleta debe iniciar con 'B' (ej: B001). Serie recibida: " + serie);
        }
    }

    /**
     * Valida que total == gravadas + igv (con tolerancia de 0.01 por redondeo).
     */
    public static void validarTotales(BigDecimal gravadas, BigDecimal igv, BigDecimal total) {
        List<String> errors = new ArrayList<>();

        if (gravadas == null) {
            errors.add("El monto gravado es obligatorio");
        }
        if (igv == null) {
            errors.add("El IGV es obligatorio");
        }
        if (total == null) {
            errors.add("El total es obligatorio");
        }
        if (!errors.isEmpty()) {
            throw new SunatValidationException("SUNAT: Campos monetarios incompletos", errors);
        }

        BigDecimal esperado = gravadas.add(igv).setScale(2, RoundingMode.HALF_UP);
        BigDecimal diff = total.subtract(esperado).abs();

        if (diff.compareTo(new BigDecimal("0.01")) > 0) {
            throw new SunatValidationException(
                    "SUNAT: El total (" + total + ") debe ser igual a gravadas (" + gravadas
                            + ") + IGV (" + igv + "). Esperado: " + esperado);
        }
    }

    /**
     * Valida que un comprobante solo pueda eliminarse si está en estado BORRADOR.
     */
    public static void validarEliminacionPermitida(String estado, String tipoDoc, String docId) {
        if (!"BORRADOR".equalsIgnoreCase(estado)) {
            throw new SunatValidationException(
                    "SUNAT: No se puede eliminar " + tipoDoc + " [" + docId
                            + "] porque su estado es '" + estado
                            + "'. Solo los comprobantes en estado BORRADOR pueden eliminarse.");
        }
    }

    /**
     * Valida que la serie tenga formato correcto: 1 letra + 3 dígitos (ej: F001, B001).
     */
    public static void validarFormatoSerie(String serie) {
        if (serie == null || !serie.matches("^[A-Z]\\d{3}$")) {
            throw new SunatValidationException(
                    "SUNAT: Formato de serie inválido '" + serie
                            + "'. Debe ser 1 letra mayúscula + 3 dígitos (ej: F001, B001).");
        }
    }
}

