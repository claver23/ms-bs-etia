package com.etlions.ms_bs_etia.app_util.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "correlativos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "serie"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CorrelativoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(name = "ultimo_numero", nullable = false)
    @Builder.Default
    private Integer ultimoNumero = 0;
}
