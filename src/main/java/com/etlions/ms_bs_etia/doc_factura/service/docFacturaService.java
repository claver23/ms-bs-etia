package com.etlions.ms_bs_etia.doc_factura.service;

import com.etlions.openapicodegen.dto.FacturaResponseDto;
import com.etlions.openapicodegen.dto.AnularFacturaDto;
import com.etlions.openapicodegen.dto.DocumentValidationResultDto;
import com.etlions.openapicodegen.dto.FacturaEmitirDto;
import com.etlions.openapicodegen.dto.FacturaListResponseDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface DocFacturaService {

    FacturaResponseDto emitirFactura(FacturaEmitirDto request);

    /** Emite una factura existente en estado BORRADOR o VALIDADO (genera XML + notifica) */
    FacturaResponseDto emitirFacturaBorrador(String facturaId);

    /** Actualiza los datos de una factura en estado BORRADOR, CON_ERROR o RECHAZADA sin emitirla */
    FacturaResponseDto actualizarBorrador(String facturaId, FacturaEmitirDto request);

    FacturaResponseDto anularFactura(String facturaId, AnularFacturaDto request);

    SuccessResponse eliminarFactura(String facturaId);

    FacturaListResponseDto listarFacturas(String companyId, Date desdeFecha, Date hastaFecha,
                                             String serie, Integer numero, String estado,
                                             Integer page, Integer size);

    FacturaResponseDto obtenerFactura(String facturaId);

    /** Genera y devuelve el XML UBL 2.1 de una factura */
    FacturaXmlResult descargarFacturaXml(String facturaId);

    /** Ejecuta validación interna de una factura y actualiza su estado a VALIDADO o CON_ERROR */
    DocumentValidationResultDto validarFactura(String facturaId);

    /** Genera y devuelve el PDF de representación impresa de una factura */
    FacturaPdfResult descargarFacturaPdf(String facturaId);

    /** Resultado de la generación XML */
    record FacturaXmlResult(byte[] content, String filename, boolean hasContent) {}
    
    /** Resultado de la generación PDF */
    record FacturaPdfResult(byte[] content, String filename, boolean hasContent) {}
}

