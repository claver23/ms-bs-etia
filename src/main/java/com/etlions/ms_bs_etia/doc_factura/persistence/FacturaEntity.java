package com.etlions.ms_bs_etia.doc_factura.persistence;

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
@Table(name = "facturas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacturaEntity {

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

    private BigDecimal gravadas;

    private BigDecimal igv;

    private BigDecimal total;

    private String estado;

    @Column(name = "hash_cdr")
    private String hashCdr;

    @Column(name = "codigo_respuesta_sunat")
    private String codigoRespuestaSunat;

    @Column(name = "descripcion_respuesta_sunat")
    private String descripcionRespuestaSunat;

    private String leyenda;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalleJson;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

