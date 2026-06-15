package com.etlions.ms_bs_etia.tabla_sunat.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.tabla_sunat.persistence.TablaSunatJsonRepository;
import com.etlions.ms_bs_etia.tabla_sunat.service.TablaSunatService;
import com.etlions.openapicodegen.dto.TablaSunatData;
import com.etlions.openapicodegen.dto.TablaSunatMenuItem;
import com.etlions.openapicodegen.dto.TablaSunatRegistro;
import com.etlions.openapicodegen.dto.TablaSunatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TablaSunatServiceImpl implements TablaSunatService {

    private final TablaSunatJsonRepository repository;

    @Override
    public List<TablaSunatMenuItem> listarMenu() {
        return repository.getMenu().stream()
                .map(m -> new TablaSunatMenuItem().nombreTabla(m.get("nombreTabla")))
                .toList();
    }

    @Override
    public TablaSunatResponseDto obtenerTabla(String nombreTabla) {
        Map<String, Object> tabla = repository.findByNombreTabla(nombreTabla)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tabla SUNAT '" + nombreTabla + "' no encontrada"));

        // Convertir registros
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawData = (List<Map<String, Object>>) tabla.get("data");

        List<TablaSunatRegistro> registros = rawData.stream()
                .map(r -> new TablaSunatRegistro()
                        .cod(r.get("cod") != null ? r.get("cod").toString() : "")
                        .descripcion(r.get("descripcion") != null ? r.get("descripcion").toString() : ""))
                .toList();

        Object regCount = tabla.get("registros");
        int totalRegistros = regCount instanceof Number ? ((Number) regCount).intValue() : registros.size();

        TablaSunatData data = new TablaSunatData()
                .nombreTabla(tabla.get("nombreTabla").toString())
                .registros(totalRegistros)
                .data(registros);

        return new TablaSunatResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Tabla SUNAT obtenida exitosamente")
                .timestamp(new Date())
                .data(data);
    }
}

