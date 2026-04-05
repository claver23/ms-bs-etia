package com.etlions.ms_bs_etia.company.service;

import com.etlions.openapicodegen.dto.ActualizarEmpresa200Response;
import com.etlions.openapicodegen.dto.CompanyRegisterRequest;
import com.etlions.openapicodegen.dto.CompanyUpdateRequest;
import com.etlions.openapicodegen.dto.ListarEmpresas200Response;
import com.etlions.openapicodegen.dto.SuccessResponse;

public interface companyService {

    ActualizarEmpresa200Response registrarEmpresa(CompanyRegisterRequest request);

    ActualizarEmpresa200Response actualizarEmpresa(String companyId, CompanyUpdateRequest request);

    SuccessResponse eliminarEmpresa(String companyId, Boolean softDelete);

    ListarEmpresas200Response listarEmpresas(Integer page, Integer size, String ruc, String estado);
}
