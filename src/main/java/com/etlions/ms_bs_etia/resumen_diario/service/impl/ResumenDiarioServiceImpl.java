package com.etlions.ms_bs_etia.resumen_diario.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.app_util.sunat.SunatSoapClient;
import com.etlions.ms_bs_etia.app_util.sunat.StatusResult;
import com.etlions.ms_bs_etia.app_util.sunat.SendResult;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.ms_bs_etia.resumen_diario.persistence.ResumenDiarioEntity;
import com.etlions.ms_bs_etia.resumen_diario.persistence.ResumenDiarioRepository;
import com.etlions.ms_bs_etia.resumen_diario.service.ResumenDiarioService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumenDiarioServiceImpl implements ResumenDiarioService {

    private final ResumenDiarioRepository resumenDiarioRepository;
    private final BoletaRepository boletaRepository;
    private final CompanyRepository companyRepository;
    private final SunatSoapClient sunatSoapClient;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ResumenDiarioResponseDto crearResumenDiario(ResumenDiarioCrearDto request) {
        String companyId = request.getCompanyId();
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", companyId));

        LocalDate fechaRef = request.getFechaReferencia() != null
                ? new java.sql.Date(request.getFechaReferencia().getTime()).toLocalDate()
                : LocalDate.now();

        // ── Buscar boletas VALIDADAS ──
        List<BoletaEntity> boletas;
        if (request.getBoletaIds() != null && !request.getBoletaIds().isEmpty()) {
            boletas = request.getBoletaIds().stream()
                    .map(id -> boletaRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Boleta", id)))
                    .filter(b -> "VALIDADO".equals(b.getEstado()))
                    .toList();
        } else {
            boletas = boletaRepository.findByCompanyIdAndEstadoAndFechaEmision(
                    companyId, "VALIDADO", fechaRef);
        }

        if (boletas.isEmpty()) {
            throw new SunatValidationException("No hay boletas VALIDADAS para incluir en el resumen diario");
        }

        // ── Generar identificador RC-{YYYYMMDD}-{NNN} ──
        LocalDate hoy = LocalDate.now();
        long count = resumenDiarioRepository.countByCompanyIdAndFechaGeneracion(companyId, hoy);
        String correlativo = String.format("%03d", count + 1);
        String identificador = "RC-" + hoy.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + correlativo;

        // ── Crear entidad ──
        ResumenDiarioEntity resumen = ResumenDiarioEntity.builder()
                .companyId(companyId)
                .identificador(identificador)
                .fechaGeneracion(hoy)
                .fechaReferencia(fechaRef)
                .estado("GENERADO")
                .cantidadBoletas(boletas.size())
                .fechaCreacion(LocalDateTime.now())
                .build();

        ResumenDiarioEntity saved = resumenDiarioRepository.save(resumen);

        // ── Asociar boletas al resumen y cambiar estado a ENVIADA ──
        for (BoletaEntity boleta : boletas) {
            boleta.setResumenDiarioId(saved.getId());
            boleta.setEstado("ENVIADA");
            boletaRepository.save(boleta);
        }

        // ── Enviar a SUNAT (mock) ──
        SendResult sendResult = sunatSoapClient.sendSummary(identificador, company.getRuc());
        if (sendResult.exitoso()) {
            saved.setEstado("ENVIADO");
            saved.setTicketSunat(sendResult.ticket());
        } else {
            saved.setEstado("CON_ERROR");
            saved.setDescripcionRespuesta(sendResult.mensaje());
        }
        saved = resumenDiarioRepository.save(saved);

        // ── Notificar ──
        notificationService.publicar(companyId,
                saved.getEstado(), "RESUMEN_DIARIO",
                saved.getId(), identificador,
                "Resumen Diario " + identificador + " — " + boletas.size() + " boletas — " + saved.getEstado());

        return buildResponse(saved, boletas);
    }

    @Override
    public ResumenDiarioListResponseDto listarResumenesDiarios(String companyId, Integer page, Integer size) {
        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize = size != null && size > 0 ? Math.min(size, 100) : 10;
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "fechaGeneracion"));

        Page<ResumenDiarioEntity> entityPage = resumenDiarioRepository.findByCompanyId(companyId, pageable);
        List<ResumenDiarioData> dataList = entityPage.getContent().stream()
                .map(e -> toData(e, null))
                .toList();

        ResumenDiarioListResponseDtoAllOfData responseData = new ResumenDiarioListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(entityPage.getNumber())
                .pageSize(entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages()));

        ResumenDiarioListResponseDto response = new ResumenDiarioListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public ResumenDiarioResponseDto obtenerResumenDiario(String resumenId) {
        ResumenDiarioEntity entity = resumenDiarioRepository.findById(resumenId)
                .orElseThrow(() -> new ResourceNotFoundException("Resumen Diario", resumenId));
        List<BoletaEntity> boletas = boletaRepository.findByResumenDiarioId(resumenId);
        return buildResponse(entity, boletas);
    }

    @Override
    @Transactional
    public ConsultaEstadoResultDto consultarEstadoResumen(String resumenId) {
        ResumenDiarioEntity resumen = resumenDiarioRepository.findById(resumenId)
                .orElseThrow(() -> new ResourceNotFoundException("Resumen Diario", resumenId));

        if (resumen.getTicketSunat() == null || resumen.getTicketSunat().isBlank()) {
            throw new SunatValidationException("El resumen no tiene ticket SUNAT asignado");
        }

        CompanyEntity company = companyRepository.findById(resumen.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", resumen.getCompanyId()));

        // ── Consultar estado con SUNAT (mock) ──
        StatusResult status = sunatSoapClient.getStatus(resumen.getTicketSunat(), company.getRuc());

        resumen.setCodigoRespuesta(status.codigoRespuesta());
        resumen.setDescripcionRespuesta(status.descripcion());

        if (status.exitoso() && "ACEPTADO".equals(status.estado())) {
            resumen.setEstado("ACEPTADO");
            // ── Actualizar estado de boletas asociadas ──
            List<BoletaEntity> boletas = boletaRepository.findByResumenDiarioId(resumenId);
            for (BoletaEntity boleta : boletas) {
                boleta.setEstado("ACEPTADA");
                boletaRepository.save(boleta);
            }
            notificationService.publicar(resumen.getCompanyId(),
                    "ACEPTADO", "RESUMEN_DIARIO",
                    resumen.getId(), resumen.getIdentificador(),
                    "Resumen " + resumen.getIdentificador() + " ACEPTADO por SUNAT");
        } else if ("RECHAZADO".equals(status.estado())) {
            resumen.setEstado("RECHAZADO");
            // ── Revertir boletas a VALIDADO para re-envío ──
            List<BoletaEntity> boletas = boletaRepository.findByResumenDiarioId(resumenId);
            for (BoletaEntity boleta : boletas) {
                boleta.setEstado("VALIDADO");
                boleta.setResumenDiarioId(null);
                boletaRepository.save(boleta);
            }
            notificationService.publicar(resumen.getCompanyId(),
                    "RECHAZADO", "RESUMEN_DIARIO",
                    resumen.getId(), resumen.getIdentificador(),
                    "Resumen " + resumen.getIdentificador() + " RECHAZADO: " + status.descripcion());
        }
        resumenDiarioRepository.save(resumen);

        return new ConsultaEstadoResultDto()
                .exitoso(status.exitoso())
                .estado(status.estado())
                .codigoRespuesta(status.codigoRespuesta())
                .descripcionRespuesta(status.descripcion())
                .ticket(resumen.getTicketSunat());
    }

    // ═══════ HELPERS ═══════

    private ResumenDiarioResponseDto buildResponse(ResumenDiarioEntity entity, List<BoletaEntity> boletas) {
        ResumenDiarioData data = toData(entity, boletas);
        return new ResumenDiarioResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }

    private ResumenDiarioData toData(ResumenDiarioEntity e, List<BoletaEntity> boletas) {
        ResumenDiarioData data = new ResumenDiarioData()
                .id(e.getId())
                .companyId(e.getCompanyId())
                .identificador(e.getIdentificador())
                .fechaGeneracion(java.sql.Date.valueOf(e.getFechaGeneracion()))
                .fechaReferencia(java.sql.Date.valueOf(e.getFechaReferencia()))
                .estado(ResumenDiarioData.EstadoEnum.fromValue(e.getEstado()))
                .ticketSunat(e.getTicketSunat())
                .codigoRespuesta(e.getCodigoRespuesta())
                .descripcionRespuesta(e.getDescripcionRespuesta())
                .cantidadBoletas(e.getCantidadBoletas());

        if (e.getFechaCreacion() != null) {
            data.setFechaCreacion(java.sql.Timestamp.valueOf(e.getFechaCreacion()));
        }
        return data;
    }
}


