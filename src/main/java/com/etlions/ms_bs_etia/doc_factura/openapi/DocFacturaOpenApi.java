package com.etlions.ms_bs_etia.doc_factura.openapi;

import com.etlions.ms_bs_etia.doc_factura.service.DocFacturaService;
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

    private final DocFacturaService docFacturaService;

    @Override
    public ResponseEntity<FacturaResponseDto> emitirFactura(FacturaEmitirDto facturaEmitirRequest) {
        return ResponseEntity.ok(docFacturaService.emitirFactura(facturaEmitirRequest));
    }

    @Override
    public ResponseEntity<FacturaResponseDto> anularFactura(String facturaId, AnularFacturaDto anularFacturaRequest) {
        return ResponseEntity.ok(docFacturaService.anularFactura(facturaId, anularFacturaRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarFactura(String facturaId) {
        return ResponseEntity.ok(docFacturaService.eliminarFactura(facturaId));
    }

    @Override
    public ResponseEntity<FacturaListResponseDto> listarFacturas(String companyId, Date desdeFecha, Date hastaFecha, String serie, Integer numero, String estado, Integer page, Integer size) {
        return ResponseEntity.ok(docFacturaService.listarFacturas(companyId, desdeFecha, hastaFecha, serie, numero, estado, page, size));
    }

    @Override
    public ResponseEntity<FacturaResponseDto> obtenerFactura(String facturaId) {
        return ResponseEntity.ok(docFacturaService.obtenerFactura(facturaId));
    }
}

