package com.etlions.ms_bs_etia.doc_factura.openapi;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.service.PdfGeneratorService;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoint para descarga de representación impresa (PDF) de facturas.
 * Separado del controller OpenAPI para evitar dependencias de codegen.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FacturaPdfController {

    private final FacturaRepository facturaRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final PdfGeneratorService pdfGeneratorService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping("/factura/{facturaId}/pdf")
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable String facturaId) {
        FacturaEntity factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));

        CompanyEntity company = companyRepository.findById(factura.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", factura.getCompanyId()));

        ClientEntity client = clientRepository.findById(factura.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", factura.getClientId()));

        // Parsear detalleJson
        List<Map<String, Object>> detalle = parseDetalleJson(factura.getDetalleJson());

        if (detalle == null || detalle.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        byte[] pdfBytes = pdfGeneratorService.generateFacturaPdf(factura, company, client, detalle);

        String filename = String.format("%s-%08d.pdf",
                factura.getSerie() != null ? factura.getSerie() : "F001",
                factura.getNumero() != null ? factura.getNumero() : 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    private List<Map<String, Object>> parseDetalleJson(String detalleJson) {
        if (detalleJson == null || detalleJson.isBlank()) return List.of();
        try {
            return OBJECT_MAPPER.readValue(detalleJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Error parseando detalleJson: {}", e.getMessage());
            return List.of();
        }
    }
}


