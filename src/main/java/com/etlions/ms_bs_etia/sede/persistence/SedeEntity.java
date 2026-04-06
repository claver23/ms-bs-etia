package com.etlions.ms_bs_etia.sede.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sedes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "codigo"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class SedeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(nullable = false, length = 3)
    private String codigo; // 001, 002, 003...

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    @Column(name = "codigo_ubigeo", length = 6)
    private String codigoUbigeo;

    private String departamento;
    private String provincia;
    private String distrito;
    private String telefono;
    private String email;

    @Builder.Default
    private String estado = "ACTIVO";

    @Builder.Default
    @Column(name = "es_default")
    private Boolean esDefault = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

