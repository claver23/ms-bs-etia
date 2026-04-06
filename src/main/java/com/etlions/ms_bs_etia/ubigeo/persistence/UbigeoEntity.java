package com.etlions.ms_bs_etia.ubigeo.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubigeos")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class UbigeoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, length = 6, nullable = false)
    private String codigo;

    private String departamento;
    private String provincia;
    private String distrito;
}

