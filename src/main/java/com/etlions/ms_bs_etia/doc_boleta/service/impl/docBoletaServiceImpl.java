package com.etlions.ms_bs_etia.doc_boleta.service.impl;

import com.etlions.ms_bs_etia.doc_boleta.mapper.BoletaMapper;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.doc_boleta.service.docBoletaService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class docBoletaServiceImpl implements docBoletaService {

    private final BoletaRepository boletaRepository;
    private final BoletaMapper boletaMapper;

    @Override
    public AnularBoleta200Response emitirBoleta(BoletaEmitirRequest request) {
        BoletaEntity entity = boletaMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());
        BoletaEntity saved = boletaRepository.save(entity);
        BoletaData data = boletaMapper.toData(saved);
        return new AnularBoleta200Response()
                .isSuccess(true)
                .status("OK")
                .message("Boleta emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public AnularBoleta200Response anularBoleta(String boletaId, AnularBoletaRequest request) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada: " + boletaId));
        entity.setEstado("ANULADA");
        BoletaEntity saved = boletaRepository.save(entity);
        BoletaData data = boletaMapper.toData(saved);
        return new AnularBoleta200Response()
                .isSuccess(true)
                .status("OK")
                .message("Boleta anulada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public SuccessResponse eliminarBoletaBorrador(String boletaId) {
        boletaRepository.deleteById(boletaId);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public ListarBoletas200Response listarBoletas(String companyId, Date desdeFecha, Date hastaFecha,
                                                   String serie, Integer numero,
                                                   Integer page, Integer size) {
        List<BoletaEntity> entities = boletaRepository.findByCompanyId(companyId);
        List<BoletaData> dataList = entities.stream()
                .map(boletaMapper::toData)
                .toList();
        ListarBoletas200ResponseAllOfData responseData = new ListarBoletas200ResponseAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        ListarBoletas200Response response = new ListarBoletas200Response();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }
}
