package com.etlions.ms_bs_etia.app_util.validation;

import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SunatValidator {

    private SunatValidator() {
    }

    /* Tipos de documento de identidad permitidos por tipo de comprobante (SUNAT) */
    private static final Set<String> FACTURA_DOC_TYPES = Set.of("6");               // RUC
    private static final Set<String> BOLETA_DOC_TYPES  = Set.of("1", "4", "7");     // DNI, C.E., Pasaporte

    /**
     * Transiciones de estado válidas para comprobantes electrónicos.
     * <pre>
     *   BORRADOR  → ENVIADA          (emitir / enviar a SUNAT)
     *   ENVIADA   → ACEPTADA         (SUNAT acepta)
     *   ENVIADA   → RECHAZADA        (SUNAT rechaza)
     *   ENVIADA   → ANULADA          (anular antes de respuesta)
     *   ACEPTADA  → ANULADA          (anulación post-aceptación)
     *   RECHAZADA → BORRADOR         (corregir para re-emitir)
     * </pre>
     */
    private static final Map<String, Set<String>> TRANSICIONES_VALIDAS = Map.of(
            "BORRADOR",   Set.of("ENVIADA"),
            "ENVIADA",    Set.of("ACEPTADA", "RECHAZADA", "ANULADA"),
            "ACEPTADA",   Set.of("ANULADA"),
            "RECHAZADA",  Set.of("BORRADOR"),
            "ANULADA",    Set.of()            // estado terminal
    );

    /**
     * Valida que la transición de estado sea permitida según las reglas SUNAT.
     *
     * @param estadoActual  Estado actual del comprobante
     * @param estadoNuevo   Estado al que se desea transicionar
     * @param tipoDoc       Tipo de documento (para mensaje: "Factura", "Boleta", etc.)
     * @param docId         ID del documento (para mensaje de error)
     */
    public static void validarTransicionEstado(String estadoActual, String estadoNuevo,
                                                String tipoDoc, String docId) {
        if (estadoActual == null) {
            throw new SunatValidationException(
                    "SUNAT: El " + tipoDoc + " [" + docId + "] no tiene un estado definido.");
        }
        Set<String> permitidos = TRANSICIONES_VALIDAS.get(estadoActual.toUpperCase());
        if (permitidos == null) {
            throw new SunatValidationException(
                    "SUNAT: Estado desconocido '" + estadoActual + "' en " + tipoDoc + " [" + docId + "].");
        }
        if (!permitidos.contains(estadoNuevo.toUpperCase())) {
            throw new SunatValidationException(
                    "SUNAT: No se puede cambiar el estado de " + tipoDoc + " [" + docId
                            + "] de '" + estadoActual + "' a '" + estadoNuevo
                            + "'. Transiciones permitidas desde '" + estadoActual + "': " + permitidos + ".");
        }
    }

    /**
     * Valida que se pueda anular un comprobante.
     * Solo se permite anular desde ENVIADA o ACEPTADA.
     */
    public static void validarAnulacionPermitida(String estado, String tipoDoc, String docId) {
        validarTransicionEstado(estado, "ANULADA", tipoDoc, docId);
    }

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

    // ── Nuevas validaciones ──────────────────────────────────────

    /**
     * Valida que un campo ID obligatorio no sea null ni vacío.
     */
    public static void validarCampoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new SunatValidationException(
                    "SUNAT: El campo '" + campo + "' es obligatorio y no puede estar vacío.");
        }
    }

    /**
     * Valida que el detalle del comprobante no esté vacío.
     * SUNAT exige al menos un ítem en todo comprobante electrónico.
     */
    public static <T> void validarDetalleNoVacio(List<T> detalle) {
        if (detalle == null || detalle.isEmpty()) {
            throw new SunatValidationException(
                    "SUNAT: El comprobante debe tener al menos un ítem en el detalle.");
        }
    }

    /**
     * Valida que cada ítem del detalle tenga descripción no vacía,
     * cantidad > 0 y valor unitario >= 0.
     *
     * @param descripciones  list of description values
     * @param cantidades     list of quantity values
     * @param valoresUnit    list of unit-value values
     */
    public static void validarItemsDetalle(List<String> descripciones,
                                            List<BigDecimal> cantidades,
                                            List<BigDecimal> valoresUnit) {
        List<String> errors = new ArrayList<>();
        int size = descripciones.size();
        for (int i = 0; i < size; i++) {
            String prefix = "Ítem [" + (i + 1) + "]: ";
            String desc = descripciones.get(i);
            if (desc == null || desc.isBlank()) {
                errors.add(prefix + "la descripción es obligatoria");
            }
            BigDecimal cant = cantidades.get(i);
            if (cant == null || cant.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(prefix + "la cantidad debe ser mayor a 0");
            }
            BigDecimal val = valoresUnit.get(i);
            if (val == null || val.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(prefix + "el valor unitario no puede ser negativo");
            }
        }
        if (!errors.isEmpty()) {
            throw new SunatValidationException("SUNAT: Errores en el detalle del comprobante", errors);
        }
    }

    /**
     * Valida que los montos (gravadas, igv, total) no sean negativos.
     */
    public static void validarMontosNoNegativos(BigDecimal gravadas, BigDecimal igv, BigDecimal total) {
        List<String> errors = new ArrayList<>();
        if (gravadas != null && gravadas.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("El monto gravado no puede ser negativo: " + gravadas);
        }
        if (igv != null && igv.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("El IGV no puede ser negativo: " + igv);
        }
        if (total != null && total.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("El total no puede ser negativo: " + total);
        }
        if (!errors.isEmpty()) {
            throw new SunatValidationException("SUNAT: Montos con valores negativos", errors);
        }
    }

    /**
     * Valida que si la moneda es USD se proporcione un tipo de cambio > 0.
     */
    public static void validarMonedaYTipoCambio(String moneda, BigDecimal tipoCambio) {
        if ("USD".equalsIgnoreCase(moneda)) {
            if (tipoCambio == null || tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new SunatValidationException(
                        "SUNAT: Cuando la moneda es USD, el tipo de cambio es obligatorio y debe ser mayor a 0.");
            }
        }
    }

    /**
     * Valida que la fecha de emisión no sea futura (SUNAT no acepta fechas futuras).
     */
    public static void validarFechaEmisionNoFutura(LocalDate fechaEmision) {
        if (fechaEmision != null && fechaEmision.isAfter(LocalDate.now())) {
            throw new SunatValidationException(
                    "SUNAT: La fecha de emisión (" + fechaEmision
                            + ") no puede ser posterior a la fecha actual (" + LocalDate.now() + ").");
        }
    }

    /**
     * Valida las reglas de forma de pago CRÉDITO:
     * la fecha de vencimiento debe ser posterior a la fecha de emisión.
     */
    public static void validarFormaPagoCredito(String formaPago,
                                                LocalDate fechaEmision,
                                                LocalDate fechaVencimiento) {
        if ("CREDITO".equalsIgnoreCase(formaPago)) {
            if (fechaVencimiento == null) {
                throw new SunatValidationException(
                        "SUNAT: Forma de pago CRÉDITO requiere una fecha de vencimiento.");
            }
            if (fechaEmision != null && !fechaVencimiento.isAfter(fechaEmision)) {
                throw new SunatValidationException(
                        "SUNAT: Para forma de pago CRÉDITO, la fecha de vencimiento ("
                                + fechaVencimiento + ") debe ser posterior a la fecha de emisión ("
                                + fechaEmision + ").");
            }
        }
    }
}

