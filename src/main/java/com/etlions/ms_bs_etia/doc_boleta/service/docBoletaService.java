package com.etlions.ms_bs_etia.doc_boleta.service;

import com.etlions.openapicodegen.dto.AnularBoleta200Response;
import com.etlions.openapicodegen.dto.AnularBoletaRequest;
import com.etlions.openapicodegen.dto.BoletaEmitirRequest;
import com.etlions.openapicodegen.dto.ListarBoletas200Response;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface docBoletaService {

    AnularBoleta200Response emitirBoleta(BoletaEmitirRequest request);

    AnularBoleta200Response anularBoleta(String boletaId, AnularBoletaRequest request);

    SuccessResponse eliminarBoletaBorrador(String boletaId);

    ListarBoletas200Response listarBoletas(String companyId, Date desdeFecha, Date hastaFecha,
                                           String serie, Integer numero,
                                           Integer page, Integer size);
}
