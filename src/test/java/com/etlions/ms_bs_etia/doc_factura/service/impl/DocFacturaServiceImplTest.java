package com.etlions.ms_bs_etia.doc_factura.service.impl;

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
import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.AllowanceChargeRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocFacturaServiceImplTest {

    @Mock private FacturaRepository facturaRepository;
    @Mock private FacturaMapper facturaMapper;
    @Mock private CorrelativoService correlativoService;
    @Mock private NotificationService notificationService;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CuotaCreditoRepository cuotaCreditoRepository;
    @Mock private AllowanceChargeRepository allowanceChargeRepository;
    @Mock private UblInvoiceXmlBuilder ublInvoiceXmlBuilder;
    @Mock private XmlStorageService xmlStorageService;
    @Mock private DocumentValidatorService documentValidatorService;

    @InjectMocks
    private DocFacturaServiceImpl service;

    // ──────── Helpers ────────

    private DetalleItem buildDetalleItem() {
        return new DetalleItem()
                .descripcion("LAPTOP HP 15")
                .cantidad(new BigDecimal("2"))
                .valorUnitario(new BigDecimal("500.00"))
                .precioUnitario(new BigDecimal("590.00"))
                .subtotal(new BigDecimal("1000.00"))
                .igv(new BigDecimal("180.00"));
    }

    private FacturaEmitirDto buildEmitirRequest() {
        return new FacturaEmitirDto()
                .companyId("COMP-1")
                .clientId("CLI-1")
                .serie("F001")
                .gravadas(new BigDecimal("100.00"))
                .igv(new BigDecimal("18.00"))
                .total(new BigDecimal("118.00"))
                .moneda(FacturaEmitirDto.MonedaEnum.PEN)
                .formaPago(FacturaEmitirDto.FormaPagoEnum.CONTADO)
                .detalle(new ArrayList<>(List.of(buildDetalleItem())));
    }

    private FacturaEntity buildEntity(String id, String estado) {
        return FacturaEntity.builder()
                .id(id).companyId("COMP-1").clientId("CLI-1")
                .serie("F001").numero(1).estado(estado)
                .gravadas(new BigDecimal("100.00"))
                .igv(new BigDecimal("18.00"))
                .total(new BigDecimal("118.00"))
                .formaPago("CONTADO")
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private FacturaData buildData() {
        FacturaData d = new FacturaData();
        d.setId("FAC-1"); d.setSerie("F001"); d.setNumero(1);
        return d;
    }

    private ClientEntity clientRuc() {
        return ClientEntity.builder().id("CLI-1").tipoDocumento("6").build();
    }

    private CompanyEntity company() {
        return CompanyEntity.builder().id("COMP-1").ruc("20517863948").estado("ACTIVO").build();
    }

    private void stubEmisionExitosa(FacturaEmitirDto req) {
        FacturaEntity e = buildEntity("FAC-1", "BORRADOR");
        when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
        when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
        when(correlativoService.siguienteNumero("COMP-1", "F001")).thenReturn(1);
        when(facturaMapper.toEntity(req)).thenReturn(e);
        when(facturaRepository.save(any(FacturaEntity.class))).thenReturn(e);
        when(facturaMapper.toData(e)).thenReturn(buildData());
        when(ublInvoiceXmlBuilder.buildInvoiceXml(any(), any(), any(), anyList(), anyList()))
                .thenReturn("<Invoice/>");
        when(xmlStorageService.guardarXml(anyString(), anyString(), anyString(), anyInt(), anyString()))
                .thenReturn("20517863948/F001/00000001/20517863948-01-F001-00000001.xml");
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Happy path")
    class EmitirHappy {

        @Test @DisplayName("emitir factura completa OK")
        void emitirExitoso() {
            FacturaEmitirDto req = buildEmitirRequest();
            stubEmisionExitosa(req);
            FacturaResponseDto r = service.emitirFactura(req);
            assertTrue(r.getIsSuccess());
            // save() se invoca 2 veces: 1ª persiste la entidad, 2ª agrega xmlContent/xmlPath
            verify(facturaRepository, times(2)).save(any());
            verify(notificationService).publicar(eq("COMP-1"), eq("EMITIDA"),
                    eq("FACTURA"), anyString(), anyString(), anyString());
        }

        @Test @DisplayName("sin serie → usa F001 por defecto")
        void sinSerie() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setSerie(null);
            FacturaEntity e = buildEntity("FAC-1", "BORRADOR");
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            when(correlativoService.siguienteNumero("COMP-1", "F001")).thenReturn(1);
            when(facturaMapper.toEntity(req)).thenReturn(e);
            when(facturaRepository.save(any())).thenReturn(e);
            when(facturaMapper.toData(e)).thenReturn(buildData());
            FacturaResponseDto r = service.emitirFactura(req);
            assertTrue(r.getIsSuccess());
            assertEquals("F001", e.getSerie());
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Campos obligatorios")
    class EmitirObligatorios {

        @Test @DisplayName("companyId null")
        void companyIdNull() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setCompanyId(null);
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
            verify(facturaRepository, never()).save(any());
        }

        @Test @DisplayName("companyId vacío")
        void companyIdBlank() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setCompanyId("   ");
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("clientId null")
        void clientIdNull() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setClientId(null);
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("clientId vacío")
        void clientIdBlank() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setClientId("  ");
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Serie")
    class EmitirSerie {

        @Test @DisplayName("serie B001 (boleta)")
        void serieBoletaFalla() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setSerie("B001");
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("formato inválido FABC")
        void formatoInvalido() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setSerie("FABC");
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Detalle obligatorio")
    class EmitirDetalle {

        @Test @DisplayName("detalle null")
        void detalleNull() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setDetalle(null);
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("detalle vacío")
        void detalleVacio() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setDetalle(new ArrayList<>());
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("ítem sin descripción")
        void itemSinDesc() {
            FacturaEmitirDto req = buildEmitirRequest();
            DetalleItem it = buildDetalleItem(); it.setDescripcion(null);
            req.setDetalle(List.of(it));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("ítem con descripción vacía")
        void itemDescVacia() {
            FacturaEmitirDto req = buildEmitirRequest();
            DetalleItem it = buildDetalleItem(); it.setDescripcion("   ");
            req.setDetalle(List.of(it));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("ítem con cantidad 0")
        void itemCantCero() {
            FacturaEmitirDto req = buildEmitirRequest();
            DetalleItem it = buildDetalleItem(); it.setCantidad(BigDecimal.ZERO);
            req.setDetalle(List.of(it));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("ítem con cantidad negativa")
        void itemCantNeg() {
            FacturaEmitirDto req = buildEmitirRequest();
            DetalleItem it = buildDetalleItem(); it.setCantidad(new BigDecimal("-1"));
            req.setDetalle(List.of(it));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("ítem con valorUnitario negativo")
        void itemValNeg() {
            FacturaEmitirDto req = buildEmitirRequest();
            DetalleItem it = buildDetalleItem(); it.setValorUnitario(new BigDecimal("-10"));
            req.setDetalle(List.of(it));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("múltiples ítems, uno inválido → errores acumulados")
        void multiItemBad() {
            FacturaEmitirDto req = buildEmitirRequest();
            DetalleItem ok = buildDetalleItem();
            DetalleItem bad = buildDetalleItem(); bad.setDescripcion(null); bad.setCantidad(BigDecimal.ZERO);
            req.setDetalle(List.of(ok, bad));
            SunatValidationException ex = assertThrows(SunatValidationException.class,
                    () -> service.emitirFactura(req));
            assertTrue(ex.getErrors().size() >= 2);
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Montos")
    class EmitirMontos {

        @Test @DisplayName("totales inconsistentes")
        void totalesIncon() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setTotal(new BigDecimal("999.99"));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("gravadas negativo")
        void gravadasNeg() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setGravadas(new BigDecimal("-100"));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("igv negativo")
        void igvNeg() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setIgv(new BigDecimal("-18"));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("total negativo")
        void totalNeg() {
            FacturaEmitirDto req = buildEmitirRequest(); req.setTotal(new BigDecimal("-118"));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("todos null")
        void montosNull() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setGravadas(null); req.setIgv(null); req.setTotal(null);
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("con cargos y descuentos → total correcto OK")
        void conCargosDescuentosOk() {
            // gravadas=100 + igv=18 + cargo=50 − descuento=30 = 138
            FacturaEmitirDto req = buildEmitirRequest();
            req.setGravadas(new BigDecimal("100.00"));
            req.setIgv(new BigDecimal("18.00"));
            req.setTotal(new BigDecimal("138.00"));
            req.setCargosDescuentos(List.of(
                    new AllowanceChargeItem().chargeIndicator(true)
                            .reasonCode("50").amount(new BigDecimal("50.00"))
                            .baseAmount(new BigDecimal("100.00")).multiplierFactor(new BigDecimal("0.50")),
                    new AllowanceChargeItem().chargeIndicator(false)
                            .reasonCode("00").amount(new BigDecimal("30.00"))
                            .baseAmount(new BigDecimal("100.00")).multiplierFactor(new BigDecimal("0.30"))
            ));
            stubEmisionExitosa(req);
            FacturaResponseDto r = service.emitirFactura(req);
            assertTrue(r.getIsSuccess());
        }

        @Test @DisplayName("con descuento pero total no lo resta → excepción")
        void conDescuentoTotalSinRestar() {
            // gravadas=100 + igv=18 − descuento=30 → esperado=88, pero total=118
            FacturaEmitirDto req = buildEmitirRequest();
            req.setCargosDescuentos(List.of(
                    new AllowanceChargeItem().chargeIndicator(false)
                            .reasonCode("00").amount(new BigDecimal("30.00"))
                            .baseAmount(new BigDecimal("100.00")).multiplierFactor(new BigDecimal("0.30"))
            ));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Empresa y cliente")
    class EmitirEmpresaCliente {

        @Test @DisplayName("empresa no encontrada")
        void empresaNoExiste() {
            FacturaEmitirDto req = buildEmitirRequest();
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("cliente no encontrado")
        void clienteNoExiste() {
            FacturaEmitirDto req = buildEmitirRequest();
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("cliente con DNI en factura")
        void clienteDni() {
            FacturaEmitirDto req = buildEmitirRequest();
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(
                    Optional.of(ClientEntity.builder().id("CLI-1").tipoDocumento("1").build()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("cliente con CE en factura")
        void clienteCE() {
            FacturaEmitirDto req = buildEmitirRequest();
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(
                    Optional.of(ClientEntity.builder().id("CLI-1").tipoDocumento("4").build()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Moneda y tipo de cambio")
    class EmitirMoneda {

        @Test @DisplayName("USD sin tipoCambio")
        void usdSinTc() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setMoneda(FacturaEmitirDto.MonedaEnum.USD); req.setTipoCambio(null);
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("USD con tipoCambio=0")
        void usdTcCero() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setMoneda(FacturaEmitirDto.MonedaEnum.USD); req.setTipoCambio(BigDecimal.ZERO);
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("USD con tipoCambio válido OK")
        void usdOk() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setMoneda(FacturaEmitirDto.MonedaEnum.USD);
            req.setTipoCambio(new BigDecimal("3.75"));
            stubEmisionExitosa(req);
            assertTrue(service.emitirFactura(req).getIsSuccess());
        }

        @Test @DisplayName("PEN sin tipoCambio OK")
        void penOk() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setTipoCambio(null);
            stubEmisionExitosa(req);
            assertTrue(service.emitirFactura(req).getIsSuccess());
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Fecha emisión")
    class EmitirFecha {

        @Test @DisplayName("fecha futura")
        void fechaFutura() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("fecha hoy OK")
        void fechaHoy() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now()));
            stubEmisionExitosa(req);
            assertTrue(service.emitirFactura(req).getIsSuccess());
        }

        @Test @DisplayName("fecha pasada OK")
        void fechaPasada() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now().minusDays(3)));
            stubEmisionExitosa(req);
            assertTrue(service.emitirFactura(req).getIsSuccess());
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("emitirFactura — Forma pago CRÉDITO")
    class EmitirCredito {

        @Test @DisplayName("CRÉDITO sin vencimiento")
        void sinVenc() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(FacturaEmitirDto.FormaPagoEnum.CREDITO);
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now()));
            req.setFechaVencimiento(null);
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("CRÉDITO vencimiento = emisión")
        void vencIgual() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(FacturaEmitirDto.FormaPagoEnum.CREDITO);
            Date hoy = java.sql.Date.valueOf(LocalDate.now());
            req.setFechaEmision(hoy); req.setFechaVencimiento(hoy);
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("CRÉDITO vencimiento anterior")
        void vencAnterior() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(FacturaEmitirDto.FormaPagoEnum.CREDITO);
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now()));
            req.setFechaVencimiento(java.sql.Date.valueOf(LocalDate.now().minusDays(5)));
            when(companyRepository.findById("COMP-1")).thenReturn(Optional.of(company()));
            when(clientRepository.findById("CLI-1")).thenReturn(Optional.of(clientRuc()));
            assertThrows(SunatValidationException.class, () -> service.emitirFactura(req));
        }

        @Test @DisplayName("CRÉDITO vencimiento posterior OK")
        void vencOk() {
            FacturaEmitirDto req = buildEmitirRequest();
            req.setFormaPago(FacturaEmitirDto.FormaPagoEnum.CREDITO);
            req.setFechaEmision(java.sql.Date.valueOf(LocalDate.now()));
            req.setFechaVencimiento(java.sql.Date.valueOf(LocalDate.now().plusDays(30)));
            stubEmisionExitosa(req);
            assertTrue(service.emitirFactura(req).getIsSuccess());
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("anularFactura")
    class AnularFactura {

        @Test @DisplayName("desde ENVIADA OK")
        void desdeEnviada() {
            FacturaEntity e = buildEntity("FAC-1", "ENVIADA");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            when(facturaRepository.save(e)).thenReturn(e);
            when(facturaMapper.toData(e)).thenReturn(buildData());
            FacturaResponseDto r = service.anularFactura("FAC-1",
                    new AnularFacturaDto().motivoAnulacion("Error"));
            assertTrue(r.getIsSuccess());
            assertEquals("ANULADA", e.getEstado());
            verify(notificationService).publicar(eq("COMP-1"), eq("ANULADA"),
                    eq("FACTURA"), anyString(), anyString(), anyString());
        }

        @Test @DisplayName("desde ACEPTADA OK")
        void desdeAceptada() {
            FacturaEntity e = buildEntity("FAC-1", "ACEPTADA");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            when(facturaRepository.save(e)).thenReturn(e);
            when(facturaMapper.toData(e)).thenReturn(buildData());
            assertTrue(service.anularFactura("FAC-1",
                    new AnularFacturaDto().motivoAnulacion("Dev")).getIsSuccess());
            assertEquals("ANULADA", e.getEstado());
        }

        @Test @DisplayName("desde BORRADOR falla")
        void desdeBorrador() {
            FacturaEntity e = buildEntity("FAC-1", "BORRADOR");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            assertThrows(SunatValidationException.class, () ->
                    service.anularFactura("FAC-1", new AnularFacturaDto().motivoAnulacion("T")));
            verify(facturaRepository, never()).save(any());
        }

        @Test @DisplayName("desde RECHAZADA falla")
        void desdeRechazada() {
            FacturaEntity e = buildEntity("FAC-1", "RECHAZADA");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            assertThrows(SunatValidationException.class, () ->
                    service.anularFactura("FAC-1", new AnularFacturaDto().motivoAnulacion("T")));
        }

        @Test @DisplayName("ya ANULADA falla")
        void yaAnulada() {
            FacturaEntity e = buildEntity("FAC-1", "ANULADA");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            assertThrows(SunatValidationException.class, () ->
                    service.anularFactura("FAC-1", new AnularFacturaDto().motivoAnulacion("T")));
        }

        @Test @DisplayName("inexistente")
        void inexistente() {
            when(facturaRepository.findById("X")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () ->
                    service.anularFactura("X", new AnularFacturaDto().motivoAnulacion("T")));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("eliminarFactura")
    class EliminarFactura {

        @Test @DisplayName("BORRADOR OK")
        void borrador() {
            FacturaEntity e = buildEntity("FAC-1", "BORRADOR");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            assertTrue(service.eliminarFactura("FAC-1").getIsSuccess());
            verify(facturaRepository).delete(e);
        }

        @Test @DisplayName("ENVIADA falla")
        void enviada() {
            FacturaEntity e = buildEntity("FAC-1", "ENVIADA");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            assertThrows(SunatValidationException.class, () -> service.eliminarFactura("FAC-1"));
            verify(facturaRepository, never()).delete(any());
        }

        @Test @DisplayName("ACEPTADA falla")
        void aceptada() {
            when(facturaRepository.findById("FAC-1"))
                    .thenReturn(Optional.of(buildEntity("FAC-1", "ACEPTADA")));
            assertThrows(SunatValidationException.class, () -> service.eliminarFactura("FAC-1"));
        }

        @Test @DisplayName("ANULADA falla")
        void anulada() {
            when(facturaRepository.findById("FAC-1"))
                    .thenReturn(Optional.of(buildEntity("FAC-1", "ANULADA")));
            assertThrows(SunatValidationException.class, () -> service.eliminarFactura("FAC-1"));
        }

        @Test @DisplayName("RECHAZADA falla")
        void rechazada() {
            when(facturaRepository.findById("FAC-1"))
                    .thenReturn(Optional.of(buildEntity("FAC-1", "RECHAZADA")));
            assertThrows(SunatValidationException.class, () -> service.eliminarFactura("FAC-1"));
        }

        @Test @DisplayName("inexistente")
        void inexistente() {
            when(facturaRepository.findById("X")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.eliminarFactura("X"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("obtenerFactura")
    class ObtenerFactura {

        @Test @DisplayName("existente OK")
        void ok() {
            FacturaEntity e = buildEntity("FAC-1", "BORRADOR");
            when(facturaRepository.findById("FAC-1")).thenReturn(Optional.of(e));
            when(facturaMapper.toData(e)).thenReturn(buildData());
            FacturaResponseDto r = service.obtenerFactura("FAC-1");
            assertTrue(r.getIsSuccess()); assertNotNull(r.getData());
        }

        @Test @DisplayName("inexistente")
        void inexistente() {
            when(facturaRepository.findById("X")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.obtenerFactura("X"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested @DisplayName("listarFacturas")
    class ListarFacturas {

        @Test @DisplayName("lista correcta")
        void listar() {
            FacturaEntity e1 = buildEntity("F1", "BORRADOR");
            FacturaEntity e2 = buildEntity("F2", "ENVIADA");
            when(facturaRepository.findByCompanyId("COMP-1")).thenReturn(List.of(e1, e2));
            when(facturaMapper.toData(e1)).thenReturn(buildData());
            when(facturaMapper.toData(e2)).thenReturn(buildData());
            FacturaListResponseDto r = service.listarFacturas("COMP-1",
                    null, null, null, null, null, 0, 10);
            assertTrue(r.getIsSuccess());
            assertEquals(2, r.getData().getContent().size());
        }

        @Test @DisplayName("lista vacía")
        void vacio() {
            when(facturaRepository.findByCompanyId("X")).thenReturn(Collections.emptyList());
            FacturaListResponseDto r = service.listarFacturas("X",
                    null, null, null, null, null, 0, 10);
            assertTrue(r.getIsSuccess());
            assertEquals(0, r.getData().getContent().size());
        }
    }
}

