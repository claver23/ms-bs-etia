package com.etlions.ms_bs_etia.serie_config.openapi;

import com.etlions.ms_bs_etia.serie_config.mapper.SerieConfigMapper;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.service.SerieConfigService;
import com.etlions.openapicodegen.api.v1.SerieConfigApi;
import com.etlions.openapicodegen.dto.SerieConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SerieConfigOpenApi implements SerieConfigApi {

    private final SerieConfigService serieConfigService;
    private final SerieConfigMapper serieConfigMapper;

    @Override
    public ResponseEntity<List<SerieConfigData>> listarSeriesConfig(String companyId, String tipoDocumento) {
        List<SerieConfigEntity> entities;
        if (tipoDocumento != null && !tipoDocumento.isBlank()) {
            entities = serieConfigService.listarPorTipo(companyId, tipoDocumento);
        } else {
            entities = serieConfigService.listarPorCompany(companyId);
        }
        List<SerieConfigData> data = entities.stream().map(serieConfigMapper::toData).toList();
        return ResponseEntity.ok(data);
    }
}
