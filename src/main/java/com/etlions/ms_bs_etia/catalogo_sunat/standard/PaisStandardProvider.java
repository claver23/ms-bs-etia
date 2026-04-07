package com.etlions.ms_bs_etia.catalogo_sunat.standard;

import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo No. 04 — ISO 3166-1: Códigos de Países.
 * Lista completa de países ISO 3166-1 alpha-2 relevantes para SUNAT.
 */
@Component
public class PaisStandardProvider implements CatalogoStandardProvider {

    private static final String CATALOGO_NOMBRE = "Catálogo No. 04: Códigos de Países";

    private static final List<CatalogoSunatRegistro> PAISES = List.of(
            // América del Sur
            reg("PE", "PERÚ"),
            reg("AR", "ARGENTINA"),
            reg("BO", "BOLIVIA"),
            reg("BR", "BRASIL"),
            reg("CL", "CHILE"),
            reg("CO", "COLOMBIA"),
            reg("EC", "ECUADOR"),
            reg("GY", "GUYANA"),
            reg("PY", "PARAGUAY"),
            reg("SR", "SURINAM"),
            reg("UY", "URUGUAY"),
            reg("VE", "VENEZUELA"),
            // América Central y Caribe
            reg("BZ", "BELICE"),
            reg("CR", "COSTA RICA"),
            reg("CU", "CUBA"),
            reg("SV", "EL SALVADOR"),
            reg("GT", "GUATEMALA"),
            reg("HT", "HAITÍ"),
            reg("HN", "HONDURAS"),
            reg("JM", "JAMAICA"),
            reg("NI", "NICARAGUA"),
            reg("PA", "PANAMÁ"),
            reg("DO", "REPÚBLICA DOMINICANA"),
            reg("TT", "TRINIDAD Y TOBAGO"),
            // América del Norte
            reg("CA", "CANADÁ"),
            reg("MX", "MÉXICO"),
            reg("US", "ESTADOS UNIDOS DE AMÉRICA"),
            reg("PR", "PUERTO RICO"),
            // Europa
            reg("DE", "ALEMANIA"),
            reg("AT", "AUSTRIA"),
            reg("BE", "BÉLGICA"),
            reg("BG", "BULGARIA"),
            reg("HR", "CROACIA"),
            reg("CY", "CHIPRE"),
            reg("CZ", "REPÚBLICA CHECA"),
            reg("DK", "DINAMARCA"),
            reg("SK", "ESLOVAQUIA"),
            reg("SI", "ESLOVENIA"),
            reg("ES", "ESPAÑA"),
            reg("EE", "ESTONIA"),
            reg("FI", "FINLANDIA"),
            reg("FR", "FRANCIA"),
            reg("GR", "GRECIA"),
            reg("HU", "HUNGRÍA"),
            reg("IE", "IRLANDA"),
            reg("IS", "ISLANDIA"),
            reg("IT", "ITALIA"),
            reg("LV", "LETONIA"),
            reg("LT", "LITUANIA"),
            reg("LU", "LUXEMBURGO"),
            reg("MT", "MALTA"),
            reg("NL", "PAÍSES BAJOS"),
            reg("NO", "NORUEGA"),
            reg("PL", "POLONIA"),
            reg("PT", "PORTUGAL"),
            reg("GB", "REINO UNIDO"),
            reg("RO", "RUMANÍA"),
            reg("RU", "RUSIA"),
            reg("SE", "SUECIA"),
            reg("CH", "SUIZA"),
            reg("UA", "UCRANIA"),
            // Asia
            reg("CN", "CHINA"),
            reg("KR", "COREA DEL SUR"),
            reg("KP", "COREA DEL NORTE"),
            reg("AE", "EMIRATOS ÁRABES UNIDOS"),
            reg("PH", "FILIPINAS"),
            reg("IN", "INDIA"),
            reg("ID", "INDONESIA"),
            reg("IQ", "IRAQ"),
            reg("IR", "IRÁN"),
            reg("IL", "ISRAEL"),
            reg("JP", "JAPÓN"),
            reg("MY", "MALASIA"),
            reg("SA", "ARABIA SAUDITA"),
            reg("SG", "SINGAPUR"),
            reg("TH", "TAILANDIA"),
            reg("TW", "TAIWÁN"),
            reg("TR", "TURQUÍA"),
            reg("VN", "VIETNAM"),
            reg("HK", "HONG KONG"),
            // África
            reg("ZA", "SUDÁFRICA"),
            reg("EG", "EGIPTO"),
            reg("MA", "MARRUECOS"),
            reg("NG", "NIGERIA"),
            reg("KE", "KENIA"),
            // Oceanía
            reg("AU", "AUSTRALIA"),
            reg("NZ", "NUEVA ZELANDA")
    );

    @Override
    public boolean supports(String nombreCatalogo) {
        return CATALOGO_NOMBRE.equalsIgnoreCase(nombreCatalogo != null ? nombreCatalogo.trim() : "");
    }

    @Override
    public List<CatalogoSunatRegistro> obtenerRegistros() {
        return PAISES;
    }

    private static CatalogoSunatRegistro reg(String codigo, String descripcion) {
        return new CatalogoSunatRegistro().codigo(codigo).descripcion(descripcion);
    }
}

