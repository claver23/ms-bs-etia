package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
import com.etlions.ms_bs_etia.doc_factura.service.docFacturaService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class docFacturaServiceImpl implements docFacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;

    @Override
    public AnularFactura200Response emitirFactura(FacturaEmitirRequest request) {
        FacturaEntity entity = facturaMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = facturaMapper.toData(saved);
        return new AnularFactura200Response()
                .isSuccess(true)
                .status("OK")
                .message("Factura emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public AnularFactura200Response anularFactura(String facturaId, AnularFacturaRequest request) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + facturaId));
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
    public SuccessResponse eliminarFactura(String facturaId) {
        facturaRepository.deleteById(facturaId);
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
}
