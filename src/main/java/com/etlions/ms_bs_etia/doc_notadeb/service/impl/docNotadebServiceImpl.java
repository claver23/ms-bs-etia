package com.etlions.ms_bs_etia.doc_notadeb.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.SunatValidator;
import com.etlions.ms_bs_etia.doc_notadeb.mapper.NotaDebitoMapper;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoEntity;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoRepository;
import com.etlions.ms_bs_etia.doc_notadeb.service.DocNotadebService;
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
public class DocNotadebServiceImpl implements DocNotadebService {

    private final NotaDebitoRepository notaDebitoRepository;
    private final NotaDebitoMapper notaDebitoMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public NotaDebitoResponseDto emitirNotaDebito(NotaDebitoEmitirDto request) {
        NotaDebitoEntity entity = notaDebitoMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());
        NotaDebitoEntity saved = notaDebitoRepository.save(entity);
        NotaDebitoData data = notaDebitoMapper.toData(saved);
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "NOTA_DEBITO",
                saved.getId(), "",
                "Nota de débito emitida");
        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public NotaDebitoResponseDto actualizarNotaDebitoBorrador(String notaDebitoId,
                                                                                NotaDebitoUpdateDto request) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));
        // — Solo borradores son editables —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de débito", notaDebitoId);
        notaDebitoMapper.updateEntity(request, entity);
        NotaDebitoEntity saved = notaDebitoRepository.save(entity);
        NotaDebitoData data = notaDebitoMapper.toData(saved);
        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarNotaDebitoBorrador(String notaDebitoId) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));
        // — SUNAT: solo borradores se eliminan —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de débito", notaDebitoId);
        notaDebitoRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public NotaDebitoListResponseDto listarNotasDebito(String companyId, String facturaReferenciaId,
                                                           Date desdeFecha, Date hastaFecha,
                                                           Integer page, Integer size) {
        List<NotaDebitoEntity> entities = notaDebitoRepository.findByCompanyId(companyId);
        List<NotaDebitoData> dataList = entities.stream()
                .map(notaDebitoMapper::toData)
                .toList();
        NotaDebitoListResponseDtoAllOfData responseData = new NotaDebitoListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        NotaDebitoListResponseDto response = new NotaDebitoListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public NotaDebitoResponseDto obtenerNotaDebito(String notaDebitoId) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));
        NotaDebitoData data = notaDebitoMapper.toData(entity);
        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}

