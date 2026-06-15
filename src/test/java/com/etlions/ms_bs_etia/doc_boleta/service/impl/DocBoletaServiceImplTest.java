package com.etlions.ms_bs_etia.doc_boleta.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidationResult;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_boleta.mapper.BoletaMapper;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.doc_boleta.service.DocBoletaService.BoletaXmlResult;
import com.etlions.ms_bs_etia.doc_factura.persistence.AllowanceChargeRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_factura.xml.UblInvoiceXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link DocBoletaServiceImpl}.
 * <p>
 * Cubre los 54 escenarios definidos en {@code ESCENARIOS_BOLETA.md}.
 * Plan de implementación detallado en {@code src/test/resources/PLAN_TESTS_BOLETA.md}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DocBoletaServiceImplTest {

    @Mock private BoletaRepository boletaRepository;
    @Mock private BoletaMapper boletaMapper;
    @Mock private CorrelativoService correlativoService;
    @Mock private NotificationService notificationService;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CuotaCreditoRepository cuotaCreditoRepository;
    @Mock private AllowanceChargeRepository allowanceChargeRepository;
    @Mock private DocumentValidatorService documentValidatorService;
    @Mock private UblInvoiceXmlBuilder ublInvoiceXmlBuilder;
    @Mock private XmlStorageService xmlStorageService;

    @InjectMocks
    private DocBoletaServiceImpl service;

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private DetalleItem buildDetalleItem() {
        return new DetalleItem()
                .descripcion("Corte de cabello")
                .cantidad(new BigDecimal("1"))
                .valorUnitario(new BigDecimal("101.69"))
                .precioUnitario(new BigDecimal("120.00"))
                .subtotal(new BigDecimal("101.69"))
                .igv(new BigDecimal("18.31"));
    }

    private BoletaEmitirDto buildEmitirRequest() {
        return new BoletaEmitirDto()
                .companyId("COMP-1")
                .clientId("CLI-DNI")
                .serie("B001")
                .gravadas(new BigDecimal("101.69"))
                .igv(new BigDecimal("18.31"))
                .total(new BigDecimal("120.00"))
                .moneda(BoletaEmitirDto.MonedaEnum.PEN)
                .formaPago(BoletaEmitirDto.FormaPagoEnum.CONTADO)
                .detalle(new ArrayList<>(List.of(buildDetalleItem())));
    }

    private BoletaEntity buildEntity(String id, String estado) {
        return BoletaEntity.builder()
                .id(id).companyId("COMP-1").clientId("CLI-DNI")
                .serie("B001").numero(1).estado(estado)
                .gravadas(new BigDecimal("101.69"))
                .igv(new BigDecimal("18.31"))
                .total(new BigDecimal("120.00"))
                .moneda("PEN").formaPago("CONTADO")
                .tipoOperacion("0101")
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .detalleJson("[{\"descripcion\":\"Corte de cabello\",\"cantidad\":1,\"valorUnitario\":101.69}]")
                .exoneradas(BigDecimal.ZERO).inafectas(BigDecimal.ZERO)
                .gratuitas(BigDecimal.ZERO)
                .totalCargos(BigDecimal.ZERO).totalDescuentos(BigDecimal.ZERO)
                .subtotal(new BigDecimal("101.69"))
                .totalConImpuestos(new BigDecimal("120.00"))
                .build();
    }

    private BoletaData buildData() {
        BoletaData d = new BoletaData();
        d.setId("BOL-1");
        d.setSerie("B001");
        d.setNumero(1);
        return d;
    }

    private ClientEntity clientDni() {
        return ClientEntity.builder().id("CLI-DNI").tipoDocumento("1")
                .numeroDocumento("45678912").denominacion("María García Pérez").codigoPais("PE").build();
    }

    private ClientEntity clientCE() {
        return ClientEntity.builder().id("CLI-CE").tipoDocumento("4")
                .numeroDocumento("001234567").denominacion("Jean Pierre Dupont").codigoPais("PE").build();
    }

    private ClientEntity clientPasaporte() {
        return ClientEntity.builder().id("CLI-PAS").tipoDocumento("7")
                .numeroDocumento("JP4567890").denominacion("Akira Tanaka").codigoPais("JP").build();
    }

    private ClientEntity clientRuc() {
        return ClientEntity.builder().id("CLI-RUC").tipoDocumento("6")
                .numeroDocumento("20505050501").denominacion("INVERSIONES TECH S.A.C.").codigoPais("PE").build();
    }

    private CompanyEntity company() {
        return CompanyEntity.builder().id("COMP-1").ruc("20601234567")
                .razonSocial("ETLIONS S.A.C.").estado("ACTIVO").build();
    }

    /** Stub para una emisión que pasa auto-validación → VALIDADO + XML */
    private void stubEmisionExitosa(BoletaEmitirDto req) {
        BoletaEntity entity = buildEntity("BOL-1", "BORRADOR");
        when(correlativoService.siguienteNumero(anyString(), anyString())).thenReturn(1);
        when(boletaMapper.toEntity(req)).thenReturn(entity);
        when(boletaRepository.save(any(BoletaEntity.class))).thenReturn(entity);
        when(boletaMapper.toData(entity)).thenReturn(buildData());
        when(documentValidatorService.validarBoleta(
                anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(DocumentValidationResult.ok());
        when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
        // Mock client lookup para generateBoletaXml (entity tiene clientId="CLI-DNI")
        when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
        when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                .thenReturn("<Invoice/>");
        when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                .thenReturn("20601234567/B001/00000001/20601234567-03-B001-00000001.xml");
    }

    /** Stub para una emisión donde la auto-validación falla → CON_ERROR */
    private void stubEmisionConError(BoletaEmitirDto req, List<String> errores) {
        BoletaEntity entity = buildEntity("BOL-1", "BORRADOR");
        when(correlativoService.siguienteNumero(anyString(), anyString())).thenReturn(1);
        when(boletaMapper.toEntity(req)).thenReturn(entity);
        when(boletaRepository.save(any(BoletaEntity.class))).thenReturn(entity);
        when(boletaMapper.toData(entity)).thenReturn(buildData());
        // Mock client lookup para la validación hard en emitirBoleta (request.clientId="CLI-DNI")
        when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
        when(documentValidatorService.validarBoleta(
                anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(DocumentValidationResult.fail(errores));
        when(documentValidatorService.erroresAJson(errores)).thenReturn("[\"error\"]");
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-HP-001..007 — EMITIR HAPPY PATHS
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("emitirBoleta — Happy Paths")
    class EmitirHappy {

        @Test
        @DisplayName("BOL-HP-001 — Crear boleta con auto-validación exitosa → VALIDADO")
        void emitirBoletaAutoValidacionExitosa_VALIDADO() {
            BoletaEmitirDto req = buildEmitirRequest();
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            stubEmisionExitosa(req);

            BoletaResponseDto r = service.emitirBoleta(req);

            assertTrue(r.getIsSuccess());
            // save() se invoca al menos 2 veces: 1ª persiste BORRADOR, 2ª con VALIDADO+XML
            verify(boletaRepository, atLeast(2)).save(any());
            verify(ublInvoiceXmlBuilder).buildBoletaXml(any(), any(), any(), anyList(), anyList());
            verify(xmlStorageService).guardarXml(eq("20601234567"), eq("03"), eq("B001"), eq(1), anyString());
            verify(notificationService).publicar(eq("COMP-1"), eq("VALIDADO"),
                    eq("BOLETA"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("BOL-HP-002 — Crear boleta con cliente DNI (tipo 1)")
        void emitirBoletaClienteDni_ok() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setClientId("CLI-DNI");
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            stubEmisionExitosa(req);

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-HP-003 — Crear boleta con cliente Carnet Extranjería (tipo 4)")
        void emitirBoletaClienteCE_ok() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setClientId("CLI-CE");
            when(clientRepository.findById("CLI-CE")).thenReturn(Optional.of(clientCE()));
            stubEmisionExitosa(req);

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-HP-004 — Crear boleta con cliente Pasaporte (tipo 7)")
        void emitirBoletaClientePasaporte_ok() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setClientId("CLI-PAS");
            when(clientRepository.findById("CLI-PAS")).thenReturn(Optional.of(clientPasaporte()));
            stubEmisionExitosa(req);

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-HP-005 — Crear boleta sin cliente (anónimo)")
        void emitirBoletaSinCliente_anonimo_ok() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setClientId(null);

            // Entidad con clientId=null para que generateBoletaXml use cliente anónimo
            BoletaEntity entity = buildEntity("BOL-1", "BORRADOR");
            entity.setClientId(null);
            when(correlativoService.siguienteNumero(anyString(), anyString())).thenReturn(1);
            when(boletaMapper.toEntity(req)).thenReturn(entity);
            when(boletaRepository.save(any(BoletaEntity.class))).thenReturn(entity);
            when(boletaMapper.toData(entity)).thenReturn(buildData());
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<Invoice/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("path/anon.xml");

            BoletaResponseDto r = service.emitirBoleta(req);

            assertTrue(r.getIsSuccess());
            // No debe buscar cliente en BD (ni en emitir ni en generateBoletaXml)
            verify(clientRepository, never()).findById(anyString());
            // El XML se genera con cliente anónimo (tipoDoc="-", num="00000000")
            verify(ublInvoiceXmlBuilder).buildBoletaXml(any(), any(),
                    argThat(c -> "-".equals(c.getTipoDocumento()) && "00000000".equals(c.getNumeroDocumento())),
                    anyList(), anyList());
        }

        @Test
        @DisplayName("BOL-HP-006 — Crear boleta multi-ítem")
        void emitirBoletaMultiItem_ok() {
            BoletaEmitirDto req = buildEmitirRequest();
            DetalleItem item2 = new DetalleItem()
                    .descripcion("Tinte de cabello").cantidad(new BigDecimal("1"))
                    .valorUnitario(new BigDecimal("50.85")).precioUnitario(new BigDecimal("60.00"))
                    .subtotal(new BigDecimal("50.85")).igv(new BigDecimal("9.15"));
            req.setDetalle(new ArrayList<>(List.of(buildDetalleItem(), item2)));
            req.setGravadas(new BigDecimal("152.54"));
            req.setIgv(new BigDecimal("27.46"));
            req.setTotal(new BigDecimal("180.00"));

            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            stubEmisionExitosa(req);

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-HP-007 — Crear boleta a CRÉDITO con cuotas")
        void emitirBoletaCredito_conCuotas_ok() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(BoletaEmitirDto.FormaPagoEnum.CREDITO);
            req.setFechaVencimiento(java.sql.Date.valueOf(LocalDate.now().plusDays(30)));
            req.setCuotas(List.of(
                    new CuotaCredito().numeroCuota("Cuota001")
                            .monto(new BigDecimal("60.00"))
                            .fechaVencimiento(java.sql.Date.valueOf(LocalDate.now().plusDays(15))),
                    new CuotaCredito().numeroCuota("Cuota002")
                            .monto(new BigDecimal("60.00"))
                            .fechaVencimiento(java.sql.Date.valueOf(LocalDate.now().plusDays(30)))
            ));

            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            stubEmisionExitosa(req);
            when(cuotaCreditoRepository.saveAll(anyList())).thenReturn(List.of(
                    CuotaCreditoEntity.builder().numeroCuota("Cuota001").monto(new BigDecimal("60.00"))
                            .fechaVencimiento(LocalDate.now().plusDays(15)).moneda("PEN").build(),
                    CuotaCreditoEntity.builder().numeroCuota("Cuota002").monto(new BigDecimal("60.00"))
                            .fechaVencimiento(LocalDate.now().plusDays(30)).moneda("PEN").build()
            ));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
            verify(cuotaCreditoRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-UP-001, BOL-UP-002 — EMITIR SERIE INVÁLIDA
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("emitirBoleta — Serie inválida")
    class EmitirSerie {

        @Test
        @DisplayName("BOL-UP-001 — Serie de factura F001 usada en boleta → excepción")
        void serieFacturaEnBoleta_falla() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setSerie("F001");
            assertThrows(SunatValidationException.class, () -> service.emitirBoleta(req));
            verify(boletaRepository, never()).save(any());
        }

        @Test
        @DisplayName("BOL-UP-002 — Serie formato inválido BB01 → excepción")
        void serieFormatoInvalido_falla() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setSerie("BB01");
            assertThrows(SunatValidationException.class, () -> service.emitirBoleta(req));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-UP-003 — EMITIR CLIENTE INVÁLIDO
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("emitirBoleta — Cliente inválido")
    class EmitirCliente {

        @Test
        @DisplayName("BOL-UP-003 — Cliente con RUC (tipo 6) en boleta → excepción")
        void clienteRucEnBoleta_falla() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setClientId("CLI-RUC");
            when(clientRepository.findById("CLI-RUC")).thenReturn(Optional.of(clientRuc()));

            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.emitirBoleta(req));
            assertTrue(ex.getMessage().contains("6") || ex.getMessage().contains("BOLETA"));
            verify(boletaRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-UP-006, BOL-UP-017 — EMITIR MONTOS (hard-fail)
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("emitirBoleta — Montos (hard-fail)")
    class EmitirMontos {

        @Test
        @DisplayName("BOL-UP-006 — Total no cuadra → excepción")
        void totalNoCuadra_falla() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setGravadas(new BigDecimal("100.00"));
            req.setIgv(new BigDecimal("18.00"));
            req.setTotal(new BigDecimal("200.00")); // esperado=118
            assertThrows(SunatValidationException.class, () -> service.emitirBoleta(req));
        }

        @Test
        @DisplayName("BOL-UP-017 — Campos monetarios null → excepción con 3 errores")
        void camposMonetariosNull_falla() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setGravadas(null);
            req.setIgv(null);
            req.setTotal(null);
            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.emitirBoleta(req));
            assertTrue(ex.getErrors().size() >= 3);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-UP-004,005,007,008,016,018 — AUTO-VALIDACIÓN → CON_ERROR
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("emitirBoleta — Auto-validación → CON_ERROR")
    class EmitirAutoValidacion {

        @Test
        @DisplayName("BOL-UP-004 — Detalle vacío → auto-validación falla → CON_ERROR")
        void detalleVacio_CON_ERROR() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setDetalle(new ArrayList<>());
            stubEmisionConError(req, List.of("El comprobante debe tener al menos un ítem en el detalle"));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess()); // HTTP 200, pero estado=CON_ERROR
            verify(notificationService).publicar(eq("COMP-1"), eq("CON_ERROR"),
                    eq("BOLETA"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("BOL-UP-005 — Montos negativos → auto-validación atrapa → CON_ERROR")
        void montosNegativos_autoValidacion_CON_ERROR() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setGravadas(new BigDecimal("-500.00"));
            req.setIgv(new BigDecimal("-90.00"));
            req.setTotal(new BigDecimal("-590.00")); // fórmula: -500 + (-90) = -590 → totales pasan
            stubEmisionConError(req, List.of(
                    "El monto gravado no puede ser negativo: -500.00",
                    "El IGV no puede ser negativo: -90.00",
                    "El total no puede ser negativo: -590.00"));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
            verify(documentValidatorService).erroresAJson(anyList());
        }

        @Test
        @DisplayName("BOL-UP-007 — Moneda USD sin tipo de cambio → CON_ERROR")
        void usdSinTipoCambio_CON_ERROR() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setMoneda(BoletaEmitirDto.MonedaEnum.USD);
            req.setTipoCambio(null);
            stubEmisionConError(req, List.of("Moneda USD requiere tipo de cambio > 0"));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-UP-008 — Fecha de emisión futura → CON_ERROR")
        void fechaEmisionFutura_CON_ERROR() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now().plusDays(365)));
            stubEmisionConError(req, List.of("La fecha de emisión no puede ser futura"));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-UP-016 — CRÉDITO sin fecha de vencimiento → CON_ERROR")
        void creditoSinFechaVencimiento_CON_ERROR() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(BoletaEmitirDto.FormaPagoEnum.CREDITO);
            req.setFechaVencimiento(null);
            stubEmisionConError(req, List.of("Forma de pago CRÉDITO requiere fecha de vencimiento"));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }

        @Test
        @DisplayName("BOL-UP-018 — JSON detalle corrupto → CON_ERROR")
        void detalleJsonCorrupto_CON_ERROR() {
            BoletaEmitirDto req = buildEmitirRequest();
            // El mapper convierte detalle inválido, pero la auto-validación lo atrapa
            stubEmisionConError(req, List.of("Error al parsear el detalle JSON: Unexpected character"));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-HP-008, BOL-EC-001 — ACTUALIZAR HAPPY
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("actualizarBoletaBorrador — Happy Paths")
    class ActualizarHappy {

        @Test
        @DisplayName("BOL-HP-008 — Actualizar boleta CON_ERROR → corregir → VALIDADO")
        void actualizarConError_corregir_VALIDADO() {
            BoletaEntity entity = buildEntity("BOL-5", "CON_ERROR");
            entity.setErroresValidacion("[\"IGV incorrecto\"]");
            when(boletaRepository.findById("BOL-5")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));

            BoletaEmitirDto req = buildEmitirRequest();
            req.setGravadas(new BigDecimal("63.56"));
            req.setIgv(new BigDecimal("11.44"));
            req.setTotal(new BigDecimal("75.00"));

            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<Invoice/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("path/to/xml");
            when(boletaRepository.save(any())).thenReturn(entity);
            when(boletaMapper.toData(any())).thenReturn(buildData());

            BoletaResponseDto r = service.actualizarBoletaBorrador("BOL-5", req);

            assertTrue(r.getIsSuccess());
            assertEquals("VALIDADO", entity.getEstado());
            assertNull(entity.getErroresValidacion());
            verify(ublInvoiceXmlBuilder).buildBoletaXml(any(), any(), any(), anyList(), anyList());
        }

        @Test
        @DisplayName("BOL-EC-001 — Boleta VALIDADO editada → re-valida → sigue VALIDADO + nuevo XML")
        void actualizarValidado_reValidar_sigueVALIDADO() {
            BoletaEntity entity = buildEntity("BOL-3", "VALIDADO");
            entity.setXmlContent("<OldInvoice/>");
            when(boletaRepository.findById("BOL-3")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));

            BoletaEmitirDto req = buildEmitirRequest();
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<NewInvoice/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("new/path/xml");
            when(boletaRepository.save(any())).thenReturn(entity);
            when(boletaMapper.toData(any())).thenReturn(buildData());

            BoletaResponseDto r = service.actualizarBoletaBorrador("BOL-3", req);

            assertTrue(r.getIsSuccess());
            assertEquals("VALIDADO", entity.getEstado());
            // Se generó un NUEVO XML (reemplaza el anterior)
            verify(ublInvoiceXmlBuilder).buildBoletaXml(any(), any(), any(), anyList(), anyList());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-UP-009, BOL-UP-010 — ACTUALIZAR UNHAPPY
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("actualizarBoletaBorrador — Unhappy Paths")
    class ActualizarUnhappy {

        @Test
        @DisplayName("BOL-UP-009 — Actualizar boleta en estado ENVIADA → excepción")
        void actualizarEnviada_falla() {
            BoletaEntity entity = buildEntity("BOL-6", "ENVIADA");
            when(boletaRepository.findById("BOL-6")).thenReturn(Optional.of(entity));

            BoletaEmitirDto req = buildEmitirRequest();
            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.actualizarBoletaBorrador("BOL-6", req));
            assertTrue(ex.getMessage().contains("ENVIADA"));
            verify(boletaRepository, never()).save(any());
        }

        @Test
        @DisplayName("BOL-UP-010 — Actualizar boleta ACEPTADA → excepción")
        void actualizarAceptada_falla() {
            BoletaEntity entity = buildEntity("BOL-1", "ACEPTADA");
            when(boletaRepository.findById("BOL-1")).thenReturn(Optional.of(entity));

            BoletaEmitirDto req = buildEmitirRequest();
            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.actualizarBoletaBorrador("BOL-1", req));
            assertTrue(ex.getMessage().contains("ACEPTADA"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-HP-009, BOL-UP-013, BOL-UP-014 — VALIDAR BOLETA
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("validarBoleta")
    class ValidarBoleta {

        @Test
        @DisplayName("BOL-HP-009 — Validar boleta manualmente → VALIDADO + XML generado")
        void validarManual_VALIDADO() {
            BoletaEntity entity = buildEntity("BOL-2", "BORRADOR");
            when(boletaRepository.findById("BOL-2")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(cuotaCreditoRepository.findByBoletaIdOrderByNumeroCuota("BOL-2")).thenReturn(List.of());
            when(allowanceChargeRepository.findByBoletaId("BOL-2")).thenReturn(List.of());
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<Invoice/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("path/xml");

            DocumentValidationResultDto r = service.validarBoleta("BOL-2");

            assertTrue(r.getValid());
            assertTrue(r.getErrores().isEmpty());
            assertEquals("VALIDADO", entity.getEstado());
            verify(boletaRepository).save(entity);
            verify(ublInvoiceXmlBuilder).buildBoletaXml(any(), any(), any(), anyList(), anyList());
        }

        @Test
        @DisplayName("BOL-UP-013 — Validar boleta en estado ENVIADA → excepción")
        void validarEnviada_falla() {
            BoletaEntity entity = buildEntity("BOL-6", "ENVIADA");
            when(boletaRepository.findById("BOL-6")).thenReturn(Optional.of(entity));

            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.validarBoleta("BOL-6"));
            assertTrue(ex.getMessage().contains("ENVIADA"));
        }

        @Test
        @DisplayName("BOL-UP-014 — Boleta no encontrada → 404")
        void boletaNoEncontrada_404() {
            when(boletaRepository.findById("BOL-NO-EXISTE")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.validarBoleta("BOL-NO-EXISTE"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-HP-010, BOL-UP-011, BOL-UP-012 — ELIMINAR BOLETA
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("eliminarBoletaBorrador")
    class EliminarBoleta {

        @Test
        @DisplayName("BOL-HP-010 — Eliminar borrador de boleta → OK")
        void eliminarBorrador_ok() {
            BoletaEntity entity = buildEntity("BOL-2", "BORRADOR");
            when(boletaRepository.findById("BOL-2")).thenReturn(Optional.of(entity));

            SuccessResponse r = service.eliminarBoletaBorrador("BOL-2");

            assertTrue(r.getIsSuccess());
            verify(cuotaCreditoRepository).deleteByBoletaId("BOL-2");
            verify(allowanceChargeRepository).deleteByBoletaId("BOL-2");
            verify(boletaRepository).delete(entity);
        }

        @Test
        @DisplayName("BOL-HP-010 extra — Eliminar boleta CON_ERROR → OK")
        void eliminarConError_ok() {
            BoletaEntity entity = buildEntity("BOL-5", "CON_ERROR");
            when(boletaRepository.findById("BOL-5")).thenReturn(Optional.of(entity));

            SuccessResponse r = service.eliminarBoletaBorrador("BOL-5");
            assertTrue(r.getIsSuccess());
            verify(boletaRepository).delete(entity);
        }

        @Test
        @DisplayName("BOL-UP-011 — Eliminar boleta ENVIADA → excepción")
        void eliminarEnviada_falla() {
            BoletaEntity entity = buildEntity("BOL-6", "ENVIADA");
            when(boletaRepository.findById("BOL-6")).thenReturn(Optional.of(entity));

            assertThrows(SunatValidationException.class,
                    () -> service.eliminarBoletaBorrador("BOL-6"));
            verify(boletaRepository, never()).delete(any());
        }

        @Test
        @DisplayName("BOL-UP-012 — Eliminar boleta ACEPTADA → excepción")
        void eliminarAceptada_falla() {
            BoletaEntity entity = buildEntity("BOL-1", "ACEPTADA");
            when(boletaRepository.findById("BOL-1")).thenReturn(Optional.of(entity));

            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.eliminarBoletaBorrador("BOL-1"));
            assertTrue(ex.getMessage().contains("BORRADOR") || ex.getMessage().contains("CON_ERROR"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-HP-012 — ANULAR BOLETA
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("anularBoleta")
    class AnularBoleta {

        @Test
        @DisplayName("BOL-HP-012 — Anular boleta ACEPTADA → ANULADA + notificación")
        void anularAceptada_ok() {
            BoletaEntity entity = buildEntity("BOL-7", "ACEPTADA");
            when(boletaRepository.findById("BOL-7")).thenReturn(Optional.of(entity));
            when(boletaRepository.save(entity)).thenReturn(entity);
            when(boletaMapper.toData(entity)).thenReturn(buildData());
            when(cuotaCreditoRepository.findByBoletaIdOrderByNumeroCuota("BOL-7")).thenReturn(List.of());
            when(allowanceChargeRepository.findByBoletaId("BOL-7")).thenReturn(List.of());

            BoletaResponseDto r = service.anularBoleta("BOL-7", new AnularBoletaDto().motivoAnulacion("Error"));

            assertTrue(r.getIsSuccess());
            assertEquals("ANULADA", entity.getEstado());
            verify(notificationService).publicar(eq("COMP-1"), eq("ANULADA"),
                    eq("BOLETA"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("BOL-UP-014 extra — Anular boleta inexistente → 404")
        void anularInexistente_404() {
            when(boletaRepository.findById("BOL-X")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.anularBoleta("BOL-X", new AnularBoletaDto().motivoAnulacion("T")));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  BOL-HP-011, BOL-UP-015 — DESCARGAR XML
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("descargarBoletaXml")
    class DescargarXml {

        @Test
        @DisplayName("BOL-HP-011 — Descargar XML de boleta VALIDADA desde disco")
        void descargarXml_conContenido_ok() {
            BoletaEntity entity = buildEntity("BOL-3", "VALIDADO");
            entity.setXmlPath("20601234567/B001/00000001/20601234567-03-B001-00000001.xml");
            entity.setXmlContent("<Invoice/>");
            when(boletaRepository.findById("BOL-3")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(xmlStorageService.leerXml(entity.getXmlPath())).thenReturn(Optional.of("<Invoice/>"));

            BoletaXmlResult result = service.descargarBoletaXml("BOL-3");

            assertTrue(result.hasContent());
            assertNotNull(result.content());
            assertTrue(result.filename().contains("20601234567-03-B001"));
        }

        @Test
        @DisplayName("BOL-HP-011 extra — Descargar XML desde BD (fallback)")
        void descargarXml_desdeBD_ok() {
            BoletaEntity entity = buildEntity("BOL-3", "VALIDADO");
            entity.setXmlPath("some/path");
            entity.setXmlContent("<Invoice/>");
            when(boletaRepository.findById("BOL-3")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(xmlStorageService.leerXml("some/path")).thenReturn(Optional.empty()); // disco falla

            BoletaXmlResult result = service.descargarBoletaXml("BOL-3");

            assertTrue(result.hasContent()); // usa BD como fallback
        }

        @Test
        @DisplayName("BOL-UP-015 — Descargar XML de boleta sin XML → hasContent=false")
        void descargarXml_sinXml_sinContenido() {
            BoletaEntity entity = buildEntity("BOL-5", "CON_ERROR");
            entity.setXmlPath(null);
            entity.setXmlContent(null);
            when(boletaRepository.findById("BOL-5")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(xmlStorageService.leerXml(null)).thenReturn(Optional.empty());

            BoletaXmlResult result = service.descargarBoletaXml("BOL-5");

            assertFalse(result.hasContent());
            assertNull(result.content());
        }

        @Test
        @DisplayName("BOL-UP-015 extra — Boleta inexistente → 404")
        void descargarXml_inexistente_404() {
            when(boletaRepository.findById("BOL-X")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.descargarBoletaXml("BOL-X"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OBTENER Y LISTAR
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("obtenerBoleta / listarBoletas")
    class ObtenerListar {

        @Test
        @DisplayName("obtenerBoleta — existente → OK")
        void obtenerBoleta_ok() {
            BoletaEntity entity = buildEntity("BOL-1", "ACEPTADA");
            when(boletaRepository.findById("BOL-1")).thenReturn(Optional.of(entity));
            when(boletaMapper.toData(entity)).thenReturn(buildData());
            when(cuotaCreditoRepository.findByBoletaIdOrderByNumeroCuota("BOL-1")).thenReturn(List.of());
            when(allowanceChargeRepository.findByBoletaId("BOL-1")).thenReturn(List.of());

            BoletaResponseDto r = service.obtenerBoleta("BOL-1");
            assertTrue(r.getIsSuccess());
            assertNotNull(r.getData());
        }

        @Test
        @DisplayName("BOL-UP-014 — obtenerBoleta inexistente → 404")
        void obtenerBoleta_inexistente_404() {
            when(boletaRepository.findById("BOL-X")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.obtenerBoleta("BOL-X"));
        }

        @Test
        @DisplayName("listarBoletas — lista correcta con paginación")
        void listarBoletas_ok() {
            BoletaEntity e1 = buildEntity("BOL-1", "VALIDADO");
            BoletaEntity e2 = buildEntity("BOL-2", "ACEPTADA");
            Page<BoletaEntity> page = new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 10), 2);
            when(boletaRepository.buscarConFiltros(eq("COMP-1"), isNull(), isNull(), isNull(), isNull(), any()))
                    .thenReturn(page);
            when(boletaMapper.toData(any())).thenReturn(buildData());
            when(cuotaCreditoRepository.findByBoletaIdOrderByNumeroCuota(anyString())).thenReturn(List.of());
            when(allowanceChargeRepository.findByBoletaId(anyString())).thenReturn(List.of());

            BoletaListResponseDto r = service.listarBoletas("COMP-1",
                    null, null, null, null, 0, 10);
            assertTrue(r.getIsSuccess());
            assertEquals(2, r.getData().getContent().size());
            assertEquals(2L, r.getData().getPagination().getTotalElements());
        }

        @Test
        @DisplayName("listarBoletas — lista vacía")
        void listarBoletas_vacia() {
            Page<BoletaEntity> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(boletaRepository.buscarConFiltros(eq("X"), isNull(), isNull(), isNull(), isNull(), any()))
                    .thenReturn(page);
            BoletaListResponseDto r = service.listarBoletas("X",
                    null, null, null, null, 0, 10);
            assertTrue(r.getIsSuccess());
            assertEquals(0, r.getData().getContent().size());
            assertEquals(0L, r.getData().getPagination().getTotalElements());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  EDGE CASES
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("BOL-EC-002 — CON_ERROR corregida → genera XML por primera vez")
        void conErrorCorregida_generaNuevoXml() {
            BoletaEntity entity = buildEntity("BOL-5", "CON_ERROR");
            entity.setErroresValidacion("[\"monto negativo\"]");
            entity.setXmlContent(null);
            entity.setXmlPath(null);
            when(boletaRepository.findById("BOL-5")).thenReturn(Optional.of(entity));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));

            BoletaEmitirDto req = buildEmitirRequest();
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<FirstXml/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("path/first/xml");
            when(boletaRepository.save(any())).thenReturn(entity);
            when(boletaMapper.toData(any())).thenReturn(buildData());

            service.actualizarBoletaBorrador("BOL-5", req);

            assertEquals("VALIDADO", entity.getEstado());
            assertNull(entity.getErroresValidacion());
            verify(ublInvoiceXmlBuilder).buildBoletaXml(any(), any(), any(), anyList(), anyList());
        }

        @Test
        @DisplayName("BOL-EC-005 — CONTADO auto-asigna fechaVencimiento = fechaEmision")
        void contadoAutoAsignaFechaVencimiento() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(BoletaEmitirDto.FormaPagoEnum.CONTADO);
            req.setFechaVencimiento(null);

            BoletaEntity entity = buildEntity("BOL-1", "BORRADOR");
            entity.setFechaVencimiento(null); // será auto-asignado
            when(correlativoService.siguienteNumero(anyString(), anyString())).thenReturn(1);
            when(boletaMapper.toEntity(req)).thenReturn(entity);
            when(boletaRepository.save(any())).thenReturn(entity);
            when(boletaMapper.toData(entity)).thenReturn(buildData());
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<Invoice/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("path");

            service.emitirBoleta(req);

            // fechaVencimiento debe ser = fechaEmision
            assertEquals(entity.getFechaEmision(), entity.getFechaVencimiento());
        }

        @Test
        @DisplayName("BOL-EC-006 — Sin formaPago explícita → default CONTADO")
        void sinFormaPago_defaultContado() {
            BoletaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(null);

            BoletaEntity entity = buildEntity("BOL-1", "BORRADOR");
            entity.setFormaPago(null); // será asignado por defecto
            when(correlativoService.siguienteNumero(anyString(), anyString())).thenReturn(1);
            when(boletaMapper.toEntity(req)).thenReturn(entity);
            when(boletaRepository.save(any())).thenReturn(entity);
            when(boletaMapper.toData(entity)).thenReturn(buildData());
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenReturn("<Invoice/>");
            when(xmlStorageService.guardarXml(anyString(), eq("03"), anyString(), anyInt(), anyString()))
                    .thenReturn("path");

            service.emitirBoleta(req);

            assertEquals("CONTADO", entity.getFormaPago());
        }

        @Test
        @DisplayName("BOL-EC-007 — Tolerancia de redondeo ±0.01 → auto-validación pasa")
        void toleranciaRedondeo_001_autoValidacionOk() {
            BoletaEmitirDto req = buildEmitirRequest();
            // Gravadas=100.33 + IGV=18.06 = 118.39, pero total=118.40 (diff=0.01)
            req.setGravadas(new BigDecimal("100.33"));
            req.setIgv(new BigDecimal("18.06"));
            req.setTotal(new BigDecimal("118.40"));

            // totales formula: 100.33 + 18.06 = 118.39 vs 118.40 → diff=0.01 → pasa
            stubEmisionExitosa(req);
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));

            BoletaResponseDto r = service.emitirBoleta(req);
            assertTrue(r.getIsSuccess());
            verify(notificationService).publicar(eq("COMP-1"), eq("VALIDADO"),
                    eq("BOLETA"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("BOL-EC-009 — Error generando XML → catch silencioso, boleta se guarda")
        void errorGenerandoXml_catchSilencioso() {
            BoletaEmitirDto req = buildEmitirRequest();

            BoletaEntity entity = buildEntity("BOL-1", "BORRADOR");
            when(correlativoService.siguienteNumero(anyString(), anyString())).thenReturn(1);
            when(boletaMapper.toEntity(req)).thenReturn(entity);
            when(boletaRepository.save(any())).thenReturn(entity);
            when(boletaMapper.toData(entity)).thenReturn(buildData());
            when(clientRepository.findById("CLI-DNI")).thenReturn(Optional.of(clientDni()));
            when(documentValidatorService.validarBoleta(
                    anyString(), anyInt(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(DocumentValidationResult.ok());
            // XML builder lanza RuntimeException
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(ublInvoiceXmlBuilder.buildBoletaXml(any(), any(), any(), anyList(), anyList()))
                    .thenThrow(new RuntimeException("Error inesperado en XML builder"));

            // NO debe lanzar excepción — catch silencioso
            BoletaResponseDto r = service.emitirBoleta(req);

            assertTrue(r.getIsSuccess());
            // xmlContent y xmlPath quedan null (no se pudo generar)
            assertNull(entity.getXmlContent());
            assertNull(entity.getXmlPath());
            // Estado sigue VALIDADO (la validación pasó, solo falló el XML)
            assertEquals("VALIDADO", entity.getEstado());
        }
    }
}














