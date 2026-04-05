package com.etlions.ms_bs_etia.doc_notadeb.service;

import com.etlions.openapicodegen.dto.ActualizarNotaDebitoBorrador200Response;
import com.etlions.openapicodegen.dto.ListarNotasDebito200Response;
import com.etlions.openapicodegen.dto.NotaDebitoEmitirRequest;
import com.etlions.openapicodegen.dto.NotaDebitoUpdateRequest;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface docNotadebService {

    ActualizarNotaDebitoBorrador200Response emitirNotaDebito(NotaDebitoEmitirRequest request);

    ActualizarNotaDebitoBorrador200Response actualizarNotaDebitoBorrador(String notaDebitoId,
                                                                        NotaDebitoUpdateRequest request);

    SuccessResponse eliminarNotaDebitoBorrador(String notaDebitoId);

    ListarNotasDebito200Response listarNotasDebito(String companyId, String facturaReferenciaId,
                                                   Date desdeFecha, Date hastaFecha,
                                                   Integer page, Integer size);
}
