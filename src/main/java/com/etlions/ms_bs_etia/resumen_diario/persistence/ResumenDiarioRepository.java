package com.etlions.ms_bs_etia.resumen_diario.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResumenDiarioRepository extends JpaRepository<ResumenDiarioEntity, String> {

    List<ResumenDiarioEntity> findByCompanyIdOrderByFechaGeneracionDesc(String companyId);

    Page<ResumenDiarioEntity> findByCompanyId(String companyId, Pageable pageable);

    List<ResumenDiarioEntity> findByCompanyIdAndFechaReferencia(String companyId, LocalDate fechaReferencia);

    /** Cuenta resúmenes del día para generar correlativo NNN */
    long countByCompanyIdAndFechaGeneracion(String companyId, LocalDate fechaGeneracion);
}

