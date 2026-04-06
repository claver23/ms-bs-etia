package com.etlions.ms_bs_etia.ubigeo.openapi;

import com.etlions.ms_bs_etia.ubigeo.persistence.UbigeoEntity;
import com.etlions.ms_bs_etia.ubigeo.mapper.UbigeoMapper;
import com.etlions.ms_bs_etia.ubigeo.service.UbigeoService;
import com.etlions.openapicodegen.api.v1.UbigeoApi;
import com.etlions.openapicodegen.dto.UbigeoData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UbigeoOpenApi implements UbigeoApi {

    private final UbigeoService ubigeoService;
    private final UbigeoMapper ubigeoMapper;

    @Override
    public ResponseEntity<List<UbigeoData>> listarUbigeos() {
        List<UbigeoEntity> entities = ubigeoService.listarTodos();
        List<UbigeoData> data = entities.stream().map(ubigeoMapper::toData).toList();
        return ResponseEntity.ok(data);
    }
}
