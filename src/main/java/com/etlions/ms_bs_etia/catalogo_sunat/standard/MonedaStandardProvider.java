package com.etlions.ms_bs_etia.catalogo_sunat.standard;

import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo No. 02 — ISO 4217: Códigos de tipo de monedas.
 * Lista de monedas relevantes para facturación electrónica en Perú.
 */
@Component
public class MonedaStandardProvider implements CatalogoStandardProvider {

    private static final String CATALOGO_NOMBRE = "Catálogo No. 02: Códigos de tipo de monedas";

    private static final List<CatalogoSunatRegistro> MONEDAS = List.of(
            reg("PEN", "Sol peruano"),
            reg("USD", "Dólar estadounidense"),
            reg("EUR", "Euro"),
            reg("GBP", "Libra esterlina"),
            reg("JPY", "Yen japonés"),
            reg("CNY", "Yuan chino"),
            reg("BRL", "Real brasileño"),
            reg("CLP", "Peso chileno"),
            reg("COP", "Peso colombiano"),
            reg("MXN", "Peso mexicano"),
            reg("ARS", "Peso argentino"),
            reg("BOB", "Boliviano"),
            reg("CAD", "Dólar canadiense"),
            reg("CHF", "Franco suizo"),
            reg("KRW", "Won surcoreano"),
            reg("AUD", "Dólar australiano"),
            reg("SEK", "Corona sueca"),
            reg("NOK", "Corona noruega"),
            reg("DKK", "Corona danesa"),
            reg("NZD", "Dólar neozelandés"),
            reg("SGD", "Dólar de Singapur"),
            reg("HKD", "Dólar de Hong Kong"),
            reg("TWD", "Nuevo dólar taiwanés"),
            reg("INR", "Rupia india"),
            reg("THB", "Baht tailandés"),
            reg("ZAR", "Rand sudafricano"),
            reg("ILS", "Nuevo shéquel israelí"),
            reg("AED", "Dírham de los Emiratos Árabes Unidos"),
            reg("SAR", "Riyal saudí"),
            reg("UYU", "Peso uruguayo"),
            reg("PYG", "Guaraní paraguayo"),
            reg("VES", "Bolívar soberano venezolano"),
            reg("CRC", "Colón costarricense"),
            reg("PAB", "Balboa panameño"),
            reg("DOP", "Peso dominicano"),
            reg("GTQ", "Quetzal guatemalteco"),
            reg("HNL", "Lempira hondureño"),
            reg("NIO", "Córdoba nicaragüense"),
            reg("CUP", "Peso cubano"),
            reg("JMD", "Dólar jamaicano")
    );

    @Override
    public boolean supports(String nombreCatalogo) {
        return CATALOGO_NOMBRE.equalsIgnoreCase(nombreCatalogo != null ? nombreCatalogo.trim() : "");
    }

    @Override
    public List<CatalogoSunatRegistro> obtenerRegistros() {
        return MONEDAS;
    }

    private static CatalogoSunatRegistro reg(String codigo, String descripcion) {
        return new CatalogoSunatRegistro().codigo(codigo).descripcion(descripcion);
    }
}

