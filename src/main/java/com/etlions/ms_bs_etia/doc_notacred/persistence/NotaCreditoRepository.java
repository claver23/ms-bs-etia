package com.etlions.ms_bs_etia.doc_notacred.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCreditoEntity, String> {

    List<NotaCreditoEntity> findByCompanyId(String companyId);

    @Query("""
            select n
            from NotaCreditoEntity n
            where n.companyId = :companyId
              and (:facturaReferenciaId is null or n.documentoReferenciaId = :facturaReferenciaId)
              and (:desdeFecha is null or n.fechaEmision >= :desdeFecha)
              and (:hastaFecha is null or n.fechaEmision <= :hastaFecha)
              and (:serie is null or n.serie = :serie)
              and (:estado is null or n.estado = :estado)
            """)
    Page<NotaCreditoEntity> buscarConFiltros(@Param("companyId") String companyId,
                                             @Param("facturaReferenciaId") String facturaReferenciaId,
                                             @Param("desdeFecha") LocalDate desdeFecha,
                                             @Param("hastaFecha") LocalDate hastaFecha,
                                             @Param("serie") String serie,
                                             @Param("estado") String estado,
                                             Pageable pageable);
}

