package com.etlions.ms_bs_etia.doc_boleta.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<BoletaEntity, String> {

    List<BoletaEntity> findByCompanyId(String companyId);

    /** Boletas VALIDADAS listas para resumen diario */
    List<BoletaEntity> findByCompanyIdAndEstado(String companyId, String estado);

    /** Boletas VALIDADAS de una fecha específica */
    List<BoletaEntity> findByCompanyIdAndEstadoAndFechaEmision(String companyId, String estado, LocalDate fechaEmision);

    /** Boletas asociadas a un resumen diario */
    List<BoletaEntity> findByResumenDiarioId(String resumenDiarioId);
}

