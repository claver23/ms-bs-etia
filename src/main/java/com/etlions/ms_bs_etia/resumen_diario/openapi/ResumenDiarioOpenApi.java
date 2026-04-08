package com.etlions.ms_bs_etia.resumen_diario.openapi;

import com.etlions.ms_bs_etia.resumen_diario.service.ResumenDiarioService;
import com.etlions.openapicodegen.api.v1.ResumenDiarioApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResumenDiarioOpenApi implements ResumenDiarioApi {

    private final ResumenDiarioService resumenDiarioService;

    @Override
    public ResponseEntity<ResumenDiarioResponseDto> crearResumenDiario(ResumenDiarioCrearDto request) {
        return ResponseEntity.ok(resumenDiarioService.crearResumenDiario(request));
    }

    @Override
    public ResponseEntity<ResumenDiarioListResponseDto> listarResumenesDiarios(String companyId, Integer page, Integer size) {
        return ResponseEntity.ok(resumenDiarioService.listarResumenesDiarios(companyId, page, size));
    }

    @Override
    public ResponseEntity<ResumenDiarioResponseDto> obtenerResumenDiario(String resumenId) {
        return ResponseEntity.ok(resumenDiarioService.obtenerResumenDiario(resumenId));
    }

    @Override
    public ResponseEntity<ConsultaEstadoResultDto> consultarEstadoResumen(String resumenId) {
        return ResponseEntity.ok(resumenDiarioService.consultarEstadoResumen(resumenId));
    }
}

