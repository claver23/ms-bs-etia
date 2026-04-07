package com.etlions.ms_bs_etia.product.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "codigo_producto")
    private String codigoProducto;

    @Column(name = "codigo_producto_sunat", length = 8)
    private String codigoProductoSunat;

    private String descripcion;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "tipo_afectacion_igv", length = 2)
    private String tipoAfectacionIgv;

    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    private BigDecimal igv;

    @Builder.Default
    private String estado = "ACTIVO";

    @JsonIgnore
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

