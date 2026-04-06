package com.etlions.ms_bs_etia.doc_boleta.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.validation.SunatValidator;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.doc_boleta.mapper.BoletaMapper;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.doc_boleta.service.DocBoletaService;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
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
public class DocBoletaServiceImpl implements DocBoletaService {

    private final BoletaRepository boletaRepository;
    private final BoletaMapper boletaMapper;
    private final CorrelativoService correlativoService;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;

    private static final String DEFAULT_SERIE_BOLETA = "B001";

    @Override
    @Transactional
    public BoletaResponseDto emitirBoleta(BoletaEmitirDto request) {
        // — Validaciones SUNAT —
        String serie = request.getSerie() != null ? request.getSerie().toUpperCase() : DEFAULT_SERIE_BOLETA;
        SunatValidator.validarFormatoSerie(serie);
        SunatValidator.validarSerieBoleta(serie);
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal());

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
        /* If CONTADO, due date = emission date (date only, no time) */
        if ("CONTADO".equalsIgnoreCase(entity.getFormaPago()) || entity.getFormaPago() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
            if (entity.getFormaPago() == null) entity.setFormaPago("CONTADO");
        }
        if (entity.getFechaVencimiento() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
        }
        entity.setFechaCreacion(LocalDateTime.now());
        BoletaEntity saved = boletaRepository.save(entity);
        BoletaData data = boletaMapper.toData(saved);
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "BOLETA",
                saved.getId(), serie + "-" + numero,
                "Boleta " + serie + "-" + numero + " emitida");
        return new BoletaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Boleta emitida exitosamente — " + serie + "-" + numero)
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
        BoletaData data = boletaMapper.toData(saved);
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
    public BoletaListResponseDto listarBoletas(String companyId, Date desdeFecha, Date hastaFecha,
                                                   String serie, Integer numero,
                                                   Integer page, Integer size) {
        List<BoletaEntity> entities = boletaRepository.findByCompanyId(companyId);
        List<BoletaData> dataList = entities.stream()
                .map(boletaMapper::toData)
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
        BoletaData data = boletaMapper.toData(entity);
        return new BoletaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}

