package com.etlions.ms_bs_etia.catalogo_sunat.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.catalogo_sunat.persistence.CatalogoSunatJsonRepository;
import com.etlions.ms_bs_etia.catalogo_sunat.service.CatalogoSunatService;
import com.etlions.ms_bs_etia.catalogo_sunat.standard.CatalogoStandardProvider;
import com.etlions.openapicodegen.dto.CatalogoSunatData;
import com.etlions.openapicodegen.dto.CatalogoSunatMenuItem;
import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;
import com.etlions.openapicodegen.dto.CatalogoSunatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogoSunatServiceImpl implements CatalogoSunatService {

    private final CatalogoSunatJsonRepository repository;
    private final List<CatalogoStandardProvider> standardProviders;

    @Override
    public List<CatalogoSunatMenuItem> listarMenu() {
        return repository.getMenu().stream()
                .map(m -> new CatalogoSunatMenuItem()
                        .nombreCatalogo(m.get("nombreCatalogo"))
                        .icono(m.get("icono")))
                .toList();
    }

    @Override
    public CatalogoSunatResponseDto obtenerCatalogo(String nombreCatalogo) {
        Map<String, Object> catalogo = repository.findByNombre(nombreCatalogo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catálogo SUNAT '" + nombreCatalogo + "' no encontrado"));

        // Convertir registros del JSON
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawData = catalogo.get("data") instanceof List<?>
                ? (List<Map<String, Object>>) catalogo.get("data")
                : Collections.emptyList();

        List<CatalogoSunatRegistro> registros;

        if (rawData.isEmpty()) {
            // Catálogo estándar: intentar resolver con un CatalogoStandardProvider
            registros = standardProviders.stream()
                    .filter(p -> p.supports(nombreCatalogo))
                    .findFirst()
                    .map(CatalogoStandardProvider::obtenerRegistros)
                    .orElse(Collections.emptyList());
        } else {
            registros = rawData.stream()
                    .map(r -> new CatalogoSunatRegistro()
                            .codigo(r.get("codigo") != null ? r.get("codigo").toString() : "")
                            .descripcion(r.get("descripcion") != null ? r.get("descripcion").toString() : ""))
                    .toList();
        }

        CatalogoSunatData data = new CatalogoSunatData()
                .nombre(catalogo.get("nombre") != null ? catalogo.get("nombre").toString() : "")
                .campo(catalogo.get("campo") != null ? catalogo.get("campo").toString() : "")
                .description(catalogo.get("description") != null ? catalogo.get("description").toString() : "")
                .catalogo(catalogo.get("catalogo") != null ? catalogo.get("catalogo").toString() : "")
                .registros(registros.size())
                .data(registros);

        return new CatalogoSunatResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Catálogo SUNAT obtenido exitosamente")
                .timestamp(new Date())
                .data(data);
    }
}



