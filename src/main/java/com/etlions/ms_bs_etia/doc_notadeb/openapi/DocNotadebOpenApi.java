package com.etlions.ms_bs_etia.doc_notadeb.openapi;

import com.etlions.ms_bs_etia.doc_notadeb.service.DocNotadebService;
import com.etlions.openapicodegen.api.v1.NotaDebitoApi;
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
public class DocNotadebOpenApi implements NotaDebitoApi {

    private final DocNotadebService docNotadebService;

    @Override
    public ResponseEntity<NotaDebitoResponseDto> emitirNotaDebito(NotaDebitoEmitirDto notaDebitoEmitirRequest) {
        return ResponseEntity.ok(docNotadebService.emitirNotaDebito(notaDebitoEmitirRequest));
    }

    @Override
    public ResponseEntity<NotaDebitoResponseDto> actualizarNotaDebitoBorrador(String notaDebitoId, NotaDebitoUpdateDto notaDebitoUpdateRequest) {
        return ResponseEntity.ok(docNotadebService.actualizarNotaDebitoBorrador(notaDebitoId, notaDebitoUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarNotaDebitoBorrador(String notaDebitoId) {
        return ResponseEntity.ok(docNotadebService.eliminarNotaDebitoBorrador(notaDebitoId));
    }

    @Override
    public ResponseEntity<NotaDebitoListResponseDto> listarNotasDebito(String companyId, String facturaReferenciaId, Date desdeFecha, Date hastaFecha, String serie, String estado, Integer page, Integer size) {
        return ResponseEntity.ok(docNotadebService.listarNotasDebito(companyId, facturaReferenciaId, desdeFecha, hastaFecha, serie, estado, page, size));
    }

    @Override
    public ResponseEntity<NotaDebitoResponseDto> obtenerNotaDebito(String notaDebitoId) {
        return ResponseEntity.ok(docNotadebService.obtenerNotaDebito(notaDebitoId));
    }

    @Override
    public ResponseEntity<DocumentValidationResultDto> validarNotaDebito(String notaDebitoId) {
        return ResponseEntity.ok(docNotadebService.validarNotaDebito(notaDebitoId));
    }

    @Override
    public ResponseEntity<NotaDebitoResponseDto> emitirNotaDebitoBorrador(String notaDebitoId) {
        return ResponseEntity.ok(docNotadebService.emitirNotaDebitoBorrador(notaDebitoId));
    }

    @Override
    public ResponseEntity<Resource> descargarNotaDebitoXml(String notaDebitoId) {
        DocNotadebService.NotaDebitoXmlResult result = docNotadebService.descargarNotaDebitoXml(notaDebitoId);
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
