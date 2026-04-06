package com.etlions.ms_bs_etia.doc_notacred.service;

import com.etlions.openapicodegen.dto.ActualizarNotaCreditoBorrador200Response;
import com.etlions.openapicodegen.dto.ListarNotasCredito200Response;
import com.etlions.openapicodegen.dto.NotaCreditoEmitirRequest;
import com.etlions.openapicodegen.dto.NotaCreditoUpdateRequest;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface docNotacredService {

    ActualizarNotaCreditoBorrador200Response emitirNotaCredito(NotaCreditoEmitirRequest request);

    ActualizarNotaCreditoBorrador200Response actualizarNotaCreditoBorrador(String notaCreditoId,
                                                                          NotaCreditoUpdateRequest request);

    SuccessResponse eliminarNotaCreditoBorrador(String notaCreditoId);

    ListarNotasCredito200Response listarNotasCredito(String companyId, String facturaReferenciaId,
                                                     Date desdeFecha, Date hastaFecha,
                                                     Integer page, Integer size);

    ActualizarNotaCreditoBorrador200Response obtenerNotaCredito(String notaCreditoId);
}
