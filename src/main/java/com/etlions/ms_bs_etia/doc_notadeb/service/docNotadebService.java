package com.etlions.ms_bs_etia.doc_notadeb.service;

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
                                                   Integer page, Integer size);

    NotaDebitoResponseDto obtenerNotaDebito(String notaDebitoId);
}

