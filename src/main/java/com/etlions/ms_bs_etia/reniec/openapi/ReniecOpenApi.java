package com.etlions.ms_bs_etia.reniec.openapi;

import com.etlions.ms_bs_etia.reniec.service.ReniecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for RENIEC / SUNAT document lookup.
 * Note: this endpoint is not yet defined in the OpenAPI contract;
 * it will be migrated once a schema is added to ms-bs-etia.yaml.
 */
@RestController
@RequestMapping("/api/v1/reniec")
@RequiredArgsConstructor
public class ReniecOpenApi {

    private final ReniecService reniecService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> consultar(
            @RequestParam String tipo,
            @RequestParam String numero) {

        Map<String, Object> result = reniecService.consultarDocumento(tipo, numero);

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}

