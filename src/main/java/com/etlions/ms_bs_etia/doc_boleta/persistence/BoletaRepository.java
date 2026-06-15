package com.etlions.ms_bs_etia.doc_boleta.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<BoletaEntity, String> {

    List<BoletaEntity> findByCompanyId(String companyId);

    @Query("""
            select b
            from BoletaEntity b
            where b.companyId = :companyId
              and (:desdeFecha is null or b.fechaEmision >= :desdeFecha)
              and (:hastaFecha is null or b.fechaEmision <= :hastaFecha)
              and (:serie is null or b.serie = :serie)
              and (:numero is null or b.numero = :numero)
            """)
    Page<BoletaEntity> buscarConFiltros(@Param("companyId") String companyId,
                                        @Param("desdeFecha") LocalDate desdeFecha,
                                        @Param("hastaFecha") LocalDate hastaFecha,
                                        @Param("serie") String serie,
                                        @Param("numero") Integer numero,
                                        Pageable pageable);

    /** Boletas VALIDADAS listas para resumen diario */
    List<BoletaEntity> findByCompanyIdAndEstado(String companyId, String estado);

    /** Boletas VALIDADAS de una fecha específica */
    List<BoletaEntity> findByCompanyIdAndEstadoAndFechaEmision(String companyId, String estado, LocalDate fechaEmision);

    /** Boletas asociadas a un resumen diario */
    List<BoletaEntity> findByResumenDiarioId(String resumenDiarioId);
}

