package com.etlions.ms_bs_etia.doc_notadeb.service;

import com.etlions.openapicodegen.dto.DocumentValidationResultDto;
import com.etlions.openapicodegen.dto.NotaDebitoResponseDto;
import com.etlions.openapicodegen.dto.NotaDebitoListResponseDto;
import com.etlions.openapicodegen.dto.NotaDebitoEmitirDto;
import com.etlions.openapicodegen.dto.NotaDebitoUpdateDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface DocNotadebService {

    NotaDebitoResponseDto emitirNotaDebito(NotaDebitoEmitirDto request);

    NotaDebitoResponseDto actualizarNotaDebitoBorrador(String notaDebitoId,
                                                                        NotaDebitoUpdateDto request);

    SuccessResponse eliminarNotaDebitoBorrador(String notaDebitoId);

    NotaDebitoListResponseDto listarNotasDebito(String companyId, String facturaReferenciaId,
                                                   Date desdeFecha, Date hastaFecha,
                                                   String serie, String estado,
                                                   Integer page, Integer size);

    NotaDebitoResponseDto obtenerNotaDebito(String notaDebitoId);

    /** Ejecuta validación interna y actualiza estado a VALIDADO o CON_ERROR */
    DocumentValidationResultDto validarNotaDebito(String notaDebitoId);

    /** Emite una ND existente en estado BORRADOR/VALIDADO (genera XML + notifica) */
    NotaDebitoResponseDto emitirNotaDebitoBorrador(String notaDebitoId);

    /** Descarga el XML UBL 2.1 de una ND */
    NotaDebitoXmlResult descargarNotaDebitoXml(String notaDebitoId);

    /** Resultado de la generación XML */
    record NotaDebitoXmlResult(byte[] content, String filename, boolean hasContent) {}
}
