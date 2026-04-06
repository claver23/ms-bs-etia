package com.etlions.ms_bs_etia.tipo_cambio.service;

import com.etlions.openapicodegen.dto.TipoCambioListResponseDto;
import com.etlions.openapicodegen.dto.TipoCambioResponseDto;
import com.etlions.openapicodegen.dto.TipoCambioCreateDto;

import java.util.Date;

public interface TipoCambioService {

    TipoCambioResponseDto obtenerTipoCambio(Date fecha, String moneda, String fuente);

    TipoCambioListResponseDto listarTiposCambio(String fuente, Integer page, Integer size);

    TipoCambioResponseDto registrarTipoCambio(TipoCambioCreateDto request);
}

