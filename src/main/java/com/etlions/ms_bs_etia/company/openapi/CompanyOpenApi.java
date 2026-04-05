package com.etlions.ms_bs_etia.company.openapi;

import com.etlions.ms_bs_etia.company.service.companyService;
import com.etlions.openapicodegen.api.v1.CompanyApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CompanyOpenApi implements CompanyApi {

    private final companyService companyService;

    @Override
    public ResponseEntity<ActualizarEmpresa200Response> registrarEmpresa(CompanyRegisterRequest companyRegisterRequest) {
        return ResponseEntity.ok(companyService.registrarEmpresa(companyRegisterRequest));
    }

    @Override
    public ResponseEntity<ActualizarEmpresa200Response> actualizarEmpresa(String companyId, CompanyUpdateRequest companyUpdateRequest) {
        return ResponseEntity.ok(companyService.actualizarEmpresa(companyId, companyUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarEmpresa(String companyId, Boolean softDelete) {
        return ResponseEntity.ok(companyService.eliminarEmpresa(companyId, softDelete));
    }

    @Override
    public ResponseEntity<ListarEmpresas200Response> listarEmpresas(Integer page, Integer size, String ruc, String estado) {
        return ResponseEntity.ok(companyService.listarEmpresas(page, size, ruc, estado));
    }
}
