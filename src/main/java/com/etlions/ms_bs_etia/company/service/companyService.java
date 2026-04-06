package com.etlions.ms_bs_etia.company.service;

import com.etlions.openapicodegen.dto.CompanyResponseDto;
import com.etlions.openapicodegen.dto.CompanyRegisterDto;
import com.etlions.openapicodegen.dto.CompanyUpdateDto;
import com.etlions.openapicodegen.dto.CompanyListResponseDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

public interface CompanyService {

    CompanyResponseDto registrarEmpresa(CompanyRegisterDto request);

    CompanyResponseDto actualizarEmpresa(String companyId, CompanyUpdateDto request);

    SuccessResponse eliminarEmpresa(String companyId, Boolean softDelete);

    CompanyListResponseDto listarEmpresas(Integer page, Integer size, String ruc, String estado);
}

