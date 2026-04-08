package com.etlions.ms_bs_etia.doc_boleta.openapi;

import com.etlions.ms_bs_etia.doc_boleta.service.DocBoletaService;
import com.etlions.openapicodegen.api.v1.BoletaApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocBoletaOpenApi implements BoletaApi {

    private final DocBoletaService docBoletaService;

    @Override
    public ResponseEntity<BoletaResponseDto> emitirBoleta(BoletaEmitirDto boletaEmitirRequest) {
        return ResponseEntity.ok(docBoletaService.emitirBoleta(boletaEmitirRequest));
    }

    @Override
    public ResponseEntity<BoletaResponseDto> anularBoleta(String boletaId, AnularBoletaDto anularBoletaRequest) {
        return ResponseEntity.ok(docBoletaService.anularBoleta(boletaId, anularBoletaRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarBoletaBorrador(String boletaId) {
        return ResponseEntity.ok(docBoletaService.eliminarBoletaBorrador(boletaId));
    }

    @Override
    public ResponseEntity<BoletaListResponseDto> listarBoletas(String companyId, Date desdeFecha, Date hastaFecha, String serie, Integer numero, Integer page, Integer size) {
        return ResponseEntity.ok(docBoletaService.listarBoletas(companyId, desdeFecha, hastaFecha, serie, numero, page, size));
    }

    @Override
    public ResponseEntity<BoletaResponseDto> obtenerBoleta(String boletaId) {
        return ResponseEntity.ok(docBoletaService.obtenerBoleta(boletaId));
    }

    @Override
    public ResponseEntity<DocumentValidationResultDto> validarBoleta(String boletaId) {
        return ResponseEntity.ok(docBoletaService.validarBoleta(boletaId));
    }
}

