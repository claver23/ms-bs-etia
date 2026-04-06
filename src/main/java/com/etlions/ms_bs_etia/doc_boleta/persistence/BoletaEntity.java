package com.etlions.ms_bs_etia.doc_boleta.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "boletas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoletaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "client_id")
    private String clientId;

    private String serie;

    private Integer numero;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    private String moneda;

    @Column(name = "forma_pago")
    private String formaPago;

    @Column(name = "tipo_cambio")
    private BigDecimal tipoCambio;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "codigo_ubigeo", length = 6)
    private String codigoUbigeo;

    private BigDecimal gravadas;

    private BigDecimal igv;

    private BigDecimal total;

    private String estado;

    private String leyenda;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalleJson;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

