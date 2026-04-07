package com.etlions.ms_bs_etia.app_util.validation;

import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SunatValidatorTest {

    // ──────────────────────────────────────────────────────────────
    //  Transiciones de estado
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarTransicionEstado")
    class TransicionEstado {

        @ParameterizedTest(name = "de {0} a {1} → OK")
        @CsvSource({
                "BORRADOR,  ENVIADA",
                "ENVIADA,   ACEPTADA",
                "ENVIADA,   RECHAZADA",
                "ENVIADA,   ANULADA",
                "ACEPTADA,  ANULADA",
                "RECHAZADA, BORRADOR"
        })
        void transicionesPermitidas(String actual, String nuevo) {
            assertDoesNotThrow(() ->
                    SunatValidator.validarTransicionEstado(actual, nuevo, "Factura", "F-1"));
        }

        @ParameterizedTest(name = "de {0} a {1} → RECHAZADA")
        @CsvSource({
                "BORRADOR,  ACEPTADA",
                "BORRADOR,  ANULADA",
                "BORRADOR,  RECHAZADA",
                "ENVIADA,   BORRADOR",
                "ACEPTADA,  BORRADOR",
                "ACEPTADA,  ENVIADA",
                "ACEPTADA,  RECHAZADA",
                "RECHAZADA, ENVIADA",
                "RECHAZADA, ANULADA",
                "ANULADA,   BORRADOR",
                "ANULADA,   ENVIADA",
                "ANULADA,   ACEPTADA",
                "ANULADA,   RECHAZADA"
        })
        void transicionesNoPermitidas(String actual, String nuevo) {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTransicionEstado(actual, nuevo, "Factura", "F-1"));
            assertTrue(ex.getMessage().contains("No se puede cambiar el estado"));
        }

        @Test
        @DisplayName("estado actual null → excepción")
        void estadoActualNull() {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTransicionEstado(null, "ENVIADA", "Factura", "F-1"));
            assertTrue(ex.getMessage().contains("no tiene un estado definido"));
        }

        @Test
        @DisplayName("estado actual desconocido → excepción")
        void estadoActualDesconocido() {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTransicionEstado("INVENTADO", "ENVIADA", "Factura", "F-1"));
            assertTrue(ex.getMessage().contains("Estado desconocido"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Anulación permitida
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarAnulacionPermitida")
    class AnulacionPermitida {

        @ParameterizedTest(name = "anular desde {0} → OK")
        @CsvSource({"ENVIADA", "ACEPTADA"})
        void anulacionDesdeEstadosPermitidos(String estado) {
            assertDoesNotThrow(() ->
                    SunatValidator.validarAnulacionPermitida(estado, "Factura", "F-1"));
        }

        @ParameterizedTest(name = "anular desde {0} → RECHAZADA")
        @CsvSource({"BORRADOR", "RECHAZADA", "ANULADA"})
        void anulacionDesdeEstadosNoPermitidos(String estado) {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarAnulacionPermitida(estado, "Factura", "F-1"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Eliminación permitida
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarEliminacionPermitida")
    class EliminacionPermitida {

        @Test
        @DisplayName("eliminar BORRADOR → OK")
        void eliminarBorradorPermitido() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarEliminacionPermitida("BORRADOR", "Factura", "F-1"));
        }

        @ParameterizedTest(name = "eliminar desde {0} → RECHAZADA")
        @CsvSource({"ENVIADA", "ACEPTADA", "RECHAZADA", "ANULADA"})
        void eliminarDesdeEstadosNoPermitidos(String estado) {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarEliminacionPermitida(estado, "Factura", "F-1"));
            assertTrue(ex.getMessage().contains("Solo los comprobantes en estado BORRADOR"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Tipo documento cliente
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarTipoDocumentoCliente")
    class TipoDocCliente {

        @Test
        @DisplayName("Factura + RUC (6) → OK")
        void facturaConRucOk() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarTipoDocumentoCliente("FACTURA", "6"));
        }

        @ParameterizedTest(name = "Factura + tipo {0} → RECHAZADA")
        @CsvSource({"1", "4", "7", "0"})
        void facturaConDocNoRuc(String tipoDoc) {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTipoDocumentoCliente("FACTURA", tipoDoc));
        }

        @ParameterizedTest(name = "Boleta + tipo {0} → OK")
        @CsvSource({"1", "4", "7"})
        void boletaConDocPermitido(String tipoDoc) {
            assertDoesNotThrow(() ->
                    SunatValidator.validarTipoDocumentoCliente("BOLETA", tipoDoc));
        }

        @Test
        @DisplayName("Boleta + RUC (6) → RECHAZADA")
        void boletaConRuc() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTipoDocumentoCliente("BOLETA", "6"));
        }

        @Test
        @DisplayName("tipo doc null → excepción")
        void tipoDocNull() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTipoDocumentoCliente("FACTURA", null));
        }

        @Test
        @DisplayName("tipo doc vacío → excepción")
        void tipoDocVacio() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTipoDocumentoCliente("FACTURA", "  "));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Serie factura / boleta
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarSerieFactura / validarSerieBoleta")
    class Series {

        @Test
        void serieFacturaConF_ok() {
            assertDoesNotThrow(() -> SunatValidator.validarSerieFactura("F001"));
        }

        @Test
        void serieFacturaConB_falla() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarSerieFactura("B001"));
        }

        @Test
        void serieFacturaNull_falla() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarSerieFactura(null));
        }

        @Test
        void serieBoletaConB_ok() {
            assertDoesNotThrow(() -> SunatValidator.validarSerieBoleta("B001"));
        }

        @Test
        void serieBoletaConF_falla() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarSerieBoleta("F001"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Formato de serie
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarFormatoSerie")
    class FormatoSerie {

        @ParameterizedTest(name = "serie {0} → OK")
        @CsvSource({"F001", "B001", "F999", "B100"})
        void formatoValido(String serie) {
            assertDoesNotThrow(() -> SunatValidator.validarFormatoSerie(serie));
        }

        @ParameterizedTest(name = "serie {0} → RECHAZADA")
        @CsvSource({"F01", "FABC", "1001", "f001", "F0001", "''"})
        void formatoInvalido(String serie) {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarFormatoSerie(serie));
        }

        @Test
        void serieNull_falla() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarFormatoSerie(null));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Validar totales
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarTotales")
    class Totales {

        @Test
        @DisplayName("gravadas + igv == total → OK")
        void totalesCorrectos() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarTotales(
                            new BigDecimal("100.00"),
                            new BigDecimal("18.00"),
                            new BigDecimal("118.00")));
        }

        @Test
        @DisplayName("diferencia ≤ 0.01 tolerada")
        void toleranciaRedondeo() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarTotales(
                            new BigDecimal("100.00"),
                            new BigDecimal("18.00"),
                            new BigDecimal("118.01")));
        }

        @Test
        @DisplayName("diferencia > 0.01 → excepción")
        void diferenciaExcedida() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTotales(
                            new BigDecimal("100.00"),
                            new BigDecimal("18.00"),
                            new BigDecimal("120.00")));
        }

        @Test
        @DisplayName("gravadas null → excepción")
        void gravadasNull() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTotales(null, new BigDecimal("18"), new BigDecimal("118")));
        }

        @Test
        @DisplayName("igv null → excepción")
        void igvNull() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTotales(new BigDecimal("100"), null, new BigDecimal("118")));
        }

        @Test
        @DisplayName("total null → excepción")
        void totalNull() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTotales(new BigDecimal("100"), new BigDecimal("18"), null));
        }

        @Test
        @DisplayName("todos null → excepción con múltiples errores")
        void todosNull() {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarTotales(null, null, null));
            assertEquals(3, ex.getErrors().size());
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Campo obligatorio
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarCampoObligatorio")
    class CampoObligatorio {

        @Test
        @DisplayName("valor válido → OK")
        void valorOk() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarCampoObligatorio("COMP-1", "companyId"));
        }

        @Test
        @DisplayName("null → excepción")
        void valorNull() {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarCampoObligatorio(null, "companyId"));
            assertTrue(ex.getMessage().contains("companyId"));
        }

        @Test
        @DisplayName("vacío → excepción")
        void valorVacio() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarCampoObligatorio("   ", "clientId"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Detalle no vacío
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarDetalleNoVacio")
    class DetalleNoVacio {

        @Test
        @DisplayName("lista con elementos → OK")
        void conElementos() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarDetalleNoVacio(List.of("item1")));
        }

        @Test
        @DisplayName("lista vacía → excepción")
        void listaVacia() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarDetalleNoVacio(List.of()));
        }

        @Test
        @DisplayName("null → excepción")
        void listaNull() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarDetalleNoVacio(null));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Items del detalle
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarItemsDetalle")
    class ItemsDetalle {

        @Test
        @DisplayName("items válidos → OK")
        void itemsOk() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarItemsDetalle(
                            List.of("Producto A"),
                            List.of(new BigDecimal("2")),
                            List.of(new BigDecimal("50.00"))));
        }

        @Test
        @DisplayName("descripción null → excepción")
        void descNull() {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarItemsDetalle(
                            Arrays.asList((String) null),
                            List.of(new BigDecimal("2")),
                            List.of(new BigDecimal("50.00"))));
            assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("descripción")));
        }

        @Test
        @DisplayName("cantidad 0 → excepción")
        void cantidadCero() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarItemsDetalle(
                            List.of("Prod"),
                            List.of(BigDecimal.ZERO),
                            List.of(new BigDecimal("50.00"))));
        }

        @Test
        @DisplayName("valorUnitario negativo → excepción")
        void valorNeg() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarItemsDetalle(
                            List.of("Prod"),
                            List.of(new BigDecimal("1")),
                            List.of(new BigDecimal("-10"))));
        }

        @Test
        @DisplayName("múltiples errores en un ítem → acumula todos")
        void multiplesErrores() {
            SunatValidationException ex = assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarItemsDetalle(
                            Arrays.asList((String) null),
                            List.of(BigDecimal.ZERO),
                            List.of(new BigDecimal("-5"))));
            assertEquals(3, ex.getErrors().size());
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Montos no negativos
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarMontosNoNegativos")
    class MontosNoNegativos {

        @Test
        @DisplayName("todos positivos → OK")
        void todosPositivos() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarMontosNoNegativos(
                            new BigDecimal("100"), new BigDecimal("18"), new BigDecimal("118")));
        }

        @Test
        @DisplayName("gravadas negativo → excepción")
        void gravadasNeg() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarMontosNoNegativos(
                            new BigDecimal("-1"), new BigDecimal("18"), new BigDecimal("118")));
        }

        @Test
        @DisplayName("igv negativo → excepción")
        void igvNeg() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarMontosNoNegativos(
                            new BigDecimal("100"), new BigDecimal("-1"), new BigDecimal("118")));
        }

        @Test
        @DisplayName("total negativo → excepción")
        void totalNeg() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarMontosNoNegativos(
                            new BigDecimal("100"), new BigDecimal("18"), new BigDecimal("-1")));
        }

        @Test
        @DisplayName("nulls no causan excepción (los valida validarTotales)")
        void nullsIgnorados() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarMontosNoNegativos(null, null, null));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Moneda y tipo de cambio
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarMonedaYTipoCambio")
    class MonedaTipoCambio {

        @Test
        @DisplayName("USD con tipo cambio válido → OK")
        void usdOk() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarMonedaYTipoCambio("USD", new BigDecimal("3.75")));
        }

        @Test
        @DisplayName("USD sin tipo cambio → excepción")
        void usdSinTc() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarMonedaYTipoCambio("USD", null));
        }

        @Test
        @DisplayName("USD con tipo cambio 0 → excepción")
        void usdTcCero() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarMonedaYTipoCambio("USD", BigDecimal.ZERO));
        }

        @Test
        @DisplayName("PEN sin tipo cambio → OK")
        void penSinTc() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarMonedaYTipoCambio("PEN", null));
        }

        @Test
        @DisplayName("PEN con tipo cambio → OK (no falla)")
        void penConTc() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarMonedaYTipoCambio("PEN", new BigDecimal("3.75")));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Fecha de emisión no futura
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarFechaEmisionNoFutura")
    class FechaEmisionNoFutura {

        @Test
        @DisplayName("fecha hoy → OK")
        void hoy() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarFechaEmisionNoFutura(LocalDate.now()));
        }

        @Test
        @DisplayName("fecha ayer → OK")
        void ayer() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarFechaEmisionNoFutura(LocalDate.now().minusDays(1)));
        }

        @Test
        @DisplayName("fecha mañana → excepción")
        void manana() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarFechaEmisionNoFutura(LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("null → OK (se asigna hoy en el servicio)")
        void nula() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarFechaEmisionNoFutura(null));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Forma de pago CRÉDITO
    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarFormaPagoCredito")
    class FormaPagoCredito {

        @Test
        @DisplayName("CONTADO sin vencimiento → OK (no aplica)")
        void contadoOk() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarFormaPagoCredito("CONTADO", LocalDate.now(), null));
        }

        @Test
        @DisplayName("CRÉDITO con vencimiento posterior → OK")
        void creditoOk() {
            assertDoesNotThrow(() ->
                    SunatValidator.validarFormaPagoCredito("CREDITO",
                            LocalDate.now(), LocalDate.now().plusDays(30)));
        }

        @Test
        @DisplayName("CRÉDITO sin vencimiento → excepción")
        void creditoSinVenc() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarFormaPagoCredito("CREDITO", LocalDate.now(), null));
        }

        @Test
        @DisplayName("CRÉDITO con vencimiento = emisión → excepción")
        void creditoVencIgual() {
            LocalDate hoy = LocalDate.now();
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarFormaPagoCredito("CREDITO", hoy, hoy));
        }

        @Test
        @DisplayName("CRÉDITO con vencimiento anterior → excepción")
        void creditoVencAnterior() {
            assertThrows(SunatValidationException.class, () ->
                    SunatValidator.validarFormaPagoCredito("CREDITO",
                            LocalDate.now(), LocalDate.now().minusDays(5)));
        }
    }
}

