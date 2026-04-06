package com.etlions.ms_bs_etia.serie_config.openapi;

import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.service.SerieConfigService;
import com.etlions.openapicodegen.api.v1.SerieConfigApi;
import com.etlions.openapicodegen.dto.SerieConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SerieConfigOpenApi implements SerieConfigApi {

    private final SerieConfigService serieConfigService;

    @Override
    public ResponseEntity<List<SerieConfigData>> listarSeriesConfig(String companyId, String tipoDocumento) {
        List<SerieConfigEntity> entities;
        if (tipoDocumento != null && !tipoDocumento.isBlank()) {
            entities = serieConfigService.listarPorTipo(companyId, tipoDocumento);
        } else {
            entities = serieConfigService.listarPorCompany(companyId);
        }
        List<SerieConfigData> data = entities.stream().map(this::toData).toList();
        return ResponseEntity.ok(data);
    }

    private SerieConfigData toData(SerieConfigEntity e) {
        SerieConfigData d = new SerieConfigData();
        d.setId(e.getId());
        d.setCompanyId(e.getCompanyId());
        d.setTipoDocumento(e.getTipoDocumento() != null ?
                SerieConfigData.TipoDocumentoEnum.fromValue(e.getTipoDocumento()) : null);
        d.setSerie(e.getSerie());
        d.setDescripcion(e.getDescripcion());
        d.setEsDefault(e.getEsDefault());
        d.setActiva(e.getActiva());
        if (e.getFechaCreacion() != null) {
            d.setFechaCreacion(Timestamp.valueOf(e.getFechaCreacion()));
        }
        return d;
    }
}

