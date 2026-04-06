package com.etlions.ms_bs_etia.tipo_cambio.service.impl;

import com.etlions.ms_bs_etia.tipo_cambio.mapper.TipoCambioMapper;
import com.etlions.ms_bs_etia.tipo_cambio.persistence.TipoCambioEntity;
import com.etlions.ms_bs_etia.tipo_cambio.persistence.TipoCambioRepository;
import com.etlions.ms_bs_etia.tipo_cambio.service.TipoCambioService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoCambioServiceImpl implements TipoCambioService {

    private final TipoCambioRepository repository;
    private final TipoCambioMapper tipoCambioMapper;

    @Override
    public TipoCambioResponseDto obtenerTipoCambio(Date fecha, String moneda, String fuente) {
        LocalDate localFecha = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String mon = (moneda != null) ? moneda.toUpperCase() : "USD";

        Optional<TipoCambioEntity> found;

        // If fuente specified, search with it
        if (fuente != null && !fuente.isBlank()) {
            found = repository.findFirstByFechaAndMonedaOrigenAndFuenteOrderByFechaCreacionDesc(
                    localFecha, mon, fuente.toUpperCase());
        } else {
            found = repository.findFirstByFechaAndMonedaOrigenOrderByFechaCreacionDesc(
                    localFecha, mon);
        }

        // Fallback: latest available for that currency
        if (found.isEmpty()) {
            found = repository.findFirstByMonedaOrigenOrderByFechaDesc(mon);
        }

        TipoCambioData data;
        if (found.isPresent()) {
            data = tipoCambioMapper.toData(found.get());
        } else {
            // No data at all — return SBS default mock
            data = new TipoCambioData();
            data.setFecha(java.sql.Date.valueOf(localFecha));
            data.setMonedaOrigen(mon);
            data.setMonedaDestino("PEN");
            data.setTasaCompra(3.7100);
            data.setTasaVenta(3.7520);
            data.setFuente("SBS_DEFAULT");
        }

        return new TipoCambioResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public TipoCambioListResponseDto listarTiposCambio(String fuente, Integer page, Integer size) {
        int p = (page != null) ? page : 0;
        int s = (size != null) ? size : 50;
        Sort sort = Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        PageRequest pageable = PageRequest.of(p, s, sort);

        Page<TipoCambioEntity> entityPage;
        if (fuente != null && !fuente.isBlank()) {
            entityPage = repository.findByFuente(fuente.toUpperCase(), pageable);
        } else {
            entityPage = repository.findAll(pageable);
        }

        List<TipoCambioData> content = entityPage.getContent().stream().map(tipoCambioMapper::toData).toList();

        PaginationMeta pagination = new PaginationMeta()
                .currentPage(p)
                .pageSize(s)
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages());

        TipoCambioListResponseDtoAllOfData dataObj = new TipoCambioListResponseDtoAllOfData();
        dataObj.setContent(content);
        dataObj.setPagination(pagination);

        TipoCambioListResponseDto response = new TipoCambioListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(dataObj);
        return response;
    }

    @Override
    public TipoCambioResponseDto registrarTipoCambio(TipoCambioCreateDto request) {
        LocalDate fecha = (request.getFecha() != null)
                ? request.getFecha().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        TipoCambioEntity entity = TipoCambioEntity.builder()
                .fecha(fecha)
                .monedaOrigen(request.getMonedaOrigen() != null ? request.getMonedaOrigen() : "USD")
                .monedaDestino(request.getMonedaDestino() != null ? request.getMonedaDestino() : "PEN")
                .tasaCompra(request.getTasaCompra() != null ? BigDecimal.valueOf(request.getTasaCompra()) : new BigDecimal("3.7100"))
                .tasaVenta(request.getTasaVenta() != null ? BigDecimal.valueOf(request.getTasaVenta()) : new BigDecimal("3.7520"))
                .fuente(request.getFuente() != null ? request.getFuente() : "SBS")
                .fechaCreacion(LocalDateTime.now())
                .build();

        TipoCambioEntity saved = repository.save(entity);
        TipoCambioData data = tipoCambioMapper.toData(saved);

        return new TipoCambioResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Tipo de cambio registrado exitosamente")
                .timestamp(new Date())
                .data(data);
    }
}
