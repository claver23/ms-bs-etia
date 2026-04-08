package com.etlions.ms_bs_etia.resumen_diario.service;

import com.etlions.openapicodegen.dto.*;

public interface ResumenDiarioService {

    ResumenDiarioResponseDto crearResumenDiario(ResumenDiarioCrearDto request);

    ResumenDiarioListResponseDto listarResumenesDiarios(String companyId, Integer page, Integer size);

    ResumenDiarioResponseDto obtenerResumenDiario(String resumenId);

    ConsultaEstadoResultDto consultarEstadoResumen(String resumenId);
}

