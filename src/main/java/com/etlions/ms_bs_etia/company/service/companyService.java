package com.etlions.ms_bs_etia.company.service;

import com.etlions.openapicodegen.dto.CompanyResponseDto;
import com.etlions.openapicodegen.dto.CompanyRegisterDto;
import com.etlions.openapicodegen.dto.CompanyUpdateDto;
import com.etlions.openapicodegen.dto.CompanyListResponseDto;
import com.etlions.openapicodegen.dto.ConexionSunatResultDto;
import com.etlions.openapicodegen.dto.SuccessResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyService {

    CompanyResponseDto registrarEmpresa(CompanyRegisterDto request);

    CompanyResponseDto actualizarEmpresa(String companyId, CompanyUpdateDto request);

    SuccessResponse eliminarEmpresa(String companyId, Boolean softDelete);

    CompanyListResponseDto listarEmpresas(Integer page, Integer size, String ruc, String estado);

    CompanyResponseDto subirCertificado(String companyId, MultipartFile archivo);

    ConexionSunatResultDto probarConexionSunat(String companyId);

    CompanyResponseDto subirLogo(String companyId, MultipartFile archivo);

    /** Returns logo bytes, or empty if no logo */
    java.util.Optional<byte[]> obtenerLogo(String companyId);

    /** Returns content type of the logo */
    String getLogoContentType(String companyId);
}

