package com.etlions.ms_bs_etia.libro_electronico.service;

import com.etlions.openapicodegen.dto.LibroElectronicoResponseDto;
import com.etlions.openapicodegen.dto.LibroGenerarDto;
import com.etlions.openapicodegen.dto.LibroElectronicoListResponseDto;

public interface LibroElectronicoService {

    LibroElectronicoResponseDto generarLibroElectronico(LibroGenerarDto request);

    LibroElectronicoListResponseDto listarLibrosElectronicos(String companyId, String tipo, String periodo, Integer page, Integer size);
}

