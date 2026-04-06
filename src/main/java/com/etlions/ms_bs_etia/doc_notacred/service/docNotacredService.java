package com.etlions.ms_bs_etia.doc_notacred.service;

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
                                                     Integer page, Integer size);

    NotaCreditoResponseDto obtenerNotaCredito(String notaCreditoId);
}

