package com.etlions.ms_bs_etia.doc_notadeb.service.impl;

import com.etlions.ms_bs_etia.doc_notadeb.mapper.NotaDebitoMapper;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoEntity;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoRepository;
import com.etlions.ms_bs_etia.doc_notadeb.service.docNotadebService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class docNotadebServiceImpl implements docNotadebService {

    private final NotaDebitoRepository notaDebitoRepository;
    private final NotaDebitoMapper notaDebitoMapper;

    @Override
    public ActualizarNotaDebitoBorrador200Response emitirNotaDebito(NotaDebitoEmitirRequest request) {
        NotaDebitoEntity entity = notaDebitoMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());
        NotaDebitoEntity saved = notaDebitoRepository.save(entity);
        NotaDebitoData data = notaDebitoMapper.toData(saved);
        return new ActualizarNotaDebitoBorrador200Response()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public ActualizarNotaDebitoBorrador200Response actualizarNotaDebitoBorrador(String notaDebitoId,
                                                                                NotaDebitoUpdateRequest request) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new RuntimeException("Nota de débito no encontrada: " + notaDebitoId));
        notaDebitoMapper.updateEntity(request, entity);
        NotaDebitoEntity saved = notaDebitoRepository.save(entity);
        NotaDebitoData data = notaDebitoMapper.toData(saved);
        return new ActualizarNotaDebitoBorrador200Response()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public SuccessResponse eliminarNotaDebitoBorrador(String notaDebitoId) {
        notaDebitoRepository.deleteById(notaDebitoId);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public ListarNotasDebito200Response listarNotasDebito(String companyId, String facturaReferenciaId,
                                                           Date desdeFecha, Date hastaFecha,
                                                           Integer page, Integer size) {
        List<NotaDebitoEntity> entities = notaDebitoRepository.findByCompanyId(companyId);
        List<NotaDebitoData> dataList = entities.stream()
                .map(notaDebitoMapper::toData)
                .toList();
        ListarNotasDebito200ResponseAllOfData responseData = new ListarNotasDebito200ResponseAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        ListarNotasDebito200Response response = new ListarNotasDebito200Response();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }
}
