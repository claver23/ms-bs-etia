package com.etlions.ms_bs_etia.doc_factura.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<FacturaEntity, String> {

    List<FacturaEntity> findByCompanyId(String companyId);

    @Query("""
            select f
            from FacturaEntity f
            where f.companyId = :companyId
              and (:desdeFecha is null or f.fechaEmision >= :desdeFecha)
              and (:hastaFecha is null or f.fechaEmision <= :hastaFecha)
              and (:serie is null or f.serie = :serie)
              and (:numero is null or f.numero = :numero)
              and (:estado is null or f.estado = :estado)
            """)
    Page<FacturaEntity> buscarConFiltros(@Param("companyId") String companyId,
                                         @Param("desdeFecha") LocalDate desdeFecha,
                                         @Param("hastaFecha") LocalDate hastaFecha,
                                         @Param("serie") String serie,
                                         @Param("numero") Integer numero,
                                         @Param("estado") String estado,
                                         Pageable pageable);
}

