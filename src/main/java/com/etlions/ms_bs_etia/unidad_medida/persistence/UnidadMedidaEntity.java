package com.etlions.ms_bs_etia.unidad_medida.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidades_medida")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class UnidadMedidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, length = 5, nullable = false)
    private String codigo;

    private String descripcion;
}

