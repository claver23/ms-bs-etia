package com.etlions.ms_bs_etia.doc_boleta.service;

import com.etlions.openapicodegen.dto.BoletaResponseDto;
import com.etlions.openapicodegen.dto.AnularBoletaDto;
import com.etlions.openapicodegen.dto.BoletaEmitirDto;
import com.etlions.openapicodegen.dto.BoletaListResponseDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface DocBoletaService {

    BoletaResponseDto emitirBoleta(BoletaEmitirDto request);

    BoletaResponseDto anularBoleta(String boletaId, AnularBoletaDto request);

    SuccessResponse eliminarBoletaBorrador(String boletaId);

    BoletaListResponseDto listarBoletas(String companyId, Date desdeFecha, Date hastaFecha,
                                           String serie, Integer numero,
                                           Integer page, Integer size);

    BoletaResponseDto obtenerBoleta(String boletaId);
}

