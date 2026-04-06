package com.etlions.ms_bs_etia.employee.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    private String nombre;
    private String cargo;

    @Column(unique = true)
    private String dni;

    @Column(unique = true)
    private String codigo;

    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

