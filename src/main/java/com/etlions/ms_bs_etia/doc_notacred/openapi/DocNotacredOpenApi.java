package com.etlions.ms_bs_etia.doc_notacred.openapi;

import com.etlions.ms_bs_etia.doc_notacred.service.DocNotacredService;
import com.etlions.openapicodegen.api.v1.NotaCreditoApi;
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
public class DocNotacredOpenApi implements NotaCreditoApi {

    private final DocNotacredService docNotacredService;

    @Override
    public ResponseEntity<NotaCreditoResponseDto> emitirNotaCredito(NotaCreditoEmitirDto notaCreditoEmitirRequest) {
        return ResponseEntity.ok(docNotacredService.emitirNotaCredito(notaCreditoEmitirRequest));
    }

    @Override
    public ResponseEntity<NotaCreditoResponseDto> actualizarNotaCreditoBorrador(String notaCreditoId, NotaCreditoUpdateDto notaCreditoUpdateRequest) {
        return ResponseEntity.ok(docNotacredService.actualizarNotaCreditoBorrador(notaCreditoId, notaCreditoUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarNotaCreditoBorrador(String notaCreditoId) {
        return ResponseEntity.ok(docNotacredService.eliminarNotaCreditoBorrador(notaCreditoId));
    }

    @Override
    public ResponseEntity<NotaCreditoListResponseDto> listarNotasCredito(String companyId, String facturaReferenciaId, Date desdeFecha, Date hastaFecha, String serie, String estado, Integer page, Integer size) {
        return ResponseEntity.ok(docNotacredService.listarNotasCredito(companyId, facturaReferenciaId, desdeFecha, hastaFecha, serie, estado, page, size));
    }

    @Override
    public ResponseEntity<NotaCreditoResponseDto> obtenerNotaCredito(String notaCreditoId) {
        return ResponseEntity.ok(docNotacredService.obtenerNotaCredito(notaCreditoId));
    }

    @Override
    public ResponseEntity<DocumentValidationResultDto> validarNotaCredito(String notaCreditoId) {
        return ResponseEntity.ok(docNotacredService.validarNotaCredito(notaCreditoId));
    }

    @Override
    public ResponseEntity<NotaCreditoResponseDto> emitirNotaCreditoBorrador(String notaCreditoId) {
        return ResponseEntity.ok(docNotacredService.emitirNotaCreditoBorrador(notaCreditoId));
    }

    @Override
    public ResponseEntity<Resource> descargarNotaCreditoXml(String notaCreditoId) {
        DocNotacredService.NotaCreditoXmlResult result = docNotacredService.descargarNotaCreditoXml(notaCreditoId);
        if (!result.hasContent()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(new ByteArrayResource(result.content()));
    }
}
