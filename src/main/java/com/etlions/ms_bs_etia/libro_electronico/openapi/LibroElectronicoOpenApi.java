package com.etlions.ms_bs_etia.libro_electronico.openapi;

import com.etlions.ms_bs_etia.libro_electronico.service.LibroElectronicoService;
import com.etlions.openapicodegen.api.v1.LibroElectronicoApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LibroElectronicoOpenApi implements LibroElectronicoApi {

    private final LibroElectronicoService libroElectronicoService;

    @Override
    public ResponseEntity<LibroElectronicoResponseDto> generarLibroElectronico(LibroGenerarDto libroGenerarRequest) {
        return ResponseEntity.ok(libroElectronicoService.generarLibroElectronico(libroGenerarRequest));
    }

    @Override
    public ResponseEntity<LibroElectronicoListResponseDto> listarLibrosElectronicos(String companyId, String tipo, String periodo, Integer page, Integer size) {
        return ResponseEntity.ok(libroElectronicoService.listarLibrosElectronicos(companyId, tipo, periodo, page, size));
    }
}

