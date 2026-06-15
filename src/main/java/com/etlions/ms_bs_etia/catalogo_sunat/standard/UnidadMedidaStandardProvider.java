package com.etlions.ms_bs_etia.catalogo_sunat.standard;

import com.etlions.ms_bs_etia.unidad_medida.persistence.UnidadMedidaRepository;
import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo No. 03 — UN/ECE Recommendation 20: Códigos de tipo de unidad de medida comercial.
 * Lee los datos desde la tabla H2 «unidades_medida».
 */
@Component
@RequiredArgsConstructor
public class UnidadMedidaStandardProvider implements CatalogoStandardProvider {

    private static final String CATALOGO_NOMBRE = "Catálogo No. 03: Códigos de tipo de unidad de medida comercial";

    private final UnidadMedidaRepository unidadMedidaRepository;

    @Override
    public boolean supports(String nombreCatalogo) {
        return CATALOGO_NOMBRE.equalsIgnoreCase(nombreCatalogo != null ? nombreCatalogo.trim() : "");
    }

    @Override
    public List<CatalogoSunatRegistro> obtenerRegistros() {
        return unidadMedidaRepository.findAll().stream()
                .map(entity -> new CatalogoSunatRegistro()
                        .codigo(entity.getCodigo())
                        .descripcion(entity.getDescripcion()))
                .toList();
    }
}

