package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.SunatValidator;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
import com.etlions.ms_bs_etia.doc_factura.service.DocFacturaService;
import com.etlions.ms_bs_etia.notification.NotificationService;
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
public class DocFacturaServiceImpl implements DocFacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;
    private final CorrelativoService correlativoService;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;

    private static final String DEFAULT_SERIE_FACTURA = "F001";

    @Override
    @Transactional
    public FacturaResponseDto emitirFactura(FacturaEmitirDto request) {
        // — Validaciones SUNAT —
        String serie = request.getSerie() != null ? request.getSerie().toUpperCase() : DEFAULT_SERIE_FACTURA;
        SunatValidator.validarFormatoSerie(serie);
        SunatValidator.validarSerieFactura(serie);
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal());

        // — Validar tipo de documento del cliente: Factura → solo RUC —
        if (request.getClientId() != null && !request.getClientId().isBlank()) {
            ClientEntity client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));
            SunatValidator.validarTipoDocumentoCliente("FACTURA", client.getTipoDocumento());
        }

        // — Correlativo automático (ignora numero del request) —
        int numero = correlativoService.siguienteNumero(request.getCompanyId(), serie);

        FacturaEntity entity = facturaMapper.toEntity(request);
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
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = facturaMapper.toData(saved);
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "FACTURA",
                saved.getId(), serie + "-" + numero,
                "Factura " + serie + "-" + numero + " emitida");
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Factura emitida exitosamente — " + serie + "-" + numero)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public FacturaResponseDto anularFactura(String facturaId, AnularFacturaDto request) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        entity.setEstado("ANULADA");
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = facturaMapper.toData(saved);
        notificationService.publicar(saved.getCompanyId(), "ANULADA", "FACTURA",
                saved.getId(), saved.getSerie() + "-" + saved.getNumero(),
                "Factura " + saved.getSerie() + "-" + saved.getNumero() + " anulada");
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Factura anulada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarFactura(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        // — SUNAT: solo se pueden eliminar borradores —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Factura", facturaId);
        facturaRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Factura eliminada exitosamente")
                .timestamp(new Date());
    }

    @Override
    public FacturaListResponseDto listarFacturas(String companyId, Date desdeFecha, Date hastaFecha,
                                                     String serie, Integer numero, String estado,
                                                     Integer page, Integer size) {
        List<FacturaEntity> entities = facturaRepository.findByCompanyId(companyId);
        List<FacturaData> dataList = entities.stream()
                .map(facturaMapper::toData)
                .toList();
        FacturaListResponseDtoAllOfData responseData = new FacturaListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        FacturaListResponseDto response = new FacturaListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public FacturaResponseDto obtenerFactura(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        FacturaData data = facturaMapper.toData(entity);
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}

