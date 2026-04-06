package com.etlions.ms_bs_etia.reniec.openapi;

import com.etlions.ms_bs_etia.reniec.service.ReniecService;
import com.etlions.openapicodegen.api.v1.ReniecApi;
import com.etlions.openapicodegen.dto.ReniecData;
import com.etlions.openapicodegen.dto.ReniecResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReniecOpenApi implements ReniecApi {

    private final ReniecService reniecService;

    @Override
    public ResponseEntity<ReniecResponseDto> consultarDocumento(String tipo, String numero) {
        ReniecData data = reniecService.consultarDocumento(tipo, numero);
        return ResponseEntity.ok(new ReniecResponseDto()
                .isSuccess(true).status("OK")
                .message("Consulta exitosa")
                .timestamp(new Date())
                .data(data));
    }
}

