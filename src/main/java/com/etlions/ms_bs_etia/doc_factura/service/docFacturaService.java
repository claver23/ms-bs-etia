package com.etlions.ms_bs_etia.doc_factura.service;

import com.etlions.openapicodegen.dto.FacturaResponseDto;
import com.etlions.openapicodegen.dto.AnularFacturaDto;
import com.etlions.openapicodegen.dto.FacturaEmitirDto;
import com.etlions.openapicodegen.dto.FacturaListResponseDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface DocFacturaService {

    FacturaResponseDto emitirFactura(FacturaEmitirDto request);

    FacturaResponseDto anularFactura(String facturaId, AnularFacturaDto request);

    SuccessResponse eliminarFactura(String facturaId);

    FacturaListResponseDto listarFacturas(String companyId, Date desdeFecha, Date hastaFecha,
                                             String serie, Integer numero, String estado,
                                             Integer page, Integer size);

    FacturaResponseDto obtenerFactura(String facturaId);

    /**
     * Obtiene el XML UBL 2.1 de una factura.
     *
     * @param facturaId ID de la factura
     * @return record con bytes, filename y flag de contenido presente;
     *         null bytes si la factura aún no tiene XML generado
     * @throws com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException si la factura no existe
     */
    FacturaXmlResult descargarFacturaXml(String facturaId);

    /** DTO inmutable para el resultado de descarga XML */
    record FacturaXmlResult(byte[] content, String filename, boolean hasContent) {}
}

