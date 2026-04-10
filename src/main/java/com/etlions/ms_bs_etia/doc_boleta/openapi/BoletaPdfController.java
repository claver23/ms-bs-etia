package com.etlions.ms_bs_etia.doc_boleta.openapi;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.service.PdfGeneratorService;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
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
 * Endpoint para descarga de representación impresa (PDF) de boletas.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BoletaPdfController {

    private final BoletaRepository boletaRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final PdfGeneratorService pdfGeneratorService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping("/boleta/{boletaId}/pdf")
    public ResponseEntity<byte[]> descargarBoletaPdf(@PathVariable String boletaId) {
        BoletaEntity boleta = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));

        CompanyEntity company = companyRepository.findById(boleta.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", boleta.getCompanyId()));

        ClientEntity client = clientRepository.findById(boleta.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", boleta.getClientId()));

        List<Map<String, Object>> detalle = parseDetalleJson(boleta.getDetalleJson());

        if (detalle == null || detalle.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        byte[] pdfBytes = pdfGeneratorService.generateBoletaPdf(boleta, company, client, detalle);

        String filename = String.format("%s-%08d.pdf",
                boleta.getSerie() != null ? boleta.getSerie() : "B001",
                boleta.getNumero() != null ? boleta.getNumero() : 0);

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

