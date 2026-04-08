package com.etlions.ms_bs_etia.doc_boleta.openapi;

import com.etlions.ms_bs_etia.doc_boleta.service.DocBoletaService;
import com.etlions.openapicodegen.api.v1.BoletaApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @Override
    public ResponseEntity<BoletaResponseDto> actualizarBoletaBorrador(String boletaId, BoletaEmitirDto boletaEmitirDto) {
        return ResponseEntity.ok(docBoletaService.actualizarBoletaBorrador(boletaId, boletaEmitirDto));
    }

    @Override
    public ResponseEntity<Resource> descargarBoletaXml(String boletaId) {
        var result = docBoletaService.descargarBoletaXml(boletaId);
        if (!result.hasContent()) {
            return ResponseEntity.noContent().build();
        }
        ByteArrayResource resource = new ByteArrayResource(result.content());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .contentLength(result.content().length)
                .body(resource);
    }
}

