package com.etlions.ms_bs_etia.tabla_sunat.openapi;

import com.etlions.ms_bs_etia.tabla_sunat.service.TablaSunatService;
import com.etlions.openapicodegen.api.v1.TablaSunatApi;
import com.etlions.openapicodegen.dto.TablaSunatMenuItem;
import com.etlions.openapicodegen.dto.TablaSunatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TablaSunatOpenApi implements TablaSunatApi {

    private final TablaSunatService tablaSunatService;

    @Override
    public ResponseEntity<List<TablaSunatMenuItem>> listarMenuTablasSunat() {
        return ResponseEntity.ok(tablaSunatService.listarMenu());
    }

    @Override
    public ResponseEntity<TablaSunatResponseDto> obtenerTablaSunat(String nombreTabla) {
        return ResponseEntity.ok(tablaSunatService.obtenerTabla(nombreTabla));
    }
}

