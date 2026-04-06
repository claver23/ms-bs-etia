package com.etlions.ms_bs_etia.doc_factura.openapi;

import com.etlions.ms_bs_etia.doc_factura.service.docFacturaService;
import com.etlions.openapicodegen.api.v1.FacturaApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocFacturaOpenApi implements FacturaApi {

    private final docFacturaService docFacturaService;

    @Override
    public ResponseEntity<AnularFactura200Response> emitirFactura(FacturaEmitirRequest facturaEmitirRequest) {
        return ResponseEntity.ok(docFacturaService.emitirFactura(facturaEmitirRequest));
    }

    @Override
    public ResponseEntity<AnularFactura200Response> anularFactura(String facturaId, AnularFacturaRequest anularFacturaRequest) {
        return ResponseEntity.ok(docFacturaService.anularFactura(facturaId, anularFacturaRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarFactura(String facturaId) {
        return ResponseEntity.ok(docFacturaService.eliminarFactura(facturaId));
    }

    @Override
    public ResponseEntity<ListarFacturas200Response> listarFacturas(String companyId, Date desdeFecha, Date hastaFecha, String serie, Integer numero, String estado, Integer page, Integer size) {
        return ResponseEntity.ok(docFacturaService.listarFacturas(companyId, desdeFecha, hastaFecha, serie, numero, estado, page, size));
    }

    @Override
    public ResponseEntity<AnularFactura200Response> obtenerFactura(String facturaId) {
        return ResponseEntity.ok(docFacturaService.obtenerFactura(facturaId));
    }
}
