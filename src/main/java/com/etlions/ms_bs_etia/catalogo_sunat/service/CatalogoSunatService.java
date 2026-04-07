package com.etlions.ms_bs_etia.catalogo_sunat.service;

import com.etlions.openapicodegen.dto.CatalogoSunatMenuItem;
import com.etlions.openapicodegen.dto.CatalogoSunatResponseDto;

import java.util.List;

public interface CatalogoSunatService {

    List<CatalogoSunatMenuItem> listarMenu();

    CatalogoSunatResponseDto obtenerCatalogo(String nombreCatalogo);
}

