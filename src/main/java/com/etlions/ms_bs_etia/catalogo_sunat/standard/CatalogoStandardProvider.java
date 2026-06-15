package com.etlions.ms_bs_etia.catalogo_sunat.standard;

import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;

import java.util.List;

/**
 * Strategy interface para proveer datos de catálogos basados en estándares internacionales.
 * Cada implementación resuelve un catálogo específico (ISO 4217, ISO 3166, UN/ECE, UBIGEO, UNSPSC).
 */
public interface CatalogoStandardProvider {

    /**
     * @return true si este provider resuelve el catálogo con el nombre indicado.
     */
    boolean supports(String nombreCatalogo);

    /**
     * @return lista de registros (codigo + descripcion) del estándar.
     */
    List<CatalogoSunatRegistro> obtenerRegistros();
}

