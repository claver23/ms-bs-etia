package com.etlions.ms_bs_etia.doc_boleta.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidationResult;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.app_util.validation.SunatFormatter;
import com.etlions.ms_bs_etia.app_util.validation.SunatValidator;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.doc_boleta.mapper.BoletaMapper;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.doc_boleta.service.DocBoletaService;
import com.etlions.ms_bs_etia.doc_factura.persistence.*;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocBoletaServiceImpl implements DocBoletaService {

    private final BoletaRepository boletaRepository;
    private final BoletaMapper boletaMapper;
    private final CorrelativoService correlativoService;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final AllowanceChargeRepository allowanceChargeRepository;
    private final DocumentValidatorService documentValidatorService;

    private static final String DEFAULT_SERIE_BOLETA = "B001";

    @Override
    @Transactional
    public BoletaResponseDto emitirBoleta(BoletaEmitirDto request) {
        // — Validaciones SUNAT —
        String serie = request.getSerie() != null ? request.getSerie().toUpperCase() : DEFAULT_SERIE_BOLETA;
        SunatValidator.validarFormatoSerie(serie);
        SunatValidator.validarSerieBoleta(serie);

        // — Calcular totalCargos / totalDescuentos desde AllowanceCharge items —
        BigDecimal sumCargos = BigDecimal.ZERO;
        BigDecimal sumDescuentos = BigDecimal.ZERO;
        if (request.getCargosDescuentos() != null) {
            for (AllowanceChargeItem ac : request.getCargosDescuentos()) {
                if (Boolean.TRUE.equals(ac.getChargeIndicator())) {
                    sumCargos = sumCargos.add(ac.getAmount());
                } else {
                    sumDescuentos = sumDescuentos.add(ac.getAmount());
                }
            }
        }
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal(),
                sumCargos, sumDescuentos);

        // — Validar tipo de documento del cliente: Boleta → solo DNI, C.E. o Pasaporte —
        if (request.getClientId() != null && !request.getClientId().isBlank()) {
            ClientEntity client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));
            SunatValidator.validarTipoDocumentoCliente("BOLETA", client.getTipoDocumento());
        }

        // — Correlativo automático (ignora numero del request) —
        int numero = correlativoService.siguienteNumero(request.getCompanyId(), serie);

        BoletaEntity entity = boletaMapper.toEntity(request);
        entity.setSerie(serie);
        entity.setNumero(numero);
        entity.setEstado("BORRADOR");
        if (entity.getFechaEmision() == null) {
            entity.setFechaEmision(LocalDate.now());
        }
        // ── Defaults UBL 2.1 ──
        if (entity.getTipoOperacion() == null) entity.setTipoOperacion("0101");
        if (entity.getHoraEmision() == null) entity.setHoraEmision(LocalTime.now());
        if (entity.getExoneradas() == null) entity.setExoneradas(BigDecimal.ZERO);
        if (entity.getInafectas() == null) entity.setInafectas(BigDecimal.ZERO);
        if (entity.getGratuitas() == null) entity.setGratuitas(BigDecimal.ZERO);
        if (entity.getTotalCargos() == null) entity.setTotalCargos(BigDecimal.ZERO);
        if (entity.getTotalDescuentos() == null) entity.setTotalDescuentos(BigDecimal.ZERO);
        if (entity.getSubtotal() == null && entity.getGravadas() != null) {
            entity.setSubtotal(entity.getGravadas());
        }
        if (entity.getTotalConImpuestos() == null && entity.getGravadas() != null && entity.getIgv() != null) {
            entity.setTotalConImpuestos(entity.getGravadas().add(entity.getIgv()));
        }
        /* If CONTADO, due date = emission date */
        if ("CONTADO".equalsIgnoreCase(entity.getFormaPago()) || entity.getFormaPago() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
            if (entity.getFormaPago() == null) entity.setFormaPago("CONTADO");
        }
        if (entity.getFechaVencimiento() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
        }

        // ── Set totalCargos/totalDescuentos (ya calculados arriba) ──
        entity.setTotalCargos(sumCargos);
        entity.setTotalDescuentos(sumDescuentos);

        entity.setFechaCreacion(LocalDateTime.now());
        BoletaEntity saved = boletaRepository.save(entity);

        // ── Persist cuotas de crédito ──
        List<CuotaCreditoEntity> cuotaEntities = persistCuotas(
                request.getCuotas(), null, saved.getId(), saved.getMoneda());

        // ── Persist cargos/descuentos globales ──
        List<AllowanceChargeEntity> acEntities = persistAllowanceCharges(
                request.getCargosDescuentos(), null, saved.getId(), saved.getMoneda());

        // ── Evaluar flag "emitir" (default: true) ──
        // Boleta: se guarda + valida internamente. No se emite directamente.
        // La emisión real es vía Resumen Diario (REQ-A — fase futura).
        DocumentValidationResult valResult = documentValidatorService.validarBoleta(
                saved.getSerie(), saved.getNumero(), saved.getClientId(),
                saved.getMoneda(), saved.getTipoCambio(),
                saved.getFormaPago(), saved.getFechaEmision(), saved.getFechaVencimiento(),
                saved.getTipoOperacion(), saved.getLeyenda(),
                saved.getGravadas(), saved.getIgv(), saved.getTotal(),
                saved.getTotalCargos(), saved.getTotalDescuentos(),
                saved.getDetalleJson());

        if (valResult.valid()) {
            saved.setEstado("VALIDADO");
            saved.setErroresValidacion(null);
        } else {
            saved.setEstado("CON_ERROR");
            saved.setErroresValidacion(documentValidatorService.erroresAJson(valResult.errores()));
        }
        saved = boletaRepository.save(saved);

        String serieNumero = SunatFormatter.formatNumeroComprobante(serie, numero);
        String notifMsg = valResult.valid()
                ? "Boleta " + serieNumero + " guardada — VALIDADO"
                : "Boleta " + serieNumero + " con errores de validación";
        notificationService.publicar(saved.getCompanyId(),
                valResult.valid() ? "VALIDADO" : "CON_ERROR", "BOLETA",
                saved.getId(), serieNumero, notifMsg);

        BoletaData data = boletaMapper.toData(saved);
        enrichBoletaData(data, cuotaEntities, acEntities);

        String msg = valResult.valid()
                ? "Boleta guardada y validada exitosamente — " + serieNumero
                : "Boleta guardada con errores de validación — " + serieNumero;
        return new BoletaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message(msg)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public BoletaResponseDto anularBoleta(String boletaId, AnularBoletaDto request) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
        entity.setEstado("ANULADA");
        BoletaEntity saved = boletaRepository.save(entity);
        BoletaData data = buildBoletaData(saved);
        notificationService.publicar(saved.getCompanyId(), "ANULADA", "BOLETA",
                saved.getId(), saved.getSerie() + "-" + saved.getNumero(),
                "Boleta " + saved.getSerie() + "-" + saved.getNumero() + " anulada");
        return new BoletaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Boleta anulada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarBoletaBorrador(String boletaId) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Boleta", boletaId);
        cuotaCreditoRepository.deleteByBoletaId(boletaId);
        allowanceChargeRepository.deleteByBoletaId(boletaId);
        boletaRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public BoletaListResponseDto listarBoletas(String companyId, Date desdeFecha, Date hastaFecha,
                                                   String serie, Integer numero,
                                                   Integer page, Integer size) {
        List<BoletaEntity> entities = boletaRepository.findByCompanyId(companyId);
        List<BoletaData> dataList = entities.stream()
                .map(this::buildBoletaData)
                .toList();
        BoletaListResponseDtoAllOfData responseData = new BoletaListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        BoletaListResponseDto response = new BoletaListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public BoletaResponseDto obtenerBoleta(String boletaId) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
        BoletaData data = buildBoletaData(entity);
        return new BoletaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }

    // ═══════ VALIDACIÓN INTERNA ═══════

    @Override
    @Transactional
    public DocumentValidationResultDto validarBoleta(String boletaId) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));

        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"CON_ERROR".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden validar boletas en estado BORRADOR, VALIDADO o CON_ERROR. Estado actual: " + entity.getEstado());
        }

        DocumentValidationResult result = documentValidatorService.validarBoleta(
                entity.getSerie(), entity.getNumero(), entity.getClientId(),
                entity.getMoneda(), entity.getTipoCambio(),
                entity.getFormaPago(), entity.getFechaEmision(), entity.getFechaVencimiento(),
                entity.getTipoOperacion(), entity.getLeyenda(),
                entity.getGravadas(), entity.getIgv(), entity.getTotal(),
                entity.getTotalCargos(), entity.getTotalDescuentos(),
                entity.getDetalleJson());

        if (result.valid()) {
            entity.setEstado("VALIDADO");
            entity.setErroresValidacion(null);
        } else {
            entity.setEstado("CON_ERROR");
            entity.setErroresValidacion(documentValidatorService.erroresAJson(result.errores()));
        }
        boletaRepository.save(entity);

        return new DocumentValidationResultDto()
                .valid(result.valid())
                .errores(result.errores())
                .advertencias(result.advertencias());
    }

    // ═══════ HELPERS ═══════

    private BoletaData buildBoletaData(BoletaEntity entity) {
        BoletaData data = boletaMapper.toData(entity);
        List<CuotaCreditoEntity> cuotas = cuotaCreditoRepository.findByBoletaIdOrderByNumeroCuota(entity.getId());
        List<AllowanceChargeEntity> acs = allowanceChargeRepository.findByBoletaId(entity.getId());
        enrichBoletaData(data, cuotas, acs);
        return data;
    }

    private void enrichBoletaData(BoletaData data,
                                  List<CuotaCreditoEntity> cuotas,
                                  List<AllowanceChargeEntity> acs) {
        if (cuotas != null && !cuotas.isEmpty()) {
            data.setCuotas(cuotas.stream().map(c -> new CuotaCredito()
                    .numeroCuota(c.getNumeroCuota())
                    .monto(c.getMonto())
                    .fechaVencimiento(java.sql.Date.valueOf(c.getFechaVencimiento()))
                    .moneda(c.getMoneda())
            ).toList());
        }
        if (acs != null && !acs.isEmpty()) {
            data.setCargosDescuentos(acs.stream().map(a -> new AllowanceChargeItem()
                    .chargeIndicator(a.getChargeIndicator())
                    .reasonCode(a.getReasonCode())
                    .reasonDescription(a.getReasonDescription())
                    .multiplierFactor(a.getMultiplierFactor())
                    .amount(a.getAmount())
                    .baseAmount(a.getBaseAmount())
            ).toList());
        }
    }

    private List<CuotaCreditoEntity> persistCuotas(List<CuotaCredito> cuotas,
                                                    String facturaId, String boletaId,
                                                    String moneda) {
        if (cuotas == null || cuotas.isEmpty()) return List.of();
        List<CuotaCreditoEntity> entities = new ArrayList<>();
        for (int i = 0; i < cuotas.size(); i++) {
            CuotaCredito c = cuotas.get(i);
            String numCuota = c.getNumeroCuota() != null ? c.getNumeroCuota()
                    : String.format("Cuota%03d", i + 1);
            LocalDate fv = c.getFechaVencimiento() != null
                    ? new java.sql.Date(c.getFechaVencimiento().getTime()).toLocalDate()
                    : null;
            entities.add(CuotaCreditoEntity.builder()
                    .facturaId(facturaId)
                    .boletaId(boletaId)
                    .numeroCuota(numCuota)
                    .monto(c.getMonto())
                    .fechaVencimiento(fv)
                    .moneda(c.getMoneda() != null ? c.getMoneda() : moneda)
                    .build());
        }
        return cuotaCreditoRepository.saveAll(entities);
    }

    private List<AllowanceChargeEntity> persistAllowanceCharges(List<AllowanceChargeItem> items,
                                                                 String facturaId, String boletaId,
                                                                 String moneda) {
        if (items == null || items.isEmpty()) return List.of();
        List<AllowanceChargeEntity> entities = new ArrayList<>();
        for (AllowanceChargeItem ac : items) {
            entities.add(AllowanceChargeEntity.builder()
                    .facturaId(facturaId)
                    .boletaId(boletaId)
                    .chargeIndicator(ac.getChargeIndicator())
                    .reasonCode(ac.getReasonCode())
                    .reasonDescription(ac.getReasonDescription())
                    .multiplierFactor(ac.getMultiplierFactor())
                    .amount(ac.getAmount())
                    .baseAmount(ac.getBaseAmount())
                    .moneda(moneda)
                    .build());
        }
        return allowanceChargeRepository.saveAll(entities);
    }
}
