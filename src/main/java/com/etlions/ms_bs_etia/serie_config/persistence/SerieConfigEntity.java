package com.etlions.ms_bs_etia.serie_config.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "series_config",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "tipo_documento", "serie"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class SerieConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento; // FACTURA, BOLETA, NOTA_CREDITO, NOTA_DEBITO

    @Column(nullable = false, length = 4)
    private String serie; // F001, B001, FC01, FD01

    private String descripcion;

    @Builder.Default
    @Column(name = "es_default")
    private Boolean esDefault = false;

    @Builder.Default
    private Boolean activa = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

