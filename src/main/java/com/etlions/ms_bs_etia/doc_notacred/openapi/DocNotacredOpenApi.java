package com.etlions.ms_bs_etia.doc_notacred.openapi;

import com.etlions.ms_bs_etia.doc_notacred.service.docNotacredService;
import com.etlions.openapicodegen.api.v1.NotaCreditoApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocNotacredOpenApi implements NotaCreditoApi {

    private final docNotacredService docNotacredService;

    @Override
    public ResponseEntity<ActualizarNotaCreditoBorrador200Response> emitirNotaCredito(NotaCreditoEmitirRequest notaCreditoEmitirRequest) {
        return ResponseEntity.ok(docNotacredService.emitirNotaCredito(notaCreditoEmitirRequest));
    }

    @Override
    public ResponseEntity<ActualizarNotaCreditoBorrador200Response> actualizarNotaCreditoBorrador(String notaCreditoId, NotaCreditoUpdateRequest notaCreditoUpdateRequest) {
        return ResponseEntity.ok(docNotacredService.actualizarNotaCreditoBorrador(notaCreditoId, notaCreditoUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarNotaCreditoBorrador(String notaCreditoId) {
        return ResponseEntity.ok(docNotacredService.eliminarNotaCreditoBorrador(notaCreditoId));
    }

    @Override
    public ResponseEntity<ListarNotasCredito200Response> listarNotasCredito(String companyId, String facturaReferenciaId, Date desdeFecha, Date hastaFecha, Integer page, Integer size) {
        return ResponseEntity.ok(docNotacredService.listarNotasCredito(companyId, facturaReferenciaId, desdeFecha, hastaFecha, page, size));
    }
}
