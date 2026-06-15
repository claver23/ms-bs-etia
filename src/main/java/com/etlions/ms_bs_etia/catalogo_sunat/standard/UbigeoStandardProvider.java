package com.etlions.ms_bs_etia.catalogo_sunat.standard;

import com.etlions.ms_bs_etia.ubigeo.persistence.UbigeoRepository;
import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo No. 13 — UBIGEO INEI: Ubicación geográfica.
 * Lee los datos desde la tabla H2 «ubigeos».
 */
@Component
@RequiredArgsConstructor
public class UbigeoStandardProvider implements CatalogoStandardProvider {

    private static final String CATALOGO_NOMBRE = "Catálogo No. 13: Ubicación geográfica (UBIGEO)";

    private final UbigeoRepository ubigeoRepository;

    @Override
    public boolean supports(String nombreCatalogo) {
        return CATALOGO_NOMBRE.equalsIgnoreCase(nombreCatalogo != null ? nombreCatalogo.trim() : "");
    }

    @Override
    public List<CatalogoSunatRegistro> obtenerRegistros() {
        return ubigeoRepository.findAll().stream()
                .map(entity -> new CatalogoSunatRegistro()
                        .codigo(entity.getCodigo())
                        .descripcion(entity.getDepartamento() + " - " + entity.getProvincia() + " - " + entity.getDistrito()))
                .toList();
    }
}

