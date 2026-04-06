package com.etlions.ms_bs_etia.user_preference.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "clave"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class UserPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(nullable = false)
    private String clave; // moneda, tema, sede, sedeDireccion, sedeLat, sedeLng

    @Column(length = 500)
    private String valor;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

