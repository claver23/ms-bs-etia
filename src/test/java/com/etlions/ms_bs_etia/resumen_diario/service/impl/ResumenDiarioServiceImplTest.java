package com.etlions.ms_bs_etia.resumen_diario.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.app_util.sunat.SendResult;
import com.etlions.ms_bs_etia.app_util.sunat.StatusResult;
import com.etlions.ms_bs_etia.app_util.sunat.SunatSoapClient;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.ms_bs_etia.resumen_diario.persistence.ResumenDiarioEntity;
import com.etlions.ms_bs_etia.resumen_diario.persistence.ResumenDiarioRepository;
import com.etlions.openapicodegen.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link ResumenDiarioServiceImpl}.
 * <p>
 * Cubre los escenarios BOL-RD-001..007, BOL-RD-UP-001..007, BOL-EC-003 y BOL-EC-008
 * definidos en {@code ESCENARIOS_BOLETA.md}.
 * Plan de implementación en {@code src/test/resources/PLAN_TESTS_BOLETA.md}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ResumenDiarioServiceImplTest {

    @Mock private ResumenDiarioRepository resumenDiarioRepository;
    @Mock private BoletaRepository boletaRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private SunatSoapClient sunatSoapClient;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ResumenDiarioServiceImpl service;

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private CompanyEntity company() {
        return CompanyEntity.builder()
                .id("COMP-1").ruc("20601234567")
                .razonSocial("ETLIONS S.A.C.").estado("ACTIVO")
                .build();
    }

    private BoletaEntity buildBoleta(String id, String estado) {
        return BoletaEntity.builder()
                .id(id).companyId("COMP-1").serie("B001").numero(1)
                .estado(estado).moneda("PEN").formaPago("CONTADO")
                .gravadas(new BigDecimal("100.00"))
                .igv(new BigDecimal("18.00"))
                .total(new BigDecimal("118.00"))
                .fechaEmision(LocalDate.of(2026, 4, 8))
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private ResumenDiarioEntity buildResumen(String id, String estado) {
        return ResumenDiarioEntity.builder()
                .id(id).companyId("COMP-1")
                .identificador("RC-20260408-001")
                .fechaGeneracion(LocalDate.of(2026, 4, 8))
                .fechaReferencia(LocalDate.of(2026, 4, 8))
                .estado(estado)
                .cantidadBoletas(2)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private ResumenDiarioCrearDto buildCrearRequest() {
        return new ResumenDiarioCrearDto()
                .companyId("COMP-1")
                .fechaReferencia(java.sql.Date.valueOf(LocalDate.of(2026, 4, 8)));
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-RD-001..003, BOL-EC-008 — CREAR RESUMEN HAPPY
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("crearResumenDiario — Happy Paths")
    class CrearResumenHappy {

        @Test
        @DisplayName("BOL-RD-001 — Crear resumen con boletas VALIDADAS del día → ENVIADO")
        void crearResumen_boletasValidadasDelDia_ok() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            BoletaEntity bol1 = buildBoleta("bol-003", "VALIDADO");
            BoletaEntity bol2 = buildBoleta("bol-008", "VALIDADO");

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findByCompanyIdAndEstadoAndFechaEmision(
                    "COMP-1", "VALIDADO", LocalDate.of(2026, 4, 8)))
                    .thenReturn(List.of(bol1, bol2));
            when(resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(eq("COMP-1"), any()))
                    .thenReturn(0L);
            when(resumenDiarioRepository.save(any())).thenAnswer(inv -> {
                ResumenDiarioEntity e = inv.getArgument(0);
                if (e.getId() == null) e.setId("rd-001");
                return e;
            });
            when(sunatSoapClient.sendSummary(anyString(), eq("20601234567")))
                    .thenReturn(new SendResult(true, "mock-ticket-001", "Enviado OK"));

            ResumenDiarioResponseDto r = service.crearResumenDiario(req);

            assertTrue(r.getIsSuccess());
            assertNotNull(r.getData());
            assertTrue(r.getData().getIdentificador().startsWith("RC-"));
            assertEquals("ENVIADO", r.getData().getEstado().getValue());
            assertEquals("mock-ticket-001", r.getData().getTicketSunat().get());

            // Verificar que las boletas cambiaron a ENVIADA
            assertEquals("ENVIADA", bol1.getEstado());
            assertEquals("ENVIADA", bol2.getEstado());
            assertNotNull(bol1.getResumenDiarioId());

            verify(notificationService).publicar(eq("COMP-1"), eq("ENVIADO"),
                    eq("RESUMEN_DIARIO"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("BOL-RD-002 — Crear resumen con boletaIds específicos")
        void crearResumen_conBoletaIdsEspecificos_ok() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            req.setBoletaIds(List.of("bol-003", "bol-004"));

            BoletaEntity bol3 = buildBoleta("bol-003", "VALIDADO");
            BoletaEntity bol4 = buildBoleta("bol-004", "VALIDADO");

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findById("bol-003")).thenReturn(Optional.of(bol3));
            when(boletaRepository.findById("bol-004")).thenReturn(Optional.of(bol4));
            when(resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(eq("COMP-1"), any()))
                    .thenReturn(0L);
            when(resumenDiarioRepository.save(any())).thenAnswer(inv -> {
                ResumenDiarioEntity e = inv.getArgument(0);
                if (e.getId() == null) e.setId("rd-002");
                return e;
            });
            when(sunatSoapClient.sendSummary(anyString(), eq("20601234567")))
                    .thenReturn(new SendResult(true, "mock-ticket-002", "OK"));

            ResumenDiarioResponseDto r = service.crearResumenDiario(req);

            assertTrue(r.getIsSuccess());
            assertEquals("ENVIADA", bol3.getEstado());
            assertEquals("ENVIADA", bol4.getEstado());
        }

        @Test
        @DisplayName("BOL-RD-003 — Múltiples resúmenes el mismo día → correlativo incrementa")
        void crearResumen_multiplesDelMismoDia_correlativoIncrementa() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            BoletaEntity bol = buildBoleta("bol-100", "VALIDADO");

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findByCompanyIdAndEstadoAndFechaEmision(
                    "COMP-1", "VALIDADO", LocalDate.of(2026, 4, 8)))
                    .thenReturn(List.of(bol));
            // Ya existe 1 resumen del día
            when(resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(eq("COMP-1"), any()))
                    .thenReturn(1L);
            when(resumenDiarioRepository.save(any())).thenAnswer(inv -> {
                ResumenDiarioEntity e = inv.getArgument(0);
                if (e.getId() == null) e.setId("rd-003");
                return e;
            });
            when(sunatSoapClient.sendSummary(anyString(), eq("20601234567")))
                    .thenReturn(new SendResult(true, "mock-ticket-003", "OK"));

            ResumenDiarioResponseDto r = service.crearResumenDiario(req);

            assertTrue(r.getIsSuccess());
            // El correlativo debe ser 002 (1 existente + 1 nuevo)
            assertTrue(r.getData().getIdentificador().endsWith("-002"));
        }

        @Test
        @DisplayName("BOL-EC-008 — Resumen con boletas de diferentes fechas → se permite")
        void crearResumen_boletasDiferentesFechas_ok() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            req.setBoletaIds(List.of("bol-A", "bol-B"));

            BoletaEntity bolA = buildBoleta("bol-A", "VALIDADO");
            bolA.setFechaEmision(LocalDate.of(2026, 4, 7));  // día anterior
            BoletaEntity bolB = buildBoleta("bol-B", "VALIDADO");
            bolB.setFechaEmision(LocalDate.of(2026, 4, 8));  // hoy

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findById("bol-A")).thenReturn(Optional.of(bolA));
            when(boletaRepository.findById("bol-B")).thenReturn(Optional.of(bolB));
            when(resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(eq("COMP-1"), any()))
                    .thenReturn(0L);
            when(resumenDiarioRepository.save(any())).thenAnswer(inv -> {
                ResumenDiarioEntity e = inv.getArgument(0);
                if (e.getId() == null) e.setId("rd-010");
                return e;
            });
            when(sunatSoapClient.sendSummary(anyString(), eq("20601234567")))
                    .thenReturn(new SendResult(true, "mock-ticket-010", "OK"));

            ResumenDiarioResponseDto r = service.crearResumenDiario(req);

            assertTrue(r.getIsSuccess());
            // Ambas boletas incluidas a pesar de fechas distintas
            assertEquals("ENVIADA", bolA.getEstado());
            assertEquals("ENVIADA", bolB.getEstado());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-RD-004, BOL-RD-005, BOL-EC-003 — CONSULTAR ESTADO
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("consultarEstadoResumen")
    class ConsultarEstado {

        @Test
        @DisplayName("BOL-RD-004 — Consultar estado → ACEPTADO → boletas ACEPTADA")
        void consultarEstado_ACEPTADO() {
            ResumenDiarioEntity resumen = buildResumen("rd-002", "ENVIADO");
            resumen.setTicketSunat("mock-ticket-001");
            BoletaEntity bol = buildBoleta("bol-006", "ENVIADA");
            bol.setResumenDiarioId("rd-002");

            when(resumenDiarioRepository.findById("rd-002")).thenReturn(Optional.of(resumen));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(sunatSoapClient.getStatus("mock-ticket-001", "20601234567"))
                    .thenReturn(new StatusResult(true, "ACEPTADO", "0", "Aceptado por SUNAT"));
            when(boletaRepository.findByResumenDiarioId("rd-002")).thenReturn(List.of(bol));

            ConsultaEstadoResultDto r = service.consultarEstadoResumen("rd-002");

            assertTrue(r.getExitoso());
            assertEquals("ACEPTADO", r.getEstado());
            assertEquals("ACEPTADO", resumen.getEstado());
            assertEquals("ACEPTADA", bol.getEstado());
            verify(notificationService).publicar(eq("COMP-1"), eq("ACEPTADO"),
                    eq("RESUMEN_DIARIO"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("BOL-RD-005 / BOL-EC-003 — Consultar estado → RECHAZADO → boletas revierten a VALIDADO")
        void consultarEstado_RECHAZADO_boletasRevierten() {
            ResumenDiarioEntity resumen = buildResumen("rd-003", "ENVIADO");
            resumen.setTicketSunat("mock-ticket-002");
            BoletaEntity bol1 = buildBoleta("bol-010", "ENVIADA");
            bol1.setResumenDiarioId("rd-003");
            BoletaEntity bol2 = buildBoleta("bol-011", "ENVIADA");
            bol2.setResumenDiarioId("rd-003");

            when(resumenDiarioRepository.findById("rd-003")).thenReturn(Optional.of(resumen));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(sunatSoapClient.getStatus("mock-ticket-002", "20601234567"))
                    .thenReturn(new StatusResult(false, "RECHAZADO", "2033", "Error de estructura"));
            when(boletaRepository.findByResumenDiarioId("rd-003")).thenReturn(List.of(bol1, bol2));

            ConsultaEstadoResultDto r = service.consultarEstadoResumen("rd-003");

            assertEquals("RECHAZADO", r.getEstado());
            assertEquals("RECHAZADO", resumen.getEstado());

            // BOL-EC-003: Boletas revierten a VALIDADO + desvinculadas
            assertEquals("VALIDADO", bol1.getEstado());
            assertNull(bol1.getResumenDiarioId());
            assertEquals("VALIDADO", bol2.getEstado());
            assertNull(bol2.getResumenDiarioId());

            verify(notificationService).publicar(eq("COMP-1"), eq("RECHAZADO"),
                    eq("RESUMEN_DIARIO"), anyString(), anyString(), contains("RECHAZADO"));
        }

        @Test
        @DisplayName("BOL-EC-003 extra — Post-rechazo, boletas disponibles para nuevo resumen")
        void rechazado_boletasDisponiblesParaNuevoResumen() {
            // Simular: resumen rechazado, boletas ya revertidas
            ResumenDiarioEntity resumenRechazado = buildResumen("rd-003", "ENVIADO");
            resumenRechazado.setTicketSunat("mock-ticket-002");
            BoletaEntity bol = buildBoleta("bol-010", "ENVIADA");
            bol.setResumenDiarioId("rd-003");

            when(resumenDiarioRepository.findById("rd-003")).thenReturn(Optional.of(resumenRechazado));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(sunatSoapClient.getStatus("mock-ticket-002", "20601234567"))
                    .thenReturn(new StatusResult(false, "RECHAZADO", "2033", "Error"));
            when(boletaRepository.findByResumenDiarioId("rd-003")).thenReturn(List.of(bol));

            service.consultarEstadoResumen("rd-003");

            // Ahora la boleta está en VALIDADO y sin resumenDiarioId
            assertEquals("VALIDADO", bol.getEstado());
            assertNull(bol.getResumenDiarioId());

            // Crear un nuevo resumen que la incluya
            ResumenDiarioCrearDto newReq = buildCrearRequest();
            when(boletaRepository.findByCompanyIdAndEstadoAndFechaEmision(
                    "COMP-1", "VALIDADO", LocalDate.of(2026, 4, 8)))
                    .thenReturn(List.of(bol));
            when(resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(eq("COMP-1"), any()))
                    .thenReturn(1L);
            when(resumenDiarioRepository.save(any())).thenAnswer(inv -> {
                ResumenDiarioEntity e = inv.getArgument(0);
                if (e.getId() == null) e.setId("rd-004");
                return e;
            });
            when(sunatSoapClient.sendSummary(anyString(), eq("20601234567")))
                    .thenReturn(new SendResult(true, "mock-ticket-new", "OK"));

            ResumenDiarioResponseDto r2 = service.crearResumenDiario(newReq);

            assertTrue(r2.getIsSuccess());
            assertEquals("ENVIADA", bol.getEstado()); // de nuevo incluida en un resumen
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-RD-006, BOL-RD-007 — LISTAR Y OBTENER
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("listarResumenesDiarios / obtenerResumenDiario")
    class ListarObtener {

        @Test
        @DisplayName("BOL-RD-006 — Listar resúmenes diarios → ordenados por fecha DESC")
        void listarResumenes_ok() {
            ResumenDiarioEntity rd1 = buildResumen("rd-001", "ACEPTADO");
            rd1.setFechaGeneracion(LocalDate.of(2026, 4, 7));
            ResumenDiarioEntity rd2 = buildResumen("rd-002", "ENVIADO");
            rd2.setFechaGeneracion(LocalDate.of(2026, 4, 8));

            when(resumenDiarioRepository.findByCompanyIdOrderByFechaGeneracionDesc("COMP-1"))
                    .thenReturn(List.of(rd2, rd1)); // ya ordenado DESC

            ResumenDiarioListResponseDto r = service.listarResumenesDiarios("COMP-1", 0, 10);

            assertTrue(r.getIsSuccess());
            assertEquals(2, r.getData().getContent().size());
        }

        @Test
        @DisplayName("BOL-RD-007 — Obtener detalle de resumen diario")
        void obtenerDetalle_ok() {
            ResumenDiarioEntity resumen = buildResumen("rd-001", "ACEPTADO");
            BoletaEntity bol = buildBoleta("bol-006", "ACEPTADA");

            when(resumenDiarioRepository.findById("rd-001")).thenReturn(Optional.of(resumen));
            when(boletaRepository.findByResumenDiarioId("rd-001")).thenReturn(List.of(bol));

            ResumenDiarioResponseDto r = service.obtenerResumenDiario("rd-001");

            assertTrue(r.getIsSuccess());
            assertNotNull(r.getData());
            assertEquals("RC-20260408-001", r.getData().getIdentificador());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-RD-UP-001..003, BOL-RD-UP-007 — CREAR RESUMEN UNHAPPY
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("crearResumenDiario — Unhappy Paths")
    class CrearResumenUnhappy {

        @Test
        @DisplayName("BOL-RD-UP-001 — Sin boletas VALIDADAS para la fecha → excepción")
        void sinBoletasValidadas_falla() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            req.setFechaReferencia(java.sql.Date.valueOf(LocalDate.of(2026, 1, 1)));

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findByCompanyIdAndEstadoAndFechaEmision(
                    "COMP-1", "VALIDADO", LocalDate.of(2026, 1, 1)))
                    .thenReturn(Collections.emptyList());

            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.crearResumenDiario(req));
            assertTrue(ex.getMessage().contains("No hay boletas VALIDADAS"));
        }

        @Test
        @DisplayName("BOL-RD-UP-002 — boletaId no encontrado → 404")
        void boletaIdNoEncontrado_404() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            req.setBoletaIds(List.of("bol-no-existe"));

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findById("bol-no-existe")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.crearResumenDiario(req));
        }

        @Test
        @DisplayName("BOL-RD-UP-003 — boletaIds con boleta BORRADOR → filtrada → lista vacía → excepción")
        void boletaNoValidada_filtradaYVacia_falla() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            req.setBoletaIds(List.of("bol-002"));

            BoletaEntity bolBorrador = buildBoleta("bol-002", "BORRADOR");
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findById("bol-002")).thenReturn(Optional.of(bolBorrador));

            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.crearResumenDiario(req));
            assertTrue(ex.getMessage().contains("No hay boletas VALIDADAS"));
        }

        @Test
        @DisplayName("BOL-RD-UP-007 — Empresa no encontrada → 404")
        void empresaNoEncontrada_404() {
            ResumenDiarioCrearDto req = new ResumenDiarioCrearDto().companyId("cmp-inexistente");
            when(companyRepository.findById("cmp-inexistente")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.crearResumenDiario(req));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-RD-UP-004..006 — CONSULTAR ESTADO UNHAPPY
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("consultarEstadoResumen — Unhappy Paths")
    class ConsultarEstadoUnhappy {

        @Test
        @DisplayName("BOL-RD-UP-004 — Consultar estado sin ticket SUNAT → excepción")
        void consultarEstado_sinTicket_falla() {
            ResumenDiarioEntity resumen = buildResumen("rd-005", "GENERADO");
            resumen.setTicketSunat(null);
            when(resumenDiarioRepository.findById("rd-005")).thenReturn(Optional.of(resumen));

            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.consultarEstadoResumen("rd-005"));
            assertTrue(ex.getMessage().contains("ticket SUNAT"));
        }

        @Test
        @DisplayName("BOL-RD-UP-005 — Envío a SUNAT falla → resumen CON_ERROR")
        void envioSunatFalla_resumenConError() {
            ResumenDiarioCrearDto req = buildCrearRequest();
            BoletaEntity bol = buildBoleta("bol-050", "VALIDADO");

            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(boletaRepository.findByCompanyIdAndEstadoAndFechaEmision(
                    "COMP-1", "VALIDADO", LocalDate.of(2026, 4, 8)))
                    .thenReturn(List.of(bol));
            when(resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(eq("COMP-1"), any()))
                    .thenReturn(0L);
            when(resumenDiarioRepository.save(any())).thenAnswer(inv -> {
                ResumenDiarioEntity e = inv.getArgument(0);
                if (e.getId() == null) e.setId("rd-err");
                return e;
            });
            // SUNAT falla
            when(sunatSoapClient.sendSummary(anyString(), eq("20601234567")))
                    .thenReturn(new SendResult(false, null, "Error de conexión SUNAT"));

            ResumenDiarioResponseDto r = service.crearResumenDiario(req);

            assertTrue(r.getIsSuccess());
            assertEquals("CON_ERROR", r.getData().getEstado().getValue());
            assertNull(r.getData().getTicketSunat().get());
        }

        @Test
        @DisplayName("BOL-RD-UP-006 — Resumen diario no encontrado → 404")
        void resumenNoEncontrado_404() {
            when(resumenDiarioRepository.findById("rd-no-existe")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.consultarEstadoResumen("rd-no-existe"));
        }

        @Test
        @DisplayName("BOL-RD-UP-006 extra — obtenerResumenDiario no encontrado → 404")
        void obtenerResumen_noEncontrado_404() {
            when(resumenDiarioRepository.findById("rd-no-existe")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.obtenerResumenDiario("rd-no-existe"));
        }
    }
}




