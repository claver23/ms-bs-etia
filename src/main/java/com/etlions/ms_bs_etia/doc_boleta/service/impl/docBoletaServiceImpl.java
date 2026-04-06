package com.etlions.ms_bs_etia.doc_boleta.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.SunatValidator;
import com.etlions.ms_bs_etia.doc_boleta.mapper.BoletaMapper;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.doc_boleta.service.docBoletaService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class docBoletaServiceImpl implements docBoletaService {

    private final BoletaRepository boletaRepository;
    private final BoletaMapper boletaMapper;
    private final CorrelativoService correlativoService;

    private static final String DEFAULT_SERIE_BOLETA = "B001";

    @Override
    @Transactional
    public AnularBoleta200Response emitirBoleta(BoletaEmitirRequest request) {
        // — Validaciones SUNAT —
        String serie = request.getSerie() != null ? request.getSerie().toUpperCase() : DEFAULT_SERIE_BOLETA;
        SunatValidator.validarFormatoSerie(serie);
        SunatValidator.validarSerieBoleta(serie);
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal());

        // — Correlativo automático (ignora numero del request) —
        int numero = correlativoService.siguienteNumero(request.getCompanyId(), serie);

        BoletaEntity entity = boletaMapper.toEntity(request);
        entity.setSerie(serie);
        entity.setNumero(numero);
        entity.setEstado("BORRADOR");
        if (entity.getFechaEmision() == null) {
            entity.setFechaEmision(LocalDate.now());
        }
        entity.setFechaCreacion(LocalDateTime.now());
        BoletaEntity saved = boletaRepository.save(entity);
        BoletaData data = boletaMapper.toData(saved);
        return new AnularBoleta200Response()
                .isSuccess(true)
                .status("OK")
                .message("Boleta emitida exitosamente — " + serie + "-" + numero)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public AnularBoleta200Response anularBoleta(String boletaId, AnularBoletaRequest request) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
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
    @Transactional
    public SuccessResponse eliminarBoletaBorrador(String boletaId) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
        // — SUNAT: solo se pueden eliminar borradores —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Boleta", boletaId);
        boletaRepository.delete(entity);
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

    @Override
    public AnularBoleta200Response obtenerBoleta(String boletaId) {
        BoletaEntity entity = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
        BoletaData data = boletaMapper.toData(entity);
        return new AnularBoleta200Response()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}
