package com.etlions.ms_bs_etia.doc_notacred.service;

import com.etlions.openapicodegen.dto.DocumentValidationResultDto;
import com.etlions.openapicodegen.dto.NotaCreditoResponseDto;
import com.etlions.openapicodegen.dto.NotaCreditoListResponseDto;
import com.etlions.openapicodegen.dto.NotaCreditoEmitirDto;
import com.etlions.openapicodegen.dto.NotaCreditoUpdateDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface DocNotacredService {

    NotaCreditoResponseDto emitirNotaCredito(NotaCreditoEmitirDto request);

    NotaCreditoResponseDto actualizarNotaCreditoBorrador(String notaCreditoId,
                                                                          NotaCreditoUpdateDto request);

    SuccessResponse eliminarNotaCreditoBorrador(String notaCreditoId);

    NotaCreditoListResponseDto listarNotasCredito(String companyId, String facturaReferenciaId,
                                                     Date desdeFecha, Date hastaFecha,
                                                     String serie, String estado,
                                                     Integer page, Integer size);

    NotaCreditoResponseDto obtenerNotaCredito(String notaCreditoId);

    /** Ejecuta validación interna y actualiza estado a VALIDADO o CON_ERROR */
    DocumentValidationResultDto validarNotaCredito(String notaCreditoId);

    /** Emite una NC existente en estado BORRADOR/VALIDADO (genera XML + notifica) */
    NotaCreditoResponseDto emitirNotaCreditoBorrador(String notaCreditoId);

    /** Descarga el XML UBL 2.1 de una NC */
    NotaCreditoXmlResult descargarNotaCreditoXml(String notaCreditoId);

    /** Resultado de la generación XML */
    record NotaCreditoXmlResult(byte[] content, String filename, boolean hasContent) {}
}
