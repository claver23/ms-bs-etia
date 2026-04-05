package com.etlions.ms_bs_etia.app_config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "test-data")
@Getter
@Setter
public class TestDataProperties {

    private CompanyData company;
    private ClientData client;
    private List<ProductData> products;
    private List<UbigeoData> ubigeos;

    @Getter
    @Setter
    public static class CompanyData {
        private String ruc;
        private String razonSocial;
        private String nombreComercial;
        private String codigoUbigeo;
        private String direccion;
        private String urbanizacion;
        private String departamento;
        private String provincia;
        private String distrito;
        private String usuarioSol;
        private String claveSol;
        private String emailContacto;
        private String telefono;
        private String regimen;
    }

    @Getter
    @Setter
    public static class ClientData {
        private String tipoDocumento;
        private String numeroDocumento;
        private String denominacion;
        private String direccion;
        private String email;
        private String telefono;
    }

    @Getter
    @Setter
    public static class ProductData {
        private String descripcion;
        private Integer cantidad;
        private BigDecimal valorUnitario;
        private BigDecimal precioUnitario;
        private BigDecimal igv;
        private BigDecimal subtotal;
        private String unidadMedida;
        private String codigoProducto;
    }

    @Getter
    @Setter
    public static class UbigeoData {
        private String codigo;
        private String departamento;
        private String provincia;
        private String distrito;
    }
}

