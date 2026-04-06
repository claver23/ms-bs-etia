package com.etlions.ms_bs_etia.doc_notacred.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.SunatValidator;
import com.etlions.ms_bs_etia.doc_notacred.mapper.NotaCreditoMapper;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_notacred.service.DocNotacredService;
import com.etlions.ms_bs_etia.notification.NotificationService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocNotacredServiceImpl implements DocNotacredService {

    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaCreditoMapper notaCreditoMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public NotaCreditoResponseDto emitirNotaCredito(NotaCreditoEmitirDto request) {
        NotaCreditoEntity entity = notaCreditoMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());
        NotaCreditoEntity saved = notaCreditoRepository.save(entity);
        NotaCreditoData data = notaCreditoMapper.toData(saved);
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "NOTA_CREDITO",
                saved.getId(), saved.getTipoDocumento() != null ? saved.getTipoDocumento() : "",
                "Nota de crédito emitida");
        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public NotaCreditoResponseDto actualizarNotaCreditoBorrador(String notaCreditoId,
                                                                                  NotaCreditoUpdateDto request) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));
        // — Solo borradores son editables —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de crédito", notaCreditoId);
        notaCreditoMapper.updateEntity(request, entity);
        NotaCreditoEntity saved = notaCreditoRepository.save(entity);
        NotaCreditoData data = notaCreditoMapper.toData(saved);
        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarNotaCreditoBorrador(String notaCreditoId) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));
        // — SUNAT: solo borradores se eliminan —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de crédito", notaCreditoId);
        notaCreditoRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public NotaCreditoListResponseDto listarNotasCredito(String companyId, String facturaReferenciaId,
                                                             Date desdeFecha, Date hastaFecha,
                                                             Integer page, Integer size) {
        List<NotaCreditoEntity> entities = notaCreditoRepository.findByCompanyId(companyId);
        List<NotaCreditoData> dataList = entities.stream()
                .map(notaCreditoMapper::toData)
                .toList();
        NotaCreditoListResponseDtoAllOfData responseData = new NotaCreditoListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        NotaCreditoListResponseDto response = new NotaCreditoListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public NotaCreditoResponseDto obtenerNotaCredito(String notaCreditoId) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));
        NotaCreditoData data = notaCreditoMapper.toData(entity);
        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}

