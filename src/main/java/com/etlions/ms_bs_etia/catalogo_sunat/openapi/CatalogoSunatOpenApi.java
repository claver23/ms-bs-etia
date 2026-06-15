package com.etlions.ms_bs_etia.catalogo_sunat.openapi;

import com.etlions.ms_bs_etia.catalogo_sunat.service.CatalogoSunatService;
import com.etlions.openapicodegen.api.v1.CatalogoSunatApi;
import com.etlions.openapicodegen.dto.CatalogoSunatMenuItem;
import com.etlions.openapicodegen.dto.CatalogoSunatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogoSunatOpenApi implements CatalogoSunatApi {

    private final CatalogoSunatService catalogoSunatService;

    @Override
    public ResponseEntity<List<CatalogoSunatMenuItem>> listarMenuCatalogosSunat() {
        return ResponseEntity.ok(catalogoSunatService.listarMenu());
    }

    @Override
    public ResponseEntity<CatalogoSunatResponseDto> obtenerCatalogoSunat(String nombreCatalogo) {
        return ResponseEntity.ok(catalogoSunatService.obtenerCatalogo(nombreCatalogo));
    }
}

