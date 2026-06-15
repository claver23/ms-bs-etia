package com.etlions.ms_bs_etia.libro_electronico.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "libros_electronicos")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class LibroElectronicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id")
    private String companyId;

    private String tipo; // ventas, compras, anexo

    private String periodo; // e.g. 2026-04

    private String formato; // TXT, XML, CSV

    private String estado; // GENERADO, ERROR, PENDIENTE

    @Column(name = "archivo_url")
    private String archivoUrl;

    @Column(name = "total_registros")
    private Integer totalRegistros;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

