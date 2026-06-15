package com.etlions.ms_bs_etia.app_config.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuración de endpoints SUNAT.
 * Lee del bloque `sunat:` en application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "sunat")
@Getter
@Setter
public class SunatProperties {

    /** Modo activo: "beta" o "produccion" */
    private String modo = "beta";

    /** Mapa de endpoints SUNAT */
    private Map<String, String> endpoints;

    /** Configuración de certificado digital */
    private Certificado certificado = new Certificado();

    /** Credenciales SOL por defecto (beta / sandbox) */
    private Sol sol = new Sol();

    /** Obtiene el endpoint de factura según el modo activo */
    public String getEndpointFactura() {
        if ("produccion".equalsIgnoreCase(modo)) {
            return endpoints.get("factura-prod");
        }
        return endpoints.get("factura-beta");
    }

    /** Obtiene el endpoint de consulta CDR según el modo activo */
    public String getEndpointConsultaCdr() {
        if ("produccion".equalsIgnoreCase(modo)) {
            return endpoints.get("consulta-cdr-prod");
        }
        return endpoints.get("consulta-cdr-beta");
    }

    public boolean isBeta() {
        return "beta".equalsIgnoreCase(modo);
    }

    @Getter @Setter
    public static class Certificado {
        /** Ruta base donde se almacenan los .p12/.pfx */
        private String basePath = "./certs";
    }

    @Getter @Setter
    public static class Sol {
        /** Usuario SOL por defecto (MODDATOS para beta) */
        private String defaultUser = "MODDATOS";
        /** Clave SOL por defecto (MODDATOS para beta) */
        private String defaultPassword = "MODDATOS";
    }
}


