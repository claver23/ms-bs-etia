package com.etlions.ms_bs_etia.company.openapi;

import com.etlions.ms_bs_etia.company.service.CompanyService;
import com.etlions.openapicodegen.api.v1.CompanyApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CompanyOpenApi implements CompanyApi {

    private final CompanyService companyService;

    @Override
    public ResponseEntity<CompanyResponseDto> registrarEmpresa(CompanyRegisterDto companyRegisterRequest) {
        return ResponseEntity.ok(companyService.registrarEmpresa(companyRegisterRequest));
    }

    @Override
    public ResponseEntity<CompanyResponseDto> actualizarEmpresa(String companyId, CompanyUpdateDto companyUpdateRequest) {
        return ResponseEntity.ok(companyService.actualizarEmpresa(companyId, companyUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarEmpresa(String companyId, Boolean softDelete) {
        return ResponseEntity.ok(companyService.eliminarEmpresa(companyId, softDelete));
    }

    @Override
    public ResponseEntity<CompanyListResponseDto> listarEmpresas(Integer page, Integer size, String ruc, String estado) {
        return ResponseEntity.ok(companyService.listarEmpresas(page, size, ruc, estado));
    }

    @Override
    public ResponseEntity<CompanyResponseDto> subirCertificado(String companyId, MultipartFile archivo) {
        return ResponseEntity.ok(companyService.subirCertificado(companyId, archivo));
    }

    @Override
    public ResponseEntity<ConexionSunatResultDto> probarConexionSunat(String companyId) {
        return ResponseEntity.ok(companyService.probarConexionSunat(companyId));
    }

    @Override
    public ResponseEntity<CompanyResponseDto> subirLogo(String companyId, MultipartFile archivo) {
        return ResponseEntity.ok(companyService.subirLogo(companyId, archivo));
    }

    @Override
    public ResponseEntity<Resource> obtenerLogo(String companyId) {
        Optional<byte[]> logoBytes = companyService.obtenerLogo(companyId);
        if (logoBytes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        String contentType = companyService.getLogoContentType(companyId);
        ByteArrayResource resource = new ByteArrayResource(logoBytes.get());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }
}

