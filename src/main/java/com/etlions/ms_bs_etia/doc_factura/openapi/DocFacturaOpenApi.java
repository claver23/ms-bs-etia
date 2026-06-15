package com.etlions.ms_bs_etia.doc_factura.openapi;

import com.etlions.ms_bs_etia.doc_factura.service.DocFacturaService;
import com.etlions.openapicodegen.api.v1.FacturaApi;
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

    @Override
    public ResponseEntity<FacturaResponseDto> emitirFacturaBorrador(String facturaId) {
        return ResponseEntity.ok(docFacturaService.emitirFacturaBorrador(facturaId));
    }

    @Override
    public ResponseEntity<FacturaResponseDto> actualizarFacturaBorrador(String facturaId, FacturaEmitirDto facturaEmitirDto) {
        return ResponseEntity.ok(docFacturaService.actualizarBorrador(facturaId, facturaEmitirDto));
    }

    @Override
    public ResponseEntity<Resource> descargarFacturaXml(String facturaId) {
        DocFacturaService.FacturaXmlResult result = docFacturaService.descargarFacturaXml(facturaId);

        if (!result.hasContent()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(new ByteArrayResource(result.content()));
    }

    @Override
    public ResponseEntity<Resource> descargarFacturaPdf(String facturaId) {
        DocFacturaService.FacturaPdfResult result = docFacturaService.descargarFacturaPdf(facturaId);

        if (!result.hasContent()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(result.content().length)
                .body(new ByteArrayResource(result.content()));
    }

    @Override
    public ResponseEntity<DocumentValidationResultDto> validarFactura(String facturaId) {
        return ResponseEntity.ok(docFacturaService.validarFactura(facturaId));
    }
}

