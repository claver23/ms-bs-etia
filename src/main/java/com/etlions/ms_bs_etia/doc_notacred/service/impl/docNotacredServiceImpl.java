package com.etlions.ms_bs_etia.doc_notacred.service.impl;

import com.etlions.ms_bs_etia.doc_notacred.mapper.NotaCreditoMapper;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_notacred.service.docNotacredService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class docNotacredServiceImpl implements docNotacredService {

    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaCreditoMapper notaCreditoMapper;

    @Override
    public ActualizarNotaCreditoBorrador200Response emitirNotaCredito(NotaCreditoEmitirRequest request) {
        NotaCreditoEntity entity = notaCreditoMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());
        NotaCreditoEntity saved = notaCreditoRepository.save(entity);
        NotaCreditoData data = notaCreditoMapper.toData(saved);
        return new ActualizarNotaCreditoBorrador200Response()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public ActualizarNotaCreditoBorrador200Response actualizarNotaCreditoBorrador(String notaCreditoId,
                                                                                  NotaCreditoUpdateRequest request) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new RuntimeException("Nota de crédito no encontrada: " + notaCreditoId));
        notaCreditoMapper.updateEntity(request, entity);
        NotaCreditoEntity saved = notaCreditoRepository.save(entity);
        NotaCreditoData data = notaCreditoMapper.toData(saved);
        return new ActualizarNotaCreditoBorrador200Response()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public SuccessResponse eliminarNotaCreditoBorrador(String notaCreditoId) {
        notaCreditoRepository.deleteById(notaCreditoId);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public ListarNotasCredito200Response listarNotasCredito(String companyId, String facturaReferenciaId,
                                                             Date desdeFecha, Date hastaFecha,
                                                             Integer page, Integer size) {
        List<NotaCreditoEntity> entities = notaCreditoRepository.findByCompanyId(companyId);
        List<NotaCreditoData> dataList = entities.stream()
                .map(notaCreditoMapper::toData)
                .toList();
        ListarNotasCredito200ResponseAllOfData responseData = new ListarNotasCredito200ResponseAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        ListarNotasCredito200Response response = new ListarNotasCredito200Response();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }
}
