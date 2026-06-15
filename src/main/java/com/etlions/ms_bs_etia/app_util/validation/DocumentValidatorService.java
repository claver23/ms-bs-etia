package com.etlions.ms_bs_etia.app_util.validation;

import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de validación interna de documentos electrónicos.
 * <p>
 * Ejecuta las mismas reglas que {@link SunatValidator} pero en modo "soft-fail":
 * acumula errores y advertencias sin lanzar excepciones, permitiendo determinar
 * si un documento debe estar en estado VALIDADO o CON_ERROR.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DocumentValidatorService {

    private final ClientRepository clientRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Valida una factura completa (todos los campos del documento + detalle + cliente).
     *
     * @param serie         Serie del comprobante
     * @param numero        Número correlativo
     * @param clientId      ID del cliente
     * @param moneda        Código de moneda (PEN/USD)
     * @param tipoCambio    Tipo de cambio (obligatorio si USD)
     * @param formaPago     Forma de pago (CONTADO/CREDITO)
     * @param fechaEmision  Fecha de emisión
     * @param fechaVenc     Fecha de vencimiento
     * @param tipoOperacion Tipo de operación (Cat. 51)
     * @param leyenda       Leyenda del comprobante
     * @param gravadas      Monto gravado
     * @param igv           Monto IGV
     * @param total         Total a pagar
     * @param totalCargos   Total cargos globales
     * @param totalDescuentos Total descuentos globales
     * @param detalleJson   Detalle de ítems como JSON
     * @return Resultado de validación con errores y advertencias
     */
    public DocumentValidationResult validarFactura(
            String serie, Integer numero, String clientId,
            String moneda, BigDecimal tipoCambio,
            String formaPago, LocalDate fechaEmision, LocalDate fechaVenc,
            String tipoOperacion, String leyenda,
            BigDecimal gravadas, BigDecimal igv, BigDecimal total,
            BigDecimal totalCargos, BigDecimal totalDescuentos,
            String detalleJson) {

        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        // ── Serie ──
        validateSerie(serie, "F", errores);

        // ── Cliente ──
        validateCliente(clientId, "FACTURA", errores);

        // ── Moneda y tipo de cambio ──
        validateMonedaTipoCambio(moneda, tipoCambio, errores, advertencias);

        // ── Fecha ──
        validateFechaEmision(fechaEmision, errores);

        // ── Forma de pago ──
        validateFormaPago(formaPago, fechaEmision, fechaVenc, errores);

        // ── Tipo de operación ──
        if (tipoOperacion == null || tipoOperacion.isBlank()) {
            errores.add("El tipo de operación es obligatorio (Cat. 51, ej: 0101)");
        }

        // ── Leyenda ──
        if (leyenda == null || leyenda.isBlank()) {
            advertencias.add("La leyenda está vacía. SUNAT recomienda incluir el monto en letras (Cat. 52)");
        }

        // ── Montos ──
        validateMontos(gravadas, igv, total, totalCargos, totalDescuentos, errores);

        // ── Detalle ──
        validateDetalle(detalleJson, errores);

        return errores.isEmpty()
                ? DocumentValidationResult.ok(advertencias)
                : DocumentValidationResult.fail(errores, advertencias);
    }

    /**
     * Valida una boleta completa.
     */
    public DocumentValidationResult validarBoleta(
            String serie, Integer numero, String clientId,
            String moneda, BigDecimal tipoCambio,
            String formaPago, LocalDate fechaEmision, LocalDate fechaVenc,
            String tipoOperacion, String leyenda,
            BigDecimal gravadas, BigDecimal igv, BigDecimal total,
            BigDecimal totalCargos, BigDecimal totalDescuentos,
            String detalleJson) {

        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        // ── Serie ──
        validateSerie(serie, "B", errores);

        // ── Cliente (opcional en boleta, pero si existe debe ser DNI/CE/Pasaporte) ──
        if (clientId != null && !clientId.isBlank()) {
            validateCliente(clientId, "BOLETA", errores);
        }

        // ── Moneda y tipo de cambio ──
        validateMonedaTipoCambio(moneda, tipoCambio, errores, advertencias);

        // ── Fecha ──
        validateFechaEmision(fechaEmision, errores);

        // ── Forma de pago ──
        validateFormaPago(formaPago, fechaEmision, fechaVenc, errores);

        // ── Tipo de operación ──
        if (tipoOperacion == null || tipoOperacion.isBlank()) {
            errores.add("El tipo de operación es obligatorio (Cat. 51, ej: 0101)");
        }

        // ── Montos ──
        validateMontos(gravadas, igv, total, totalCargos, totalDescuentos, errores);

        // ── Detalle ──
        validateDetalle(detalleJson, errores);

        return errores.isEmpty()
                ? DocumentValidationResult.ok(advertencias)
                : DocumentValidationResult.fail(errores, advertencias);
    }

    /**
     * Valida una nota de crédito completa (Cat.09 — código motivo, documento referencia, montos, detalle).
     */
    public DocumentValidationResult validarNotaCredito(
            String serie, Integer numero, String clientId,
            String moneda, BigDecimal tipoCambio,
            String formaPago, LocalDate fechaEmision, LocalDate fechaVenc,
            String tipoOperacion, String leyenda,
            BigDecimal gravadas, BigDecimal igv, BigDecimal total,
            BigDecimal totalCargos, BigDecimal totalDescuentos,
            String detalleJson,
            String serieDocRef, Integer numeroDocRef, String tipoDocumentoReferencia,
            String codigoMotivoSunat, String motivo) {

        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        // ── Serie NC: debe iniciar con F (si ref factura) o B (si ref boleta) ──
        if (serie != null && !serie.isBlank()) {
            validateSerieNcNd(serie, tipoDocumentoReferencia, errores);
        } else {
            errores.add("La serie es obligatoria");
        }

        // ── Documento referencia ──
        validateDocumentoReferencia(serieDocRef, numeroDocRef, tipoDocumentoReferencia, errores);

        // ── Código motivo SUNAT Cat.09 (01-10) ──
        validateCodigoMotivo(codigoMotivoSunat, "09", errores);

        // ── Motivo/descripción ──
        if (motivo == null || motivo.isBlank()) {
            advertencias.add("Se recomienda incluir el motivo descriptivo de la nota de crédito");
        }

        // ── Cliente ──
        if (clientId != null && !clientId.isBlank()) {
            validateCliente(clientId, tipoDocumentoReferencia != null && tipoDocumentoReferencia.toLowerCase().contains("boleta") ? "BOLETA" : "FACTURA", errores);
        }

        // ── Moneda y tipo de cambio ──
        validateMonedaTipoCambio(moneda, tipoCambio, errores, advertencias);

        // ── Fecha ──
        validateFechaEmision(fechaEmision, errores);

        // ── Montos ──
        validateMontos(gravadas, igv, total, totalCargos, totalDescuentos, errores);

        // ── Detalle ──
        validateDetalle(detalleJson, errores);

        return errores.isEmpty()
                ? DocumentValidationResult.ok(advertencias)
                : DocumentValidationResult.fail(errores, advertencias);
    }

    /**
     * Valida una nota de débito completa (Cat.10 — código motivo, documento referencia, montos, detalle).
     */
    public DocumentValidationResult validarNotaDebito(
            String serie, Integer numero, String clientId,
            String moneda, BigDecimal tipoCambio,
            String formaPago, LocalDate fechaEmision, LocalDate fechaVenc,
            String tipoOperacion, String leyenda,
            BigDecimal gravadas, BigDecimal igv, BigDecimal total,
            BigDecimal totalCargos, BigDecimal totalDescuentos,
            String detalleJson,
            String serieDocRef, Integer numeroDocRef, String tipoDocumentoReferencia,
            String codigoMotivoSunat, String motivo) {

        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        // ── Serie ND: debe iniciar con F (si ref factura) o B (si ref boleta) ──
        if (serie != null && !serie.isBlank()) {
            validateSerieNcNd(serie, tipoDocumentoReferencia, errores);
        } else {
            errores.add("La serie es obligatoria");
        }

        // ── Documento referencia ──
        validateDocumentoReferencia(serieDocRef, numeroDocRef, tipoDocumentoReferencia, errores);

        // ── Código motivo SUNAT Cat.10 (01-03) ──
        validateCodigoMotivo(codigoMotivoSunat, "10", errores);

        // ── Motivo/descripción ──
        if (motivo == null || motivo.isBlank()) {
            advertencias.add("Se recomienda incluir el motivo descriptivo de la nota de débito");
        }

        // ── Cliente ──
        if (clientId != null && !clientId.isBlank()) {
            validateCliente(clientId, tipoDocumentoReferencia != null && tipoDocumentoReferencia.toLowerCase().contains("boleta") ? "BOLETA" : "FACTURA", errores);
        }

        // ── Moneda y tipo de cambio ──
        validateMonedaTipoCambio(moneda, tipoCambio, errores, advertencias);

        // ── Fecha ──
        validateFechaEmision(fechaEmision, errores);

        // ── Montos ──
        validateMontos(gravadas, igv, total, totalCargos, totalDescuentos, errores);

        // ── Detalle ──
        validateDetalle(detalleJson, errores);

        return errores.isEmpty()
                ? DocumentValidationResult.ok(advertencias)
                : DocumentValidationResult.fail(errores, advertencias);
    }

    /**
     * Serializa una lista de errores a JSON para persistir en la entidad.
     */
    public String erroresAJson(List<String> errores) {
        try {
            return OBJECT_MAPPER.writeValueAsString(errores);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * Deserializa errores de validación desde JSON.
     */
    public List<String> jsonAErrores(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    // ═══════ VALIDACIONES INTERNAS ═══════

    private void validateSerie(String serie, String prefijo, List<String> errores) {
        if (serie == null || !serie.matches("^[A-Z]\\d{3}$")) {
            errores.add("Formato de serie inválido '" + serie + "'. Debe ser 1 letra + 3 dígitos (ej: " + prefijo + "001)");
            return;
        }
        if (!serie.toUpperCase().startsWith(prefijo)) {
            errores.add("La serie debe iniciar con '" + prefijo + "' para este tipo de documento. Serie recibida: " + serie);
        }
    }

    private void validateCliente(String clientId, String tipoComprobante, List<String> errores) {
        if (clientId == null || clientId.isBlank()) {
            errores.add("El cliente (clientId) es obligatorio");
            return;
        }
        var clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            errores.add("Cliente no encontrado: " + clientId);
            return;
        }
        ClientEntity client = clientOpt.get();
        try {
            SunatValidator.validarTipoDocumentoCliente(tipoComprobante, client.getTipoDocumento());
        } catch (SunatValidationException e) {
            errores.add(e.getMessage());
        }
    }

    private void validateMonedaTipoCambio(String moneda, BigDecimal tipoCambio,
                                           List<String> errores, List<String> advertencias) {
        if (moneda == null || moneda.isBlank()) {
            errores.add("La moneda es obligatoria (PEN o USD)");
            return;
        }
        if ("USD".equalsIgnoreCase(moneda)) {
            if (tipoCambio == null || tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
                errores.add("Moneda USD requiere tipo de cambio > 0");
            }
        } else if ("PEN".equalsIgnoreCase(moneda)) {
            if (tipoCambio != null && tipoCambio.compareTo(BigDecimal.ZERO) > 0) {
                advertencias.add("Se informó tipo de cambio (" + tipoCambio + ") pero la moneda es PEN — será ignorado");
            }
        }
    }

    private void validateFechaEmision(LocalDate fechaEmision, List<String> errores) {
        if (fechaEmision == null) {
            errores.add("La fecha de emisión es obligatoria");
            return;
        }
        if (fechaEmision.isAfter(LocalDate.now())) {
            errores.add("La fecha de emisión (" + fechaEmision + ") no puede ser futura");
        }
    }

    private void validateFormaPago(String formaPago, LocalDate fechaEmision,
                                    LocalDate fechaVenc, List<String> errores) {
        if ("CREDITO".equalsIgnoreCase(formaPago)) {
            if (fechaVenc == null) {
                errores.add("Forma de pago CRÉDITO requiere fecha de vencimiento");
            } else if (fechaEmision != null && !fechaVenc.isAfter(fechaEmision)) {
                errores.add("Fecha de vencimiento (" + fechaVenc + ") debe ser posterior a la fecha de emisión (" + fechaEmision + ")");
            }
        }
    }

    private void validateMontos(BigDecimal gravadas, BigDecimal igv, BigDecimal total,
                                 BigDecimal totalCargos, BigDecimal totalDescuentos,
                                 List<String> errores) {
        if (gravadas == null) errores.add("El monto gravado es obligatorio");
        if (igv == null) errores.add("El IGV es obligatorio");
        if (total == null) errores.add("El total es obligatorio");

        if (gravadas != null && gravadas.compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El monto gravado no puede ser negativo: " + gravadas);
        }
        if (igv != null && igv.compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El IGV no puede ser negativo: " + igv);
        }
        if (total != null && total.compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El total no puede ser negativo: " + total);
        }

        // Validar fórmula: total = gravadas + igv + cargos - descuentos
        if (gravadas != null && igv != null && total != null) {
            BigDecimal cargos = totalCargos != null ? totalCargos : BigDecimal.ZERO;
            BigDecimal descuentos = totalDescuentos != null ? totalDescuentos : BigDecimal.ZERO;
            BigDecimal esperado = gravadas.add(igv).add(cargos).subtract(descuentos)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal diff = total.subtract(esperado).abs();
            if (diff.compareTo(new BigDecimal("0.01")) > 0) {
                errores.add("El total (" + total + ") no cuadra: gravadas(" + gravadas
                        + ") + IGV(" + igv + ")"
                        + (cargos.signum() > 0 ? " + cargos(" + cargos + ")" : "")
                        + (descuentos.signum() > 0 ? " − descuentos(" + descuentos + ")" : "")
                        + " = " + esperado);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateDetalle(String detalleJson, List<String> errores) {
        if (detalleJson == null || detalleJson.isBlank() || "[]".equals(detalleJson.trim())) {
            errores.add("El comprobante debe tener al menos un ítem en el detalle");
            return;
        }
        try {
            List<Map<String, Object>> items = OBJECT_MAPPER.readValue(detalleJson,
                    new TypeReference<List<Map<String, Object>>>() {});
            if (items.isEmpty()) {
                errores.add("El comprobante debe tener al menos un ítem en el detalle");
                return;
            }
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                String prefix = "Ítem [" + (i + 1) + "]: ";
                Object desc = item.get("descripcion");
                if (desc == null || desc.toString().isBlank()) {
                    errores.add(prefix + "la descripción es obligatoria");
                }
                Object cant = item.get("cantidad");
                if (cant == null || toBigDecimal(cant).compareTo(BigDecimal.ZERO) <= 0) {
                    errores.add(prefix + "la cantidad debe ser mayor a 0");
                }
                Object val = item.get("valorUnitario");
                if (val == null || toBigDecimal(val).compareTo(BigDecimal.ZERO) < 0) {
                    errores.add(prefix + "el valor unitario no puede ser negativo");
                }
            }
        } catch (Exception e) {
            errores.add("Error al parsear el detalle JSON: " + e.getMessage());
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Valida la serie de NC/ND: debe empezar con F si ref es factura, B si ref es boleta.
     */
    private void validateSerieNcNd(String serie, String tipoDocumentoReferencia, List<String> errores) {
        if (serie == null || !serie.matches("^[A-Z]\\d{3}$")) {
            errores.add("Formato de serie inválido '" + serie + "'. Debe ser 1 letra + 3 dígitos (ej: F001)");
            return;
        }
        if (tipoDocumentoReferencia != null) {
            String tipoRef = tipoDocumentoReferencia.toLowerCase();
            if (tipoRef.contains("factura") && !serie.startsWith("F")) {
                errores.add("NC/ND referenciada a factura debe tener serie que inicie con 'F'. Serie recibida: " + serie);
            } else if (tipoRef.contains("boleta") && !serie.startsWith("B")) {
                errores.add("NC/ND referenciada a boleta debe tener serie que inicie con 'B'. Serie recibida: " + serie);
            }
        }
    }

    /**
     * Valida documento de referencia obligatorio para NC/ND.
     */
    private void validateDocumentoReferencia(String serieDocRef, Integer numeroDocRef,
                                              String tipoDocumentoReferencia, List<String> errores) {
        if (serieDocRef == null || serieDocRef.isBlank()) {
            errores.add("La serie del documento de referencia (serieDocRef) es obligatoria");
        }
        if (numeroDocRef == null || numeroDocRef <= 0) {
            errores.add("El número del documento de referencia (numeroDocRef) es obligatorio y debe ser > 0");
        }
        if (tipoDocumentoReferencia == null || tipoDocumentoReferencia.isBlank()) {
            errores.add("El tipo de documento de referencia es obligatorio ('factura' o 'boleta')");
        }
    }

    /**
     * Valida código de motivo SUNAT.
     * Cat.09 para NC (01-10), Cat.10 para ND (01-03).
     */
    private void validateCodigoMotivo(String codigo, String catalogo, List<String> errores) {
        if (codigo == null || codigo.isBlank()) {
            errores.add("El código de motivo SUNAT (Cat." + catalogo + ") es obligatorio");
            return;
        }
        if ("09".equals(catalogo)) {
            // Cat.09: 01-10 son códigos válidos para NC
            try {
                int val = Integer.parseInt(codigo);
                if (val < 1 || val > 10) {
                    errores.add("Código de motivo NC inválido '" + codigo + "'. Cat.09 acepta valores del 01 al 10");
                }
            } catch (NumberFormatException e) {
                errores.add("Código de motivo NC debe ser numérico: " + codigo);
            }
        } else if ("10".equals(catalogo)) {
            // Cat.10: 01-03 son códigos válidos para ND
            try {
                int val = Integer.parseInt(codigo);
                if (val < 1 || val > 3) {
                    errores.add("Código de motivo ND inválido '" + codigo + "'. Cat.10 acepta valores del 01 al 03");
                }
            } catch (NumberFormatException e) {
                errores.add("Código de motivo ND debe ser numérico: " + codigo);
            }
        }
    }
}

