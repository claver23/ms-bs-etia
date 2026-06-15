package com.etlions.ms_bs_etia.sede.openapi;

import com.etlions.ms_bs_etia.sede.mapper.SedeMapper;
import com.etlions.ms_bs_etia.sede.persistence.SedeEntity;
import com.etlions.ms_bs_etia.sede.service.SedeService;
import com.etlions.ms_bs_etia.serie_config.mapper.SerieConfigMapper;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.service.SerieConfigService;
import com.etlions.openapicodegen.api.v1.SedeApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SedeOpenApi implements SedeApi {

    private final SedeService sedeService;
    private final SerieConfigService serieConfigService;
    private final SedeMapper sedeMapper;
    private final SerieConfigMapper serieConfigMapper;

    @Override
    public ResponseEntity<SedeListResponseDto> listarSedes(String companyId, String estado) {
        List<SedeEntity> entities;
        if (estado != null && !estado.isBlank()) {
            entities = sedeService.listarPorCompanyYEstado(companyId, estado);
        } else {
            entities = sedeService.listarPorCompany(companyId);
        }
        List<SedeData> data = entities.stream().map(this::enrichSedeData).toList();
        return ResponseEntity.ok(new SedeListResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(data));
    }

    @Override
    public ResponseEntity<SedeResponseDto> crearSede(SedeCreateDto request) {
        SedeEntity entity = sedeMapper.toEntity(request);
        boolean genSeries = request.getGenerarSeries() == null || request.getGenerarSeries();
        SedeEntity saved = sedeService.crearSede(entity, genSeries);
        return ResponseEntity.ok(new SedeResponseDto()
                .isSuccess(true).status("OK")
                .message("Sede creada exitosamente")
                .timestamp(new Date()).data(enrichSedeData(saved)));
    }

    @Override
    public ResponseEntity<SedeResponseDto> actualizarSede(String sedeId, SedeUpdateDto request) {
        SedeEntity updated = SedeEntity.builder()
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .build();
        SedeEntity saved = sedeService.actualizarSede(sedeId, updated);
        return ResponseEntity.ok(new SedeResponseDto()
                .isSuccess(true).status("OK")
                .message("Sede actualizada exitosamente")
                .timestamp(new Date()).data(enrichSedeData(saved)));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarSede(String sedeId) {
        sedeService.eliminarSede(sedeId);
        return ResponseEntity.ok(new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Sede eliminada exitosamente")
                .timestamp(new Date()));
    }

    @Override
    public ResponseEntity<SedeResponseDto> obtenerSede(String sedeId) {
        SedeEntity entity = sedeService.obtenerSede(sedeId);
        return ResponseEntity.ok(new SedeResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(enrichSedeData(entity)));
    }

    @Override
    public ResponseEntity<SedeSeriesResponseDto> generarSeriesSede(String sedeId) {
        List<SerieConfigEntity> series = sedeService.generarSeriesParaSede(sedeId);
        List<SerieConfigData> data = series.stream().map(serieConfigMapper::toData).toList();
        return ResponseEntity.ok(new SedeSeriesResponseDto()
                .isSuccess(true).status("OK")
                .message("Series generadas exitosamente")
                .timestamp(new Date()).data(data));
    }

    /** Maps entity to DTO and enriches with associated series */
    private SedeData enrichSedeData(SedeEntity e) {
        SedeData d = sedeMapper.toData(e);
        List<SerieConfigEntity> series = serieConfigService.listarPorSede(e.getId());
        d.setSeries(series.stream().map(serieConfigMapper::toData).toList());
        return d;
    }
}
