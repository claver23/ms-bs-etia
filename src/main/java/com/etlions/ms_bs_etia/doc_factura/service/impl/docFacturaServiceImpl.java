package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.SunatValidator;
import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
import com.etlions.ms_bs_etia.doc_factura.service.docFacturaService;
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
public class docFacturaServiceImpl implements docFacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;
    private final CorrelativoService correlativoService;

    private static final String DEFAULT_SERIE_FACTURA = "F001";

    @Override
    @Transactional
    public AnularFactura200Response emitirFactura(FacturaEmitirRequest request) {
        // — Validaciones SUNAT —
        String serie = request.getSerie() != null ? request.getSerie().toUpperCase() : DEFAULT_SERIE_FACTURA;
        SunatValidator.validarFormatoSerie(serie);
        SunatValidator.validarSerieFactura(serie);
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal());

        // — Correlativo automático (ignora numero del request) —
        int numero = correlativoService.siguienteNumero(request.getCompanyId(), serie);

        FacturaEntity entity = facturaMapper.toEntity(request);
        entity.setSerie(serie);
        entity.setNumero(numero);
        entity.setEstado("BORRADOR");
        if (entity.getFechaEmision() == null) {
            entity.setFechaEmision(LocalDate.now());
        }
        entity.setFechaCreacion(LocalDateTime.now());
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = facturaMapper.toData(saved);
        return new AnularFactura200Response()
                .isSuccess(true)
                .status("OK")
                .message("Factura emitida exitosamente — " + serie + "-" + numero)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public AnularFactura200Response anularFactura(String facturaId, AnularFacturaRequest request) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        entity.setEstado("ANULADA");
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = facturaMapper.toData(saved);
        return new AnularFactura200Response()
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
    public ListarFacturas200Response listarFacturas(String companyId, Date desdeFecha, Date hastaFecha,
                                                     String serie, Integer numero, String estado,
                                                     Integer page, Integer size) {
        List<FacturaEntity> entities = facturaRepository.findByCompanyId(companyId);
        List<FacturaData> dataList = entities.stream()
                .map(facturaMapper::toData)
                .toList();
        ListarFacturas200ResponseAllOfData responseData = new ListarFacturas200ResponseAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        ListarFacturas200Response response = new ListarFacturas200Response();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public AnularFactura200Response obtenerFactura(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        FacturaData data = facturaMapper.toData(entity);
        return new AnularFactura200Response()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}
