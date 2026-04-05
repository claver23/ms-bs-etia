package com.etlions.ms_bs_etia.doc_notadeb.openapi;

import com.etlions.ms_bs_etia.doc_notadeb.service.docNotadebService;
import com.etlions.openapicodegen.api.v1.NotaDebitoApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocNotadebOpenApi implements NotaDebitoApi {

    private final docNotadebService docNotadebService;

    @Override
    public ResponseEntity<ActualizarNotaDebitoBorrador200Response> emitirNotaDebito(NotaDebitoEmitirRequest notaDebitoEmitirRequest) {
        return ResponseEntity.ok(docNotadebService.emitirNotaDebito(notaDebitoEmitirRequest));
    }

    @Override
    public ResponseEntity<ActualizarNotaDebitoBorrador200Response> actualizarNotaDebitoBorrador(String notaDebitoId, NotaDebitoUpdateRequest notaDebitoUpdateRequest) {
        return ResponseEntity.ok(docNotadebService.actualizarNotaDebitoBorrador(notaDebitoId, notaDebitoUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarNotaDebitoBorrador(String notaDebitoId) {
        return ResponseEntity.ok(docNotadebService.eliminarNotaDebitoBorrador(notaDebitoId));
    }

    @Override
    public ResponseEntity<ListarNotasDebito200Response> listarNotasDebito(String companyId, String facturaReferenciaId, Date desdeFecha, Date hastaFecha, Integer page, Integer size) {
        return ResponseEntity.ok(docNotadebService.listarNotasDebito(companyId, facturaReferenciaId, desdeFecha, hastaFecha, page, size));
    }
}
