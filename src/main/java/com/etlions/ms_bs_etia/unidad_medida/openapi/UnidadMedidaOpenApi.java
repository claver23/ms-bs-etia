package com.etlions.ms_bs_etia.unidad_medida.openapi;

import com.etlions.ms_bs_etia.unidad_medida.persistence.UnidadMedidaEntity;
import com.etlions.ms_bs_etia.unidad_medida.service.UnidadMedidaService;
import com.etlions.openapicodegen.api.v1.UnidadMedidaApi;
import com.etlions.openapicodegen.dto.UnidadMedidaData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UnidadMedidaOpenApi implements UnidadMedidaApi {

    private final UnidadMedidaService unidadMedidaService;

    @Override
    public ResponseEntity<List<UnidadMedidaData>> listarUnidadesMedida() {
        List<UnidadMedidaEntity> entities = unidadMedidaService.listarTodas();
        List<UnidadMedidaData> data = entities.stream().map(this::toData).toList();
        return ResponseEntity.ok(data);
    }

    private UnidadMedidaData toData(UnidadMedidaEntity e) {
        UnidadMedidaData d = new UnidadMedidaData();
        d.setId(e.getId());
        d.setCodigo(e.getCodigo());
        d.setDescripcion(e.getDescripcion());
        return d;
    }
}

