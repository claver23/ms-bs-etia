package com.etlions.ms_bs_etia.tabla_sunat.service;

import com.etlions.openapicodegen.dto.TablaSunatMenuItem;
import com.etlions.openapicodegen.dto.TablaSunatResponseDto;

import java.util.List;

public interface TablaSunatService {

    List<TablaSunatMenuItem> listarMenu();

    TablaSunatResponseDto obtenerTabla(String nombreTabla);
}

