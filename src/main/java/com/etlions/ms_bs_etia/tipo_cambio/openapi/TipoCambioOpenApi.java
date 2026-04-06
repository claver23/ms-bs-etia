package com.etlions.ms_bs_etia.tipo_cambio.openapi;

import com.etlions.ms_bs_etia.tipo_cambio.service.TipoCambioService;
import com.etlions.openapicodegen.api.v1.TipoCambioApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TipoCambioOpenApi implements TipoCambioApi {

    private final TipoCambioService tipoCambioService;

    @Override
    public ResponseEntity<TipoCambioResponseDto> obtenerTipoCambio(Date fecha, String moneda, String fuente) {
        return ResponseEntity.ok(tipoCambioService.obtenerTipoCambio(fecha, moneda, fuente));
    }

    @Override
    public ResponseEntity<TipoCambioListResponseDto> listarTiposCambio(String fuente, Integer page, Integer size) {
        return ResponseEntity.ok(tipoCambioService.listarTiposCambio(fuente, page, size));
    }

    @Override
    public ResponseEntity<TipoCambioResponseDto> registrarTipoCambio(TipoCambioCreateDto tipoCambioCreateRequest) {
        return ResponseEntity.ok(tipoCambioService.registrarTipoCambio(tipoCambioCreateRequest));
    }
}

